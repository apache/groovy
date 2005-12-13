<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri='http://www.taobits.net/gvtags' prefix='gv' %>
<html>	
	<gv:bean var="entry" scope="request" />	
    <head>
      <title>View Entry</title>
   </head>

   <body>
      <h2><gv:out expr="entry.title" /> - <gv:out expr="entry.date" /></h2>
	  <p>
	  		<gv:out expr="entry.body" />
	  </p>
	  <p>
			<gv:generic tag='a' hrefExpr='"/blog/app/entry/edit?id=${entry.id}"'>Edit Post</gv:generic>
	  </p>
	  <p><b><a href="#comments">Comments:</a></b></p>
	  <gv:for var="c" in="entry.comments">
	  		<p><b>Left by:</b> <gv:out expr="c.authorName" /></p>
			<p>
				<gv:out expr="c.body" />
			</p>
	  </gv:for>	  
	  <p>
	  		<b>Add Comment:</b>
	  </p>
	  <form action="createComment" method="POST">
	  		<input type="hidden" name="entryId" value="<gv:out expr='entry.id' />" />
	  		
	  		<p> Name: <input name="authorName" type="text" /></p>
	  		<p> Email: <input name="authorEmail" type="text" /></p>
	  		<p> Homepage: <input name="authorBlogURL" type="text" /></p>	  		
	  		<p><textarea name="body" ></textarea></p>
	  		<p><input type="submit" value="Add" /></p>
	  </form>
   </body> 
</html>