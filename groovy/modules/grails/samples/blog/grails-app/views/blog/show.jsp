<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri='http://www.taobits.net/gvtags' prefix='gvt' %>
<html>	
	<gvt:bean var="blog" scope="request" />	
    <head>
      <title>Welcome to  <gvt:out expr="blog.name"/></title>
   </head>

   <body>
      <h1>Welcome to <a href="<gvt:out expr='blog.owner.email'/>"><gvt:out expr="blog.name"/></a></h1>
	  <gvt:for var="e" in="blog.entries">
	  		<p>
				<b><gvt:out expr="e.title" /></b> - <gvt:out expr="e.date" />
			</p>
			<p>
				<gvt:out expr="e.body" />
			</p>	
			<p><b>Comments:</b></p>
			 <gvt:for in="e.comments" var="c">
			 	<div style="border:1px solid black;padding: 5px;margin-bottom: 2px;">
					<p><gvt:out expr="c.body" /></p>
					<p>Left by <a href="<gvt:out expr='c.authorEmail'/>"><gvt:out expr="c.authorName" /></a>
						(<a href="<gvt:out expr='c.authorBlogURL'/>"><gvt:out expr='c.authorBlogURL'/></a>)
					</p>
				</div>
			 </gvt:for>
	  </gvt:for>
	  <p></p>
   </body> 
</html>