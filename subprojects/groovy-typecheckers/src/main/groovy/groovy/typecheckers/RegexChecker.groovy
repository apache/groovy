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
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

import static org.codehaus.groovy.ast.ClassHelper.PATTERN_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.syntax.Types.isAssignment
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.checkCompatibleAssignmentTypes

/**
 * Checks at compile-time for cases of invalid regex usage where the actual regex string can be identified
 * (e.g. inline or defined by a local variable with an initial value or a field with an initial value).
 * A number of errors which would normally surface only at runtime are handled.
 * <ul>
 * <li>
 * Invalid pattern definitions when using the Groovy pattern operator or the JDK's {@code Pattern#compile} method:
 * <pre>
 *     ~/\w{3/               // missing closing repetition quantifier brace
 *     ~"(.)o(.*"            // missing closing group bracket
 *     Pattern.compile(/?/)  // dangling meta character '?' (Java longhand)
 * </pre>
 * These examples illustrate explicitly defined constant strings but local variable
 * or field definitions where a constant string can be determined are also checked.
 * </li>
 * <li>
 * Invalid regex strings in conjunction with Groovy's regex find and regex match expressions, or the JDK's {@code Pattern#matches} method:
 * <pre>
 *     'foobar'  =~ /f[o]{2/        // missing closing repetition quantifier brace
 *     'foobar' ==~ /(foo/          // missing closing group bracket
 *     Pattern.matches(/?/, 'foo')  // dangling meta character '?' (Java longhand)
 * </pre>
 * </li>
 * Invalid group count terms where the regex string can be determined and the group index can
 * be determined to be a constant.
 * <li>
 * <pre>
 *     def m = 'foobar' =~ /(...)(...)/
 *     assert m[0][1] == 'foo'     // okay
 *     assert m[0][3]              // type error: only two groups in regex
 * </pre>
 * And similarly for Java long-hand variants:
 * <pre>
 *     Pattern p = Pattern.compile('(...)(...)')
 *     Matcher m = p.matcher('foobar')
 *     assert m.find()
 *     assert m.group(1) == 'foo'  // okay
 *     assert m.group(3)           // type error: only two groups in regex
 * </pre>
 * </li>
 * </ul>
 *
 * Also, when using the regex find operator, smarter type inferencing occurs.
 * For an expression like {@code matcher[0]}, the {@code getAt} extension method for {@code Matcher}
 * is called. This may return a String (if no groups occur within the regex) or a list of String values
 * if grouping is used, hence the declared return type of the mentioned {@code getAt} method is {@code Object}
 * to account for these two possibilities. When using {@code RegexChecker}, the inferred type will be either
 * {@code String} or {@code List&lt;String&gt;} when a regex string can be identified.
 *
 * Over time, the idea would be to support more cases as per:
 * https://checkerframework.org/manual/#regex-checker
 * https://homes.cs.washington.edu/~mernst/pubs/regex-types-ftfjp2012.pdf
 * https://github.com/typetools/checker-framework/tree/master/checker/src/main/java/org/checkerframework/checker/regex
 */
@Incubating
class RegexChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {
    private static final String REGEX_GROUP_COUNT = RegexChecker.simpleName + "_INFERRED_GROUP_COUNT"
    private static final String REGEX_MATCHER_RESULT_TYPE = RegexChecker.simpleName + "_MATCHER_RESULT_INFERRED_TYPE"
    private static final ClassNode MATCHER_TYPE = ClassHelper.make(Matcher)

