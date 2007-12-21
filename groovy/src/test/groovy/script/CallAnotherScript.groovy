import java.io.File

println("About to call another script")

script = new GroovyShell()
script.run(new File("src/test/groovy/script/HelloWorld.groovy"), [])

println("Done")
