
a = 1
evaluate("a = 3")
assert a == 3

println("Done and now set a to ${a}")


println("About to call another script")

evaluate(new File("src/test/groovy/script/HelloWorld.groovy"))

println("Done")
