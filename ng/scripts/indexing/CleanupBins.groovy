import groovy.sql.Sql

final String contigSeparator = "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn";

class Feature {
	Integer feature_id
	String uniquename
	String type
	String featureLocatedOn
	Integer fmin
	Integer fmax
	Boolean is_obsolete
	Integer phase
	int strand
	String residues
	Integer organism_id
}

class Cvterm {
	Integer cvterm_id
	String name
}

def getFeature(String uniquename, Sql sql) {
	Feature feature = sql.firstRow("""
		SELECT f.uniquename, f.feature_id, f.residues, type.name as type, f.organism_id
		FROM feature f
		JOIN cvterm type ON f.type_id = type.cvterm_id
		WHERE f.uniquename = ${uniquename}
	""")
	return feature
}

def generateBinContigs(Feature bin, String contigSeparator, Sql sql) {

	String sequence = bin.residues;
	String sequenceName = bin.uniquename + "_";

	def contigs = []
	Integer nCount = 0

	Integer start = 0
	Integer end = 0

	Integer lastPos

	Boolean unfinished = true

	Integer contigTypeID = sql.firstRow("""
		SELECT cvterm_id FROM cvterm where name = 'contig'
	""").cvterm_id

	println "Contig feature type id ${contigTypeID}"

	while (unfinished) {

		if (lastPos == null) {
			start = 0
		} else {
			start = lastPos + contigSeparator.length()
		}

		Integer pos = sequence.indexOf(contigSeparator, start)

		if (pos == -1) {
			unfinished = false
			end = sequence.length()
		} else {
			end = pos
		}

		String contigName = "${sequenceName}${nCount}_${start}-${end}"
		println contigName

		String contigSequence = sequence.substring(start, end);
		Integer contigSequenceLength = contigSequence.length()

		if (contigSequenceLength >= 50) {
			println contigSequence.substring(0,25) + " ... " + contigSequence.substring(contigSequenceLength-25,contigSequenceLength) + " (${contigSequenceLength} residues)"
		} else {
			println contigSequence
		}


		lastPos = pos

		Feature contig = new Feature()
		contig.residues = sequence
		contig.uniquename = contigName
		contig.fmin = start
		contig.fmax = end
		contig.organism_id = bin.organism_id
		contig.featureLocatedOn = bin.uniquename

		Integer seqlen = contig.residues.length()

		Feature existingContigFeature = getFeature(contig.uniquename, sql)

		if (existingContigFeature == null) {

			sql.execute("""
				INSERT INTO feature (uniqueName, residues, organism_id, type_id, seqlen) VALUES
				(${contig.uniquename}, ${contig.residues}, ${contig.organism_id}, ${contigTypeID}, ${seqlen})
			""")

			// run the select again to get the feature_id
			existingContigFeature = getFeature(contig.uniquename, sql)

		} else {
			println "Contig feature already exists for ${existingContigFeature.uniquename} with a feature_id of ${existingContigFeature.feature_id}"
		}

		contig.feature_id = existingContigFeature.feature_id

		contigs << contig

		nCount++;
	}
	return contigs
}

def getBinContigs(Feature bin, sql) {
	def contigs = []
	sql.eachRow("""
		SELECT
			f.feature_id,
			f.uniqueName,
			type.name as type,
			fl.fmin,
			fl.fmax,
			f.is_obsolete

		FROM feature f

			JOIN cvterm type ON f.type_id = type.cvterm_id AND type.name in ('contig', 'region')
			JOIN featureloc fl ON (f.feature_id = fl.feature_id AND fl.srcfeature_id = (select feature_id from feature where uniqueName = ${bin.uniquename} ) )

		WHERE f.uniquename NOT LIKE '%archived:source%' AND f.uniquename NOT LIKE '%archived:misc_feature%'

		ORDER BY fl.fmin, fl.fmax;

	""") { row ->
				Feature contig = new Feature( row.toRowResult() )
				contig.featureLocatedOn = bin.uniquename
				contigs << contig
			}
	return contigs
}

def Boolean contigHasEMBLQualifierType(Feature contig, Sql sql) {
	def rows = sql.rows("""
		SELECT value FROM featureprop
			JOIN cvterm ON featureprop.type_id = cvterm.cvterm_id AND cvterm.name = 'EMBL_qualifier'
			WHERE feature_id = ${contig.feature_id}
			AND value like '/type%';
	""")
	if (rows.size() > 0) {
		return true
	}
	return false
}

