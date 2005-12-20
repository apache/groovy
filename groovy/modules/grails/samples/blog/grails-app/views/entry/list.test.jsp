<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>	
    <head>
      <title>Weblog</title>
   </head>

   <body>
   	  <center><h1>Grails Weblog</h1></center>
   	  
      <h2>Entries:</h2>
      <p><a href="create">Add Post</a>
	  <c:forEach var="e" items="${entryList}">
	      <h3><c:out value="${e.title}" /> - <c:out value="${e.date}" /></h3>
		  <p>
		  		<c:out value="${e.body}" />
		  </p>
		  <p>
		  		<a href="edit?id=<c:out value='${e.id}' />">Edit Post</href>
		  </p>
		  <p><b>
		  <a href="show?id=<c:out value='${e.id}' />#comments">Comments</a>:</b></p>
	  </c:forEach>
   </body> 
</html>