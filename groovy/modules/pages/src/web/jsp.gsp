<html>
<head>
<title>GroovyPages Test</title>
</head>
<body>
<h2 align="center">JSP Syntax</h2>
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
<p>&nbsp;&nbsp;&nbsp; This is an example of GroovyPages showing the use of the 
JSP syntax.&nbsp; This syntax is exactly like JSP except that the code portions 
are Groovy, rather than Java.&nbsp; GroovyPages also support an alternate 
editor-visible syntax which is sometimes preferable when the programmer is also the 
page designer or when they want to see and edit their in the web editor's 
formatted, normal view, rather than in raw, noisy HTML all of the time.</p>
<% // Make sure count is set
if (session.count == null) session.count = 1 
%>
<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber1">
  <tr>
    <td valign="top" width="50%">
    <table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber2">
      <tr>
        <td colspan="2" align="center"><b>Page Arguments</b></td>
      </tr>
      <tr>
        <td>Argument</td>
        <td>Value</td>
      </tr>
		<% // For each parameter
		for (it in request) { %>
      <tr>
        <td><%= it.key %>&nbsp;</td>
        <td><%= it.value %>&nbsp;</td>
      </tr>
		<% } %>
    </table>
    </td>
    <td valign="top" width="50%">
    <table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber3">
      <tr>
        <td colspan="2" align="center"><b>Session Variables</b></td>
      </tr>
      <tr>
        <td>Variable</td>
        <td>Value</td>
      </tr>
		<% // For each session variable
		session.each() { %>
      <tr>
        <td><%= it.key %>&nbsp;</td>
        <td><%= it.value %>&nbsp;</td>
      </tr>
		<% } %>
    </table>
    </td>
  </tr>
</table>
<% // Increment the count on each refresh
session.count++ 
%>
<h3>The Source Code</h3>
<pre><span style="background-color: #99FF99">&lt;% // Make sure count is set<br>if (session.count == null) session.count = 1<br>%&gt;</span><br>&lt;table border=&quot;0&quot; cellpadding=&quot;0&quot; cellspacing=&quot;0&quot; style=&quot;border-collapse: 
collapse&quot; bordercolor=&quot;#111111&quot; width=&quot;100%&quot; id=&quot;AutoNumber1&quot;&gt;<br>  &lt;tr&gt;<br>    &lt;td valign=&quot;top&quot; width=&quot;50%&quot;&gt;<br>      &lt;table border=&quot;1&quot; cellpadding=&quot;0&quot; cellspacing=&quot;0&quot; style=&quot;border-collapse: collapse&quot;
        bordercolor=&quot;#111111&quot; width=&quot;100%&quot; id=&quot;AutoNumber2&quot;&gt;<br>        &lt;tr&gt;<br>          &lt;td colspan=&quot;2&quot; align=&quot;center&quot;&gt;&lt;b&gt;Page Arguments&lt;/b&gt;&lt;/td&gt;<br>        &lt;/tr&gt;<br>        &lt;tr&gt;<br>          &lt;td&gt;Argument&lt;/td&gt;<br>          &lt;td&gt;Value&lt;/td&gt;<br>        &lt;/tr&gt;<br>        <span style="background-color: #99FF99">&lt;% // For each parameter<br>        for (it in request) { %&gt;</span><br>        &lt;tr&gt;<br>          &lt;td&gt;<span style="background-color: #99FF99">&lt;%= it.key %&gt;</span>&amp;nbsp;&lt;/td&gt;<br>          &lt;td&gt;<span style="background-color: #99FF99">&lt;%= it.value %&gt;</span>&amp;nbsp;&lt;/td&gt;<br>        &lt;/tr&gt;<br>        <span style="background-color: #99FF99">&lt;% } %&gt;</span><br>      &lt;/table&gt;<br>    &lt;/td&gt;<br>    &lt;td valign=&quot;top&quot; width=&quot;50%&quot;&gt;<br>      &lt;table border=&quot;1&quot; cellpadding=&quot;0&quot; cellspacing=&quot;0&quot; style=&quot;border-collapse: collapse&quot;
        bordercolor=&quot;#111111&quot; width=&quot;100%&quot; id=&quot;AutoNumber3&quot;&gt;<br>        &lt;tr&gt;<br>          &lt;td colspan=&quot;2&quot; align="center"&gt;&lt;b&gt;Session Variables&lt;/b&gt;&lt;/td&gt;<br>        &lt;/tr&gt;<br>        &lt;tr&gt;<br>          &lt;td&gt;Variable&lt;/td&gt;<br>          &lt;td&gt;Value&lt;/td&gt;<br>        &lt;/tr&gt;<br>        <span style="background-color: #99FF99">&lt;% // For each session variable<br>        session.each() { %&gt;</span><br>        &lt;tr&gt;<br>          &lt;td&gt;<span style="background-color: #99FF99">&lt;%= it.key %&gt;</span>&amp;nbsp;&lt;/td&gt;<br>          &lt;td&gt;<span style="background-color: #99FF99">&lt;%= it.value %&gt;</span>&amp;nbsp;&lt;/td&gt;<br>        &lt;/tr&gt;<br>        <span style="background-color: #99FF99">&lt;% } %&gt;</span><br>      &lt;/table&gt;<br>    &lt;/td&gt;<br>  &lt;/tr&gt;<br>&lt;/table&gt;
<span style="background-color: #99FF99">&lt;% // Increment the count on each refresh<br>session.count++<br>%&gt;</span></pre>
</body>
</html>