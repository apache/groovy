<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri='http://www.taobits.net/gvtags' prefix='gv' %>
<html>	
	<gv:bean var="errors" scope="request" />		
    <head>
      <title>Create Entry</title>
   </head>

   <body>
   		<h1>Create Entry</h1>
   		<form action="save" method="POST">
   			<input type="hidden" name="date" value="<gv:out expr='new Date()' />" />
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