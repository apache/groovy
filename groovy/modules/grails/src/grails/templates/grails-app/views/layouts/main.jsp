<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<html>
	<head>
		<title><decorator:title default="Default Title" /></title>
		<decorator:head />		
	</head>
	<body onload="<decorator:getProperty property='body.onload'/>">
			 <decorator:body />				 	
	</body>	
</html>