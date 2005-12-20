<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>	
    <head>
      <title>Create Entry</title>
   </head>

   <body>
   		<h1>Create Entry</h1>
   		<form action="save" method="POST">
	      <h2>
	      	<input type="text" name="title"  />      		
	      </h2>
		  <p>
	  		<textarea name="body" ></textarea>
		  </p>
		  <p>
				<input type="submit" value="Save" />
		  </p>
	  	</form>
   </body> 
</html>