println """
<html>
    <head>
        <title>Groovy Servlet Example - Visiting the zoo</title>
    </head>
    <body>
    <a href="../"><img src="../images/return.gif" width="24" height="24" border="0"></a><a href="../">Return</a>
    <p>
"""

Animal shark = new zoo.fish.Shark()
Animal trout = new zoo.fish.Trout()
Animal forelle = new zoo.HommingbergerGepardenforelle()

println """
     <p>Shark<br>
     ${shark.saySomething("\"Where is the trout?\"")}

     <p>Trout<br>
     ${trout.saySomething("Here is the trout!")}

     <p>Forelle<br>
     ${forelle.saySomething("\"<a href=\"http://www.hommingberger-forelle.de\">There is no spoon.</a>\"")}
     <!-- http://en.wikipedia.org/wiki/Nigritude_ultramarine -->
"""

println """
    </body>
</html>
"""
