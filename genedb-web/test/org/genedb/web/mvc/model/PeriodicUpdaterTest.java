package org.genedb.web.mvc.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.genedb.db.audit.MockChangeSetImpl;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * To see the final results of the method under test, simply set the cacheSynchroniser.setNoPrintResult(false); 
 * @author larry@sangerinstitute
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PeriodicUpdaterTest extends AbstractUpdaterTest{


    @Autowired
    PeriodicUpdater periodicUpdater;
    
    /**
     * Test the adding, replacement and removal of a ToplevelFeature
     * @throws Exception
     */
    //@Test
    public void testTopLevelFeatureChangeSet()throws Exception{
        Integer newTopLevelFeature = 1;//Pf3D7_01
        Integer changedTopLevelFeature = 886;//Pf3D7_02
        Integer deletedTopLevelFeature = 9493;//Pf3D7_03
        
        //Clear all the caches
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        //prevent excessive log printing
        cacheSynchroniser.setNoPrintResult(true);
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Add new Transcript feature to change set
        List<Integer> newFeatureIds = new ArrayList<Integer>();
        changeSet.getNewMap().put(TopLevelFeature.class, newFeatureIds); 
        newFeatureIds.add(newTopLevelFeature);
        
        //Change  transcript feature in change set
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(TopLevelFeature.class, changedFeatureIds); 
        changedFeatureIds.add(changedTopLevelFeature);
        
        //Delete  transcript feature from change set
        cacheSynchroniser.getBmf().getContextMapMap().put(deletedTopLevelFeature, "test test test");
        List<Integer> deletedFeatureIds = new ArrayList<Integer>();
        changeSet.getDeletedMap().put(TopLevelFeature.class, deletedFeatureIds); 
        deletedFeatureIds.add(deletedTopLevelFeature);
        
        

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //ContextMap with featureID 1 is not null
        String contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(newTopLevelFeature);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_01"));
        
        //ContextMap with featureID 886 is not null
        contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(changedTopLevelFeature);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_02"));

        
        //Assert Transcript DTO with featureID 19 IS null
        contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(deletedTopLevelFeature);
        Assert.assertNull(contextMap);
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
    }
    
    /**
     * A changed Gap triggers a TopLevelFeature to be replaced
     * @throws Exception
     */
    //@Test
    public void testGapChangeSet()throws Exception{
        //Gap feature ID 17620's Unique name is 'gap116670-116769:corrected'
        //The corresponding TopLevelFeature for this is 15901(Pf3D7_07)
        Integer changedGap = 17620;
        
        //Clear all the caches
        CacheSynchroniser cacheSynchroniser = (CacheSynchroniser)periodicUpdater.getIndexUpdaters().get(1);
        cacheSynchroniser.getBmf().getDtoMap().clear();
        cacheSynchroniser.getBmf().getContextMapMap().clear();
        
        //prevent excessive log printing
        cacheSynchroniser.setNoPrintResult(true);
        
        //Get the changeset
        MockChangeSetImpl changeSet = 
            (MockChangeSetImpl)periodicUpdater.getChangeTracker().changes(PeriodicUpdaterTest.class.getName());
        
        //Change  Gap feature in change set
        List<Integer> changedFeatureIds = new ArrayList<Integer>();
        changeSet.getChangedMap().put(Gap.class, changedFeatureIds); 
        changedFeatureIds.add(changedGap);
        
        

        
        
        /****************************
         * Execute class under test 
         ****************************/
        boolean noErrors = periodicUpdater.processChangeSet();
        
        //ContextMap with featureID 15901 is not null
        String contextMap = cacheSynchroniser.getBmf().getContextMapMap().get(15901);
        Assert.assertNotNull(contextMap);
        Assert.assertTrue(contextMap.contains("Pf3D7_07"));
        
        //Assert No severe errors found
        Assert.assertTrue(noErrors);
        
    }
}
