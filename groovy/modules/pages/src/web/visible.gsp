<html>
<head>
<title>GroovyPages Test</title>
</head>
<body>
<h2 align="center">Visible Syntax</h2>
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
<p>&nbsp;&nbsp;&nbsp; This is an example of GroovyPages showing the use of the editor 
visible syntax.&nbsp; This syntax uses curly braces like %&#123; groovlet }% and $&#123; 
expression }, instead of the less-than greater-than of JSP, like &lt;% groovlet %&gt; 
and &lt;%= expression %&gt;.&nbsp; Though both syntaxes are supported in GroovyPages, 
using visible syntax is sometimes preferable when the programmer is also the 
page designer or when they want to see and edit their in the web editor's 
formatted, normal view, rather than in raw, noisy HTML all of the time.&nbsp; 
It's perfectly acceptable to mix both syntaxes on the same page.</p>
<p><i><b>Make sure count is set and increment it on each refresh</b></i><br>
^p%{<br>
if (session.count == null) session.count = 1<br>
}%</p>
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
      <tr>
        <td colspan="2"><i><b>For each parameter</b></i><br>
^tr%{ for (it in request) { }%</td>
      </tr>
      <tr>
        <td>${ it.key }</td>
        <td>${ it.value }</td>
      </tr>
      <tr>
        <td colspan="2">^tr%{ } }% <i><b>Comments are safe before and after 
        stripping tags</b></i></td>
      </tr>
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
      <tr>
        <td colspan="2"><i><b>For each session variable</b></i><br>
^tr%{ session.each() { }%</td>
      </tr>
      <tr>
        <td>${ it.key }</td>
        <td>${ it.value }</td>
      </tr>
      <tr>
        <td colspan="2">^tr%{ } }% <i><b>because they will get 
        stripped off</b></i></td>
      </tr>
    </table>
    </td>
  </tr>
</table>
<p><i><b>Increment the count on each refresh</b></i><br>
^p%{
session.count++
}%</p>
<h3>The Source Code</h3>
<p>&nbsp;&nbsp; You will note that the source code below appears more noisy when 
compared with JSP syntax.&nbsp; It is when you look at the raw HTML.&nbsp; But 
web editors help you to stay out of HTML mode, and when you edit your page that 
way most of the noise goes away.</p>
<pre>&lt;p&gt;&lt;i&gt;&lt;b&gt;Make sure count is set&lt;/b&gt;&lt;/i&gt;&lt;br&gt;
<span style="background-color: #99FF99">^p%&#123;&lt;br&gt;
if (session.count == null) session.count = 1&lt;br&gt;
}%</span>&lt;/p&gt;
&lt;table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber1"&gt;
  &lt;tr&gt;
    &lt;td valign="top" width="50%"&gt;
    &lt;table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber2"&gt;
      &lt;tr&gt;
        &lt;td colspan="2" align="center"&gt;&lt;b&gt;Page Arguments&lt;/b&gt;&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td&gt;Argument&lt;/td&gt;
        &lt;td&gt;Value&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td colspan="2"&gt;<span style="background-color: #99FF99">&lt;i&gt;&lt;b&gt;For each parameter&lt;/b&gt;&lt;/i&gt;&lt;br&gt;
          ^tr%&#123; for (it in request) &#123; }%</span>&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td&gt;<span style="background-color: #99FF99">$&#123; it.key }</span>&lt;/td&gt;
        &lt;td&gt;<span style="background-color: #99FF99">$&#123; it.value }</span>&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td colspan="2"&gt;<span style="background-color: #99FF99">^tr%&#123; } }% &lt;i&gt;&lt;b&gt;Comments are safe before and after 
        stripping tags&lt;/b&gt;&lt;/i&gt;</span>&lt;/td&gt;
      &lt;/tr&gt;
    &lt;/table&gt;
    &lt;/td&gt;
    &lt;td valign="top" width="50%"&gt;
    &lt;table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" id="AutoNumber3"&gt;
      &lt;tr&gt;
        &lt;td colspan="2" align="center"&gt;&lt;b&gt;Session Variables&lt;/b&gt;&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td&gt;Variable&lt;/td&gt;
        &lt;td&gt;Value&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td colspan="2"&gt;<span style="background-color: #99FF99">&lt;i&gt;&lt;b&gt;For each session variable&lt;/b&gt;&lt;/i&gt;&lt;br&gt;
          ^tr%&#123; session.each() &#123; }%</span>&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td&gt;<span style="background-color: #99FF99">$&#123; it.key }</span>&lt;/td&gt;
        &lt;td&gt;<span style="background-color: #99FF99">$&#123; it.value }</span>&lt;/td&gt;
      &lt;/tr&gt;
      &lt;tr&gt;
        &lt;td colspan="2"&gt;<span style="background-color: #99FF99">^tr%&#123; } }% &lt;i&gt;&lt;b&gt;because they will get 
        stripped off&lt;/b&gt;&lt;/i&gt;</span>&lt;/td&gt;
      &lt;/tr&gt;
    &lt;/table&gt;
    &lt;/td&gt;
  &lt;/tr&gt;
&lt;/table&gt;
&lt;p&gt;&lt;i&gt;&lt;b&gt;Increment the count on each refresh&lt;/b&gt;&lt;/i&gt;&lt;br&gt;
<span style="background-color: #99FF99">^p%&#123; session.count++ }%</span>&lt;/p&gt;
</pre>
</body>
</html>