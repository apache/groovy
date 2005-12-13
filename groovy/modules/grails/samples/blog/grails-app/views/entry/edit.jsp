<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri='http://www.taobits.net/gvtags' prefix='gv' %>
<html>	
	<gv:bean var="entry" scope="request" />	
	<gv:bean var="errors" scope="request" />		
    <head>
      <title>Edit Entry</title>
   </head>

   <body>
   		<h1>Edit Entry</h1>
   		<form action="update" method="POST">
   			<input type="hidden" name="id" value="<gv:out expr='entry.id' />" />
	      <h2>
	      	<input type="text" name="title" value="<gv:out expr='entry.title' />" />      		
	      </h2>
		  <p>
	  		<textarea name="body" ><gv:out expr='entry.body' /></textarea>
		  </p>
		  <p>
				<input type="submit" value="Update" />
		  </p>
	  	</form>
   </body> 
</html>