package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh
import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList

class VariableCompletorTest extends CompletorTestSupport {

    void testKnownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [xyzabc: ""]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            VariableSyntaxCompletor completor = new VariableSyntaxCompletor(groovyshMock)
            def candidates = []
            assertEquals(true, completor.complete(tokenList("xyz"), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testKnownVarMultiple() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [bad: "", xyzabc: "", xyzfff: "", nope: ""]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            VariableSyntaxCompletor completor = new VariableSyntaxCompletor(groovyshMock)
            def candidates = []
            assertEquals(true, completor.complete(tokenList("xyz"), candidates))
            assertEquals(["xyzabc", "xyzfff"], candidates)
        }
    }

    void testUnknownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            VariableSyntaxCompletor completor = new VariableSyntaxCompletor(groovyshMock)
            def candidates = []
            assertEquals([], candidates)
            assertEquals(false, completor.complete(tokenList("xyz"), candidates))
        }
    }
}
