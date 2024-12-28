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
package groovy.typecheckers

import org.apache.groovy.lang.annotation.Incubating
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.macro.matcher.ASTMatcher
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport

import java.time.temporal.TemporalAccessor

import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.getWrapper
import static org.codehaus.groovy.ast.ClassHelper.makeCached

/**
 * Checks at compile-time for incorrect format strings in format methods such as {@code printf} and {@code String.format}.
 * A format method’s specification requires that:
 * <ul>
 • <li>The format string’s syntax is valid</li>
 • <li>The correct number of arguments is passed</li>
 • <li>Each argument has the appropriate type</li>
 * </ul>
 * <ul>
 * <li>
 * Example invalid format strings include:
 * <pre>
 *     String.format('%y', 7)                 // unknown conversion: 'y'
 *     sprintf('%d', 'some string')           // illegal conversion: d != java.lang.String
 *     printf("%d %s", 7)                     // missing argument: '%s'
 *     System.out.printf('%-tT', new Date())  // missing width
 *     String.format(Locale.US, '%#b', true)  // invalid flag
 * </pre>
 * </li>
 * </ul>
 *
 * Modelled on the checker framework checker with the same name:
 * https://checkerframework.org/manual/#formatter-checker
 * https://homes.cs.washington.edu/~mernst/pubs/format-string-issta2014-abstract.html
 * https://github.com/typetools/checker-framework/tree/master/checker/src/main/java/org/checkerframework/checker/formatter
 */
