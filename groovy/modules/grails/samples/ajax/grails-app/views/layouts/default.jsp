<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd">
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<html>
	<head>
		<title><decorator:title default="Google Example" /></title>
		<meta HTTP-EQUIV="Pragma" CONTENT="no-cache" /> 
		<meta HTTP-EQUIV="Expires" CONTENT="-1" />		
		<meta http-equiv="content-type" content="text/html; charset=UTF-8"></meta>
		<title>Auto-Complete Google Example</title>
		<style><!--
			body,td,a,p,.h{font-family:arial,sans-serif;}
			.h{font-size: 20px;}
			.q{color:#0000cc;}
.autoComplete {
	font-weight:bold;
	font-size: 0.95em;	
	background-color: white;
	border: 1px solid black;
	padding: 1px;	
}
.autoCompleteResultOver {
	background-color:#00287A;
	color:white;
}
			//-->
		</style>
		<script type="text/javascript" src="js/prototype.js"></script>
		<script type="text/javascript" src="js/rico.js"></script>
		<script type="text/javascript" src="js/grails.js"></script>
		<decorator:head />				
	</head>
	<body bgcolor=#ffffff  text=#000000 link=#0000cc vlink=#551a8b alink=#ff0000 topmargin=3 marginheight=3 onload="<decorator:getProperty property='body.onload'/>">
		<center><img src="http://www.google.com/intl/en_uk/images/logo.gif" width=276 height=110 alt="Google"><br><br>
		
		 <div class="body">
			 <decorator:body />				 	
		 </div>	
	</body>
	
</html>