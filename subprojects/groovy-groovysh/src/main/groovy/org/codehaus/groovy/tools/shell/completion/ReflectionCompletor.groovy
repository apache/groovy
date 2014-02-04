/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.tools.shell.Groovysh

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

import static org.codehaus.groovy.antlr.parser.GroovyTokenTypes.*

/**
 * Completes fields and methods of Classes or instances.
 * Does not quite respect the contract of IdentifierCompletor, as last Token may be a dot or not,
 * thus also returns as int the cursor position.
 */
class ReflectionCompletor {

    Groovysh shell
    static NavigablePropertiesCompleter propertiesCompleter = new NavigablePropertiesCompleter()
    int metaclass_completion_prefix_length

    ReflectionCompletor(Groovysh shell) {
        this(shell, 0)
    }

    /**
     *
     * @param shell
     * @param metaclass_completion_prefix_length how long the prefix must be to disaply candidates from metaclass
     */
    ReflectionCompletor(Groovysh shell, int metaclass_completion_prefix_length) {
        this.shell = shell
        this.metaclass_completion_prefix_length = metaclass_completion_prefix_length
    }

    public int complete(final List<GroovySourceToken> tokens, List candidates) {
        GroovySourceToken currentElementToken = null
        GroovySourceToken dotToken
        List<GroovySourceToken> previousTokens
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("must be invoked with at least 2 tokens, one of which is dot")
        }
        if (tokens.last().getType() == DOT) {
            dotToken = tokens.last()
            previousTokens = tokens[0..-2]
        } else {
            if (!tokens[-2] == DOT) {
                throw new IllegalArgumentException("must be invoked with token list with dot at last position or one position before")
            }
            currentElementToken = tokens.last()
            dotToken = tokens[-2]
            previousTokens = tokens[0..-3]
        }
        Object instance = getInvokerClassOrInstance(previousTokens)
        if (instance == null) {
            return -1
        }

        String identifierPrefix
        if (currentElementToken) {
            identifierPrefix = currentElementToken.getText()
        } else {
            identifierPrefix = ""
        }

        // look for public methods/fields that match the prefix
        Collection<String> myCandidates = getPublicFieldsAndMethods(instance, identifierPrefix)
        boolean showAllMethods = identifierPrefix.length() >= this.metaclass_completion_prefix_length
        // Also add metaclass methods if prefix is long enough (user would usually not care about those)
        myCandidates.addAll(getMetaclassMethods(
                instance,
                identifierPrefix,
                showAllMethods))
        if (!showAllMethods) {
            // user probably does not care to see default Object / GroovyObject Methods,
            // they obfuscate the business logic
            removeStandardMethods(myCandidates)
        }
        // specific DefaultGroovyMethods only suggested for suitable instances
        addDefaultMethods(instance, identifierPrefix, myCandidates)

        if (myCandidates.size() > 0) {
            candidates.addAll(myCandidates.sort())
            int lastDot
            // dot could be on previous line
            if (currentElementToken && dotToken.getLine() != currentElementToken.getLine()) {
                lastDot = currentElementToken.getColumn() - 1
            } else {
                lastDot = dotToken.getColumn()
            }
            return lastDot
        }

