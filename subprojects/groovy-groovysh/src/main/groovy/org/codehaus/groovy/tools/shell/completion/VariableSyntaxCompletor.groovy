package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Completor completing variable and method names from known variables in the shell
 */
public class VariableSyntaxCompletor implements IdentifierCompletor {

    Groovysh shell

    VariableSyntaxCompletor(Groovysh shell) {
        this.shell = shell
    }

    @Override
    public boolean complete(final List<GroovySourceToken> tokens, List candidates) {
        String prefix = tokens.last().getText()
        Map vars = shell.interp.context.variables
        boolean foundMatch = false
        for (String varName in vars.keySet()) {
            if (acceptName(varName, prefix)) {
                if (vars.get(varName) instanceof MethodClosure) {
                    if (((MethodClosure)vars.get(varName)).getMaximumNumberOfParameters() > 0) {
                        varName += "("
                    } else {
                        varName += "()"
                    }
                }
                foundMatch = true
                candidates << varName
            }
        }
        return foundMatch
    }


    private static boolean acceptName(String name, String prefix) {
        return (!prefix || name.startsWith(prefix)) &&
               (!(name.contains('$')) && !name.startsWith("_"));
    }
}
