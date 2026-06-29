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
package org.codehaus.groovy.macro.matcher

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.matcher.internal.MatchingConstraintsBuilder

import java.util.function.Predicate

/**
 * Matches AST nodes against pattern ASTs.
 *
 * @since 2.5.0
 */
@AutoFinal @CompileStatic
class ASTMatcher extends ContextualClassCodeVisitor {

    /**
     * Wildcard marker used in matcher patterns.
     */
    public static final String WILDCARD = '_'

    private boolean match = true
    private Object  current

    private ASTMatcher() {
    }

    /** Returns no source unit because AST matching is source-independent. */
    @Override
    protected SourceUnit getSourceUnit() {
    }

    /**
     * Matches an AST with another AST (pattern). It will return true if the AST matches
     * all the nodes from the pattern AST.
     * @param node the AST we want to match with
     * @param pattern the pattern AST we want to match to
     * @return true if this AST matches the pattern
     */
    static boolean matches(ASTNode node, ASTNode pattern) {
        ASTMatcher matcher = new ASTMatcher()
        matcher.current = node
        matcher.match = true
        if (pattern instanceof ClassNode) {
            matcher.visitClass(pattern)
        } else {
            pattern.visit(matcher)
        }
        return matcher.match
    }

    private boolean failIfNot(boolean value) {
        match = (match && value)
        return match
    }

    private static boolean matchByName(String patternText, String nodeText) {
        return nodeText.equals(patternText) || WILDCARD.equals(patternText)
    }

    private static boolean isWildcardExpression(Object exp) {
        return (exp instanceof VariableExpression && WILDCARD.equals(exp.getName())
            || (exp instanceof ConstantExpression && WILDCARD.equals(exp.getValue())))
    }

    /**
     * Locates all nodes in the given AST which match the pattern AST.
     * This operation can cost a lot, because it tries to match a sub-tree
     * to every node of the AST.
     * @param node an AST Node
     * @param pattern a pattern to be found somewhere in the AST
     * @return a list of {@link TreeContext}, always not null.
     */
    static List<TreeContext> find(ASTNode node, ASTNode pattern) {
        def finder = new ASTFinder(pattern)
        node.visit(finder)
        finder.matches
    }

    /**
     * Returns a {@link Predicate} that tests whether a node matches the supplied pattern AST. This
     * is the bridge between exemplar pattern matching and the fluent
     * {@link org.codehaus.groovy.ast.query.AstQuery} API, letting a pattern be used as a filter:
     * <pre>
     * import static org.codehaus.groovy.macro.matcher.ASTMatcher.matching
     * AstQuery.from(node).descendants(MethodCallExpression).where(matching(macro { _.foo() })).list()
     * </pre>
     * The predicate reports only whether the shape matches (honouring {@code _} wildcards); use
     * {@link #find(ASTNode, ASTNode)} when placeholder bindings need to be extracted.
     *
     * @param pattern the pattern AST to match candidate nodes against
     * @return a predicate that returns {@code true} when a node matches the pattern
     * @since 6.0.0
     */
    static Predicate<ASTNode> matching(ASTNode pattern) {
        { ASTNode node -> matches(node, pattern) } as Predicate<ASTNode>
    }

    private void storeContraints(ASTNode src) {
        def constraints = src.getNodeMetaData(MatchingConstraints)
        if (constraints) {
            treeContext.putUserdata(MatchingConstraints, constraints)
        }
    }

    /**
     * Executes the supplied closure with the current matching constraints as delegate.
     *
     * @param defaultValue the value to return when no constraints are available
     * @param code the code to execute with the current constraints
     * @return the closure result, or {@code defaultValue} when no constraints are present
     */
    def <T> T ifConstraint(T defaultValue, @DelegatesTo(value=MatchingConstraints, strategy=Closure.DELEGATE_FIRST) Closure<T> code) {
        def constraints = (List<MatchingConstraints>) treeContext.getUserdata(MatchingConstraints, true)
        if (constraints) {
            def clone = (Closure<T>) code.clone()
            clone.resolveStrategy = Closure.DELEGATE_FIRST
            clone.delegate = constraints[0]
            clone()
        } else {
            defaultValue
        }
    }

