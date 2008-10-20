package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.cfg.FeatureTypeUtils;
import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.DirectRepeatRegion;
import org.gmod.schema.feature.FivePrimeUTR;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RRNA;
import org.gmod.schema.feature.RepeatRegion;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.ThreePrimeUTR;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.feature.UTR;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.ObjectManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deals with loading an organism from an EMBL file into a Chado database.
 * It's expected to be configured as a singleton Spring bean. The main
 * calling point (and only public method, apart from the property setters)
 * is {@link #load(EmblFile)}.
 *
 * @author rh11
 *
 */
@Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
class EmblLoader {
    private static final Logger logger = Logger.getLogger(EmblLoader.class);

    // Constants

    /**
     * Whether or not the transcript type (mRNA, tRNA etc) should be appended
     * to the uniquename of the transcript feature in the database. We must do this
     * because Artemis requires each feature to have a globally unique name, and does
     * not work correctly if a transcript has the same uniquename as its gene.
     * Moreover, this can't be changed without causing serious potential problems
     * elsewhere, because the GFF3 feature format requires feature names to be
     * globally unique, and we want to be able to export our data in GFF3 format.
     */
    private static final boolean APPEND_TYPE_TO_TRANSCRIPT_UNIQUENAME = true;

    // Injected beans
    private CvDao cvDao;
    private GeneralDao generalDao;
    private OrganismDao organismDao;
    private ObjectManager objectManager;     // See #afterPropertiesSet()
    private SessionFactory sessionFactory;

    private SynonymManager synonymManager = new SynonymManager();

    // Configurable parameters
    private Organism organism;
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Supercontig.class;

    public enum OverwriteExisting {YES, NO, MERGE}
    private OverwriteExisting overwriteExisting = OverwriteExisting.NO;

    private boolean sloppyControlledCuration = false;

    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    /**
     * Set the class of top-level feature that this EMBL file represents.
     * The default, if this method is not called, is <code>Supercontig</code>.
     *
     * @param topLevelFeatureClass
     */
    public void setTopLevelFeatureClass(Class<? extends TopLevelFeature> topLevelFeatureClass) {
        this.topLevelFeatureClass = topLevelFeatureClass;
    }

    /**
     * Whether we should overwrite an existing top-level feature if it has
     * the same name as the one specified in this file. The default, if this
     * method is not called, is <code>NO</code>.
     *
     * If overwriteExisting is <code>NO</code>, the file will be skipped on the
     * grounds that it's already loaded. If it's <code>YES</code>, the previously
     * existing top-level feature, and features located on it, will
     * be deleted first. If it's <code>MERGE</code>, the existing top-level feature
     * and all features located on it will be retained, and any features
     * specified in this file will be loaded in addition.
     *
     * @param overwriteExisting <code>YES</code> if we should overwrite an
     * existing top-level feature, <code>NO</code> if not, or <code>MERGE</code>
     * if we should merge the contents of this file with an existing feature.
     */
    public void setOverwriteExisting(OverwriteExisting overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    /**
     * Whether to deal with controlled_curation qualifiers that don't have the expected
     * format. The default, if this method is not called, is <code>false</code>.
     *
     * If set to true, we simply parse any dbxref from the /controlled_curation qualifier,
     * and add the complete text as a /curation qualifier.
     *
     * @param sloppyControlledCuration
     */
    public void setSloppyControlledCuration(boolean sloppyControlledCuration) {
        this.sloppyControlledCuration = sloppyControlledCuration;
    }

    private Session session;

    /**
     * The main calling point for this class. Takes a parsed EMBL file, and loads
     * it into the database. Each call to this method constitutes a separate
     * Hibernate transaction. Even though the EMBL file has been parsed before
     * this method is called, its internal consistency has not been verified.
     * If we encounter a problem, a <code>DataError</code> is thrown and the
     * transaction is rolled back.
     *
     * @param emblFile the parsed EMBL file
     * @throws DataError if a data problem is discovered
     */
    public void load(EmblFile emblFile) throws DataError {
        /*
         * Thanks to the @Transactional annotation on this class,
         * Spring will automatically initiate a transaction when
         * we're called, which will be committed on successful
         * return or rolled back if we throw an exception.
         */

        this.session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        Feature existingTopLevelFeature = (Feature) session.createCriteria(Feature.class)
            .add(Restrictions.eq("organism", organism))
            .add(Restrictions.eq("uniqueName", emblFile.getAccession()))
            .uniqueResult();

        TopLevelFeature topLevelFeature = null;

        if (existingTopLevelFeature != null) {
            switch (overwriteExisting) {
            case YES:
                logger.trace(String.format("Deleting existing feature '%s' (ID=%d)",
                    existingTopLevelFeature.getUniqueName(), existingTopLevelFeature.getFeatureId()));

                existingTopLevelFeature.delete();
                break;
            case NO:
                logger.error(String.format("The organism '%s' already has feature '%s'",
                    organism.getCommonName(), emblFile.getAccession()));
                return;
            case MERGE:
                if (existingTopLevelFeature instanceof TopLevelFeature) {
                    topLevelFeature = (TopLevelFeature) existingTopLevelFeature;
                } else {
                    logger.error(String.format("We can't merge onto the feature '%s', because it's not a top-level feature",
                        existingTopLevelFeature.getUniqueName()));
                    return;
                }
                break;
            }
        } else if (overwriteExisting == OverwriteExisting.MERGE) {
            logger.error(String.format("Cannot MERGE because feature '%s' does not exist", emblFile.getAccession()));
            return;
        }

        synonymManager.startSession(session);
        taxonomicDivision = emblFile.getTaxonomicDivision();
        logger.trace("taxonomicDivision = " + taxonomicDivision);

        if (topLevelFeature == null) {
            logger.info("Creating topLevelFeature: " + emblFile.getAccession());
            topLevelFeature = TopLevelFeature.make(topLevelFeatureClass, emblFile.getAccession(), organism);
            topLevelFeature.markAsTopLevelFeature();
            topLevelFeature.setResidues(emblFile.getSequence());
            session.persist(topLevelFeature);
        }

        init(topLevelFeature);
        EmblLocation.Join contigLocations = emblFile.getContigLocations();
        if (contigLocations != null) {
            loadContigsAndGaps(contigLocations);
        }
        loadFeatures(emblFile.getFeatureTable());
    }

    private String taxonomicDivision;
    private TopLevelFeature topLevelFeature;
    private Map<String,AbstractGene> genesByUniqueName;
    private Map<String,Transcript> transcriptsByUniqueName;
    private NavigableMap<Integer,Contig> contigsByStart;

    private void init(TopLevelFeature topLevelFeature) {
        if (topLevelFeature == null) {
            throw new IllegalArgumentException("topLevelFeature cannot be null");
        }
        this.topLevelFeature = topLevelFeature;
        this.genesByUniqueName = new HashMap<String,AbstractGene>();
        this.transcriptsByUniqueName = new HashMap<String,Transcript>();
        this.contigsByStart = new TreeMap<Integer,Contig>();
    }

    private void loadContigsAndGaps(EmblLocation.Join locations) throws DataError {
        int pos = 0; // Position (interbase) on topLevelFeature
        for(EmblLocation location: locations.locations) {
            if (location instanceof EmblLocation.External) {
                EmblLocation.External externalLocation = (EmblLocation.External) location;
                int contigLength = externalLocation.simple.getLength();

                logger.debug(String.format("Creating contig '%s' at %d-%d", externalLocation.accession, pos, pos + contigLength));
                Contig contig = TopLevelFeature.make(Contig.class, externalLocation.accession, organism);
                contig.setResidues(topLevelFeature.getResidues(pos, pos + contigLength));
                session.persist(contig);
                topLevelFeature.addLocatedChild(contig, pos, pos + contigLength, (short) 0, 0);

                contigsByStart.put(pos, contig);

                pos += contigLength;
            } else if (location instanceof EmblLocation.Gap) {
                EmblLocation.Gap gapLocation = (EmblLocation.Gap) location;
                int gapLength = gapLocation.getLength();

                logger.debug(String.format("Creating gap at %d-%d", pos, pos + gapLength));
                Gap gap = topLevelFeature.addGap(pos, pos + gapLength);
                session.persist(gap);

                pos += gapLength;
            } else {
                throw new DataError("The CO section should contain only external references and gaps");
            }
        }
    }

    private void locate(Feature feature, EmblLocation location) {
        locate(feature, location.getFmin(), location.getFmax(), (short) location.getStrand(), null);
    }

    private void locate(Feature feature, int fmin, int fmax, short strand, Integer phase) {
        topLevelFeature.addLocatedChild(feature, fmin, fmax, strand, phase);
        Contig contig = contigsByStart.isEmpty() ? null : contigsByStart.floorEntry(fmin).getValue();
        if (contig == null || fmax > contig.getFmax()) {
            logger.debug(String.format("The feature '%s' (%s) is not contained in a contig",
                feature.getUniqueName(), feature.getName()));
            return;
        }
        logger.debug(String.format("The feature '%s' lies on contig '%s'", feature.getUniqueName(), contig.getUniqueName()));
        contig.addLocatedChild(feature, fmin - contig.getFmin(), fmax - contig.getFmin(), strand, phase, 1, 1);
    }

    private void loadFeatures(FeatureTable featureTable) throws DataError {
        List<FeatureTable.Feature> utrs = new ArrayList<FeatureTable.Feature>();

        if (featureTable == null) {
            logger.error("No feature table found!");
            return;
        }

        for (FeatureTable.Feature feature: featureTable.getFeatures()) {
            try {
                loadFeature(utrs, feature);
            }
            catch (DataError e) {
                logger.warn("Caught DataError while loading feature. Setting line number to " + feature.lineNumber);
                e.setLineNumber(feature.lineNumber);
                throw e;
            }
        }

        for (FeatureTable.Feature utr: utrs) {
            try {
                loadUTR(utr);
            }
            catch (DataError e) {
                logger.debug("Caught DataError while loading UTR. Setting line number to " + utr.lineNumber);
                e.setLineNumber(utr.lineNumber);
                throw e;
            }
        }
    }

    private void loadFeature(List<FeatureTable.Feature> utrs,
            FeatureTable.Feature feature) throws DataError {
        String featureType = feature.type;

        if (feature.location.getFmax() < feature.location.getFmin()) {
            throw new DataError("Location has fmax before fmin");
        }

        if (featureType.equals("repeat_region")) {
            loadRepeatRegion(feature);
        }
        else if (featureType.equals("CDS")) {
            loadCDS((FeatureTable.CDSFeature) feature);
        }
        else if (featureType.equals("tRNA")) {
            loadTRNA(feature);
        }
        else if (featureType.equals("rRNA")) {
            loadRRNA(feature);
        }
        else if (featureType.equals("misc_RNA")) {
            loadNcRNA(feature);
        }
        else if (featureType.equals("3'UTR") || featureType.equals("5'UTR")) {
            utrs.add(feature);
        }
        else {
            logger.warn(String.format("Ignoring '%s' feature on line %d", featureType, feature.lineNumber));
        }
    }

    private void loadRepeatRegion(FeatureTable.Feature repeatRegionFeature) throws DataError {
        String repeatRegionName = repeatRegionFeature.getQualifierValue("FEAT_NAME");
        EmblLocation repeatRegionLocation = repeatRegionFeature.location;
        int fmin = repeatRegionLocation.getFmin();
        int fmax = repeatRegionLocation.getFmax();

        String repeatType = repeatRegionFeature.getQualifierValue("rpt_type");
        final Class<? extends RepeatRegion> repeatRegionClass;
        if (repeatType == null) {
            repeatRegionClass = RepeatRegion.class;
        } else if (repeatType.equals("direct")) {
            repeatRegionClass = DirectRepeatRegion.class;
        } else {
            throw new DataError(String.format("Unknown repeat type '%s'", repeatType));
        }

        logger.debug(String.format("Creating repeat region '%s' of type '%s' at %d-%d", repeatRegionName, repeatRegionClass, fmin, fmax));
        RepeatRegion repeatRegion = RepeatRegion.make(repeatRegionClass,
            organism, String.format("%s:repeat:%d-%d", topLevelFeature.getUniqueName(), fmin, fmax), repeatRegionName);

        int rank=0;
        for(String note : repeatRegionFeature.getQualifierValues("note")) {
            repeatRegion.addFeatureProp(note, "feature_property", "comment", rank++);
        }

        session.persist(repeatRegion);
        locate(repeatRegion, fmin, fmax, (short)0, 0);
    }

    /**
     * Abstract superclass for gene loaders.
     * <p>
     * It is the responsibility of each implementing class to set at least
     * the fields <code>geneUniqueName</code>, <code>transcriptUniqueName</code>
     * and <code>geneName</code> in its constructor.
     *
     * @author rh11
     *
     */
    abstract class GeneLoader {
        protected FeatureTable.Feature feature;
        protected EmblLocation location;
        protected boolean isPseudo = false;
        protected boolean singlySpliced = true;
        protected String geneUniqueName = null;
        protected String transcriptUniqueName = null;
        protected String geneName;
        protected Transcript transcript;

        /**
         * The focal feature is the one to which annotation is added.
         * This is the polypeptide where possible, or the transcript if
         * there is no polypeptide.
         */
        protected Feature focalFeature;
        protected Integer phase = null;

        public GeneLoader(FeatureTable.Feature feature) {
            this.feature  = feature;
            this.location = feature.location;
        }

        protected Class<? extends AbstractGene> getGeneClass() {
            return isPseudo ? Pseudogene.class : Gene.class;
        }

        protected abstract void processTranscriptQualifiers() throws DataError;
        protected abstract Class<? extends Transcript> getTranscriptClass();

        protected String getTranscriptType() {
            /*
             * This assumes that transcript feature classes are annotated with term
             * rather than accession, which is true at the time of writing.
             */
            return FeatureTypeUtils.getFeatureTypeForClass(getTranscriptClass()).term();
        }

        /**
         * The main entry point to a gene loader.
         */
        public void load() throws DataError {
            if (geneUniqueName == null) {
                throw new RuntimeException("Cannot load a gene with no uniqueName");
            }
            if (transcriptUniqueName == null) {
                throw new RuntimeException("Cannot load a transcript with no uniqueName");
            }
            loadTranscript(loadOrFetchGene());
        }

        private AbstractGene loadOrFetchGene() {
            if (singlySpliced) {
                AbstractGene gene = createSinglySplicedGene();
                genesByUniqueName.put(geneUniqueName, gene);
                return gene;
            } else {
                if (genesByUniqueName.containsKey(geneUniqueName)) {
                    logger.debug(String.format("Gene for shared ID '%s' already exists", geneUniqueName));
                    return genesByUniqueName.get(geneUniqueName);
                } else {
                    // This is the first transcript, so create the gene
                    AbstractGene gene = createGene();
                    genesByUniqueName.put(geneUniqueName, gene);
                    return gene;
                }
            }
        }

        private AbstractGene createSinglySplicedGene() {
            if (transcriptUniqueName.contains(".")) {
                logger.warn(String.format(
                    "The transcript '%s' is not alternately spliced, yet its systematic name contains a dot",
                    transcriptUniqueName));
            }
            return createGene();
        }

        private AbstractGene createGene() {
            logger.debug(String.format("Creating gene '%s' (%s)", geneUniqueName, geneName));
            AbstractGene gene = AbstractGene.make(getGeneClass(), organism, geneUniqueName, geneName);
            locate(gene, location);
            session.persist(gene);
            return gene;
        }

        private void loadTranscript(AbstractGene gene) throws DataError {
            logger.debug(String.format("Creating transcript '%s' for gene '%s'", transcriptUniqueName, gene.getUniqueName()));
            if (APPEND_TYPE_TO_TRANSCRIPT_UNIQUENAME) {
                this.transcript = gene.makeTranscript(getTranscriptClass(),
                    String.format("%s:%s", transcriptUniqueName, getTranscriptType()), location.getFmin(), location.getFmax());
            } else {
                this.transcript = gene.makeTranscript(getTranscriptClass(), transcriptUniqueName, location.getFmin(), location.getFmax());
            }

            focalFeature = transcript;
            if (transcript instanceof ProductiveTranscript) {
                Polypeptide polypeptide = ((ProductiveTranscript) transcript).getProtein();
                if (polypeptide != null) {
                    focalFeature = polypeptide;
                }
            }

            transcriptsByUniqueName.put(transcriptUniqueName, transcript);
            processTranscriptQualifiers();

            loadExons();
        }

        /**
         * For each <code>/&lt;qualifierName&gt;</code> qualifier, add a synonym of type
         * <code>synonymType</code> to the transcript.
         *
         * @param qualifierName the name of the qualifer
         * @param synonymType the type of synonym. Should be a term in the <code>genedb_synonym_type</code> CV
         * @param isCurrent whether the synonym is current or not
         */
        protected void addTranscriptSynonymsFromQualifier(String qualifierName, String synonymType, boolean isCurrent) {
            Set<String> synonyms = new HashSet<String>();
            for (String synonymString: feature.getQualifierValues(qualifierName)) {
                if (synonyms.contains(synonymString)) {
                    logger.error(String.format("The qualifier /%s=\"%s\" is repeated on transcript '%s'",
                        qualifierName, synonymString, transcriptUniqueName));
                    continue;
                }
                synonyms.add(synonymString);

                logger.debug(String.format("Adding %s '%s' for transcript", synonymType, synonymString));
                Synonym synonym = synonymManager.getSynonym(synonymType, synonymString);
                transcript.addSynonym(synonym, isCurrent, /*isInternal:*/ false);
            }
        }

        /**
         * For each <code>/&lt;qualifierName&gt;</code> qualifier, add a property of type
         * <code>synonymType</code> to the polypeptide, if there is one, or else to the
         * transcript.
         *
         * @param qualifierName the qualifier name
         * @param propertyCvName the name of the CV to which the property term belongs.
         *          Should be either <code>feature_property</code> for built-in Chado
         *          properties, or <code>genedb_misc</code> for local additions.
         * @param propertyTermName the term name corresponding to the property to add.
         *          If it belongs to the <code>genedb_misc</code> CV, it should be a child of
         *          the term <code>genedb_misc:feature_props</code>.
         */
        protected void processPropertyQualifiers(String qualifierName, String propertyCvName, String propertyTermName) {
            int rank = 0;
            for(String qualifierValue: feature.getQualifierValues(qualifierName)) {
                logger.debug(String.format("Adding %s:%s '%s' for transcript",
                                propertyCvName, propertyTermName, qualifierValue));
                focalFeature.addFeatureProp(qualifierValue, propertyCvName, propertyTermName, rank++);
            }
        }

        protected void processCvTermQualifiers(String qualifierName, String cvName, boolean createTerms)
        throws DataError {
            processCvTermQualifiers(qualifierName, cvName, createTerms, null);
        }

        protected void processCvTermQualifiers(String qualifierName, String cvName,
                boolean createTerms, TermNormaliser termNormaliser)
                throws DataError {
            List<String> terms = feature.getQualifierValues(qualifierName);

            for (String term: terms) {
                String normalisedTerm = term;
                if (termNormaliser != null) {
                    normalisedTerm = termNormaliser.normalise(term);
                }
                FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cvName, normalisedTerm, createTerms);
                if (featureCvTerm == null) {
                    throw new DataError(
                        String.format("Failed to find term '%s' in CV '%s'", normalisedTerm, cvName));
                }
                session.persist(featureCvTerm);
            }
        }

        private void loadExons() throws DataError {
            int exonIndex = 0;
            for (EmblLocation exonLocation: location.getParts()) {
                if (exonLocation instanceof EmblLocation.External) {
                    throw new DataError("Found an external exon (trans-splicing). We can't handle that yet.");
                }
                String exonUniqueName = String.format("%s:exon:%d", transcriptUniqueName, ++exonIndex);
                logger.debug(String.format("Creating exon '%s' at %d-%d", exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax()));
                AbstractExon exon = transcript.createExon(exonUniqueName, exonLocation.getFmin(), exonLocation.getFmax(), phase);
                session.persist(exon);
            }
        }


        private Pattern subqualifierPattern = Pattern.compile("\\G\\s*([^=]+)=\\s*([^;]*)\\s*(?:;|\\z)");

        protected void processCuration() throws DataError {
            processPropertyQualifiers("curation", "genedb_misc", "curation");

            if (sloppyControlledCuration) {
                processControlledCurationSloppy();
            } else {
                processControlledCurationStrict();
            }
        }
        private void processControlledCurationSloppy() throws DataError {
            int rank = feature.getQualifierValues("curation").size();
            for (String controlledCuration: feature.getQualifierValues("controlled_curation")) {
                String curation = String.format("[%s]", controlledCuration);
                logger.trace(String.format("Sloppy curation: adding /curation=\"%s\" with rank %d", curation, rank));
                focalFeature.addFeatureProp(curation, "genedb_misc", "curation", rank++);

                Matcher matcher = subqualifierPattern.matcher(controlledCuration);
                while (matcher.find()) {
                    String key = matcher.group(1).toLowerCase();
                    String value = matcher.group(2);

                    if (key.equals("db_xref") && value.length() > 0) {
                        if (value.indexOf(":") < 0) {
                            value = "PMID:" + value;
                        }
                        logger.trace(String.format("Sloppy controlled_curation: adding dbxref for '%s'", value));
                        addDbXRefs(value);
                    }
                }
            }
        }

        private Set<String> subqualifiers = new HashSet<String>() {{
            Collections.addAll(this,
                "term", "cv", "qualifier", "evidence", "db_xref", "residue", "attribution", "date");
        }};

        private void processControlledCurationStrict() throws DataError {
            String controlledCuration = feature.getQualifierValue("controlled_curation");
            if (controlledCuration == null) {
                return;
            }

            Matcher matcher = subqualifierPattern.matcher(controlledCuration);
            Map<String, String> valuesByKey = new HashMap<String, String>();
            while (matcher.find()) {
                String key = matcher.group(1).toLowerCase();
                String value = matcher.group(2);

                if (subqualifiers.contains(key)) {
                    valuesByKey.put(key, value);
                }
            }

            if (!valuesByKey.containsKey("term")) {
                throw new DataError("/controlled_curation has no 'term' field");
            }
            String term = valuesByKey.get("term");
            String cv   = valuesByKey.containsKey("cv") ? valuesByKey.get("cv") : "genedb_misc";

            FeatureCvTerm featureCvTerm = focalFeature.addCvTerm(cv, term);

            featureCvTerm.addPropIfNotNull("feature_property", "date",   valuesByKey.get("date"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "attribution", valuesByKey.get("attribution"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "evidence",    valuesByKey.get("evidence"));
            featureCvTerm.addPropIfNotNull("genedb_misc", "qualifier",   valuesByKey.get("qualifier"));

            if (valuesByKey.containsKey("db_xref")) {
                addDbXRefs(valuesByKey.get("db_xref"));
            }
        }

        private Pattern dbxrefPattern = Pattern.compile("([^:]+):(.*)");
        /**
         * Add DbXRefs.
         * @param dbxrefs a pipe-separated list of <code>db:accession</code>
         * @throws DataError
         */
        protected void addDbXRefs(String dbxrefs) throws DataError {
            for (String dbxref: dbxrefs.split("\\|")) {
                Matcher matcher = dbxrefPattern.matcher(dbxref);
                if (!matcher.matches()) {
                    throw new DataError(String.format("db_xref '%s' is not of the form database:accession", dbxref));
                }
                String dbName = matcher.group(1);
                String accession = matcher.group(2);
                Db db = generalDao.getDbByName(dbName);
                if (db == null) {
                    throw new DataError(String.format("Unrecognised database '%s' in dbxref", dbName));
                }
                DbXRef dbXRef = new DbXRef(db, accession);
                focalFeature.addDbXRef(dbXRef);
            }
        }
    }

    class CDSLoader extends GeneLoader {
        public CDSLoader(FeatureTable.CDSFeature cdsFeature) throws DataError {
            super(cdsFeature);

            isPseudo = cdsFeature.isPseudo();
            geneUniqueName = cdsFeature.getSharedId();
            transcriptUniqueName = cdsFeature.getUniqueName();

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms. The genes do
                // not have primary names.
                geneName = null;
            }

            String codonStart = cdsFeature.getQualifierValue("codon_start");
            if (codonStart != null) {
                try {
                    phase = Integer.parseInt(codonStart) - 1;
                } catch (NumberFormatException e) {
                    throw new DataError(
                        String.format("Could not parse value of /codon_start qualifier ('%s')", codonStart));
                }
                if (phase < 0 || phase > 2) {
                    throw new DataError(
                        String.format("Value of /codon_start qualifier out of range (%d)", phase+1));
                }
            }

            singlySpliced = false;
            if (geneUniqueName == null) {
                singlySpliced = true;
                geneUniqueName = transcriptUniqueName;
            }
        }

        /**
         * Use the qualifiers of the CDS feature to add various bits of annotation
         * to the transcript (or to the polypeptide, if there is one). Specifically,
         * add synonyms, properties and products.
         */
        @Override
        protected void processTranscriptQualifiers() throws DataError {

            addTranscriptSynonymsFromQualifier("synonym", "synonym", true);
            addTranscriptSynonymsFromQualifier("previous_systematic_id", "systematic_id", false);
            addTranscriptSynonymsFromQualifier("systematic_id", "systematic_id", true);
            addTranscriptSynonymsFromQualifier("temporary_systematic_id", "temporary_systematic_id", true);

            processPropertyQualifiers("note",     "feature_property", "comment");
            processPropertyQualifiers("method",     "genedb_misc",      "method");
            processPropertyQualifiers("colour",     "genedb_misc",      "colour");
            processPropertyQualifiers("status",     "genedb_misc",      "status");
            processPropertyQualifiers("fasta_file", "genedb_misc",      "fasta_file");

            processCvTermQualifiers("class", "RILEY", false, normaliseRileyNumber);
            processCvTermQualifiers("product", "genedb_products", true);

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms.
                addTranscriptSynonymsFromQualifier("gene", "synonym", true);
            }

            processCuration();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return isPseudo ? PseudogenicTranscript.class : MRNA.class;
        }
    }

    /**
     * Convert the qualifier value into a canonical form before treating it as
     * a term name. What this means will depend
     */
    private static interface TermNormaliser {
        public String normalise(String term) throws DataError;
    }

    /**
     * A term normaliser for Riley classification numbers, which for example
     * will normalise "2.2.07" to "2.2.7".
     */
    private static TermNormaliser normaliseRileyNumber = new TermNormaliser() {
        private final Pattern RILEY_PATTERN = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})");
        public String normalise(String term) throws DataError {
            Matcher matcher = RILEY_PATTERN.matcher(term);
            if (!matcher.matches()) {
                throw new DataError(String.format("Failed to parse Riley number '%s'", term));
            }
            return String.format("%d.%d.%d",
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
        }
    };

    private void loadCDS(FeatureTable.CDSFeature cdsFeature) throws DataError {
        new CDSLoader(cdsFeature).load();
    }


    /* tRNA */
    private int syntheticTRNAIndex = 1;
    private class TRNALoader extends GeneLoader {

        public TRNALoader(FeatureTable.Feature feature) throws DataError {
            super(feature);

            geneUniqueName = transcriptUniqueName = feature.getUniqueName(false);
            if (geneUniqueName == null) {
                geneUniqueName = transcriptUniqueName = makeSyntheticName();
            }
        }

        private String makeSyntheticName() {
            return String.format("%s_tRNA%d", topLevelFeature.getUniqueName(), syntheticTRNAIndex++);
        }

        @Override
        protected void processTranscriptQualifiers() throws DataError {
            processPropertyQualifiers("note",      "feature_property", "comment");
            processPropertyQualifiers("anticodon", "feature_property", "anticodon");
            processCvTermQualifiers("product", "genedb_products", true);
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return TRNA.class;
        }
    }

    private void loadTRNA(FeatureTable.Feature feature) throws DataError {
        new TRNALoader(feature).load();
    }


    /* rRNA */
    private int syntheticRRNAIndex = 1;
    private class RRNALoader extends GeneLoader {
        public RRNALoader(FeatureTable.Feature feature) throws DataError {
            super(feature);

            geneUniqueName = transcriptUniqueName = feature.getUniqueName(false);
            if (geneUniqueName == null) {
                geneUniqueName = transcriptUniqueName = makeSyntheticName();
            }
        }

        private String makeSyntheticName() {
            return String.format("%s_rRNA%d", topLevelFeature.getUniqueName(), syntheticRRNAIndex++);
        }

        @Override
        protected void processTranscriptQualifiers() throws DataError {
            processPropertyQualifiers("note",      "feature_property", "comment");
            processCvTermQualifiers("product", "genedb_products", true);
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return RRNA.class;
        }
    }
    private void loadRRNA(FeatureTable.Feature feature) throws DataError {
        new RRNALoader(feature).load();
    }


    /* ncRNA (from misc_RNA features) */
    private int syntheticNcRNAIndex = 1;
    private class NcRNALoader extends GeneLoader {
        public NcRNALoader(FeatureTable.Feature feature) throws DataError {
            super(feature);

            geneUniqueName = transcriptUniqueName = feature.getUniqueName(false);
            if (geneUniqueName == null) {
                geneUniqueName = transcriptUniqueName = makeSyntheticName();
            }
        }

        private String makeSyntheticName() {
            return String.format("%s_ncRNA%d", topLevelFeature.getUniqueName(), syntheticNcRNAIndex++);
        }

        @Override
        protected void processTranscriptQualifiers() throws DataError {
            processPropertyQualifiers("note",  "feature_property", "comment");
            processCvTermQualifiers("product", "genedb_products", true);

            if (taxonomicDivision.equals("PRO")) {
                // Bacteria don't have splicing, so a CDS feature is a gene and
                // a transcript and that is the end of it. One or more /gene
                // qualifiers may be used to indicate synonyms.
                addTranscriptSynonymsFromQualifier("gene", "synonym", true);
            }

            processCuration();
        }

        @Override
        protected Class<? extends Transcript> getTranscriptClass() {
            return NcRNA.class;
        }
    }
    private void loadNcRNA(FeatureTable.Feature feature) throws DataError {
        new NcRNALoader(feature).load();
    }


    /* UTR */
    private void loadUTR(FeatureTable.Feature utrFeature) throws DataError {
        String utrType = utrFeature.type;
        EmblLocation utrLocation = utrFeature.location;
        String uniqueName = utrFeature.getUniqueName();

        logger.debug(String.format("Loading %s for '%s' at %s", utrType, uniqueName, utrLocation));

        Transcript transcript = transcriptsByUniqueName.get(uniqueName);
        if (transcript == null) {
            throw new DataError(String.format("Could not find transcript '%s' for %s", uniqueName, utrType));
        }

        Class<? extends UTR> utrClass;
        if (utrType.equals("3'UTR")) {
            utrClass = ThreePrimeUTR.class;
        } else if (utrType.equals("5'UTR")) {
            utrClass = FivePrimeUTR.class;
        } else {
            throw new RuntimeException(String.format("Unrecognised UTR feature type '%s'", utrType));
        }

        int part = 1;
        List<EmblLocation> utrParts = utrLocation.getParts();
        for (EmblLocation utrPartLocation: utrParts) {
            String utrUniqueName = String.format("%s:%dutr", uniqueName, utrClass == ThreePrimeUTR.class ? 3 : 5);
            if (utrParts.size() > 1) {
                utrUniqueName += ":" + part;
            }

            logger.debug(String.format("Creating %s feature '%s' at %d-%d",
                utrType, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax()));

            UTR utr = transcript.createUTR(utrClass, utrUniqueName, utrPartLocation.getFmin(), utrPartLocation.getFmax());
            session.persist(utr);
            ++ part;
        }
    }

    /* Setters and Spring stuff */
    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    /**
     * Set the ObjectManager. This is expected to be called by Spring.
     * We will inject the GeneralDao object into the ObjectManager ourselves from
     * {@link #afterPropertiesSet}, so this ObjectManager need not have the GeneralDao
     * injected yet. This avoids circularity.
     *
     * @param objectManager
     */
    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void afterPropertiesSet() {
        synonymManager.setObjectManager(objectManager);

        /*
         * We cannot set the DAOs of the objectManager
         * directly in FileProcessor.xml, because that creates a circular
         * reference that (understandably) causes Spring to
         * throw a tantrum. Thus we inject them into
         * here, and pass them to the ObjectManager after Spring
         * configuration.
         */
        objectManager.setGeneralDao(generalDao);
        objectManager.setCvDao(cvDao);
    }
}