@Incubating
class FormatStringChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {
    private static final ClassNode LOCALE_TYPE = makeCached(Locale)
    private static final ClassNode FORMATTER_TYPE = makeCached(Formatter)
    private String formatSpecifier = /%(\d+\$)?([-#+ 0,(\<]*)?(\d+)?(\.\d+)?([tT])?([a-zA-Z%])/
    private List<ASTNode> formatMethods = [
        macro(CompilePhase.SEMANTIC_ANALYSIS) { String.format(a) }.withConstraints { varargPlaceholder a },
        macro { _.format(a) }.withConstraints { varargPlaceholder a },
        macro { sprintf(a) }.withConstraints { varargPlaceholder a },
        macro { _.sprintf(a) }.withConstraints { varargPlaceholder a },
        macro { printf(a) }.withConstraints{ varargPlaceholder a },
        macro { _.printf(a) }.withConstraints{ varargPlaceholder a },
        macro { _.formatted(a) }.withConstraints{ varargPlaceholder a }
    ]

    @Override
    Object run() {
        afterMethodCall { call ->
            def method = getTargetMethod(call)
            def annoNames = method.annotations*.classNode*.name
            if (annoNames.any { name ->
                name in [
                    'groovy.typecheckers.FormatMethod',
                    'org.checkerframework.checker.formatter.qual.FormatMethod'
                ]
            }) {
                makeVisitor().checkFormatterMethod(call)
            }
        }
        afterVisitMethod { MethodNode method ->
            def visitor = makeVisitor()
            method.code.visit(visitor)
        }
    }

    private makeVisitor() {
        new CheckingVisitor() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                if (formatMethods.any { ASTMatcher.matches(call, it) }) {
                    checkFormatterMethod(call)
                }
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                if (formatMethods.any { ASTMatcher.matches(call, it) }) {
                    checkFormatterMethod(call)
                }
            }

            void checkFormatterMethod(MethodCall call) {
                def arguments = call.arguments
                if (arguments.size() > 0 && getType(arguments[0]) == LOCALE_TYPE) {
                    arguments = arguments.tail()
                }
                Expression formatString
                if (call.methodAsString == 'formatted') {
                    formatString = findConstExp(call.receiver, String)
                } else {
                    if (call.methodAsString == 'format') {
                        def callType = call.receiver instanceof ClassExpression ? call.receiver.type : getType(call.receiver)
                        if (callType !in [FORMATTER_TYPE, STRING_TYPE]) return
                    }
                    formatString = findConstExp(arguments[0], String)
                    arguments = arguments.tail()
                }
                if (formatString == null) return
                def vals = arguments.collect { findConstExp(it, Object) }
                def allArgsConstants = vals.every { it != null }
                if (allArgsConstants) {
                    checkFormatStringConstantArgs(formatString, vals*.value, call)
                } else {
                    checkFormatStringTypes(formatString, arguments, call)
                }
            }

            void checkFormatStringTypes(Expression expression, List<Expression> args, Expression target) {
                int next = 0
                int prevIndex = -1
                expression.value.eachMatch(formatSpecifier) { spec ->
                    def (all, argIndex, flags, width, precision, tgroup, conversion) = spec
                    def flagList = flags?.toList()
                    if (argIndex) {
                        argIndex -= '$'
                        argIndex = argIndex.toInteger()
                    }
                    int indexToUse = argIndex ?: next
                    if (flagList.contains('<')) {
                        if (prevIndex == -1) {
                            addStaticTypeError("MissingFormatArgument: Format specifier '$all'", target)
                            return
                        } else {
                            indexToUse = prevIndex
                        }
                    }
                    if (indexToUse >= args.size()) {
                        addStaticTypeError("MissingFormatArgument: Format specifier '$all'", target)
                        return
                    }
                    def arg = args[indexToUse]
                    Object type = getWrapper(getType(arg)).typeClass
                    if (tgroup) {
                        if (!'HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc'.contains(conversion)) {
                            addStaticTypeError("UnknownFormatConversion: Conversion = 't${conversion}'", target)
                        }
                        if (precision) {
                            addStaticTypeError("IllegalFormatPrecision: ${precision - '.'}", target)
                        }
                        if (!([Long, Calendar, Date, TemporalAccessor].any { it.isAssignableFrom(type) })) {
                            addStaticTypeError("IllegalFormatConversion: $conversion != $type.name", target)
                        }
                        checkBadFlags(flagList, conversion, target, '#+ 0,(')
                        if (flagList.contains('-') && !width) {
                            addStaticTypeError("MissingFormatWidth: $all", target)
                        }
                    } else {
                        def dupFlag = flagList.countBy().find { flag, count -> count > 1 }?.key
                        if (dupFlag) {
                            addStaticTypeError("DuplicateFormatFlags: Flags = '$dupFlag'", target)
                        }
                        switch (conversion) {
                            case '%':
                            case 'n':
                                break
                            case ['c', 'C']: // char type
                                if (type !in [Character, Byte, Short, Integer]) {
                                    addStaticTypeError("IllegalFormatConversion: $conversion != $type.name", target)
                                }
                                checkBadFlags(flagList, conversion, target, '#+ 0,(')
                                checkBadWidth(flagList, width, all, target)
                                break
                            case ['d', 'o', 'x', 'X']: // integral type
                                if (conversion == 'd') {
                                    checkBadFlags(flagList, conversion, target, '#')
                                } else {
                                    checkBadFlags(flagList, conversion, target, ',')
                                }
                                checkBadWidth(flagList, width, all, target)
                                if (type !in [Byte, Short, Integer, Long, BigInteger]) {
                                    addStaticTypeError("IllegalFormatConversion: $conversion != $type.name", target)
                                }
                                if (precision) {
                                    addStaticTypeError("IllegalFormatPrecision: ${precision - '.'}", target)
                                }
                                if ((flagList.contains('+') && flagList.contains(' '))
                                    || (flagList.contains('-') && flagList.contains('0'))) {
                                    addStaticTypeError("IllegalFormatFlags: Flags = '$flags'", target)
                                }
                                break
                            case ['e', 'E', 'f', 'g', 'G', 'a', 'A']: // float type
                                if (conversion in ['a', 'A']) {
                                    checkBadFlags(flagList, conversion, target, '(,')
                                } else if (conversion in ['e', 'E']) {
                                    checkBadFlags(flagList, conversion, target, ',')
                                } else if (conversion in ['g', 'G']) {
                                    checkBadFlags(flagList, conversion, target, '#')
                                }
                                checkBadWidth(flagList, width, all, target)
                                if (type !in [Float, Double, BigDecimal]) {
                                    addStaticTypeError("IllegalFormatConversion: $conversion != $type.name", target)
                                }
                                break
                            case ['b', 'B', 'h', 'H', 's', 'S']: // general type
                                if (conversion in ['b', 'B', 'h', 'H']) {
                                    checkBadFlags(flagList, conversion, target, '#')
                                }
                                checkBadFlags(flagList, conversion, target, '+ 0,(')
                                break
                            default:
                                addStaticTypeError("UnknownFormatConversion: Conversion = '${conversion}'", target)
                        }
                    }
                    prevIndex = indexToUse
                    if (!argIndex && !flagList.contains('<')) {
                        next++
                    }
                }
            }

            private checkBadWidth(flagList, width, all, Expression target) {
                if (flagList.contains('-') && !width) {
                    addStaticTypeError("MissingFormatWidth: $all", target)
                }
            }

            void checkBadFlags(flagList, conversion, Expression target, String badFlags) {
                def mismatched = flagList?.findAll { badFlags.contains(it) }.join()
                if (mismatched) {
                    addStaticTypeError("FormatFlagsConversionMismatch: Conversion = $conversion, Flags = '$mismatched'", target)
                }
            }

            void checkFormatStringConstantArgs(ConstantExpression formatString, args, Expression target) {
                try {
                    new Formatter().format(formatString.value, *args)
                } catch (IllegalFormatException ex) {
                    String additional = formatString.lineNumber != target.lineNumber ?
                        " @ line $formatString.lineNumber, column $formatString.columnNumber: " : ': '
                    addStaticTypeError("${ex.class.simpleName - 'Exception'}$additional$ex.message", target)
                }
            }

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                if (decl.variableExpression != null) {
                    if (isConstantExpression(decl.rightExpression)) {
                        localConstVars.put(decl.variableExpression, decl.rightExpression)
                    }
                }
            }
        }
    }
}
