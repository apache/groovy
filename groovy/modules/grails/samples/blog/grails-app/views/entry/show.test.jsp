<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>	
    <head>
      <title>View Entry</title>
   </head>

   <body>
      <h2><c:out value="${entry.title}" /> - <c:out value="${entry.date}" /></h2>
	  <p>
	  		<c:out value="${entry.body}" />
	  </p>
	  <p>
	  		<a href="edit?id=<c:out value='${entry.id}' />">Edit Post</a>
	  </p>
	  <p><b><a href="#comments">Comments:</a></b></p>
	  <c:forEach var="c" items="${entry.comments}">
	  		<p><b>Left by:</b> <c:out value="${c.authorName}" /></p>
			<p>
				<c:out value="${c.body}" />
			</p>
	  </c:forEach>	  
	  <p>
	  		<b>Add Comment:</b>
	  </p>
	  <form action="createComment" method="POST">
	  		<input type="hidden" name="entryId" value="<c:out value='${entry.id}' />" />
	  		
	  		<p> Name: <input name="authorName" type="text" /></p>
	  		<p> Email: <input name="authorEmail" type="text" /></p>
	  		<p> Homepage: <input name="authorBlogURL" type="text" /></p>	  		
	  		<p><textarea name="body" ></textarea></p>
	  		<p><input type="submit" value="Add" /></p>
	  </form>
   </body> 
</html>