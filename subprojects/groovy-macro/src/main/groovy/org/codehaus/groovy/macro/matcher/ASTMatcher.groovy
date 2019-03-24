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

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
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
import org.codehaus.groovy.ast.expr.FieldExpression
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
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
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
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.matcher.internal.MatchingConstraintsBuilder

@CompileStatic
class ASTMatcher extends ContextualClassCodeVisitor {

    public static final String WILDCARD = "_";

    private Object current = null
    private boolean match = true

    private ASTMatcher() {}

    @Override
    protected SourceUnit getSourceUnit() {
        null
    }

    /**
     * Matches an AST with another AST (pattern). It will return true if the AST matches
     * all the nodes from the pattern AST.
     * @param node the AST we want to match with
     * @param pattern the pattern AST we want to match to
     * @return true if this AST matches the pattern
     */
    public static boolean matches(ASTNode node, ASTNode pattern) {
        ASTMatcher matcher = new ASTMatcher()
        matcher.current = node
        matcher.match = true
        if (pattern instanceof ClassNode) {
            matcher.visitClass(pattern)
        } else {
            pattern.accept(matcher)
        }

        matcher.match
    }

    private boolean failIfNot(boolean value) {
        match = match && value
    }

    private static boolean matchByName(String patternText, String nodeText) {
        return nodeText.equals(patternText) || WILDCARD.equals(patternText);
    }

    private static boolean isWildcardExpression(Object exp) {
        return (exp instanceof VariableExpression && WILDCARD.equals(exp.getName())
            || (exp instanceof ConstantExpression && WILDCARD.equals(exp.getValue())));
    }

    /**
     * Locates all nodes in the given AST which match the pattern AST.
     * This operation can cost a lot, because it tries to match a sub-tree
     * to every node of the AST.
     * @param node an AST Node
     * @param pattern a pattern to be found somewhere in the AST
     * @return a list of {@link TreeContext}, always not null.
     */
    public static List<TreeContext> find(ASTNode node, ASTNode pattern) {
        ASTFinder finder = new ASTFinder(pattern);
        node.accept(finder);

        finder.matches
    }

    private void storeContraints(ASTNode src) {
        def constraints = src.getNodeMetaData(MatchingConstraints)
        if (constraints) {
            treeContext.putUserdata(MatchingConstraints, constraints)
        }
    }

