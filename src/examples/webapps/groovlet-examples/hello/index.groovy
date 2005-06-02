import java.util.Date

if (session.counter == null) {
  session.counter = 1
}

println """
<html>
    <head>
        <title>Groovy Servlet Example - hello</title>
    </head>
    <body>

Hello, ${request.remoteHost}: ${session.counter}! ${new Date()}

<dl>
 <dt><b>requestURI</b></dt><dd>${request.requestURI}</dd>
 <dt><b>servletPath</b></dt><dd>${request.servletPath}</dd>
 <dt><b>pathInfo</b></dt><dd>${request.pathInfo}</dd>
</dl>

    </body>
</html>
"""
session.counter = session.counter + 1