    @Override
    Object run() {
        beforeVisitMethod { MethodNode method ->
            def visitor = new CheckingVisitor() {
                @Override
                void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
                    super.visitBitwiseNegationExpression(expression)
                    def exp = findConstExp(expression.expression, String)
                    checkRegex(exp, expression)
                }

                @Override
                void visitBinaryExpression(BinaryExpression expression) {
                    super.visitBinaryExpression(expression)
                    if (expression.operation.type in [Types.FIND_REGEX, Types.MATCH_REGEX]) {
                        def exp = findConstExp(expression.rightExpression, String)
                        checkRegex(exp, expression)
                    } else if (expression.operation.type == Types.LEFT_SQUARE_BRACKET) {
                        if (isVariableExpression(expression.leftExpression)) {
                            def var = findTargetVariable(expression.leftExpression)
                            def groupCount = var?.getNodeMetaData(REGEX_GROUP_COUNT)
                            if (groupCount != null) {
                                expression.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                                if (groupCount == 0) {
                                    expression.putNodeMetaData(REGEX_MATCHER_RESULT_TYPE, STRING_TYPE)
                                } else {
                                    expression.putNodeMetaData(REGEX_MATCHER_RESULT_TYPE, buildListType(STRING_TYPE.plainNodeReference))
                                }
                            }
                        }
                    }
                }

                @Override
                void visitMethodCallExpression(MethodCallExpression call) {
                    super.visitMethodCallExpression(call)
                    if (isClassExpression(call.objectExpression)) {
                        checkPatternMethod(call, call.objectExpression.type)
                    } else if (isPattern(call.receiver) && call.methodAsString == 'matcher') {
                        def var = findTargetVariable(call.receiver)
                        def groupCount = var?.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (groupCount != null) {
                            call.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                        }
                    }
                }

                @Override
                void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                    super.visitStaticMethodCallExpression(call)
                    checkPatternMethod(call, call.ownerType)
                }

                private void checkPatternMethod(MethodCall call, ClassNode classType) {
                    def arguments = call.arguments
                    if (classType == PATTERN_TYPE && call.methodAsString in ['compile', 'matches'] && arguments.expressions) {
                        def exp = findConstExp(arguments.getExpression(0), String)
                        checkRegex(exp, call)
                    }
                }

                @Override
                void visitDeclarationExpression(DeclarationExpression decl) {
                    super.visitDeclarationExpression(decl)
                    if (decl.variableExpression != null) {
                        if (isConstantExpression(decl.rightExpression)) {
                            localConstVars.put(decl.variableExpression, decl.rightExpression)
                        }
                        def groupCount = decl.rightExpression.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (groupCount != null) {
                            decl.variableExpression.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                        }
                    }
                }

            }
            method.code.visit(visitor)
        }

        incompatibleAssignment { lhsType, rhsType, expr ->
            if (isBinaryExpression(expr) && isAssignment(expr.operation.type)) {
                def from = expr.rightExpression
                if (isBinaryExpression(from) && from.operation.type == Types.LEFT_SQUARE_BRACKET && getType(from.leftExpression) == MATCHER_TYPE) {
                    ClassNode inferred = from.getNodeMetaData(REGEX_MATCHER_RESULT_TYPE)
                    if (inferred) {
                        handled = true
                        if (checkCompatibleAssignmentTypes(lhsType, inferred, from)) {
                            storeType(expr, inferred)
                        } else {
                            addStaticTypeError('Cannot assign value of type ' + inferred + ' to variable of type ' + lhsType, expr)
                        }
                    }
                }
            }
        }

        methodNotFound { receiverType, name, argList, argTypes, call ->
            def receiver = call.receiver
            if (isBinaryExpression(receiver) && receiver.operation.type == Types.LEFT_SQUARE_BRACKET && getType(receiver.leftExpression) == MATCHER_TYPE) {
                ClassNode inferred = receiver.getNodeMetaData(REGEX_MATCHER_RESULT_TYPE)
                if (inferred) {
                    makeDynamic(call, inferred)
                }
            }
        }

        afterVisitMethod { MethodNode method ->
            def visitor = new CheckingVisitor() {
                @Override
                void visitDeclarationExpression(DeclarationExpression decl) {
                    super.visitDeclarationExpression(decl)
                    if (decl.variableExpression != null) {
                        if (isConstantExpression(decl.rightExpression)) {
                            localConstVars.put(decl.variableExpression, decl.rightExpression)
                        }
                        def groupCount = decl.rightExpression.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (groupCount != null) {
                            decl.variableExpression.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                        }
                    }
                }

                @Override
                void visitMethodCallExpression(MethodCallExpression call) {
                    if (isPattern(call.receiver) && call.methodAsString == 'matcher') {
                        def var = findTargetVariable(call.receiver)
                        def groupCount = var?.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (groupCount != null) {
                            call.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                        }
                    }
                    super.visitMethodCallExpression(call)
                    if (isVariableExpression(call.objectExpression) && call.methodAsString == 'group' && isMatcher(call.receiver) && call.arguments.expressions) {
                        def var = findTargetVariable(call.receiver)
                        def maxCnt = var?.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (maxCnt != null) {
                            def cnt = findConstExp(call.arguments.getExpression(0), Integer).value
                            if (cnt > maxCnt) {
                                addStaticTypeError("Invalid group count " + cnt + " for regex with " + maxCnt + " group" + (maxCnt == 1 ? "" : "s"), call)
                            }
                        }
                    }
                }

                @Override
                void visitBinaryExpression(BinaryExpression expression) {
                    super.visitBinaryExpression(expression)
                    if (expression.operation.type == Types.LEFT_SQUARE_BRACKET) {
                        def maxCnt = expression.leftExpression?.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (maxCnt != null) {
                            def cnt = findConstExp(expression.rightExpression, Integer).value
                            if (cnt > maxCnt) {
                                addStaticTypeError("Invalid group count " + cnt + " for regex with " + maxCnt + " group" + (maxCnt == 1 ? "" : "s"), expression)
                            }
                        }
                    }
                }
            }
            method.code.visit(visitor)
        }
    }

    private boolean isMatcher(Expression obj) {
        obj.type == MATCHER_TYPE ||
                obj.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == MATCHER_TYPE ||
                obj.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == MATCHER_TYPE
    }

    private boolean isPattern(Expression obj) {
        obj.type == PATTERN_TYPE ||
                obj.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == PATTERN_TYPE ||
                obj.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == PATTERN_TYPE
    }

    private void checkRegex(ConstantExpression regex, Expression target) {
        if (regex == null) return
        try {
            def pattern = Pattern.compile(regex.value)
            Matcher m = pattern.matcher('')
            target.putNodeMetaData(REGEX_GROUP_COUNT, m.groupCount())
        } catch (PatternSyntaxException ex) {
            String additional = regex.lineNumber != target.lineNumber ?
                " @ line $regex.lineNumber, column $regex.columnNumber: " : ": "
            addStaticTypeError("Bad regex" + additional + ex.message, target)
        }
    }

}