        // no candidates
        return -1
    }

    /**
     * Takes the last ? tokens of the list that form a simple expression,
     * evaluates it and returns a result. "Simple" means evaluation is known to be
     * side-effect free.
     */
    Object getInvokerClassOrInstance(List<GroovySourceToken> groovySourceTokens) {
        if (!groovySourceTokens || groovySourceTokens.last().getType() == DOT) {
            // we expect the list of tokens before a dot.
            return null
        }
        // first, try to detect a sequence of token before the dot that can safely be evaluated.
        List<GroovySourceToken> invokerTokens = getInvokerTokens(groovySourceTokens)
        if (invokerTokens) {
            try {
                String instanceRefExpression = tokenListToEvalString(invokerTokens)
                instanceRefExpression = instanceRefExpression.replace('\n', '')
                Object instance = shell.interp.evaluate([shell.getImportStatements()] + ['true'] + [instanceRefExpression])
                return instance
            } catch (MissingPropertyException |
                    MissingMethodException |
                    MissingFieldException |
                    MultipleCompilationErrorsException e) {

            }
        }
        return null
    }

    /**
     * return the last tokens of a list that form an expression to be completed after the next dot, or null if
     * expression cannot be detected. This discards Expressions that could easily have side effects or be long
     * in evaluation. However it assumes that operators can be evaluated without side-effect or long running
     * operation. Users who use operators for which this does not hold should not use tab completion.
     * @param groovySourceTokens
     * @return
     */
    static List<GroovySourceToken> getInvokerTokens(List<GroovySourceToken> groovySourceTokens) {
       // implementation goes backwards on token list, adding strings
        // to be evaluated later
        // need to collect using Strings, to support evaluation of string literals
        Stack<Integer> expectedOpeners = new Stack<Integer>()
        int validIndex = groovySourceTokens.size()
        if (validIndex == 0) {
            return null
        }
        GroovySourceToken lastToken = null
        outerloop:
        for (GroovySourceToken loopToken in groovySourceTokens.reverse()) {
            switch (loopToken.getType()) {
            // a combination of any of these can be evaluated without side effects
            // this just avoids any parentheses,
            // could maybe be extended further if harmless parentheses can be detected .
            // This allows already a lot of powerful simple completions, like [foo: Baz.bar]["foo"].
                case STRING_LITERAL:
                    // must escape String for evaluation, need the original string e.g. for mapping key
                    break;
                case LPAREN:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                    if (expectedOpeners.pop() != LPAREN) {
                        return null
                    }
                    break
                case LBRACK:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                    if (expectedOpeners.pop() != LBRACK) {
                        return null
                    }
                    break
                case RBRACK:
                    expectedOpeners.push(LBRACK)
                    break
                case RPAREN:
                    expectedOpeners.push(LPAREN)
                    break
                // tokens which indicate we have reached the beginning of a statement
                // operator tokens (must not be evaluated, as they can have side effects via evil overriding
                case COMPARE_TO:
                case EQUAL:
                case NOT_EQUAL:
                case ASSIGN:
                case GT:
                case LT:
                case GE:
                case LE:
                case PLUS:
                case PLUS_ASSIGN:
                case MINUS:
                case MINUS_ASSIGN:
                case STAR:
                case STAR_ASSIGN:
                case DIV:
                case DIV_ASSIGN:
                case BOR:
                case BOR_ASSIGN:
                case BAND:
                case BAND_ASSIGN:
                case BXOR:
                case BXOR_ASSIGN:
                case BNOT:
                case LOR:
                case LAND:
                case LNOT:
                case LITERAL_in:
                case LITERAL_instanceof:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                    break
                // tokens which indicate we have reached the beginning of a statement
                case LCURLY:
                case SEMI:
                case STRING_CTOR_START:
                    break outerloop
                // tokens we accept
                case IDENT:
                   if (lastToken) {
                       if (lastToken.getType() == LPAREN) {
                           //Method invocation,must be avoided
                           return null
                       }
                       if (lastToken.getType() == IDENT) {
                           // could be attempt to invoke closure like "foo.each bar.baz"
                           return null
                       }
                   }
                    break
                // may begin expression when outside brackets (from back)
                case RANGE_INCLUSIVE:
                case RANGE_EXCLUSIVE:
                case COLON:
                case COMMA:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                // harmless literals
                case LITERAL_true:
                case LITERAL_false:
                case NUM_INT:
                case NUM_FLOAT:
                case NUM_LONG:
                case NUM_DOUBLE:
                case NUM_BIG_INT:
                case NUM_BIG_DECIMAL:
                case MEMBER_POINTER:
                case DOT:
                    break
                default:
                    return null
            } // end switch
            validIndex --
            lastToken = loopToken
        } // end for
        return groovySourceTokens[(validIndex)..-1]
    }

    static String tokenListToEvalString(List<GroovySourceToken> groovySourceTokens) {
        StringBuilder builder = new StringBuilder()
        for (GroovySourceToken token: groovySourceTokens) {
            if (token.getType() == STRING_LITERAL) {
                builder.append("\"").append(token.getText()).append("\"")
            } else {
                builder.append(token.getText())
            }
        }
        return builder.toString()
    }

    static boolean acceptName(String name, String prefix) {
        return (!prefix || name.startsWith(prefix)) &&
                (!(name.contains('$')) && !name.startsWith("_"));
    }

    static Collection<String> getMetaclassMethods(Object instance, String prefix, boolean includeMetaClassImplMethods) {
        Set<String> rv = new HashSet<String>()
        MetaClass metaclass = InvokerHelper.getMetaClass(instance)
        if (includeMetaClassImplMethods || ! metaclass instanceof MetaClassImpl) {
            metaclass.metaMethods.each { MetaMethod mmit ->
                if (acceptName(mmit.name, prefix)) {
                    rv << mmit.getName() + (mmit.parameterTypes.length == 0 ? "()" : "(")
                }
            }
        }
        return rv.sort()
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
        Class clazz = instance.getClass()
        if (clazz == null) {
            return rv;
        }

        boolean isClass = (clazz == Class)
        if (isClass) {
            clazz = instance as Class
        }

        Class loopclazz = clazz
        while (loopclazz != null && loopclazz != Object && loopclazz != GroovyObject) {
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

        // other completions that are commonly possible with properties
        if (!isClass) {
            propertiesCompleter.addCompletions(instance, prefix, rv)
        }

        return rv.sort()
    }

    /**
     * removes candidates that, most of the times, a programmer does not want to see in completion
     * @param candidates
     */
    static removeStandardMethods(Collection<String> candidates) {
        for (String defaultMethod in [
                'clone()', 'finalize()', 'getClass()',
                'getMetaClass()', 'getProperty(',  'invokeMethod(', 'setMetaClass(', 'setProperty(',
                'equals(', 'hashCode()', 'toString()',
                'notify()', 'notifyAll()', 'wait(', 'wait()']) {
            candidates.remove(defaultMethod)
        }
    }

    /**
     * Offering all DefaultGroovyMethods on any object is too verbose, hiding all
     * removes user-friendlyness. So here util methods will be added to candidates
     * if the instance is of a suitable type.
     * This does not need to be strictly complete, only the most useful functions may appear.
     */
    static addDefaultMethods(Object instance, String prefix, Collection<String> candidates) {
        if (instance instanceof Iterable) {
            [
                    'any()', 'any(',
                    'collect()', 'collect(',
                    'combinations()',
                    'count(',
                    'countBy(',
                    'drop(',
                    'dropWhile(',
                    'each()', 'each(',
                    'eachPermutation(',
                    'every()', 'every(',
                    'find(', 'findResult(', 'findResults(',
                    'flatten()',
                    'inject(',
                    'intersect(',
                    'join(',
                    'max()', 'min()',
                    'reverse()',
                    'size()',
                    'sort()',
                    'split(',
                    'take(', 'takeWhile(',
                    'toSet()',
                    'retainAll(', 'removeAll(',
                    'unique()', 'unique('
            ].findAll({it.startsWith(prefix)}).each({candidates.add(it)})
            if (instance instanceof List) {
                [
                        'collate(',
                        'pop()',
                        'transpose()'
                ].findAll({it.startsWith(prefix)}).each({candidates.add(it)})
            }
        }
        if (instance instanceof Map) {
            [
                    'any(',
                    'drop(',
                    'each(',
                    'find(', 'findAll(', 'findResult(', 'findResults(',
                    'groupEntriesBy(', 'groupBy(',
                    'inject(', 'intersect(',
                    'spread()',
                    'subMap(',
                    'take(', 'takeWhile('
            ].findAll({it.startsWith(prefix)}).each({candidates.add(it)})
        }
        if (instance instanceof Number) {
            [
                    'abs()',
                    'downto(',
                    'times(',
                    'power(',
                    'upto('
            ].findAll({it.startsWith(prefix)}).each({candidates.add(it)})
        }
        Class clazz = instance.getClass()
        if (clazz != null && clazz != Class && clazz.isArray()) {
            [
                    'any()', 'any(',
                    'collect()', 'collect(',
                    'count(',
                    'countBy(',
                    'drop(',
                    'dropWhile(',
                    'each()', 'each(',
                    'every()', 'every(',
                    'find(', 'findResult(',
                    'flatten()',
                    'inject(',
                    'join(',
                    'max()', 'min()',
                    'reverse()',
                    'size()',
                    'sort()',
                    'split(',
                    'take(', 'takeWhile('
            ].findAll({it.startsWith(prefix)}).each({candidates.add(it)})
        }
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

}
