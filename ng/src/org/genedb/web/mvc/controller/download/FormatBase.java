package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.tmpquery.GeneDetail;
import org.gmod.schema.mapped.Feature;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * A base for formatter class collating common properties.
 * 
 * @author gv1
 */
public abstract class FormatBase {
	
	private Logger logger = Logger.getLogger(FormatBase.class);
	
	protected String fieldSeparator = "";
	protected String fieldInternalSeparator = "";
	protected String recordSeparator = "";
	
	protected String postFieldSeparator = "";
	protected String postFieldInternalSeparator = "";
	protected String postRecordSeparator = "";
	
	protected String headerContentStart = "";
	protected String footerContentStart = "";
	
	protected String blankField = "";
	
	protected List<OutputOption> outputOptions = new ArrayList<OutputOption>();
	
	protected boolean header;
	
	protected Writer writer;
	
	private SequenceDao sequenceDao;
	
	protected TransactionTemplate transactionTemplate;
	
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}
	
	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}
	
	public void setPostFieldSeparator(String postFieldSeparator) {
		this.postFieldSeparator = postFieldSeparator;
	}
	
	public void setFieldInternalSeparator(String fieldInternalSeparator) {
		this.fieldInternalSeparator = fieldInternalSeparator;
	}
	
	public void setPostFieldInternalSeparator(String postFieldInternalSeparator) {
		this.postFieldInternalSeparator = postFieldInternalSeparator;
	}
	
	public void setBlankField(String blankField) {
		this.blankField = blankField;
	}
	
	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}
	
	public void setPostRecordSeparator(String postRecordSeparator) {
		this.postRecordSeparator = postRecordSeparator;
	}
	
	public void setOutputOptions(List<OutputOption> outputOptions) {
		this.outputOptions = outputOptions;
	}
	
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void setHeader(boolean header) {
		this.header = header;
	}
	
	public void setHeaderContentStart(String headerContentStart) {
		this.headerContentStart = headerContentStart;
	}
	
	public void setFooterContentStart(String footerContentStart) {
		this.footerContentStart = footerContentStart;
	}
	
//	private BerkeleyMapFactory bmf;
//    
//    public void setBmf(BerkeleyMapFactory bmf) {
//        this.bmf = bmf;
//    }
    
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
    
    private Map<String, Feature> features;
    
    private void getFeatures(List<String> uniqueNames) {
		features = new HashMap<String, Feature>();
		List<Feature> allFeatures = sequenceDao.getFeaturesByUniqueNames(uniqueNames);    		
		for (Feature feature : allFeatures) {
			String uniqueName = feature.getUniqueName();
			features.put(uniqueName, feature);
		}
    }
    
    /**
     * Checks to see if all the options set for this formatter can be run only using Lucene.
     * 
     */
    protected boolean onlyNeedLuceneLookups() {
    	boolean available = true;
    	for (OutputOption outputOption : outputOptions) {
    		if (! GeneDetailFieldValueExctractor.availableFromLucene(outputOption)) {
    			available = false;
    		}
    	}
    	return available;
    }
    
    /**
     * Any set of entries that contains something other than mRNA requires features.
     * @param entries
     * @return
     */
    protected boolean requireFeatures(List<GeneDetail> entries) {
    	for (GeneDetail entry : entries) {
    		if (! entry.getType().equals("mRNA")) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Formats the results, with headers and footers. Pages through the entries (to speed up Feature lookups by chunking them).
     * 
     * @param entries
     * @throws IOException
     */
    public void format(final List<GeneDetail> entries) throws IOException {
    	formatHeader();
    	
    	final int max = entries.size() -1;
    	int start = 0;
    	int window = 1000;
    	int stop = start + window;
    	
    	final boolean allOptionsAvailableFromLucene = onlyNeedLuceneLookups();
    	
    	//int count = 0;
    	while (start <= max) {
    		
    		if (stop > max) {
    			// List.sublist, the end coordinate is NOT inclusive
    			stop = max + 1;
    		}
    		
    		if (start > max) {
    			break;
    		}
    		
    		logger.debug(String.format("%s :: %s", start, stop));
    		
    		final int final_start = start;
    		final int final_stop = stop;
    		
    		transactionTemplate.execute(new TransactionCallback<Object>() {
    	        @Override
    	        public Object doInTransaction(TransactionStatus status) {
    	        	
		    		final List<GeneDetail> currentEntries = entries.subList(final_start, final_stop);
		    		
		    		//count += currentEntries.size();
		    		
		    		if (! allOptionsAvailableFromLucene) {
		    			if (requireFeatures(currentEntries)) {
		        			
		        			List<String> uniqueNames = new ArrayList<String>();
		            		
		            		for (GeneDetail detail : currentEntries) {
		            			uniqueNames.add(detail.getSystematicId());
		            		}
		            		
		            		getFeatures(uniqueNames);
		            		
		        		} else {
		        			// make sure we reset the features map... (no need to carry a live instance of it when it's no longer needed)
		        			features = null;
		        		}
		    		}
		    		
		    		
		    		
    	        	try {
						formatBody(currentEntries);
					} catch (IOException e) {
						e.printStackTrace();
						logger.error(e.getMessage());
					}
    	        	
    	        	return true;
    	        }
    		});
    		
    		start += window;
    		stop = start + window;
    	}
    	
    	// logger.debug(String.format("%s==%s", count, entries.size()));
    	
    	formatFooter();
    	
    	logger.info("Export complete");
    }
    
	
	abstract public void formatHeader() throws IOException;
	
	abstract public void formatBody(List<GeneDetail> entries) throws IOException;
	
	abstract public void formatFooter() throws IOException;
	
	protected GeneDetailFieldValueExctractor getExtractor(GeneDetail entry) {
		return new GeneDetailFieldValueExctractor(entry, sequenceDao, features, fieldInternalSeparator, blankField);
	}
	
	protected List<String> getFieldValues(GeneDetailFieldValueExctractor extractor, List<OutputOption> outputOptions) {
		List<String> values = new ArrayList<String>();
		for (OutputOption outputOption : outputOptions) {
			values.add(extractor.getFieldValue(outputOption));
		}
		return values;
	}
	

}
