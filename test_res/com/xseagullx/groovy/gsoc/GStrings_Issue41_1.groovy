class A {
    private void testSlashy() {
        println(/Hello/)
        (/Hello/ + 1)
    }

    private void testSlashyEscapes() {
        println(/Hello/)
        println(/'"Hello/)
        (/Hello\/sadf\u1245 \234 \t\\s / + 1)
    }

    private void testSlashyGStrings() {
        println(/Hello${asdd} sd /)
        println(/Hello${asdd} ${1234 + 134} sd /)
    }

    private void testDoubleQuoted() {
        println("${ variable }")
        println(" Text ${ variable } text")
        println(" Text ${ variable } text ${ variable } ${ variable + 1 } ${ "variable" }")
        println(" Text ${ "inner${ variable } ${ variable }" } text ${ variable } ${ variable + 1 } ${ "variable" }")
    }

    private void testDoubleQuotedPath() {
        println("$variable")
        println(" Text $variable.var text")
        println(" Text $variable text $variable.var ${ variable + 1 }")
    }
}
