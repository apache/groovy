package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Completor completingclasses defined in the shell
 */
public class CustomClassSyntaxCompletor implements IdentifierCompletor {

    Groovysh shell

    CustomClassSyntaxCompletor(Groovysh shell) {
        this.shell = shell
    }

    @Override
    public boolean complete(final List<GroovySourceToken> tokens, List candidates) {
        String prefix = tokens.last().getText()
        boolean foundMatch = false
        Class[] classes = shell.interp.classLoader.loadedClasses
        if (classes.size() > 0) {
            List<String> classnames = classes.collect {Class it -> it.getName()}
            for (String varName in classnames) {
                if (varName.startsWith(prefix)) {
                    candidates << varName
                    foundMatch = true
                }
            }
        }
        return foundMatch
    }
}
