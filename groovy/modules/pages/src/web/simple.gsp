<html>
<head>
<title>Simple GroovyPages Example</title>
</head>
<body>
<h2 align="center">Simple Example</h2>
<table border="0" cellpadding="2" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber4">
  <tr>
    <td>
	<i>Links:</i></td>
    <td>
	<i>
	<a href="default.htm">Examples Home</a></i></td>
    <td>
	<i> <a href="simple.gsp">Simple Example</a></i></td>
    <td>
	<i>
    <a href="visible.gsp?arg=This+is+a+test+argument&lastCount=${session.count}">
    Visible Syntax</a></i></td>
    <td>
	<i>
    <a href="jsp.gsp?arg=This+is+a+test+argument&lastCount=<%=session.count%>">
    JSP Syntax</a></i></td>
  </tr>
</table>
<p>&nbsp;&nbsp;&nbsp; This is a simple example of GroovyPages.&nbsp; This page 
does the same thing as the <a href="simple.groovy">Groovlet Example</a>.&nbsp; 
From the source code below, you can see that GroovyPages are very similar to JSP, 
except much easier to use because Groovy is underneath.</p>
<%@ page import="java.util.Date" %>

<% if (session.count == null) session.count = 1 %>
<p>Hello, ${request.remoteHost}: ${session.count}! ${new Date()}</p>
<% session.count++ %>
<h3>The Source Code</h3>
<pre>&lt;html>
&lt;head>
&lt;title>Simple Example&lt;/title>
&lt;/head>
&lt;body>
<span style="background-color: #99FF99">&lt;%@ page import="java.util.Date" %&gt;</span>

<span style="background-color: #99FF99">&lt;% if session.count == null
session.count = 1 %&gt;</span>
&lt;p&gt;Hello, <span style="background-color: #99FF99">$&#123;request.remoteHost}</span>: <span style="background-color: #99FF99">$&#123;session.count}</span>! <span style="background-color: #99FF99">$&#123;new Date()}</span>&lt;/p&gt;
<span style="background-color: #99FF99">&lt;% session.count++ %&gt;</span>
&lt;/body>
&lt;/html></pre>
</body>
</html>