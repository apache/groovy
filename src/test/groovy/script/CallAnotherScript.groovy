
println("About to call another script")

script = new GroovyShell()
script.run("src/test/groovy/script/HelloWorld.groovy", null)

println("Done")
