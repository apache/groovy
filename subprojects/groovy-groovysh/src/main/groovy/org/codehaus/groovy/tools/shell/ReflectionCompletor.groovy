package org.codehaus.groovy.tools.shell

import jline.Completor
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Implements the Completor interface to provide competions for
 * GroovyShell by using reflection on global variables.
 *
 * @author <a href="mailto:probabilitytrees@gmail.com">Marty Saxton</a>
 */
class ReflectionCompletor implements Completor {

    private Groovysh shell;

    ReflectionCompletor(Groovysh shell) {
        this.shell = shell
    }

    int complete(String buffer, int cursor, List candidates) {

        int identifierStart = findIdentifierStart(buffer, cursor)
        String identifierPrefix = identifierStart != -1 ? buffer.substring(identifierStart, cursor) : ""
        int lastDot = buffer.lastIndexOf('.')

        // if there are no dots, and there is a valid identifier prefix
        if (lastDot == -1 ) {
            if (identifierStart != -1) {
                List myCandidates = findMatchingVariables(identifierPrefix)
                if (myCandidates.size() > 0) {
                    candidates.addAll(myCandidates)
                    return identifierStart

                }
            }
        } else {
            // there are 1 or more dots
            // if ends in a dot, or if there is a valid identifier prefix
            if (lastDot == cursor - 1 || identifierStart != -1) {
                // evaluate the part before the dot to get an instance
                String instanceRefExpression = buffer.substring(0, lastDot)
                try {
                    def instance = shell.interp.evaluate([instanceRefExpression])
                    if (instance != null) {
                        // look for public methods/fields that match the prefix
                        List myCandidates = getPublicFieldsAndMethods(instance, identifierPrefix)
                        if (myCandidates.size() > 0) {
                            candidates.addAll(myCandidates)
                            return lastDot + 1
                        }
                    }
                } catch (MissingPropertyException |
                         MissingMethodException |
                         MissingFieldException |
                         MultipleCompilationErrorsException e) {
                }
            }
        }

        // no candidates
        return -1
    }

    /**
     * Parse a buffer to determine the start index of the groovy identifier
     * @param buffer the buffer to parse
     * @param endingAt the end index within the buffer
     * @return the start index of the identifier, or -1 if the buffer
     * does not contain a valid identifier that ends at endingAt
     */
    int findIdentifierStart(String buffer, int endingAt) {
        // if the string is empty then there is no expression
        if (endingAt == 0) {
            return -1
        }
        // if the last character is not valid then there is no expression
        char lastChar = buffer.charAt(endingAt - 1)
        if (!Character.isJavaIdentifierPart(lastChar)) {
            return -1
        }
        // scan backwards until the beginning of the expression is found
        int startIndex = endingAt - 1
        while (startIndex > 0 && Character.isJavaIdentifierPart(buffer.charAt(startIndex - 1))) {
            --startIndex
        }
        return startIndex
    }


    /**
     * Build a list of public fields and methods for an object
     * that match a given prefix.
     * @param instance the object
     * @param prefix the prefix that must be matched
     * @return the list of public methods and fields that begin with the prefix
     */
    static Collection<String> getPublicFieldsAndMethods(Object instance, String prefix) {
        List<String> rv = []
        Class clazz = instance.class
        if (clazz == null) {
            clazz = instance.getClass()
        }
        if (clazz == null) {
            return rv;
        }
        if (clazz == Class) {
            clazz = instance as Class
        }

        clazz.fields.each { Field fit ->
            if (fit.name.startsWith(prefix)) {
                rv << fit.name
            }
        }
        clazz.methods.each { Method mit ->
            if (mit.name.startsWith(prefix)) {
                rv << mit.name + (mit.parameterTypes.length == 0 ? "()" : "(")
            }
        }
        InvokerHelper.getMetaClass(instance).metaMethods.each { MetaMethod mmit ->
            if (mmit.name.startsWith(prefix)) {
                rv << mmit.name + (mmit.parameterTypes.length == 0 ? "()" : "(")
            }
        }
        return rv.sort().unique()
    }

    /**
     * Build a list of variables defined in the shell that
     * match a given prefix.
     * @param prefix the prefix to match
     * @return the list of variables that match the prefix
     */
    List findMatchingVariables(String prefix) {
        def matches = []
        for (String varName in shell.interp.context.variables.keySet()) {
            if (varName.startsWith(prefix)) {
                matches << varName
            }
        }
        return matches
    }
}