def getFeaturesSpanningTheInsidesOfAContig(Feature bin, Feature contig, sql) {
	def features = []


	sql.eachRow("""
		SELECT f.feature_id, f.uniqueName, type.name as type, fl.phase, fl.strand , fl.fmin, fl.fmax
		FROM feature f
			JOIN featureloc fl ON f.feature_id = fl.feature_id AND fl.srcfeature_id = ${bin.feature_id}
			JOIN cvterm type ON f.type_id = type.cvterm_id
		WHERE fl.fmin >= ${contig.fmin} AND fl.fmax <= ${contig.fmax}
		AND f.uniqueName != ${contig.uniquename}
		AND type.name NOT IN ('contig', 'region')

	""") { row ->
		Feature feature = new Feature( row.toRowResult() )
		features << feature
	}
	return features
}

def Cvterm getTopLevelFeatureCvtermId(Sql sql) {
	Cvterm cvterm = sql.firstRow("select cvterm_id from cvterm join cv using (cv_id) where cv.name = 'genedb_misc' and cvterm.name = 'top_level_seq'")
	return cvterm
}


def promoteContigToTopLevel (Feature bin, Feature feature, Cvterm topLevelType, Boolean generated, Sql sql ) {

	def residues = bin.residues.substring(feature.fmin, feature.fmax)
	def seqlen = residues.length();

	sql.execute("""
		UPDATE feature set residues = ${residues}, seqlen = ${seqlen}
			WHERE feature_id = ${feature.feature_id}
	""")


	def rows = sql.rows("""
		SELECT f.feature_id
		FROM feature f
		JOIN featureprop fp on fp.feature_id = f.feature_id
			AND fp.type_id = ${topLevelType.cvterm_id}
			AND f.feature_id = ${feature.feature_id}
	""")


	println "Attempting to delete featureloc of ${feature.uniquename} ${feature.feature_id} on ${bin.uniquename} ${bin.feature_id}"
	sql.execute("""
		DELETE FROM featureloc WHERE featureloc.feature_id = ${feature.feature_id} AND featureloc.srcfeature_id = ${bin.feature_id}
	""")


	if (rows.size() > 0) {
		println "${feature.uniquename} already is a top level feature"
	} else {
		e = """
			INSERT INTO featureprop (feature_id, type_id, value)
				VALUES ( ${feature.feature_id}, ${topLevelType.cvterm_id}, 'true')
		"""
		sql.execute("""
			INSERT INTO featureprop (feature_id, type_id, value)
				VALUES ( ${feature.feature_id}, ${topLevelType.cvterm_id}, 'true')
		""")
	}
}

def relocateFeatureToContig(Feature bin, Feature contig, Feature feature, Sql sql) {

	def flrow = sql.firstRow("""
		SELECT fl.featureloc_id, f.feature_id, f.uniqueName, fl.is_fmin_partial, fl.is_fmax_partial
		FROM feature f
			JOIN featureloc fl ON f.feature_id = fl.feature_id AND fl.srcfeature_id = ${bin.feature_id}
		WHERE f.uniquename = ${feature.uniquename}
	""")

	Integer featureloc_id = flrow.featureloc_id
	def is_fmin_partial = flrow.is_fmin_partial
	def is_fmax_partial = flrow.is_fmax_partial

	if (featureloc_id == null) {
		throw new RuntimeException("The featureloc is null for ${feature.uniquename} on ${bin.uniquename} ")
	}

	sql.execute("""
		DELETE FROM featureloc where featureloc_id = ${featureloc_id}
	""")

	def newFmin = feature.fmin - contig.fmin
	def newFmax = feature.fmax - contig.fmin

	println "         ${feature.fmin}, ${feature.fmax}, ${is_fmin_partial}, ${is_fmax_partial} -> ${newFmin}, ${newFmax} (${feature.phase} - ${feature.strand})"

	sql.execute("""
		INSERT INTO featureloc (srcfeature_id, feature_id, fmin,
			                    is_fmin_partial, fmax, is_fmax_partial,
			                    phase, strand) VALUES
			(${contig.feature_id}, ${feature.feature_id}, ${newFmin},
				 ${is_fmin_partial}, ${newFmax}, ${is_fmax_partial},
				 ${feature.phase}, ${feature.strand})
	""")
}