    private String findPlaceholder(Object exp) {
        ifConstraint(null) {
            if (exp instanceof VariableExpression && placeholders.contains(exp.name)) {
                return exp.name
            } else if (exp instanceof ConstantExpression && placeholders.contains(exp.value)) {
                return exp.value
            } else if (exp instanceof ArgumentListExpression && exp.size() == 1 && exp[0] instanceof VariableExpression) {
                // TODO currently matches exactly all args, consider allowing
                // other non-vararg nodes before the vararg one
                VariableExpression ve = exp[0] as VariableExpression
                if (varargPlaceholders.contains(ve.name)) {
                    return ve.name
                }
            }
            return null
        }
    }

    private void doWithNode(Object patternNode, Object foundNode, Closure cl) {
        Class expectedClass = patternNode ? patternNode.class : Object
        if (expectedClass == null) {
            expectedClass = Object
        }

        boolean doPush = (treeContext.node != foundNode && foundNode instanceof ASTNode)
        if (doPush) {
            pushContext((ASTNode)foundNode)
            if (patternNode instanceof ASTNode) {
                storeContraints(patternNode)
            }
        }

        if (!isWildcardExpression(patternNode)) {
            String placeholder = findPlaceholder(patternNode)
            if (placeholder) {
                def alreadySeenAST = treeContext.getUserdata("placeholder_$placeholder", true)
                if (alreadySeenAST == null) {
                    treeContext.parent.putUserdata("placeholder_$placeholder", foundNode)
                } else {
                    // during the tree inspection, placeholder already found
                    // so we need to check that they are identical
                    failIfNot(matches((ASTNode) alreadySeenAST[0], (ASTNode) foundNode))
                }
            } else if (match && (foundNode == null || expectedClass.isAssignableFrom(foundNode.class))) {
                Object old = current
                current = foundNode
                cl()
                current = old
            } else {
                failIfNot(false)
            }
        }
        if (doPush) {
            popContext()
        }
    }

    /** Matches a class node against the current candidate. */
    @Override
    void visitClass(ClassNode node) {
        doWithNode(node, current) {
            visitAnnotations(node)
            doWithNode(node.package, ((ClassNode) current).package) {
                visitPackage(node.package)
            }
            doWithNode(node.module, ((ClassNode) current).module) {
                visitImports(node.module)
            }

            def cur = (ClassNode) current
            failIfNot(cur.superClass==node.superClass)

            def intfs = node.interfaces
            def curIntfs = cur.interfaces
            failIfNot(intfs.length == curIntfs.length)
            if (intfs.length == curIntfs.length) {
                for (int i = 0; i < intfs.length && match; i += 1) {
                    failIfNot(intfs[i] == curIntfs[i])
                }
            }
            def nodeProps = node.properties
            def curProps = cur.properties
            if (nodeProps.size() == curProps.size()) {
                Iterator<? extends ASTNode> iter = curProps.iterator()
                // now let's visit the contents of the class
                for (PropertyNode pn : nodeProps) {
                    doWithNode(pn, iter.next()) {
                        visitProperty(pn)
                    }
                }

                def nodeFields = node.fields
                def curFields = cur.fields
                if (nodeFields.size() == curFields.size()) {
                    iter = curFields.iterator()
                    for (FieldNode fn : nodeFields) {
                        doWithNode(fn, iter.next()) {
                            visitField(fn)
                        }
                    }

                    def nodeConstructors = node.declaredConstructors
                    def curConstructors = cur.declaredConstructors
                    if (nodeConstructors.size() == curConstructors.size()) {
                        iter = curConstructors.iterator()
                        for (ConstructorNode cn : nodeConstructors) {
                            doWithNode(cn, iter.next()) {
                                visitConstructor(cn)
                            }
                        }

                        def nodeMethods = node.methods
                        def curMethods = cur.methods
                        if (nodeMethods.size() == curMethods.size()) {
                            iter = curMethods.iterator()
                            for (MethodNode mn : nodeMethods) {
                                doWithNode(mn, iter.next()) {
                                    visitMethod(mn)
                                }
                            }
                            visitObjectInitializerStatements(node)
                        }
                    }
                }
            }
            failIfNot(matchByName(cur.name, node.name))
        }
    }

