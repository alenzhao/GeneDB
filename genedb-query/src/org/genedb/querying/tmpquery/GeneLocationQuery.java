package org.genedb.querying.tmpquery;

import org.genedb.querying.core.HqlQuery;
import org.genedb.querying.core.QueryClass;
import org.genedb.querying.core.QueryParam;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@QueryClass(
        title="Transcripts by their type",
        shortDesc="Get a list of transcripts by type",
        longDesc=""
    )
public class GeneLocationQuery extends OrganismHqlQuery {

    //private static final Logger logger = Logger.getLogger(GeneLocationQuery.class);

    @QueryParam(
            order=1,
            title="Name of feature"
    )
    private String topLevelFeatureName;


    @QueryParam(
            order=2,
            title="Min coordinate"
    )
    private int min;


    @QueryParam(
            order=3,
            title="Max coordinate"
    )
    private int max;

    @Override
    protected String getHql() {
        return "select f.uniqueName from Feature f, FeatureLoc fl where fl.sourceFeature.uniqueName=:topLevelFeatureName and fl.fmin >= :min and fl.fmax <= :max @ORGANISM@ order by f.organism";
    }


    @Override
    protected String getOrganismHql() {
        // TODO Auto-generated method stub
        return null;
    }

//    public Map<String, Object> prepareModelData() {
//        Map<String, String> typeMap = new HashMap<String, String>();
//        typeMap.put("mRNA", "protein-coding");
//        typeMap.put("pseudogene", "pseudogenic transcript");
//        typeMap.put("tRNA", "tRNA");
//        typeMap.put("snoRNA", "snoRNA");
//
//        Map<String, Object> ret = new HashMap<String, Object>();
//        ret.put("typeMap", typeMap);
//        return ret;
//    }

    // ------ Autogenerated code below here

    public void setTopLevelFeatureName(String topLevelFeatureName) {
        this.topLevelFeatureName = topLevelFeatureName;
    }

    public String getTopLevelFeatureName() {
        return topLevelFeatureName;
    }


    public int getMin() {
        return min;
    }


    public int getMax() {
        return max;
    }


    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }


    @Override
    protected String[] getParamNames() {
        return new String[] {"topLevelFeatureName", "min", "max"};
    }

    @Override
    protected void populateQueryWithParams(org.hibernate.Query query) {
    	super.populateQueryWithParams(query);
        query.setString("topLevelFeatureName", topLevelFeatureName);
        query.setInteger("min", min);
        query.setInteger("max", max);
    }


            @Override
            @SuppressWarnings("unused")
            public void validate(Object target, Errors errors) {
                return;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean supports(Class clazz) {
                return GeneLocationQuery.class.isAssignableFrom(clazz);
            }



}
