class A {
    private optionalParenthesis() {
        println "hello"
        log a, b
        this.a "arg1", "arg2"
        ifTrue { -> }
    }

    private blackGroovyMagic() {
        method argument method2 argument2 method3 {-> closureCode }
        path.method argument method2 argument2 method3 {-> closureCode }
        path.path2.method argument method2 argument2 method3 {-> closureCode }
        path.path2.method argument method2 argument2 method3 {-> closureCode } field
    }

    /*
        A command-expression is composed of an even number of elements
        The elements are alternating a method name, and its parameters (can be named and non-named parameters)
        A parameter element can be any kind of expression (ie. a method call foo(), foo{}, or some expression like x+y)
        All those pairs of method name and parameters are actually chained method calls (ie. send "hello" to "Guillaume" is two methods chained one after the other as send("hello").to("Guillaume"))

        Closures can be passed without comma between them. // see 1
        Last identifier is optional. // 2
    */
    private commandExpression() {
        foo {->c}
        foo {->c} {->c} // 1
        foo a1
        foo a1()
        foo a1 {->c}
        foo a1 a2
        foo a1() a2
        // foo a1 a2()                       // Is it valid?
        foo a1 a2 {->c}
        foo a1 {->c} a2
        foo a1 {->c} {->c} a2 // 1
        foo a1 {->c} a2 {->c}
        foo a1 a2 a3                         // == this.foo(a1).a2(a3) // 2
        foo a1() a2 a3()
        // foo a1 a2() a3                    // Is it valid?
        foo a1 a2 a3 {->c}
        foo a1 a2 a3 a4
        foo a1 a2 a3 a4 {->c}
        foo a1 a2 a3 a4 a5
        foo a1() a2 a3() a4 a5()
        foo a1 a2 a3 a4 a5 {->c}
    }

    private closureArguments() {
        foo {-> }
        foo() {-> }
        foo(arg1, arg2) {->}
        foo(arg1, arg2) {->} {->}
    }
}





















