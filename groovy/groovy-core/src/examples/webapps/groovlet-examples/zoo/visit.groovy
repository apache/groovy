Animal shark = new zoo.fish.Shark()
Animal trout = new zoo.fish.Trout()
Animal forelle = new zoo.HommingbergerGepardenforelle()

println """
<html>
    <head>
        <title>Groovy Servlet Example - Visiting the zoo</title>
    </head>
    <body>
     <p>Shark<br> 
     ${shark.saySomething("\"Where is the trout?\"")}
     <p>Trout<br> 
     ${trout.saySomething("Here is the trout!")}
     <p>Forelle<br> 
     ${forelle.saySomething("\"<a href=\"http://www.hommingberger-forelle.de\">There is no spoon.</a>\"")}
    </body>
</html>
"""