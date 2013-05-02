package org.codehaus.groovy.tools.shell

import jline.Completor
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MethodClosure

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

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

    int complete(String bufferLine, int cursor, List candidates) {
        // for shell commands, don't complete
        int commandEnd = bufferLine.indexOf(' ')
        if (commandEnd != -1) {
            String commandToken = bufferLine.substring(0, commandEnd);
            for (command in shell.registry.commands()) {
                if (commandToken == command.name || commandToken in command.aliases) {
                    return -1
                }
            }
        }

        int identifierStart = findIdentifierStart(bufferLine, cursor)
        String identifierPrefix = identifierStart != -1 ? bufferLine.substring(identifierStart, cursor) : ""
        // get last dot before cursor
        int lastDot = bufferLine.substring(0, cursor).lastIndexOf('.')
        // if there are no dots, or the dot is not before the prefix
        if (lastDot == -1 || (identifierStart > -1 && lastDot < identifierStart - 1)) {
            //if there is a valid identifier prefix
            if (identifierStart != -1) {
                List<String> myCandidates = findMatchingVariables(identifierPrefix)
                myCandidates.addAll(findMatchingKeywords(identifierPrefix))
                myCandidates.addAll(findMatchingCustomClasses(identifierPrefix))
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
                int previousIdentifierStart = findIdentifierStart(bufferLine, lastDot)
                if (previousIdentifierStart == -1) {
                    // this should rarely happen, example: foo(.baz
                    return -1
                }
                // prevent misleading completion for variable foo != Bar.foo
                // scanning over points is dangerous in Groovy because Bar.foo can evaluate to Bar.getFoo(),
                // which should not be evaluated during completion
                if (previousIdentifierStart > 0 && bufferLine.charAt(previousIdentifierStart - 1) == '.') {
                    return -1
                }
                String instanceRefExpression = bufferLine.substring(previousIdentifierStart, lastDot)
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
        Set<String> rv = new HashSet<String>()
        Set.getInterfaces()
        Class clazz = instance.class
        if (clazz == null) {
            clazz = instance.getClass()
        }
        if (clazz == null) {
            return rv;
        }
        boolean isClass = false
        if (clazz == Class) {
            isClass = true
            clazz = instance as Class
        }

        InvokerHelper.getMetaClass(instance).metaMethods.each { MetaMethod mmit ->
            if (acceptName(mmit.name, prefix)) {
                rv << mmit.getName() + (mmit.parameterTypes.length == 0 ? "()" : "(")
            }
        }

        Class loopclazz = clazz
        while (loopclazz != null) {
            addClassFieldsAndMethods(loopclazz, isClass, prefix, rv)
            loopclazz = loopclazz.superclass
        }
        if (clazz.isArray() && !isClass) {
            // Arrays are special, these public members cannot be found via Reflection
            for (String member in ['length', 'clone()']) {
                if (member.startsWith(prefix)) {
                    rv.add(member)
                }
            }
        }
        return rv.sort()
    }

    private static Collection<String> addClassFieldsAndMethods(final Class clazz, final boolean staticOnly, final String prefix, Collection rv) {
        Field[] fields = staticOnly ? clazz.fields : clazz.getDeclaredFields()
        fields.each { Field fit ->
            if (acceptName(fit.name, prefix)) {
                int modifiers = fit.getModifiers()
                if (Modifier.isPublic(modifiers) && (!staticOnly || Modifier.isStatic(modifiers))) {
                    if (!clazz.isEnum() || !(!staticOnly && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) && fit.getType() == clazz)) {
                        rv << fit.name
                    }
                }
            }
        }
        Method[] methods = staticOnly ? clazz.methods : clazz.getDeclaredMethods()
        methods.each { Method methIt ->
            String name = methIt.getName()
            if (name.startsWith("super\$")) {
                name = name.substring(name.find("^super\\\$.*\\\$").length())
            }
            if (acceptName(name, prefix)) {
                int modifiers = methIt.getModifiers()
                if (Modifier.isPublic(modifiers) && (!staticOnly || Modifier.isStatic(modifiers))) {
                    rv << name + (methIt.parameterTypes.length == 0 ? "()" : "(")
                }
            }
        }
        for (interface_ in clazz.getInterfaces()) {
            addClassFieldsAndMethods(interface_, staticOnly, prefix, rv)
        }
    }

    private static boolean acceptName(String name, String prefix) {
        return (!prefix || name.startsWith(prefix)) &&
                (!(name.contains('$')) && !name.startsWith("_"));
    }

    /**
     * Build a list of variables defined in the shell that
     * match a given prefix.
     * @param prefix the prefix to match
     * @return the list of variables that match the prefix
     */
    List<String> findMatchingVariables(String prefix) {
        List<String> matches = []
        Map vars = shell.interp.context.variables
        for (String varName in vars.keySet()) {
            if (acceptName(varName, prefix)) {
                if (vars.get(varName) instanceof MethodClosure) {
                    if (((MethodClosure)vars.get(varName)).getMaximumNumberOfParameters() > 0) {
                        varName += "("
                    } else {
                        varName += "()"
                    }
                }
                matches << varName
            }
        }
        return matches
    }

    /**
     * Build a list of classes defined in the shell that
     * match a given prefix.
     * @param prefix the prefix to match
     * @return the list of variables that match the prefix
     */
    List<String> findMatchingCustomClasses(String prefix) {
        List<String> matches = []
        Class[] classes = shell.interp.classLoader.loadedClasses
        if (classes.size() > 0) {
            List<String> classnames = classes.collect {Class it -> it.getName()}
            for (String varName in classnames) {
                if (varName.startsWith(prefix)) {
                    matches << varName
                }
            }
        }
        return matches
    }

    final String[] KEYWORDS = [
            "abstract",
            "assert", "boolean", "break", "byte",
            "case",
            // "catch (", // special
            "char", "class", "continue",
            "def", // short, but keep, else "default" completes, annoyingly
            "default",
            "do",
            "double",
            "else", "enum", "extends",
            //"false",// value
            "final",
            //"finally {", // special
            "float",
            //"for (", // special
            //"if (", // special
            //"import",
            "in",
            "instanceof",
            "int", // short, but keeping for consistency, all primitives
            "interface",
            "long",
            //"native",
            "new",
            //"null", // value
            "private", "protected", "public",
            "return", "short",
            "static",
            //"super",// value
            //"switch (", // special
            "synchronized",
            //"this", // value
            //threadsafe,
            "throw", "throws",
            "transient",
            //"true", // value
            //"try {", //special
            "void", "volatile"
            //"while (" // special
    ]

    // VALUE_KEYWORDS and SPECIAL_FUNCTIONS completed without added blank
    final String[] VALUE_KEYWORDS = [
            "true",
            "false",
            "this",
            "super",
            "null"]

    final String[] SPECIAL_FUNCTIONS = [
            "catch (",
            "finally {",
            "for (",
            "if (",
            "switch (",
            "try {",
            "while ("]

    List<String> findMatchingKeywords(String prefix) {
        List<String> matches = []
        for (String varName in KEYWORDS) {
            if (varName.startsWith(prefix)) {
                matches << varName + " "
            }
        }
        for (String varName in VALUE_KEYWORDS) {
            if (varName.startsWith(prefix)) {
                matches << varName
            }
        }
        for (String varName in SPECIAL_FUNCTIONS) {
            if (varName.startsWith(prefix)) {
                matches << varName
            }
        }
        return matches
    }
}
