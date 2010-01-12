package org.genedb.querying.tmpquery;

import org.genedb.querying.core.QueryClass;
import org.genedb.querying.core.QueryParam;

import java.util.Date;

@QueryClass(
        title="Get features by date",
        shortDesc="Get a list of features that have changed since a certain date",
        longDesc=""
    )
public class DateQuery extends OrganismHqlQuery {

    @QueryParam(
            order=1,
            title="Date of modification"
    )
    protected Date date;


    @QueryParam(
            order=2,
            title="After this date"
    )
    protected boolean after;

    @QueryParam(
            order=3,
            title="Just look at creation date"
    )
    protected boolean created;

    @Override
    public String getQueryDescription() {
    	return "Searches for features who have been changed before or after a certain date.";
    }

    @Override
    public String getQueryName() {
        return "Date";
    }

    @Override
    protected String getHql() {
        System.err.println("date in hql is '"+date+"'");
        String operator = after ? ">" : "<";
        String typeOfDate = created ? "timeAccessioned" : "timeLastModified";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("select f.uniqueName from Feature f where f.%s %s :date @ORGANISM@ ", typeOfDate, operator));
        sb.append(RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES);
        sb.append(" order by f.organism");
        return sb.toString();
    }

    // ------ Autogenerated code below here

    public Date getDate() {
        return date;
    }


    public void setDate(Date date) {
        System.err.println("date in setter is '"+date+"'");
        this.date = date;
    }


    public boolean isCreated() {
        return created;
    }


    public void setCreated(boolean created) {
        this.created = created;
    }


    public boolean isAfter() {
        return after;
    }

    public void setAfter(boolean after) {
        this.after = after;
    }

    @Override
    protected String[] getParamNames() {
        return new String[] {"date", "created", "after"};
    }

    @Override
    protected void populateQueryWithParams(org.hibernate.Query query) {
    	// GV1 added super call
    	super.populateQueryWithParams(query);
        query.setDate("date", date);
    }

}