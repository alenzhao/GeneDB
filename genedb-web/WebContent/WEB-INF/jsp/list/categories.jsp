<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Category List" />
<format:page>
<br>

<c:url value="/Search" var="url">
	<c:param name="organism" value="${organism}" />
	<c:param name="category" value="${category}" />
</c:url>
  <div id="col-2-1">
	<display:table name="results" id="row" pagesize="30"
		requestURI="/category/${category}" class="simple" cellspacing="0"
		cellpadding="4">
		<display:column title="Category - ${category}">
			<c:url value="${url}" var="final">
				<c:param name="term" value="${row.name}" />
			</c:url>
			<a href="<c:url value="/Query/controlledCuration">"><c:param name="cvTermName" value="${row.name}" /><c:param name="taxons" value="${taxons}" /><c:param name="cv" value="${category}"/></c:url>">
				<c:out value="${row.name}" /></a>
		</display:column>
		<display:column property="count" title="Count" />
	</display:table>
  </div>

</format:page>
