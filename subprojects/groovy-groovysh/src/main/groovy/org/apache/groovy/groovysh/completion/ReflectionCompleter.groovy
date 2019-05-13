/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.groovysh.completion

import org.apache.groovy.groovysh.Groovysh
import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.tools.shell.util.Preferences
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiRenderer

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.regex.Pattern

/**
 * Completes fields and methods of Classes or instances.
 * Does not quite respect the contract of IdentifierCompleter, as last Token may be a dot or not,
 * thus also returns as int the cursor position.
 */
class ReflectionCompleter implements GroovyTokenTypes {

    private static final NavigablePropertiesCompleter PROPERTIES_COMPLETER = new NavigablePropertiesCompleter()
    private static final Pattern BEAN_ACCESSOR_PATTERN = ~'^(get|set|is)[A-Z].*'

    final Groovysh shell

    /**
     *
     * @param shell
     * @param metaclass_completion_prefix_length how long the prefix must be to display candidates from metaclass
     */
    ReflectionCompleter(final Groovysh shell) {
        this.shell = shell
    }

    int complete(final List<GroovySourceToken> tokens, final List<CharSequence> candidates) {
        GroovySourceToken currentElementToken = null
        GroovySourceToken dotToken
        List<GroovySourceToken> previousTokens
        if (tokens.size() < 2) {
            throw new IllegalArgumentException('must be invoked with at least 2 tokens, one of which is dot' + tokens*.text)
        }
        if (tokens.last().type == DOT || tokens.last().type == OPTIONAL_DOT || tokens.last().type == SPREAD_DOT) {
            dotToken = tokens.last()
            previousTokens = tokens[0..-2]
        } else {
            if (tokens[-2].type != DOT && tokens[-2].type != OPTIONAL_DOT && tokens[-2].type != SPREAD_DOT) {
                throw new IllegalArgumentException('must be invoked with token list with dot at last position or one position before' + tokens*.text)
            }
            currentElementToken = tokens.last()
            dotToken = tokens[-2]
            previousTokens = tokens[0..-3]
        }

        Object instanceOrClass = getInvokerClassOrInstance(previousTokens)
        if (instanceOrClass == null) {
            return -1
        }
        if (dotToken.type == SPREAD_DOT) {
            /**
             * for aggregate types, find an arbitrary collection-member
             * element within the instance. This may cause invalid completion candidates when the collection is not
             * homogeneous, but still better than no completion at all. Alternatively the union or intersection of
             * candidate completions could be built. For non-aggregate types, we assume that whatever find()
             * returns is useful for *. completion as well.
             */
            instanceOrClass = instanceOrClass.find()
            if (instanceOrClass == null) {
                return -1
            }
        }

        String identifierPrefix
        if (currentElementToken) {
            identifierPrefix = currentElementToken.text
        } else {
            identifierPrefix = ''
        }

        return completeInstanceMembers(instanceOrClass, identifierPrefix, candidates, currentElementToken, dotToken)
    }

