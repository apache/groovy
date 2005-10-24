
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>


<html>
   <head>
      <title>Welcome to  <c:out value="${blog.name}"/></title>
   </head>

   <body>
      <h1>Welcome to <a href="<c:out value='${blog.owner.email}'/>"><c:out value="${blog.name}"/></a></h1>
	  <c:forEach items="${blog.entries}" var="e">
	  		<p>
				<b><c:out value="${e.title}" /></b> - <c:out value="${e.date}" />
			</p>
			<p>
				<c:out value="${e.body}" />
			</p>	
			<p><b>Comments:</b></p>
			 <c:forEach items="${e.comments}" var="c">
			 	<div style="border:1px solid black;padding: 5px;margin-bottom: 2px;">
					<p><c:out value="${c.body}" /></p>
					<p>Left by <a href="<c:out value='${c.authorEmail}'/>"><c:out value="${c.authorName}" /></a>
						(<a href="<c:out value='${c.authorBlogURL}'/>"><c:out value='${c.authorBlogURL}'/></a>)
					</p>
				</div>
			 </c:forEach>
	  </c:forEach>
	  <p></p>
   </body>
</html>