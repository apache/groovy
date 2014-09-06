package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh

import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList

class CustomClassCompletorTest extends CompletorTestSupport {

    void testKnownClass() {
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: [String]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            CustomClassSyntaxCompletor completor = new CustomClassSyntaxCompletor(groovyshMock)
            def candidates = []
            // in the shell, only Classes in the default package occur,but well...
            assert completor.complete(tokenList('jav'), candidates)
            assert ['java.lang.String'] == candidates
        }
    }
}