    /** Matches object initializer statements for the current class candidate. */
    @Override
    protected void visitObjectInitializerStatements(ClassNode node) {
        doWithNode(node, current) {
            def initializers = ((ClassNode) current).objectInitializerStatements
            if (initializers.size() == node.objectInitializerStatements.size()) {
                def iterator = initializers.iterator()
                for (Statement element : node.objectInitializerStatements) {
                    doWithNode(element, iterator.next()) {
                        element.visit(this)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    /** Matches a package node against the current candidate. */
    @Override
    void visitPackage(PackageNode node) {
        if (node) {
            doWithNode(node, current) {
                visitAnnotations(node)
                node.visit(this)
            }
        }
    }

    /** Matches module imports against the current candidate. */
    @Override
    void visitImports(ModuleNode node) {
        if (node) {
            doWithNode(node, current) {
                def imports = ((ModuleNode) current).imports
                if (imports.size() == node.imports.size()) {
                    def iter = imports.iterator()
                    for (ImportNode importNode : node.imports) {
                        doWithNode(importNode, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.visit(this)
                        }
                    }
                } else {
                    failIfNot(false)
                    return
                }
                def starImports = ((ModuleNode) current).starImports
                if (starImports.size() == node.starImports.size()) {
                    def iter = starImports.iterator()
                    for (starImport in node.starImports) {
                        doWithNode(starImport, iter.next()) {
                            visitAnnotations(starImport)
                            starImport.visit(this)
                        }
                    }
                } else {
                    failIfNot(false)
                    return
                }
                def staticImports = ((ModuleNode) current).staticImports
                if (staticImports.size() == node.staticImports.size()) {
                    def iter = staticImports.values().iterator()
                    for (staticImport in node.staticImports.values()) {
                        doWithNode(staticImport, iter.next()) {
                            visitAnnotations(staticImport)
                            staticImport.visit(this)
                        }
                    }
                } else {
                    failIfNot(false)
                }
                def staticStarImports = ((ModuleNode) current).staticStarImports
                if (staticStarImports.size() == node.staticStarImports.size()) {
                    def iter = staticStarImports.values().iterator()
                    for (staticStarImport in node.staticStarImports.values()) {
                        doWithNode(staticStarImport, iter.next()) {
                            visitAnnotations(staticStarImport)
                            staticStarImport.visit(this)
                        }
                    }
                } else {
                    failIfNot(false)
                }
            }
        }
    }

    /** Matches annotations on the current annotated node. */
    @Override
    void visitAnnotations(AnnotatedNode node) {
        doWithNode(node, current) {
            List<AnnotationNode> refAnnotations = node.annotations
            AnnotatedNode cur = (AnnotatedNode) current
            List<AnnotationNode> curAnnotations = cur.annotations
            if (refAnnotations.size() != curAnnotations.size()) {
                failIfNot(false)
                return
            }
            if (refAnnotations.empty) return
            def iter = curAnnotations.iterator()
            for (AnnotationNode an : refAnnotations) {
                AnnotationNode curNext = iter.next()
                failIfNot(an.classNode == curNext.classNode)
                def refEntrySet = an.members.entrySet()
                def curEntrySet = curNext.members.entrySet()
                if (refEntrySet.size() == curEntrySet.size()) {
                    def entryIt = curEntrySet.iterator()
                    for (Map.Entry<String, Expression> member : refEntrySet) {
                        def next = entryIt.next()
                        if (next.key == member.key) {
                            doWithNode(member.value, next.value) {
                                member.value.visit(this)
                            }
                        } else {
                            failIfNot(false)
                        }
                    }
                } else {
                    failIfNot(false)
                }
            }
        }
    }

    /** Matches a class code container against the current candidate. */
    @Override
    protected void visitClassCodeContainer(Statement code) {
        doWithNode(code, current) {
            if (code) {
                code.visit(this)
            }
        }
    }

    /** Matches a declaration expression against the current candidate. */
    @Override @CompileDynamic
    void visitDeclarationExpression(DeclarationExpression expression) {
        doWithNode(expression, current) {
            super.visitDeclarationExpression(expression)
        }
    }

    /** Matches a constructor or method node against the current candidate. */
    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        doWithNode(node, current) {
            visitAnnotations(node)
            def cur = (MethodNode) current
            doWithNode(node.code, cur.code) {
                visitClassCodeContainer(node.code)
            }
            def params = node.parameters
            def curParams = cur.parameters
            if (params.length == curParams.length) {
                params.eachWithIndex { Parameter entry, int i ->
                    doWithNode(entry, curParams[i]) {
                        visitAnnotations(entry)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    /** Matches a field node against the current candidate. */
    @Override
    void visitField(FieldNode node) {
        doWithNode(node, current) {
            visitAnnotations(node)
            def fieldNode = (FieldNode) current
            failIfNot(matchByName(node.name, fieldNode.name))
            failIfNot(fieldNode.originType == node.originType)
            failIfNot(fieldNode.modifiers == node.modifiers)

            Expression init = node.initialExpression
            Expression curInit = fieldNode.initialExpression
            if (init) {
                if (curInit) {
                    doWithNode(init, curInit) {
                        init.visit(this)
                    }
                } else {
                    failIfNot(false)
                }
            } else if (curInit) {
                failIfNot(false)
            }
        }
    }

    /** Matches a property node against the current candidate. */
    @Override
    void visitProperty(PropertyNode node) {
        doWithNode(node, current) {
            PropertyNode pNode = (PropertyNode) current
            visitAnnotations(node)

            Statement statement = node.getterBlock
            Statement curStatement = pNode.getterBlock
            doWithNode(statement, curStatement) {
                visitClassCodeContainer(statement)
            }

            statement = node.setterBlock
            curStatement = pNode.setterBlock
            doWithNode(statement, curStatement) {
                visitClassCodeContainer(statement)
            }

            Expression init = node.initialExpression
            Expression curInit = pNode.initialExpression
            if (init) {
                if (curInit) {
                    doWithNode(init, curInit) {
                        init.visit(this)
                    }
                } else {
                    failIfNot(false)
                }
            } else if (curInit) {
                failIfNot(false)
            }
        }
    }

    /** Matches an expression statement against the current candidate. */
    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        doWithNode(statement.expression, ((ExpressionStatement) current).expression) {
            visitStatement(statement)
            statement.expression.visit(this)
        }
    }

    /** Matches a block statement against the current candidate. */
    @Override
    void visitBlockStatement(BlockStatement block) {
        doWithNode(block, current) {
            def statements = ((BlockStatement) current).statements
            if (statements.size() == block.statements.size()) {
                def iter = statements.iterator()
                for (Statement statement : block.statements) {
                    doWithNode(statement, iter.next()) {
                        statement.visit(this)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    /** Matches a method call expression against the current candidate. */
    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        doWithNode(call, current) {
            def mce = (MethodCallExpression) current
            doWithNode(call.objectExpression, mce.objectExpression) {
                call.objectExpression.visit(this)
            }
            doWithNode(call.method, mce.method) {
                call.method.visit(this)
            }
            doWithNode(call.arguments, mce.arguments) {
                call.arguments.visit(this)
            }
            failIfNot(matchByName(call.methodAsString, mce.methodAsString)
                    && call.safe == mce.safe
                    && call.spreadSafe == mce.spreadSafe
                    && call.implicitThis == mce.implicitThis)
        }
    }

    /** Matches a constructor call expression against the current candidate. */
    @Override
    void visitConstructorCallExpression(ConstructorCallExpression call) {
        doWithNode(call, current) {
            def cur = (ConstructorCallExpression) current
            doWithNode(call.arguments, cur.arguments) {
                call.arguments.visit(this)
                failIfNot(call.type == cur.type)
            }
        }
    }

    /** Matches a binary expression against the current candidate. */
    @Override
    void visitBinaryExpression(BinaryExpression expression) {
        doWithNode(expression, current) {
            def bin = (BinaryExpression) current
            def leftExpression = expression.getLeftExpression()
            doWithNode(leftExpression, bin.leftExpression) {
                leftExpression.visit(this)
            }
            def rightExpression = expression.getRightExpression()
            doWithNode(rightExpression, bin.rightExpression) {
                rightExpression.visit(this)
            }
            if (bin.operation.type != expression.operation.type) {
                failIfNot(ifConstraint(false) {
                    if (tokenPredicate) {
                        tokenPredicate.apply(bin.operation)
                    } else {
                        false
                    }
                })
            }
            failIfNot(ifConstraint(true) {
                if (eventually) {
                    eventually.apply(treeContext)
                } else {
                    true
                }
            })
        }
    }

    /** Matches a ternary expression against the current candidate. */
    @Override
    void visitTernaryExpression(TernaryExpression expression) {
        doWithNode(expression, current) {
            TernaryExpression te = (TernaryExpression) current
            doWithNode(expression.booleanExpression, te.booleanExpression) {
                expression.booleanExpression.visit(this)
            }
            def trueExpression = expression.trueExpression
            doWithNode(trueExpression, te.trueExpression) {
                trueExpression.visit(this)
            }
            def falseExpression = expression.falseExpression
            doWithNode(falseExpression, te.falseExpression) {
                falseExpression.visit(this)
            }
        }
    }

    /** Matches a postfix expression against the current candidate. */
    @Override
    void visitPostfixExpression(PostfixExpression expression) {
        doWithNode(expression, current) {
            def origExpr = expression.expression
            def curExpr = (PostfixExpression) current
            doWithNode(origExpr, curExpr.expression) {
                origExpr.visit(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    /** Matches a prefix expression against the current candidate. */
    @Override
    void visitPrefixExpression(PrefixExpression expression) {
        doWithNode(expression, current) {
            def origExpr = expression.expression
            def curExpr = (PrefixExpression) current
            doWithNode(origExpr, curExpr.expression) {
                origExpr.visit(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    /** Matches a boolean expression against the current candidate. */
    @Override
    void visitBooleanExpression(BooleanExpression expression) {
        doWithNode(expression, current) {
            doWithNode(expression.expression, ((BooleanExpression) current).expression) {
                expression.expression.visit(this)
            }
        }
    }

    /** Matches a not expression against the current candidate. */
    @Override
    void visitNotExpression(NotExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            def cur = ((NotExpression) current).expression
            doWithNode(expr, cur) {
                expr.visit(this)
            }
        }
    }

    /** Matches a closure expression against the current candidate. */
    @Override
    void visitClosureExpression(ClosureExpression expression) {
        doWithNode(expression, current) {
            def code = expression.code
            def cl = (ClosureExpression) current
            doWithNode(code, cl.code) {
                code.visit(this)
                checkParameters(expression.parameters, cl.parameters)
            }
        }
    }

    private void checkParameters(Parameter[] nodeParams, Parameter[] curParams) {
        if (nodeParams == null && curParams != null || nodeParams != null && curParams == null) {
            failIfNot(false)
            return
        }
        if (nodeParams) {
            if (curParams.length == nodeParams.length) {
                for (int i = 0; i < nodeParams.length && match; i += 1) {
                    def n = nodeParams[i]
                    def c = curParams[i]
                    doWithNode(n, c) {
                        failIfNot(matchByName(n.name, c.name)
                            && n.originType == c.originType
                            && n.type == c.originType)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    /** Matches a tuple expression against the current candidate. */
    @Override
    void visitTupleExpression(TupleExpression expression) {
        doWithNode(expression, current) {
            doWithNode(expression.expressions, ((TupleExpression) current).expressions) {
                visitListOfExpressions(expression.expressions)
            }
        }
    }

    /** Matches a list expression against the current candidate. */
    @Override
    void visitListExpression(ListExpression expression) {
        doWithNode(expression, current) {
            def exprs = expression.expressions
            doWithNode(exprs, ((ListExpression) current).expressions) {
                visitListOfExpressions(exprs)
            }
        }
    }

    /** Matches an array expression against the current candidate. */
    @Override
    void visitArrayExpression(ArrayExpression expression) {
        doWithNode(expression, current) {
            def expressions = expression.expressions
            def size = expression.sizeExpression
            def cur = (ArrayExpression) current
            def curExprs = cur.expressions
            def curSize = cur.sizeExpression
            doWithNode(expressions, curExprs) {
                visitListOfExpressions(expressions)
            }
            doWithNode(size, curSize) {
                visitListOfExpressions(size)
            }
            failIfNot(expression.elementType == cur.elementType)
        }
    }

    /** Matches a map expression against the current candidate. */
    @Override
    void visitMapExpression(MapExpression expression) {
        doWithNode(expression, current) {
            def entries = expression.mapEntryExpressions
            def curEntries = ((MapExpression) current).mapEntryExpressions
            doWithNode(entries, curEntries) {
                visitListOfExpressions(entries)
            }
        }
    }

    /** Matches a map-entry expression against the current candidate. */
    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {
        doWithNode(expression, current) {
            def key = expression.keyExpression
            def value = expression.valueExpression
            def cur = (MapEntryExpression) current
            def curKey = cur.keyExpression
            def curValue = cur.valueExpression
            doWithNode(key, curKey) {
                key.visit(this)
            }
            doWithNode(value, curValue) {
                value.visit(this)
            }
        }
    }

    /** Matches a range expression against the current candidate. */
    @Override
    void visitRangeExpression(RangeExpression expression) {
        doWithNode(expression, current) {
            def from = expression.from
            def to = expression.to
            def cur = (RangeExpression) current
            def curFrom = cur.from
            def curTo = cur.to
            doWithNode(from, curFrom) {
                from.visit(this)
            }
            doWithNode(to, curTo) {
                to.visit(this)
            }
        }
    }

    /** Matches a spread expression against the current candidate. */
    @Override
    void visitSpreadExpression(SpreadExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((SpreadExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    /** Matches a method-pointer expression against the current candidate. */
    @Override
    void visitMethodPointerExpression(MethodPointerExpression expression) {
        doWithNode(expression, current) {
            def cur = (MethodPointerExpression) current
            def expr = expression.expression
            def methodName = expression.methodName
            def curExpr = cur.expression
            def curName = cur.methodName
            doWithNode(expr, curExpr) {
                expr.visit(this)
            }
            doWithNode(methodName, curName) {
                methodName.visit(this)
            }
        }
    }

    /** Matches a unary-minus expression against the current candidate. */
    @Override
    void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((UnaryMinusExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    /** Matches a unary-plus expression against the current candidate. */
    @Override
    void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((UnaryPlusExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    /** Matches a bitwise-negation expression against the current candidate. */
    @Override
    void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((BitwiseNegationExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    /** Matches a cast expression against the current candidate. */
    @Override
    void visitCastExpression(CastExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((CastExpression) current).expression) {
                expr.visit(this)
            }
            failIfNot(expression.type == ((CastExpression) current).type)
        }
    }

    /** Matches a constant expression against the current candidate. */
    @Override @CompileDynamic
    void visitConstantExpression(ConstantExpression expression) {
        doWithNode(expression, current) {
            def cur = (ConstantExpression) current
            super.visitConstantExpression(expression)
            failIfNot(expression.type == cur.type && expression.value == cur.value)
        }
    }

    /** Matches a class expression against the current candidate. */
    @Override @CompileDynamic
    void visitClassExpression(ClassExpression expression) {
        doWithNode(expression, current) {
            super.visitClassExpression(expression)
            def cexp = (ClassExpression) current
            failIfNot(cexp.type == expression.type)
        }
    }

    /** Matches a variable expression against the current candidate. */
    @Override
    void visitVariableExpression(VariableExpression expression) {
        doWithNode(expression, current) {
            def curVar = (VariableExpression) current
            failIfNot(matchByName(expression.name, curVar.name)
                    && expression.type == curVar.type && expression.originType == curVar.originType)
        }
    }

    /** Matches a property expression against the current candidate. */
    @Override
    void visitPropertyExpression(PropertyExpression expression) {
        doWithNode(expression, current) {
            def currentPexp = (PropertyExpression) current
            doWithNode(expression.objectExpression, currentPexp.objectExpression) {
                expression.objectExpression.visit(this)
            }
            doWithNode(expression.property, currentPexp.property) {
                expression.property.visit(this)
            }
            failIfNot(expression.propertyAsString == currentPexp.propertyAsString
                    && expression.implicitThis == currentPexp.implicitThis
                    && expression.safe == currentPexp.safe
                    && expression.spreadSafe == currentPexp.spreadSafe)
        }
    }

    /** Matches an attribute expression against the current candidate. */
    @Override
    void visitAttributeExpression(AttributeExpression expression) {
        doWithNode(expression, current) {
            def currentPexp = (AttributeExpression) current
            doWithNode(expression.objectExpression, currentPexp.objectExpression) {
                expression.objectExpression.visit(this)
            }
            doWithNode(expression.property, currentPexp.property) {
                expression.property.visit(this)
            }
            failIfNot(expression.propertyAsString == currentPexp.propertyAsString
                    && expression.implicitThis == currentPexp.implicitThis
                    && expression.safe == currentPexp.safe
                    && expression.spreadSafe == currentPexp.spreadSafe)
        }
    }

    /** Matches a GString expression against the current candidate. */
    @Override
    void visitGStringExpression(GStringExpression expression) {
        doWithNode(expression, current) {
            def cur = (GStringExpression) current
            def strings = expression.strings
            def values = expression.values
            def curStrings = cur.strings
            def curValues = cur.values
            doWithNode(strings, curStrings) {
                visitListOfExpressions(strings)
            }
            doWithNode(values, curValues) {
                visitListOfExpressions(values)
            }
        }
    }

    /** Matches a list of expressions against the current candidate list. */
    @Override
    void visitListOfExpressions(List<? extends Expression> list) {
        if (list == null) return
        def currentExprs = (List<Expression>) current
        if (currentExprs.size() != list.size()) {
            failIfNot(false)
            return
        }
        def iter = currentExprs.iterator()
        for (Expression expression : list) {
            def next = iter.next()
            doWithNode(expression, next) {
                expression.visit(this)
            }
        }
    }

    /** Matches a closure-list expression against the current candidate. */
    @Override
    void visitClosureListExpression(ClosureListExpression cle) {
        doWithNode(cle, current) {
            def exprs = cle.expressions
            doWithNode(exprs, ((ClosureListExpression)current).expressions) {
                visitListOfExpressions(exprs)
            }
        }
    }

    /** Matches an if/else statement against the current candidate. */
    @Override
    void visitIfElse(IfStatement ifElse) {
        doWithNode(ifElse, current) {
            visitStatement(ifElse)
            def cur = (IfStatement) current
            def bool = ifElse.booleanExpression
            def ifBlock = ifElse.ifBlock
            def elseBlock = ifElse.elseBlock
            doWithNode(bool, cur.booleanExpression) {
                bool.visit(this)
            }
            doWithNode(ifBlock, cur.ifBlock) {
                ifBlock.visit(this)
            }
            failIfNot(elseBlock && cur.elseBlock || !elseBlock && !cur.elseBlock)
            doWithNode(elseBlock, cur.elseBlock) {
                elseBlock.visit(this)
            }
        }
    }

    /** Matches a for loop against the current candidate. */
    @Override
    void visitForLoop(ForStatement forLoop) {
        doWithNode(forLoop, current) {
            visitStatement(forLoop)
            def cur = (ForStatement) current
            def col = forLoop.collectionExpression
            def block = forLoop.loopBlock
            doWithNode(col, cur.collectionExpression) {
                col.visit(this)
            }
            doWithNode(block, cur.loopBlock) {
                block.visit(this)
            }
        }
    }

    /** Matches a while loop against the current candidate. */
    @Override
    void visitWhileLoop(WhileStatement loop) {
        doWithNode(loop, current) {
            visitStatement(loop)
            def cur = (WhileStatement) current
            def bool = loop.booleanExpression
            def block = loop.loopBlock
            doWithNode(bool, cur.booleanExpression) {
                bool.visit(this)
            }
            doWithNode(block, cur.loopBlock) {
                block.visit(this)
            }
        }
    }

    /**
     * Annotates an AST node with matching constraints. Call this on an AST
     * intended to be used as a pattern only; it attaches node metadata that
     * customises behaviour during pattern matching.
     *
     * @param pattern a pattern AST
     * @param constraintsSpec a closure specification of matching constraints
     * @return the same pattern, annotated with constraints
     * @since 2.5.0
     */
    static ASTNode withConstraints(ASTNode pattern, @DelegatesTo(value=MatchingConstraintsBuilder, strategy=Closure.DELEGATE_ONLY) Closure constraintsSpec) {
        def builder = new MatchingConstraintsBuilder()
        def constraints = builder.build(constraintsSpec)
        pattern.putNodeMetaData(MatchingConstraints, constraints)

        pattern
    }
}
