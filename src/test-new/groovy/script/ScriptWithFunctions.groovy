def foo(list, value) {
    println "Calling function foo() with param ${value}"
    list << value
}

x = []
foo(x, 1)
foo(x, 2)
assert x == [1, 2]

println "Creating list ${x}"