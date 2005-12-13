<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri='http://www.taobits.net/gvtags' prefix='gv' %>
<html>	
	<gv:bean var="entryList" scope="request" />	
    <head>
      <title>Weblog</title>
   </head>

   <body>
   	  <center><h1>Grails Weblog</h1></center>
   	  
      <h2>Entries:</h2>
      <p><a href="create">Add Post</a>
	  <gv:for var="e" in="entryList">
	      <h3><gv:out expr="e.title" /> - <gv:out expr="e.date" /></h3>
		  <p>
		  		<gv:out expr="e.body" />
		  </p>
		  <p>
				<gv:generic tag='a' hrefExpr='"edit?id=${e.id}"'>Edit Post</gv:generic>
		  </p>
		  <p><b><gv:generic tag='a' hrefExpr='"show?id=${e.id}#comments"'>Comments</gv:generic> ( <gv:out expr="e.comments.size()" />  ):</b></p>
	  </gv:for>
   </body> 
</html>