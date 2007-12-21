println """
<html>
    <head>
        <title>Groovy Servlet Example - hello</title>
    </head>
    <body>
    <a href="../"><img src="../images/return.gif" width="24" height="24" border="0"></a><a href="../">Return</a>
    <p>
"""

session = request.getSession(true);

if (session.counter == null) {
  session.counter = 1
}


println """Hello, ${request.remoteHost}! ${new java.util.Date()}"""

println """
<dl>
 <dt><b>requestURI</b></dt><dd>${request.requestURI}</dd>
 <dt><b>servletPath</b></dt><dd>${request.servletPath}</dd>
 <dt><b>session.counter</b></dt><dd>${session.counter}</dd>
</dl>
"""

println """
    </body>
</html>
"""

session.counter = session.counter + 1
