import java.util.Date
if (session.count == null) {
	session.count = 1
}
out.println(<<<EOS
<html>
<head>
<title>Groovlet Example</title>
</head>
<body>
<h2 align="center">Groovelet Example</h2>
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
    <a href="jsp.gsp?arg=This+is+a+test+argument&lastCount=${session.count}">
    JSP Syntax</a></i></td>
  </tr>
</table>
<p>&nbsp;&nbsp;&nbsp; This is a simple example of a Groovlet using GroovyServlet.&nbsp; This page
does the same thing as the GroovyPages <a href="simple.gsp">Simple Example</a>.&nbsp;
From the source code below, you can see that it doesn't take much to write a simple servlet with
Groovy.  But for more complex servlets, GroovyPages can be a great benefit because it is embedded
in your HTML, like JSP.</p>
<p>Hello, ${request.remoteHost}: ${session.count}! ${new Date()}</p>
<h3>The Source Code</h3>
<pre><span style="background-color: #99FF99">import java.util.Date
if (session.count == null) {
	session.count = 1
}
out.println(<<<&#69;OS</span>
&lt;html>
&lt;head>
&lt;title>Groovlet Example&lt;/title>
&lt;/head>
&lt;body>
&lt;p>Hello, <span style="background-color: #99FF99">$&#123;request.remoteHost}</span>: <span style="background-color: #99FF99">$&#123;session.count}</span>! <span style="background-color: #99FF99">$&#123;new Date()}</span>&lt;/p>
&lt;/body>
&lt;/html>
<span style="background-color: #99FF99">&#69;OS)
session.count = session.count + 1</span>
</pre>
</body>
</html>
EOS)
session.count = session.count + 1
