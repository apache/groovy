println """

<!-- Groovy Groovlet Examples. -->

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <meta name="Author" content="Groovy Developers">
   <title>Groovy - Groovlet Examples</title>
   <link rel="stylesheet" href="codehaus-style.css" type="text/css">
</head>

<body bgcolor="#FFFFFF">

<a href="http://groovy.codehaus.org"><img src="images/groovy.png" border="0"></a>

<h1>Groovlet examples showing GroovyServlet in action</h1>
<p>
These examples will only work when viewed via an http URL. They will not work
if you are viewing these pages via a <tt>file://...</tt> URL. You need to
deploy the <tt>war</tt> archive into a servlet container like Tomcat, Jetty or
any other will do as well.
</p>

<p>
To navigate your way through the examples, the following icons will help:

<table border=0>
 <tr>
  <td width="30"><img src="images/execute.gif" ></td>
  <td>Executes the example</td>
</tr>
<tr>
  <td width="30"><img src="images/code.gif"></td>
  <td>Look at the source code for the example</td>
</tr>
<tr>
  <td width="30"><img src="images/return.gif"></td>
  <td>Return to this screen</td>
</tr>
</table>
</p>

<p>Tip: To see the cookie interactions with your browser, try turning on
the "notify when setting a cookie" option in your browser preferences.
This will let you see when a session is created and give some feedback
when looking at the cookie demo.
</p>

<h2>Table of content</h2>

"""

println """

<table BORDER=0 CELLSPACING=5 WIDTH="85%" >

<!-- Begin Groovlet -->
<tr VALIGN=TOP>
<td>Hello World</td>

<td VALIGN=TOP WIDTH="30%"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP><a href="hello/hello.groovy">Execute</a></td>

<td WIDTH="30%"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP>Source</td>
</tr>
<!-- End Groovlet -->


<!-- Begin Groovlet -->
<tr VALIGN=TOP>
<td>Zoo</td>

<td VALIGN=TOP WIDTH="30%"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP><a href="zoo/zoo.groovy">Execute</a></td>

<td WIDTH="30%"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP>Source</td>
</tr>
<!-- End Groovlet -->
</table>

<h2>Note</h2>

<p>The source code for these examples does not contain all of the
source code that is actually in the example, only the important sections
of code. Code not important to understand the example has been removed
for clarity.
</body>
</html>
"""