<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="featureCvTerms" type="java.util.Collection" required="true" %>
<%@ attribute name="organism" type="java.lang.String" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="db" uri="db" %>

<c:if test="${fn:length(featureCvTerms) > 0}">
  <c:forEach items="${featureCvTerms}" var="fctDTO" varStatus="status">
  <tr>
  <th><c:if test="${status.first}">${title}</c:if></th>
  <td><a href="http://www.genedb.org/amigo-cgi/term-details.cgi?term=GO%3A${fctDTO.typeAccession}">${fctDTO.typeName}</a></td>
  <td">
    <c:forEach items="${fctDTO.props.qualifier}" var="qualifier" varStatus="st">
      <c:if test="${!st.first}"> | </c:if>
      ${qualifier}
    </c:forEach>
  </td>
  <td class="grey-text">
    <c:forEach items="${fctDTO.props.evidence}" var="evidence">
    ${evidence}
    </c:forEach>&nbsp;
    <c:forEach items="${fctDTO.pubs}" var="pub">
    (${pub})
    </c:forEach>
  </td>
  <td>
    <c:forEach items="${fctDTO.dbXRefDtoList}" var="fctdbx">
      <a href="${fctdbx.urlPrefix}${fctdbx.accession}">${fctdbx.dbName}:${fctdbx.accession}</a>
    </c:forEach>
    <c:if test="${fctDTO.withFrom != 'null'}">
      <db:dbXRefLink dbXRef="${fctDTO.withFrom}"/>
    </c:if>
  </td>
  </tr>
  </c:forEach>
</c:if>