def usage () {
	println """
groovy -cp /path/to/psql-driver.jar CleanupBins.groovy config [rollback|commit] [bins]
	Arguments (in order):
		config
			the name of the genedb config to be used
		action [rollback|commit]
			instruct a dry run - without committing to the db (defaults to commit)
		bins
			the uniquename of the bin chromosome features, comma-separated (if not supplied, this will mean all the bins in the db)
"""
}

if (args.length < 2) {
	usage()
	System.exit(101)
}

String config = this.args[0]

Boolean rollback = false;
if (this.args[1] == "rollback") {
	rollback = true;
} else if (this.args[1] == "commit") {
	rollback = false;
} else {
	println "Invalid instruction - must be either rollback or commit"
	usage()
	System.exit(1)
}

String bins = (args.length >= 3) ? this.args[2] : null;

Properties props = new java.util.Properties()
props.load(new FileInputStream("property-file.${config}"))

def dbname = props.getProperty('dbname')
def dbport = props.getProperty('dbport')
def dbhost = props.getProperty('dbhost')

// we don't use the config's username and password in this case
def dbuser = "pathdb"
def dbpassword = "LongJ!@n"


Sql sql = Sql.newInstance(
	"jdbc:postgresql://${dbhost}:${dbport}/${dbname}",
	"${dbuser}",
	"${dbpassword}",
	"org.postgresql.Driver")

def bin_list = []

if (bins == null) {
	sql.eachRow("""
		select uniquename
			from feature
			join feature_cvterm on feature_cvterm.feature_id = feature.feature_id
			join cvterm on feature_cvterm.cvterm_id = cvterm.cvterm_id and cvterm.name = 'bin_chromosome'
		""", { row -> bin_list << row.toRowResult().uniquename
		})
} else if (bins.contains(",")) {
	def untrimmed_bins = bins.split(",")
	for (String bin : untrimmed_bins) {
		bin_list << bin.trim()
	}
} else {
	bin_list << bins
}

println "Processing bins:"
println bin_list

try {

	sql.withTransaction {

		for (String bin : bin_list) {
			Feature binFeature = getFeature(bin, sql)

			if (binFeature == null) {
				println "The bin ${bin} does not exist!"
				System.exit(1)
			}

			if (binFeature.type != "chromosome") {
				println "${bin} is not a chromosome, it's a ${binFeature.type}"
				System.exit(1)
			}

			Boolean containsContigSeparator = (binFeature.residues.indexOf(contigSeparator) == -1) ? false : true ;

			Cvterm topLevelType = getTopLevelFeatureCvtermId(sql)

			def contigs = getBinContigs(binFeature, sql)
			Boolean generated = false

			if (contigs.size() == 0) {

				println "No contig feature! Attempting to create them."

				println "Contains contig separators? ${containsContigSeparator}"

				if (! containsContigSeparator) {
					println "Could not find any contig separators."
					System.exit(1)
				}

				contigs = generateBinContigs(binFeature, contigSeparator, sql)
				generated = true

			}


			for (Feature contig in contigs) {

				println ">> ${contig.uniquename} ${contig.feature_id} "

				if (contigHasEMBLQualifierType(contig, sql)) {
					continue
				}

				def features = getFeaturesSpanningTheInsidesOfAContig(binFeature, contig, sql)

                promoteContigToTopLevel(binFeature, contig, topLevelType, generated, sql)
				if (features.size() > 0) {
					for (Feature feature : features) {
						println " >>>> ${feature.uniquename} ${feature.type}"
						relocateFeatureToContig(binFeature, contig, feature, sql)
					}
				}


			}

			println "Deleting old bin ${binFeature.feature_id} ${binFeature.uniquename} and any remaining subfeatures that have not been promoted to top level features."
			sql.execute("""
				DELETE FROM feature WHERE feature_id IN (SELECT feature_id FROM featureloc WHERE srcfeature_id = ${binFeature.feature_id})
			""")
			sql.execute("""
				DELETE FROM featureloc WHERE srcfeature_id = ${binFeature.feature_id}
			""")
			sql.execute("""
				DELETE FROM feature WHERE feature_id = ${binFeature.feature_id}
			""")

			if (rollback) {
				println "Rolling back ..."
				sql.rollback()
			}
		}


	}
} finally {
	if (sql != null) {
		println "Closing connection ..."
		sql.close()
	}
}



