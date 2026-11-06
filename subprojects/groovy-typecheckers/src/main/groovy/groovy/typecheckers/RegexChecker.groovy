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

import groovy.util.regex.RegexGuard
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
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
 * Named groups are checked the same way when the name is a constant:
 * <pre>
 *     Matcher m = 'foobar' =~ /(?&lt;first&gt;...)(?&lt;second&gt;...)/
 *     assert m.find()
 *     assert m.group('first') == 'foo'  // okay
 *     assert m.group('third')           // type error: no such named group
 * </pre>
 * The group index or name passed to {@code start} and {@code end} is checked in the same way.
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
 *
 * @since 4.0.0
 */
class RegexChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    private static final ClassNode MATCHER_TYPE = ClassHelper.make(Matcher)
    private static final ClassNode REGEX_GUARD_TYPE = ClassHelper.make(RegexGuard)
    private static final String REGEX_GROUP_COUNT = RegexChecker.getSimpleName() + '_INFERRED_GROUP_COUNT'
    private static final String REGEX_STRING = RegexChecker.getSimpleName() + '_INFERRED_REGEX'
    private static final String REGEX_MATCHER_RESULT_TYPE = RegexChecker.getSimpleName() + '_MATCHER_RESULT_INFERRED_TYPE'
    // the usual spellings of a named-capturing group opener, including whitespace in COMMENTS mode;
    // used only to list names in messages, as COMMENTS mode allows other spellings
    private static final Pattern GROUP_NAME = ~/\(\?<\s*([a-zA-Z](?:\s*[a-zA-Z0-9])*)\s*>/
    // String-receiver methods whose first argument is a regex; covers both the JDK methods
    // (matches, split, replaceAll, replaceFirst) and the CharSequence extension methods
    private static final List<String> REGEX_ARG_METHODS = [
        'matches', 'find', 'findAll', 'findGroups', 'findAllGroups',
        'eachMatch', 'replaceAll', 'replaceFirst', 'split', 'splitEachLine'
    ].asImmutable()

    /**
     * Registers regex validation and matcher type-inference hooks for visited methods.
     */
    @Override
    Object run() {
        beforeVisitMethod { MethodNode method ->
            method.code?.visit(new CheckingVisitor() {
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
                        if (expression.leftExpression instanceof VariableExpression) {
                            def var = findTargetVariable(expression.leftExpression)
                            def groupCount = regexMetaData(var, REGEX_GROUP_COUNT)
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
                    if (call.objectExpression instanceof ClassExpression) {
                        checkPatternMethod(call, call.objectExpression.type)
                    } else if (isString(call.receiver) && call.methodAsString in REGEX_ARG_METHODS && call.arguments.expressions) {
                        def exp = findConstExp(call.arguments.getExpression(0), String)
                        checkRegex(exp, call)
                        if (call.methodAsString == 'findGroups' && call.getNodeMetaData(REGEX_GROUP_COUNT) == null) {
                            // no constant regex string found directly, but a pattern-operator
                            // argument or a pattern variable may still carry a group count
                            def arg = call.arguments.getExpression(0)
                            def source = arg instanceof VariableExpression ? findTargetVariable(arg) : arg
                            def groupCount = regexMetaData(source, REGEX_GROUP_COUNT)
                            if (groupCount != null) {
                                call.putNodeMetaData(REGEX_GROUP_COUNT, groupCount)
                            }
                        }
                    } else if (isPattern(call.receiver) && call.methodAsString == 'matcher') {
                        def source = call.receiver instanceof VariableExpression ? findTargetVariable(call.receiver) : call.receiver
                        copyRegexMetaData(source, call)
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
                    } else if (classType == REGEX_GUARD_TYPE && arguments.expressions) {
                        // deadline-guarded evaluation: matches/matcher (typically written by hand)
                        // take the pattern first; the operator-shaped matchRegex/findRegex hooks
                        // (generated by @SafeRegex from the regex operators) take the input first
                        if (call.methodAsString in ['matches', 'matcher']) {
                            checkRegex(findConstExp(arguments.getExpression(0), String), call)
                        } else if (call.methodAsString in ['matchRegex', 'findRegex'] && arguments.expressions.size() > 1) {
                            checkRegex(findConstExp(arguments.getExpression(1), String), call)
                        }
                    }
                }

                @Override
                void visitDeclarationExpression(DeclarationExpression decl) {
                    super.visitDeclarationExpression(decl)
                    if (decl.variableExpression != null) {
                        if (decl.rightExpression instanceof ConstantExpression) {
                            localConstVars.put(decl.variableExpression, decl.rightExpression)
                        }
                        copyRegexMetaData(decl.rightExpression, decl.variableExpression)
                    } else if (decl.isMultipleAssignmentDeclaration() && decl.rightExpression instanceof MethodCallExpression
                            && decl.rightExpression.methodAsString == 'findGroups') {
                        // findGroups yields the full match plus one element per group; more
                        // variables than that can never all be bound, even on a successful match
                        def groupCount = decl.rightExpression.getNodeMetaData(REGEX_GROUP_COUNT)
                        if (groupCount != null) {
                            int numVars = decl.tupleExpression.expressions.size()
                            if (numVars > groupCount + 1) {
                                addStaticTypeError('Too many variables ' + numVars + ' for findGroups: a regex with ' + groupCount +
                                        ' group' + (groupCount == 1 ? '' : 's') + ' yields at most ' + (groupCount + 1) +
                                        ' elements (the full match plus each group)', decl)
                            }
                        }
                    }
                }
            })
        }

        incompatibleAssignment { lhsType, rhsType, expr ->
            if (expr instanceof BinaryExpression && isAssignment(expr.operation.type)) {
                def from = expr.rightExpression
                if (from instanceof BinaryExpression && from.operation.type == Types.LEFT_SQUARE_BRACKET && getType(from.leftExpression) == MATCHER_TYPE) {
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

        methodNotFound { receiverType, name, argList, argTypes, MethodCall call ->
            def receiver = call.receiver
            if (receiver instanceof BinaryExpression && receiver.operation.type == Types.LEFT_SQUARE_BRACKET && getType(receiver.leftExpression) == MATCHER_TYPE) {
                ClassNode inferred = receiver.getNodeMetaData(REGEX_MATCHER_RESULT_TYPE)
                if (inferred) {
                    makeDynamic(call, inferred)
                }
            }
        }

        afterVisitMethod { MethodNode method ->
            method.code?.visit(new CheckingVisitor() {
                @Override
                void visitDeclarationExpression(DeclarationExpression decl) {
                    super.visitDeclarationExpression(decl)
                    if (decl.variableExpression != null) {
                        if (decl.rightExpression instanceof ConstantExpression) {
                            localConstVars.put(decl.variableExpression, decl.rightExpression)
                        }
                        copyRegexMetaData(decl.rightExpression, decl.variableExpression)
                    }
                }

                @Override
                void visitMethodCallExpression(MethodCallExpression call) {
                    if (isPattern(call.receiver) && call.methodAsString == 'matcher') {
                        def source = call.receiver instanceof VariableExpression ? findTargetVariable(call.receiver) : call.receiver
                        copyRegexMetaData(source, call)
                    }
                    super.visitMethodCallExpression(call)
                    if (call.objectExpression instanceof VariableExpression && call.methodAsString in ['group', 'start', 'end'] && isMatcher(call.receiver) && call.arguments.expressions) {
                        def var = findTargetVariable(call.receiver)
                        def maxCnt = regexMetaData(var, REGEX_GROUP_COUNT)
                        if (maxCnt != null) {
                            def arg = call.arguments.getExpression(0)
                            def cnt = findConstExp(arg, Integer)?.value
                            if (cnt != null) {
                                if (cnt > maxCnt) {
                                    addStaticTypeError('Invalid group count ' + cnt + ' for regex with ' + maxCnt + ' group' + (maxCnt == 1 ? '' : 's'), call)
                                }
                            } else {
                                // a group name: it must be one the regex defines
                                def name = findConstExp(arg, String)?.value
                                String regex = regexMetaData(var, REGEX_STRING)
                                if (name != null && regex != null && !definesGroup(regex, name)) {
                                    def names = findGroupNames(regex)
                                    String known = names ? 'named group' + (names.size() == 1 ? ' ' : 's ') + names.join(', ') : 'no named groups'
                                    addStaticTypeError("Invalid group name '" + name + "' for regex with " + known, call)
                                }
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
                            def cnt = findConstExp(expression.rightExpression, Integer)?.value
                            if (cnt != null && cnt > maxCnt) {
                                addStaticTypeError('Invalid group count ' + cnt + ' for regex with ' + maxCnt + ' group' + (maxCnt == 1 ? '' : 's'), expression)
                            }
                        }
                    }
                }
            })
        }
    }

    //--------------------------------------------------------------------------

    private boolean isMatcher(Expression exp) {
        exp.type == MATCHER_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == MATCHER_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == MATCHER_TYPE
    }

    private boolean isString(Expression exp) {
        exp.type == STRING_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == STRING_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == STRING_TYPE
    }

    private boolean isPattern(Expression exp) {
        exp.type == PATTERN_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == PATTERN_TYPE
            || exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == PATTERN_TYPE
    }

    private void checkRegex(ConstantExpression regex, Expression target) {
        if (regex == null) return
        try {
            def pattern = Pattern.compile(regex.value)
            Matcher m = pattern.matcher('')
            target.putNodeMetaData(REGEX_GROUP_COUNT, m.groupCount())
            target.putNodeMetaData(REGEX_STRING, regex.value)
        } catch (PatternSyntaxException ex) {
            String additional = regex.lineNumber != target.lineNumber ?
                " @ line $regex.lineNumber, column $regex.columnNumber: " : ': '
            addStaticTypeError('Bad regex' + additional + ex.message, target)
        }
    }

    /**
     * Checks whether a regex that is known to compile defines a named-capturing group. A named
     * backreference to an undefined group does not compile, so the JDK answers exactly, whatever
     * the spelling of the group in COMMENTS mode; the newline ends a trailing comment. A regex
     * whose unterminated quote swallows the backreference reports every name as defined.
     * A name that is not a legal group name is never defined; it is rejected before probing, as
     * COMMENTS mode would otherwise ignore whitespace within it and other characters could end
     * the backreference early.
     */
    private static boolean definesGroup(String regex, String name) {
        if (!(name ==~ /[a-zA-Z][a-zA-Z0-9]*/)) return false
        try {
            Pattern.compile(regex + '\n\\k<' + name + '>')
            true
        } catch (PatternSyntaxException ignore) {
            false
        }
    }

    /**
     * Lists the named-capturing groups of a regex that is known to compile, for use in messages.
     * The text scan finds the usual spellings of a group opener; each candidate is confirmed with
     * {@link #definesGroup} so openers in quoted text or character classes are left out.
     */
    private static List<String> findGroupNames(String regex) {
        List<String> names = []
        Matcher m = GROUP_NAME.matcher(regex)
        while (m.find()) {
            names << m.group(1).replaceAll(/\s+/, '')
        }
        names.unique().findAll { definesGroup(regex, it) }
    }

    /**
     * Returns the regex metadata recorded on a node, or {@code null} when there is none. A variable that
     * resolves to something other than an AST node, such as a script binding variable, is not tracked.
     */
    private static Object regexMetaData(source, String key) {
        source instanceof ASTNode ? source.getNodeMetaData(key) : null
    }

    private static void copyRegexMetaData(source, target) {
        for (String key : [REGEX_GROUP_COUNT, REGEX_STRING]) {
            def value = regexMetaData(source, key)
            if (value != null) {
                target.putNodeMetaData(key, value)
            }
        }
    }
}