    public <T> T ifConstraint(T defaultValue, @DelegatesTo(MatchingConstraints) Closure<T> code) {
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
            if ((exp instanceof VariableExpression && placeholders.contains(exp.name))) {
                return exp.name
            } else if ((exp instanceof ConstantExpression && placeholders.contains(exp.value))) {
                return exp.value
            } else {
                null
            }
        }
    }

    private void doWithNode(Object patternNode, Object foundNode, Closure cl) {
        Class expectedClass = patternNode?patternNode.class:Object
        if (expectedClass == null) {
            expectedClass = Object
        }

        boolean doPush = treeContext.node!=foundNode && foundNode instanceof ASTNode
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
                if (alreadySeenAST==null) {
                    treeContext.parent.putUserdata("placeholder_$placeholder", foundNode)
                } else {
                    // during the tree inspection, placeholder already found
                    // so we need to check that they are identical
                    failIfNot(matches((ASTNode)alreadySeenAST[0], (ASTNode)foundNode))
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

    @Override
    public void visitClass(final ClassNode node) {
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
                for (int i = 0; i < intfs.length && match; i++) {
                    failIfNot(intfs[i] == curIntfs[i])
                }
            }
            def nodeProps = node.properties
            def curProps = cur.properties
            if (nodeProps.size() == curProps.size()) {
                def iter = curProps.iterator()
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

    @Override
    protected void visitObjectInitializerStatements(final ClassNode node) {
        doWithNode(node, current) {
            def initializers = ((ClassNode) current).objectInitializerStatements
            if (initializers.size() == node.objectInitializerStatements.size()) {
                def iterator = initializers.iterator()
                for (Statement element : node.objectInitializerStatements) {
                    doWithNode(element, iterator.next()) {
                        element.accept(this)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitPackage(final PackageNode node) {
        if (node) {
            doWithNode(node, current) {
                visitAnnotations(node)
                node.accept(this)
            }
        }
    }

    @Override
    public void visitImports(final ModuleNode node) {
        if (node) {
            doWithNode(node, current) {
                ModuleNode module = (ModuleNode) current
                def imports = module.imports
                if (imports.size() == node.imports.size()) {
                    def iter = imports.iterator()
                    for (ImportNode importNode : node.imports) {
                        doWithNode(importNode, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.accept(this)
                        }
                    }
                } else {
                    failIfNot(false)
                    return
                }
                imports = module.starImports
                if (imports.size() == node.starImports.size()) {
                    def iter = imports.iterator()
                    for (ImportNode importNode : node.starImports) {
                        doWithNode(importNode, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.accept(this)
                        }
                    }
                } else {
                    failIfNot(false)
                    return
                }
                imports = module.staticImports
                if (imports.size() == node.staticImports.size()) {
                    def iter = imports.values().iterator()
                    for (ImportNode importNode : node.staticImports.values()) {
                        doWithNode(importNode, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.accept(this)
                        }
                    }
                } else {
                    failIfNot(false)
                }
                imports = module.staticStarImports
                if (imports.size() == node.staticStarImports.size()) {
                    def iter = imports.values().iterator()
                    for (ImportNode importNode : node.staticStarImports.values()) {
                        doWithNode(importNode, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.accept(this)
                        }
                    }
                } else {
                    failIfNot(false)
                }
            }
        }
    }

    @Override
    public void visitAnnotations(final AnnotatedNode node) {
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

                // skip built-in properties
                if (an.builtIn) {
                    if (!curNext.builtIn) {
                        failIfNot(false)
                    }
                    continue
                }
                failIfNot(an.classNode==curNext.classNode)
                def refEntrySet = an.members.entrySet()
                def curEntrySet = curNext.members.entrySet()
                if (refEntrySet.size() == curEntrySet.size()) {
                    def entryIt = curEntrySet.iterator()
                    for (Map.Entry<String, Expression> member : refEntrySet) {
                        def next = entryIt.next()
                        if (next.key == member.key) {
                            doWithNode(member.value, next.value) {
                                member.value.accept(this)
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

    @Override
    protected void visitClassCodeContainer(final Statement code) {
        doWithNode(code, current) {
            if (code) {
                code.accept(this)
            }
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        doWithNode(expression, current) {
            super.visitDeclarationExpression(expression)
        }
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
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

    @Override
    public void visitField(final FieldNode node) {
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
                        init.accept(this)
                    }
                } else {
                    failIfNot(false)
                }
            } else if (curInit) {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitProperty(final PropertyNode node) {
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
                        init.accept(this)
                    }
                } else {
                    failIfNot(false)
                }
            } else if (curInit) {
                failIfNot(false)
            }
        }
    }

    @Override
    void visitExpressionStatement(final ExpressionStatement statement) {
        doWithNode(statement.expression, ((ExpressionStatement) current).expression) {
            visitStatement(statement)
            statement.expression.accept(this)
        }
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        doWithNode(block, current) {
            def statements = ((BlockStatement) current).statements
            if (statements.size() == block.statements.size()) {
                def iter = statements.iterator()
                for (Statement statement : block.statements) {
                    doWithNode(statement, iter.next()) {
                        statement.accept(this)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        doWithNode(call, current) {
            def mce = (MethodCallExpression) current
            doWithNode(call.objectExpression, mce.objectExpression) {
                call.objectExpression.accept(this)
            }
            doWithNode(call.method, mce.method) {
                call.method.accept(this)
            }
            doWithNode(call.arguments, mce.arguments) {
                call.arguments.accept(this)
            }
            failIfNot(matchByName(call.methodAsString, mce.methodAsString) &&
                    (call.safe == mce.safe) &&
                    (call.spreadSafe == mce.spreadSafe) &&
                    (call.implicitThis == mce.implicitThis))
        }
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        super.visitStaticMethodCallExpression(call);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        doWithNode(call, current) {
            def cur = (ConstructorCallExpression) current
            doWithNode(call.arguments, cur.arguments) {
                call.arguments.accept(this)
                failIfNot(call.type == cur.type)
            }
        }
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        doWithNode(expression, current) {
            def bin = (BinaryExpression) current
            def leftExpression = expression.getLeftExpression()
            doWithNode(leftExpression, bin.leftExpression) {
                leftExpression.accept(this)
            }
            def rightExpression = expression.getRightExpression()
            doWithNode(rightExpression, bin.rightExpression) {
                rightExpression.accept(this)
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

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        doWithNode(expression, current) {
            TernaryExpression te = (TernaryExpression) current
            doWithNode(expression.booleanExpression, te.booleanExpression) {
                expression.booleanExpression.accept(this)
            }
            def trueExpression = expression.trueExpression
            doWithNode(trueExpression, te.trueExpression) {
                trueExpression.accept(this)
            }
            def falseExpression = expression.falseExpression
            doWithNode(falseExpression, te.falseExpression) {
                falseExpression.accept(this)
            }
        }
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        doWithNode(expression, current) {
            def origExpr = expression.expression
            def curExpr = (PostfixExpression) current
            doWithNode(origExpr, curExpr.expression) {
                origExpr.accept(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        doWithNode(expression, current) {
            def origExpr = expression.expression
            def curExpr = (PrefixExpression) current
            doWithNode(origExpr, curExpr.expression) {
                origExpr.accept(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        doWithNode(expression, current) {
            doWithNode(expression.expression, ((BooleanExpression) current).expression) {
                expression.expression.accept(this)
            }
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            def cur = ((NotExpression) current).expression
            doWithNode(expr, cur) {
                expr.accept(this)
            }
        }
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        doWithNode(expression, current) {
            def code = expression.code
            def cl = (ClosureExpression) current
            doWithNode(code, cl.code) {
                code.accept(this)
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
                for (int i = 0; i < nodeParams.length && match; i++) {
                    def n = nodeParams[i]
                    def c = curParams[i]
                    doWithNode(n, c) {
                        failIfNot(matchByName(n.name, c.name) &&
                                (n.originType == c.originType) &&
                                (n.type == c.originType))
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        doWithNode(expression, current) {
            doWithNode(expression.expressions, ((TupleExpression) current).expressions) {
                visitListOfExpressions(expression.expressions)
            }
        }
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        doWithNode(expression, current) {
            def exprs = expression.expressions
            doWithNode(exprs, ((ListExpression) current).expressions) {
                visitListOfExpressions(exprs)
            }
        }
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
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

    @Override
    public void visitMapExpression(final MapExpression expression) {
        doWithNode(expression, current) {
            def entries = expression.mapEntryExpressions
            def curEntries = ((MapExpression) current).mapEntryExpressions
            doWithNode(entries, curEntries) {
                visitListOfExpressions(entries)
            }
        }
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        doWithNode(expression, current) {
            def key = expression.keyExpression
            def value = expression.valueExpression
            def cur = (MapEntryExpression) current
            def curKey = cur.keyExpression
            def curValue = cur.valueExpression
            doWithNode(key, curKey) {
                key.accept(this)
            }
            doWithNode(value, curValue) {
                value.accept(this)
            }
        }
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        doWithNode(expression, current) {
            def from = expression.from
            def to = expression.to
            def cur = (RangeExpression) current
            def curFrom = cur.from
            def curTo = cur.to
            doWithNode(from, curFrom) {
                from.accept(this)
            }
            doWithNode(to, curTo) {
                to.accept(this)
            }
        }
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((SpreadExpression) current).expression) {
                expr.accept(this)
            }
        }
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        doWithNode(expression, current) {
            def cur = (MethodPointerExpression) current
            def expr = expression.expression
            def methodName = expression.methodName
            def curExpr = cur.expression
            def curName = cur.methodName
            doWithNode(expr, curExpr) {
                expr.accept(this)
            }
            doWithNode(methodName, curName) {
                methodName.accept(this)
            }
        }
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((UnaryMinusExpression) current).expression) {
                expr.accept(this)
            }
        }
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((UnaryPlusExpression) current).expression) {
                expr.accept(this)
            }
        }
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((BitwiseNegationExpression) current).expression) {
                expr.accept(this)
            }
        }
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        doWithNode(expression, current) {
            def expr = expression.expression
            doWithNode(expr, ((CastExpression) current).expression) {
                expr.accept(this)
            }
            failIfNot(expression.type == ((CastExpression) current).type)
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitConstantExpression(final ConstantExpression expression) {
        doWithNode(expression, current) {
            def cur = (ConstantExpression) current
            super.visitConstantExpression(expression)
            failIfNot((expression.type == cur.type) &&
                    (expression.value == cur.value))
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitClassExpression(final ClassExpression expression) {
        doWithNode(expression, current) {
            super.visitClassExpression(expression)
            def cexp = (ClassExpression) current
            failIfNot(cexp.type == expression.type)
        }
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        doWithNode(expression, current) {
            def curVar = (VariableExpression) current
            failIfNot(matchByName(expression.name, curVar.name) &&
                    (expression.type == curVar.type) &&
                    (expression.originType == curVar.originType))
        }
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        doWithNode(expression, current) {
            def currentPexp = (PropertyExpression) current
            doWithNode(expression.objectExpression, currentPexp.objectExpression) {
                expression.objectExpression.accept(this)
            }
            doWithNode(expression.property, currentPexp.property) {
                expression.property.accept(this)
            }
            failIfNot((expression.propertyAsString == currentPexp.propertyAsString) &&
                    (expression.implicitThis == currentPexp.implicitThis) &&
                    (expression.safe == currentPexp.safe) &&
                    (expression.spreadSafe == currentPexp.spreadSafe))
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        doWithNode(expression, current) {
            def currentPexp = (AttributeExpression) current
            doWithNode(expression.objectExpression, currentPexp.objectExpression) {
                expression.objectExpression.accept(this)
            }
            doWithNode(expression.property, currentPexp.property) {
                expression.property.accept(this)
            }
            failIfNot((expression.propertyAsString == currentPexp.propertyAsString) &&
                    (expression.implicitThis == currentPexp.implicitThis) &&
                    (expression.safe == currentPexp.safe) &&
                    (expression.spreadSafe == currentPexp.spreadSafe))
        }
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
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

    @Override
    void visitListOfExpressions(final List<? extends Expression> list) {
        if (list == null) return;
        def currentExprs = (List<Expression>) current
        if (currentExprs.size() != list.size()) {
            failIfNot(false)
            return
        }
        def iter = currentExprs.iterator()
        for (Expression expression : list) {
            def next = iter.next()
            doWithNode(expression, next) {
                expression.accept(this)
            }
        }
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        doWithNode(cle, current) {
            def exprs = cle.expressions
            doWithNode(exprs, ((ClosureListExpression)current).expressions) {
                visitListOfExpressions(exprs)
            }
        }
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        super.visitBytecodeExpression(cle);
    }

    @Override
    void visitIfElse(final IfStatement ifElse) {
        doWithNode(ifElse, current) {
            visitStatement(ifElse)
            def cur = (IfStatement) current
            def bool = ifElse.booleanExpression
            def ifBlock = ifElse.ifBlock
            def elseBlock = ifElse.elseBlock
            doWithNode(bool, cur.booleanExpression) {
                bool.accept(this)
            }
            doWithNode(ifBlock, cur.ifBlock) {
                ifBlock.accept(this)
            }
            failIfNot(elseBlock && cur.elseBlock || !elseBlock && !cur.elseBlock)
            doWithNode(elseBlock, cur.elseBlock) {
                elseBlock.accept(this)
            }
        }
    }

    @Override
    void visitForLoop(final ForStatement forLoop) {
        doWithNode(forLoop, current) {
            visitStatement(forLoop)
            def cur = (ForStatement) current
            def col = forLoop.collectionExpression
            def block = forLoop.loopBlock
            doWithNode(col, cur.collectionExpression) {
                col.accept(this)
            }
            doWithNode(block, cur.loopBlock) {
                block.accept(this)
            }
        }
    }

    @Override
    void visitWhileLoop(final WhileStatement loop) {
        doWithNode(loop, current) {
            visitStatement(loop)
            def cur = (WhileStatement) current
            def bool = loop.booleanExpression
            def block = loop.loopBlock
            doWithNode(bool, cur.booleanExpression) {
                bool.accept(this)
            }
            doWithNode(block, cur.loopBlock) {
                block.accept(this)
            }
        }
    }

    /**
     * // todo: experimental!
     *
     * Annotates an AST node with matching contraints. This method should be called
     * on an AST intended to be used as a pattern only. It will put node metadata on
     * the AST node allowing customized behavior in pattern matching.
     *
     * @param pattern a pattern AST
     * @param constraintsSpec a closure specification of matching constraints
     * @return the same pattern, annotated with constraints
     */
    public static ASTNode withConstraints(
            final ASTNode pattern,
            final @DelegatesTo(value=MatchingConstraintsBuilder, strategy=Closure.DELEGATE_ONLY) Closure constraintsSpec) {
        def builder = new MatchingConstraintsBuilder()
        def constraints = builder.build(constraintsSpec)
        pattern.putNodeMetaData(MatchingConstraints, constraints)

        pattern
    }
}
