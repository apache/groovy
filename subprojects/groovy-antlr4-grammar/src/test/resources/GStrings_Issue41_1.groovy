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
        "${ variable }"
        " Text ${ variable } text"
        " Text ${ variable } text ${ variable } ${ variable + 1 } ${ "variable" }"
        " Text ${ "inner${ variable } ${ variable }" } text ${ variable } ${ variable + 1 } ${ "variable" }"
    }

    private void testDoubleQuotedPath() {
        "$variable"
        "$v"
        "$variable.var"
        " Text $variable.var text"
        " Text $variable text $variable.var ${ variable + 1 }"
    }

    private void testDoubleQuotedPathTrailingDot() {
        "$variable.var."
        "$variable.var...."
        "$variable.var. ${}$aa$b."
    }

    private void testSlashyPath() {
        (/Hello$a$ab.nb/)
        (/Hello${}$fhjg/ + 1)
        (/Hello${}$fhjg..$df.d.s./ + 1)
    }
}
