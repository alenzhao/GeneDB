<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>

<format:header name="Lucene Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<sp:form name="lucene" commandName="luceneSearch" action="LuceneSearch" method="get">
	<table align="center" width="50%">
		<tr><td colspan="3">
      		<font color="red"><sp:errors path="*" /></font>
    	</td></tr>
		<tr>
			<td>Fields: 
				<misc:luceneFields/>
			</td>
			<td>Search String: <sp:input path="query" /></td>
			<td><input type="submit" value="Submit" /></td>
		</tr>
	</table>
</sp:form>
<format:footer />