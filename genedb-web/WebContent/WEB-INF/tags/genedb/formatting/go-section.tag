<%@ tag display-name="go-section"
        body-content="empty" %>
<%@ attribute name="cvName" required="true" %>
<%@ attribute name="feature" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>


<db:filtered-loop items="${polypeptide.featureCvTerms}" cv="${cvName}" var="featCvTerm" varStatus="status">
  <tr>
<!--    <td>GO:${featCvTerm.cvTerm.dbXRef.accession}</td>-->
    <td width="40%" align="left">
    	${featCvTerm.cvTerm.name}
    </td>
    <td width="8%" align="left">
    	<db:propByName collection="${featCvTerm.featureCvTermProps}" name="qualifier" var="qualifiers">
    	<c:forEach items="${qualifiers}" var="qualifier" varStatus="st">
    		<c:if test="${st.count > 1}"> | </c:if>
    		${qualifier.value}
    	</c:forEach>
    	</db:propByName>
    </td>
    <td width="32%" align="left">
    <db:propByName collection="${featCvTerm.featureCvTermProps}" name="evidence" var="evidence">
    	<c:forEach items="${evidence}" var="ev">
    		${ev.value}
    	</c:forEach>
    </db:propByName>&nbsp;
   	<c:forEach items="${featCvTerm.featureCvTermPubs}" var="fctp">
   		(${fctp.pub.uniqueName})
   	</c:forEach>
   	</td>
    <td width="10%" align="left">
    	<c:forEach items="${featCvTerm.featureCvTermDbXRefs}" var="fctdbx">
    		${fctdbx.dbXRef.db.name}${fctdbx.dbXRef.accession}
    	</c:forEach>
    </td>
  	<td width="10%" align="left">n others</td>
  </tr>
</db:filtered-loop>