    private int completeInstanceMembers(final Object instanceOrClass,
                                        final String identifierPrefix,
                                        final List<CharSequence> candidates,
                                        final GroovySourceToken currentElementToken,
                                        final GroovySourceToken dotToken) {
        // look for public methods/fields that match the prefix
        Collection<ReflectionCompletionCandidate> myCandidates = getPublicFieldsAndMethods(instanceOrClass, identifierPrefix)

        boolean showAllMethods = (identifierPrefix.length() >= Integer.valueOf(Preferences.get(Groovysh.METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY, '3')))
        // Also add metaclass methods if prefix is long enough (user would usually not care about those)
        myCandidates.addAll(getMetaclassMethods(
                instanceOrClass,
                identifierPrefix,
                showAllMethods).collect({ String it -> new ReflectionCompletionCandidate(it) }))

        if (!showAllMethods) {
            // user probably does not care to see default Object / GroovyObject Methods,
            // they obfuscate the business logic
            removeStandardMethods(myCandidates)
        }

        // specific DefaultGroovyMethods only suggested for suitable instances
        myCandidates.addAll(getDefaultMethods(instanceOrClass,
                identifierPrefix).collect({ String it -> new ReflectionCompletionCandidate(it, AnsiRenderer.Code.BLUE.name()) }))

        if (myCandidates.size() > 0) {
            myCandidates = myCandidates.sort()
            if (Boolean.valueOf(Preferences.get(Groovysh.COLORS_PREFERENCE_KEY, 'true'))) {
                candidates.addAll(myCandidates.collect(
                        { ReflectionCompletionCandidate it ->
                            AnsiRenderer.render(it.value,
                                    it.jAnsiCodes.toArray(new String[it.jAnsiCodes.size()]))
                        }))
            } else {
                candidates.addAll(myCandidates*.value)
            }

            int lastDot
            // dot could be on previous line
            if (currentElementToken && dotToken.line != currentElementToken.line) {
                lastDot = currentElementToken.column - 1
            } else {
                // Spread-dot has length 2!
                lastDot = dotToken.column + (dotToken.getText().length() - 1)
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
    Object getInvokerClassOrInstance(final List<GroovySourceToken> groovySourceTokens) {
        if (!groovySourceTokens
                || groovySourceTokens.last().type == DOT
                || groovySourceTokens.last().type == OPTIONAL_DOT) {
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
    static List<GroovySourceToken> getInvokerTokens(final List<GroovySourceToken> groovySourceTokens) {
        int validIndex = groovySourceTokens.size()
        if (validIndex == 0) {
            return []
        }
        // implementation goes backwards on token list, adding strings
        // to be evaluated later
        // need to collect using Strings, to support evaluation of string literals
        Stack<Integer> expectedOpeners = new Stack<Integer>()
        GroovySourceToken lastToken = null
        outerloop:
        for (GroovySourceToken loopToken in groovySourceTokens.reverse()) {
            switch (loopToken.type) {
            // a combination of any of these can be evaluated without side effects
            // this just avoids any parentheses,
            // could maybe be extended further if harmless parentheses can be detected .
            // This allows already a lot of powerful simple completions, like [foo: Baz.bar]['foo'].
                case STRING_LITERAL:
                    // must escape String for evaluation, need the original string e.g. for mapping key
                    break
                case LPAREN:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                    if (expectedOpeners.pop() != LPAREN) {
                        return []
                    }
                    break
                case LBRACK:
                    if (expectedOpeners.empty()) {
                        break outerloop
                    }
                    if (expectedOpeners.pop() != LBRACK) {
                        return []
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
                        if (lastToken.type == LPAREN) {
                            //Method invocation,must be avoided
                            return []
                        }
                        if (lastToken.type == IDENT) {
                            // could be attempt to invoke closure like 'foo.each bar.baz'
                            return []
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
                case OPTIONAL_DOT:
                    break
                default:
                    return null
            } // end switch
            validIndex--
            lastToken = loopToken
        } // end for
        return groovySourceTokens[(validIndex)..-1]
    }

    static String tokenListToEvalString(final List<GroovySourceToken> groovySourceTokens) {
        StringBuilder builder = new StringBuilder()
        for (GroovySourceToken token : groovySourceTokens) {
            if (token.type == STRING_LITERAL) {
                builder.append('\'').append(token.text).append('\'')
            } else {
                builder.append(token.text)
            }
        }
        return builder.toString()
    }

    static boolean acceptName(final String name, final String prefix) {
        return (!prefix || name.startsWith(prefix)) &&
                (!(name.contains('$')) && !(name.startsWith('_')))
    }

    static Collection<String> getMetaclassMethods(final Object instance, final String prefix, final boolean includeMetaClassImplMethods) {
        Set<String> rv = new HashSet<String>()
        MetaClass metaclass = InvokerHelper.getMetaClass(instance)
        if (includeMetaClassImplMethods || !(metaclass instanceof MetaClassImpl)) {
            metaclass.metaMethods.each { MetaMethod mmit ->
                if (acceptName(mmit.name, prefix)) {
                    rv << mmit.getName() + (mmit.parameterTypes.length == 0 ? '()' : '(')
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
    static Collection<ReflectionCompletionCandidate> getPublicFieldsAndMethods(final Object instance, final String prefix) {
        Set<ReflectionCompletionCandidate> rv = new HashSet<ReflectionCompletionCandidate>()
        Class clazz = instance.getClass()
        if (clazz == null) {
            return rv
        }

        boolean isClass = (clazz == Class)
        if (isClass) {
            clazz = instance as Class
        }

        Class loopclazz = clazz
        // render immediate class members bold when completing an instance
        boolean renderBold = !isClass
        // hide static members for instances unless user typed a prefix
        boolean showStatic = isClass || (prefix.length() >= Integer.valueOf(Preferences.get(Groovysh.METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY, '3')))
        while (loopclazz != null && loopclazz != Object && loopclazz != GroovyObject) {
            addClassFieldsAndMethods(loopclazz, showStatic, !isClass, prefix, rv, renderBold)
            renderBold = false
            loopclazz = loopclazz.superclass
        }
        if (clazz.isArray() && !isClass) {
            // Arrays are special, these public members cannot be found via Reflection
            for (String member : ['length', 'clone()']) {
                if (member.startsWith(prefix)) {
                    rv.add(new ReflectionCompletionCandidate(member, Ansi.Attribute.INTENSITY_BOLD.name()))
                }
            }
        }

        // other completions that are commonly possible with properties
        if (!isClass) {
            Set<String> candidates = new HashSet<String>()
            PROPERTIES_COMPLETER.addCompletions(instance, prefix, candidates)
            rv.addAll(candidates.collect({ String it -> new ReflectionCompletionCandidate(it, AnsiRenderer.Code.MAGENTA.name()) }))
        }

        return rv.sort()
    }

    /**
     * removes candidates that, most of the times, a programmer does not want to see in completion
     * @param candidates
     */
    static removeStandardMethods(final Collection<ReflectionCompletionCandidate> candidates) {
        for (String defaultMethod : [
                'clone()', 'finalize()', 'getClass()',
                'getMetaClass()', 'getProperty(', 'invokeMethod(', 'setMetaClass(', 'setProperty(',
                'equals(', 'hashCode()', 'toString()',
                'notify()', 'notifyAll()', 'wait(', 'wait()']) {
            for (ReflectionCompletionCandidate candidate : candidates) {
                if (defaultMethod.equals(candidate.value)) {
                    candidates.remove(candidate)
                    break
                }
            }
        }
    }

    /**
     * Offering all DefaultGroovyMethods on any object is too verbose, hiding all
     * removes user-friendliness. So here util methods will be added to candidates
     * if the instance is of a suitable type.
     * This does not need to be strictly complete, only the most useful functions may appear.
     */
    static List<String> getDefaultMethods(final Object instance, final String prefix) {
        List<String> candidates = []
        if (instance instanceof Iterable) {
            [
                    'any()', 'any(',
                    'collect()', 'collect(',
                    'combinations()',
                    'count(',
                    'countBy(',
                    'drop(', 'dropRight(', 'dropWhile(',
                    'each()', 'each(',
                    'eachPermutation(',
                    'every()', 'every(',
                    'find(', 'findResult(', 'findResults(',
                    'flatten()',
                    'init()',
                    'inject(',
                    'intersect(',
                    'join(',
                    'max()', 'min()',
                    'reverse()',
                    'size()',
                    'sort()',
                    'split(',
                    'take(', 'takeRight(', 'takeWhile(',
                    'toSet()',
                    'retainAll(', 'removeAll(',
                    'unique()', 'unique('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
            if (instance instanceof Collection) {
                [
                        'grep('
                ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
            }
            if (instance instanceof List) {
                [
                        'collate(',
                        'execute()', 'execute(',
                        'pop()',
                        'transpose()'
                ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
            }
        }
        if (instance instanceof Map) {
            [
                    'any(',
                    'collect(',
                    'collectEntries(',
                    'collectMany(',
                    'count(',
                    'drop(',
                    'each(',
                    'every(',
                    'find(', 'findAll(', 'findResult(', 'findResults(',
                    'groupEntriesBy(', 'groupBy(',
                    'inject(', 'intersect(',
                    'max(', 'min(',
                    'sort(',
                    'spread()',
                    'subMap(',
                    'take(', 'takeWhile('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof File) {
            [
                    'append(',
                    'createTempDir()', 'createTempDir(',
                    'deleteDir()', 'directorySize()',
                    'eachByte(', 'eachDir(', 'eachDirMatch(', 'eachDirRecurse(', 'eachFile(', 'eachFileMatch(', 'eachFileRecurse(', 'eachLine(',
                    'filterLine(',
                    'getBytes()', 'getText()', 'getText(',
                    'newInputStream()', 'newOutputStream()', 'newPrintWriter()', 'newPrintWriter(', 'newReader()', 'newReader(', 'newWriter()', 'newWriter(',
                    'readBytes()', 'readLines(',
                    'setBytes(', 'setText(', 'size()', 'splitEachLine(',
                    'traverse(',
                    'withInputStream(', 'withOutputStream(', 'withPrintWriter(', 'withReader(', 'withWriter(', 'withWriterAppend(', 'write('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof String) {
            [
                    'capitalize()', 'center(', 'collectReplacements(', 'count(',
                    'decodeBase64()', 'decodeHex()', 'denormalize()',
                    'eachLine(', 'eachMatch(', 'execute()', 'execute(',
                    'find(', 'findAll(',
                    'isAllWhitespace()', 'isBigDecimal()', 'isBigInteger()', 'isDouble()', 'isFloat()', 'isInteger()', 'isLong()', 'isNumber()',
                    'normalize()',
                    'padLeft(', 'padRight(',
                    'readLines()', 'reverse()',
                    'size()', 'splitEachLine(', 'stripIndent(', 'stripMargin(',
                    'toBigDecimal()', 'toBigInteger()', 'toBoolean()', 'toCharacter()', 'toDouble()', 'toFloat()', 'toInteger()',
                    'toList()', 'toLong()', 'toSet()', 'toShort()', 'toURI()', 'toURL()',
                    'tokenize(', 'tr('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof URL) {
            [
                    'eachLine(',
                    'filterLine(',
                    'getBytes()', 'getBytes(', 'getText()', 'getText(',
                    'newInputStream()', 'newInputStream(', 'newReader()', 'newReader(',
                    'readLines()', 'readLines(',
                    'splitEachLine(',
                    'withInputStream(', 'withReader('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof InputStream) {
            [
                    'eachLine(',
                    'filterLine(',
                    'getBytes()', 'getText()', 'getText(',
                    'newReader()', 'newReader(',
                    'readLines()', 'readLines(',
                    'splitEachLine(',
                    'withReader(', 'withStream('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof OutputStream) {
            [
                    'newPrintWriter()', 'newWriter()', 'newWriter(',
                    'setBytes(',
                    'withPrintWriter(', 'withStream(', 'withWriter('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        if (instance instanceof Number) {
            [
                    'abs()',
                    'downto(',
                    'times(',
                    'power(',
                    'upto('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        Class clazz = instance.getClass()
        if (clazz != null && clazz != Class && clazz.isArray()) {
            [
                    'any()', 'any(',
                    'collect()', 'collect(',
                    'count(',
                    'countBy(',
                    'drop(', 'dropRight(', 'dropWhile(',
                    'each()', 'each(',
                    'every()', 'every(',
                    'find(', 'findResult(',
                    'flatten()',
                    'init()',
                    'inject(',
                    'join(',
                    'max()', 'min()',
                    'reverse()',
                    'size()',
                    'sort()',
                    'split(',
                    'take(', 'takeRight(', 'takeWhile('
            ].findAll({ it.startsWith(prefix) }).each({ candidates.add(it) })
        }
        return candidates
    }

    private static Collection<ReflectionCompletionCandidate> addClassFieldsAndMethods(final Class clazz,
                                                                                      final boolean includeStatic,
                                                                                      final boolean includeNonStatic,
                                                                                      final String prefix,
                                                                                      final Collection<ReflectionCompletionCandidate> rv,
                                                                                      final boolean renderBold) {

        Field[] fields = (includeStatic && !includeNonStatic) ? clazz.fields : clazz.getDeclaredFields()
        fields.each { Field fit ->
            if (acceptName(fit.name, prefix)) {
                int modifiers = fit.getModifiers()
                if (Modifier.isPublic(modifiers) && (Modifier.isStatic(modifiers) ? includeStatic : includeNonStatic)) {
                    if (!clazz.isEnum()
                            || !(!includeStatic && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) && fit.type == clazz)) {
                        ReflectionCompletionCandidate candidate = new ReflectionCompletionCandidate(fit.name)
                        if (!Modifier.isStatic(modifiers)) {
                            if (renderBold) {
                                candidate.jAnsiCodes.add(Ansi.Attribute.INTENSITY_BOLD.name())
                            }
                        }
                        rv << candidate
                    }
                }
            }
        }
        Method[] methods = (includeStatic && !includeNonStatic) ? clazz.methods : clazz.getDeclaredMethods()
        for (Method methIt : methods) {
            String name = methIt.getName()
            if (name.startsWith("super\$")) {
                name = name.substring(name.find("^super\\\$.*\\\$").length())
            }
            int modifiers = methIt.getModifiers()
            if (Modifier.isPublic(modifiers) && (Modifier.isStatic(modifiers) ? includeStatic : includeNonStatic)) {
                boolean fieldnameSuggested = false
                // bean fieldname can be used instead of accessor, tidies up completion candidates
                // the same goes for static fields // accessors
                if (name.matches(BEAN_ACCESSOR_PATTERN)) {
                    String fieldname = getFieldnameForAccessor(name, methIt.parameterTypes.length)
                    if (fieldname != null && fieldname != 'metaClass' && fieldname != 'property') {
                        if (acceptName(fieldname, prefix)) {
                            fieldnameSuggested = true
                            ReflectionCompletionCandidate fieldCandidate = new ReflectionCompletionCandidate(fieldname)
                            if (!rv.contains(fieldCandidate)) {
                                if (!Modifier.isStatic(modifiers) && renderBold) {
                                    fieldCandidate.jAnsiCodes.add(Ansi.Attribute.INTENSITY_BOLD.name())
                                }

                                rv.add(fieldCandidate)
                            }
                        }
                    }
                }
                if (!fieldnameSuggested && acceptName(name, prefix)) {
                    ReflectionCompletionCandidate candidate = new ReflectionCompletionCandidate(name + (methIt.parameterTypes.length == 0 ? '()' : '('))
                    if (!Modifier.isStatic(modifiers) && renderBold) {
                        candidate.jAnsiCodes.add(Ansi.Attribute.INTENSITY_BOLD.name())
                    }
                    rv.add(candidate)
                }
            }
        }

        for (Class interface_ : clazz.getInterfaces()) {
            addClassFieldsAndMethods(interface_, includeStatic, includeNonStatic, prefix, rv, false)
        }
    }

    static CharSequence getFieldnameForAccessor(String accessor, int parameterLength) {
        String fieldname = null
        if (accessor.startsWith('get')) {
            if (parameterLength == 0) {
                fieldname = accessor.substring(3)
            }
        } else if (accessor.startsWith('set')) {
            if (parameterLength == 1) {
                fieldname = accessor.substring(3)
            }
        } else if (accessor.startsWith('is')) {
            if (parameterLength == 0) {
                fieldname = accessor.substring(2)
            }
        } else {
            throw new IllegalStateException('getFieldnameForAccessor called with invalid accessor : ' + accessor)
        }
        if (fieldname == null) {
            return null
        }
        return fieldname[0].toLowerCase() + fieldname.substring(1)
    }
}
