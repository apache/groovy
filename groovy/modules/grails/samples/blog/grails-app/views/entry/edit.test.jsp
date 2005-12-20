<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>	
    <head>
      <title>Edit Entry</title>
   </head>

   <body>
   		<h1>Edit Entry</h1>
   		<form action="update" method="POST">
   			<input type="hidden" name="id" value="<c:out value='${entry.id}' />" />
	      <h2>
	      	<input type="text" name="title" value="<c:out value='${entry.title}' />" />      		
	      </h2>
		  <p>
	  		<textarea name="body" ><c:out value='${entry.body}' /></textarea>
		  </p>
		  <p>
				<input type="submit" value="Update" />
		  </p>
	  	</form>
   </body> 
</html>