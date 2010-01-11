package org.genedb.web.tags.db;

import org.apache.commons.lang.StringUtils;
import org.genedb.db.taxon.TaxonNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

public class SimpleSelectTag extends AbstractHomepageTag {

    @Override
    protected void display(TaxonNode root, JspWriter out) throws IOException {

        out.write("\n<select name=\"taxons\">");

        //Find the previously selected taxons if any in Request
        PageContext pageContext = (PageContext) getJspContext();
        String previouslySelectedTaxonsA = pageContext.getRequest().getParameter("taxons");
        String previouslySelectedTaxonsB = pageContext.getRequest().getParameter("taxonNodeName");
        String previouslySelectedTaxons = previouslySelectedTaxonsA;
        if ( !org.springframework.util.StringUtils.hasText(previouslySelectedTaxons)) {
            previouslySelectedTaxons = previouslySelectedTaxonsB;
        }

        // Pre-launch hack
        String context = (String) pageContext.getAttribute("organismContext");
        if ( !org.springframework.util.StringUtils.hasText(previouslySelectedTaxons)) {
            previouslySelectedTaxons = context;
        }

        String indentSpaces = "&nbsp;&nbsp;";
        displayImmediateChildren(root, out, 0, indentSpaces, previouslySelectedTaxons);

        out.write("\n</select>");
    }


    /**
     *
     * @param node
     * @param out
     * @param indent
     * @param previouslySelectedTaxons Should be pre-populated in a postback or redirect
     * @throws IOException
     */
    private void displayImmediateChildren(TaxonNode node, JspWriter out, int indent, String indentSpaces, String previouslySelectedTaxons) throws IOException {

        out.write("\n<option ");
        out.write(" class=\"");
        out.write("Level");
        out.write(String.valueOf(indent));
        out.write("DropDown");
        out.write("\"");
        out.write(" value=\"");
        out.write(node.getLabel());
        out.write("\"");

        if(!StringUtils.isEmpty(previouslySelectedTaxons) && previouslySelectedTaxons.equals(node.getLabel())){
            out.write(" selected ");
        }
        out.write(">");
        if (indent > 7) {
            indent=8;
        }
        out.write(indentSpaces);

        List<TaxonNode> children = node.getChildren();

        //Remove underscores
        String displayLabel = node.getLabel().replace("_", " ");

        //Replace <i>Root</i> label with <i>All Organisms</>
        if(displayLabel!= null && displayLabel.toLowerCase().equals("root")){
            displayLabel = "All Organisms";
        }
        // FIXME Use database for these values
        if (displayLabel != null && displayLabel.toLowerCase().equals("tbruceibrucei427")) {
            displayLabel = "Tbrucei brucei 427";
        }
        if (displayLabel != null && displayLabel.toLowerCase().equals("tbruceibrucei927")) {
            displayLabel = "Tbrucei brucei 927";
        }

        //Reformat leaf nodes to add a dot and space between first char and the rest of chars
        if(children!= null && children.size()==0){
            StringBuffer sb = new StringBuffer(displayLabel);
            sb.insert(1, ". ");
            out.write(sb.toString());
        }else{
            out.write(displayLabel);
        }
        out.write("</option>");
        Collections.sort(children, new Comparator<TaxonNode>() {
            @Override
            public int compare(TaxonNode arg0, TaxonNode arg1) {
                String label0 = arg0.getLabel();
                String label1 = arg1.getLabel();
                return label0.compareToIgnoreCase(label1);
            }
        });
        for (TaxonNode child : children) {
            if(child.isChildrenPopulated()){
                displayImmediateChildren(
                        child, out, indent+1, indentSpaces + "&nbsp;&nbsp;&nbsp;", previouslySelectedTaxons);
            }
        }
    }

}