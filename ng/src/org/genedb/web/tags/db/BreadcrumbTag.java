package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static javax.servlet.jsp.PageContext.REQUEST_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;
import static org.genedb.web.mvc.controller.WebConstants.TAXON_NODE;

import org.apache.log4j.Logger;
import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.web.mvc.controller.WebConstants;

import org.springframework.web.util.WebUtils;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BreadcrumbTag extends SimpleTagSupport {

	private String selection;
	private boolean showingHomepage = false;
	public void setShowingHomepage(boolean showingHomepage) {
		this.showingHomepage = showingHomepage;
	}

	private Map<String, String> cache = Maps.newHashMap();

    public void setSelection(String selection) {
		this.selection = selection;
	}

	private static final Logger logger = Logger.getLogger(BreadcrumbTag.class);
	String separator = "<div class=\"breadcumb breadcrumb-separator ui-state-disabled ui-corner-all\"  > <span class=\"ui-icon ui-icon-carat-1-e\"></span> </div>";

    @Override
    public void doTag() throws JspException, IOException {

        JspWriter out = getJspContext().getOut();

    	String path = checkCache(selection);
    	if (path != null) {
            out.write(path);
            return;
    	}
    	
        TaxonNodeManager tnm = (TaxonNodeManager) getJspContext().getAttribute(TAXON_NODE_MANAGER,
            APPLICATION_SCOPE);

        if (tnm == null) {
            logger.error("Failed to find TaxonNodeManager in JSP context");
            return;
        }
        
        logger.debug(selection);
        
        TaxonNode taxonNode = tnm.getTaxonNodeForLabel(selection);

        	//(TaxonNode) getJspContext().getAttribute(TAXON_NODE, REQUEST_SCOPE);
        if (taxonNode == null) {
            logger.error("Failed to find '" + selection + "' TaxonNode in JSP context");
            return;
        }

        String prefix = getContextPathFromJspContext(getJspContext());
        String trail = checkCache(selection);
        if (trail == null) {
            StringBuilder buf = new StringBuilder();
            List<TaxonNode> nodes = tnm.getHierarchy(taxonNode);
            boolean first = true;
            
            buf.append("<div class=\"breadcrumb-container\" >");
            
            for (TaxonNode node : nodes) {
                if (!first) {
                    buf.append(separator);
                }
                if (node.isWebLinkable()) {
                	buf.append("<div class=\"breadcrumb breadcrumb-link ui-state-default ui-corner-all\"   >");
                    buf.append("<a href=\"");
                    buf.append(prefix);
                    buf.append("Homepage/");
                    buf.append(node.getLabel());
                    buf.append("\">");
                    
                } else {
                	buf.append("<div class=\"breadcrumb ui-state-disabled ui-corner-all\" style=\"float: left; \"  >");
                }
                // TODO Don't hyperlink last org if page is homepage
                
                String displayLabel = node.getLabel();
                if (displayLabel.equals("Root")) {
                	displayLabel = "All Organisms";
                } else if (node.getName(TaxonNameType.HTML_FULL) != null) {
                	displayLabel = node.getName(TaxonNameType.HTML_FULL);
                }
                
                
                buf.append(displayLabel);
                
                if (node.isWebLinkable()) {
                    buf.append("</a>");
                }
                
                buf.append("</div>");
                first = false;
            }
            
            buf.append("</div><div style='clear:both;'></div>");
            
            trail = buf.toString();
            setCache(selection, trail);
        }

        //trail += separator + getJspContext().getAttribute(WebConstants.CRUMB, REQUEST_SCOPE);
        out.write(trail);
    }

    private String getContextPathFromJspContext(JspContext context) {
        String prefix = (String) context.getAttribute(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE,
            REQUEST_SCOPE);
        if (!prefix.equals("/")) {
            prefix += "/";
        }
        return prefix;
    }

    private String checkCache(String name) {
        if (cache.containsKey(name)) {
        	return cache.get(name);
        }
        return null;
    }

    private void setCache(String name, String path) {
        cache.put(name, path);
    }

}
