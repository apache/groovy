/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.macro.matcher

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.control.SourceUnit

@CompileStatic
class ASTMatcher extends ClassCodeVisitorSupport {

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
            pattern.visit(matcher)
        }

        matcher.match
    }

    private boolean failIfNot(boolean value) {
        match = match && value
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
        node.visit(finder);

        finder.matches
    }

    private void doWithNode(Class expectedClass, Object next, Closure cl) {
        if (expectedClass == null) {
            expectedClass = Object
        }
        if (match && (next == null || expectedClass.isAssignableFrom(next.class))) {
            Object old = current
            current = next
            cl()
            current = old
        } else {
            failIfNot(false)
        }
    }

    @Override
    public void visitClass(final ClassNode node) {
        doWithNode(ClassNode, current) {
            visitAnnotations(node)
            doWithNode(PackageNode, ((ClassNode) current).package) {
                visitPackage(node.package)
            }
            doWithNode(ModuleNode, ((ClassNode) current).module) {
                visitImports(node.module)
            }

            def cur = (ClassNode) current
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
                    doWithNode(pn.class, iter.next()) {
                        visitProperty(pn)
                    }
                }

                def nodeFields = node.fields
                def curFields = cur.fields
                if (nodeFields.size() == curFields.size()) {
                    iter = curFields.iterator()
                    for (FieldNode fn : nodeFields) {
                        doWithNode(fn.class, iter.next()) {
                            visitField(fn)
                        }
                    }

                    def nodeConstructors = node.declaredConstructors
                    def curConstructors = cur.declaredConstructors
                    if (nodeConstructors.size() == curConstructors.size()) {
                        iter = curConstructors.iterator()
                        for (ConstructorNode cn : nodeConstructors) {
                            doWithNode(cn.class, iter.next()) {
                                visitConstructor(cn)
                            }
                        }

                        def nodeMethods = node.methods
                        def curMethods = cur.methods
                        if (nodeMethods.size() == curMethods.size()) {
                            iter = curMethods.iterator()
                            for (MethodNode mn : nodeMethods) {
                                doWithNode(mn.class, iter.next()) {
                                    visitMethod(mn)
                                }
                            }
                            visitObjectInitializerStatements(node)
                        }
                    }
                }
            }
            failIfNot(cur.name == node.name)
        }
    }

    @Override
    protected void visitObjectInitializerStatements(final ClassNode node) {
        doWithNode(ClassNode, current) {
            def initializers = ((ClassNode) current).objectInitializerStatements
            if (initializers.size() == node.objectInitializerStatements.size()) {
                def iterator = initializers.iterator()
                for (Statement element : node.objectInitializerStatements) {
                    doWithNode(element.class, iterator.next()) {
                        element.visit(this)
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
            doWithNode(node.class, current) {
                visitAnnotations(node)
                node.visit(this)
            }
        }
    }

    @Override
    public void visitImports(final ModuleNode node) {
        if (node) {
            doWithNode(ModuleNode, current) {
                ModuleNode module = (ModuleNode) current
                def imports = module.imports
                if (imports.size() == node.imports.size()) {
                    def iter = imports.iterator()
                    for (ImportNode importNode : node.imports) {
                        doWithNode(importNode.class, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.visit(this)
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
                        doWithNode(importNode.class, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.visit(this)
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
                        doWithNode(importNode.class, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.visit(this)
                        }
                    }
                } else {
                    failIfNot(false)
                }
                imports = module.staticStarImports
                if (imports.size() == node.staticStarImports.size()) {
                    def iter = imports.values().iterator()
                    for (ImportNode importNode : node.staticStarImports.values()) {
                        doWithNode(importNode.class, iter.next()) {
                            visitAnnotations(importNode)
                            importNode.visit(this)
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
        doWithNode(AnnotatedNode, current) {
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
                            doWithNode(member.value.class, next.value) {
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

    @Override
    protected void visitClassCodeContainer(final Statement code) {
        doWithNode(Statement, current) {
            if (code) {
                code.visit(this)
            }
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        doWithNode(DeclarationExpression, current) {
            super.visitDeclarationExpression(expression)
        }
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        doWithNode(MethodNode, current) {
            visitAnnotations(node)
            def cur = (MethodNode) current
            doWithNode(Statement, cur.code) {
                visitClassCodeContainer(node.code)
            }
            def params = node.parameters
            def curParams = cur.parameters
            if (params.length == curParams.length) {
                params.eachWithIndex { Parameter entry, int i ->
                    doWithNode(entry.class, curParams[i]) {
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
        doWithNode(FieldNode, current) {
            visitAnnotations(node)
            def fieldNode = (FieldNode) current
            failIfNot(fieldNode.name == node.name)
            failIfNot(fieldNode.originType == node.originType)
            failIfNot(fieldNode.modifiers == node.modifiers)

            Expression init = node.initialExpression

            Expression curInit = fieldNode.initialExpression
            if (init) {
                if (curInit) {
                    doWithNode(init.class, curInit) {
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

    @Override
    public void visitProperty(final PropertyNode node) {
        doWithNode(PropertyNode, current) {
            PropertyNode pNode = (PropertyNode) current
            visitAnnotations(node)

            Statement statement = node.getterBlock
            Statement curStatement = pNode.getterBlock
            doWithNode(statement?.class, curStatement) {
                visitClassCodeContainer(statement)
            }

            statement = node.setterBlock
            curStatement = pNode.setterBlock
            doWithNode(statement?.class, curStatement) {
                visitClassCodeContainer(statement)
            }

            Expression init = node.initialExpression
            Expression curInit = pNode.initialExpression
            if (init) {
                if (curInit) {
                    doWithNode(init.class, curInit) {
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

    @Override
    void visitExpressionStatement(final ExpressionStatement statement) {
        doWithNode(statement.expression.class, ((ExpressionStatement) current).expression) {
            visitStatement(statement)
            statement.expression.visit(this)
        }
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        doWithNode(BlockStatement, current) {
            def statements = ((BlockStatement) current).statements
            if (statements.size() == block.statements.size()) {
                def iter = statements.iterator()
                for (Statement statement : block.statements) {
                    doWithNode(statement.class, iter.next()) {
                        statement.visit(this)
                    }
                }
            } else {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        doWithNode(MethodCallExpression, current) {
            def mce = (MethodCallExpression) current
            doWithNode(call.objectExpression.class, mce.objectExpression) {
                call.objectExpression.visit(this)
            }
            doWithNode(call.method.class, mce.method) {
                call.method.visit(this)
            }
            doWithNode(call.arguments.class, mce.arguments) {
                call.arguments.visit(this)
            }
            failIfNot((call.methodAsString == mce.methodAsString) &&
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
        def cur = (ConstructorCallExpression) current
        doWithNode(call.arguments.class, cur.arguments) {
            call.arguments.visit(this)
            failIfNot(call.type == cur.type)
        }
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        doWithNode(BinaryExpression, current) {
            def bin = (BinaryExpression) current
            def leftExpression = expression.getLeftExpression()
            doWithNode(leftExpression.class, bin.leftExpression) {
                leftExpression.visit(this)
            }
            def rightExpression = expression.getRightExpression()
            doWithNode(rightExpression.class, bin.rightExpression) {
                rightExpression.visit(this)
            }
            if (bin.operation.type != expression.operation.type) {
                failIfNot(false)
            }
        }
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        doWithNode(TernaryExpression, current) {
            TernaryExpression te = (TernaryExpression) current
            doWithNode(BooleanExpression, te.booleanExpression) {
                expression.booleanExpression.visit(this)
            }
            def trueExpression = expression.trueExpression
            doWithNode(trueExpression.class, te.trueExpression) {
                trueExpression.visit(this)
            }
            def falseExpression = expression.falseExpression
            doWithNode(falseExpression.class, te.falseExpression) {
                falseExpression.visit(this)
            }
        }
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        doWithNode(PostfixExpression, current) {
            def origExpr = expression.expression
            def curExpr = (PostfixExpression) current
            doWithNode(origExpr.class, curExpr.expression) {
                origExpr.visit(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        doWithNode(PrefixExpression, current) {
            def origExpr = expression.expression
            def curExpr = (PrefixExpression) current
            doWithNode(origExpr.class, curExpr.expression) {
                origExpr.visit(this)
                failIfNot(expression.operation.type == curExpr.operation.type)
            }
        }
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        doWithNode(BooleanExpression, current) {
            doWithNode(expression.expression.class, ((BooleanExpression) current).expression) {
                expression.expression.visit(this)
            }
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        doWithNode(NotExpression, current) {
            def expr = expression.expression
            def cur = ((NotExpression) current).expression
            doWithNode(expr.class, cur) {
                expr.visit(this)
            }
        }
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        doWithNode(ClosureExpression, current) {
            def code = expression.code
            def cl = (ClosureExpression) current
            doWithNode(code.class, cl.code) {
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
                for (int i = 0; i < nodeParams.length && match; i++) {
                    def n = nodeParams[i]
                    def c = curParams[i]
                    doWithNode(n.class, c) {
                        failIfNot((n.name == c.name) &&
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
        doWithNode(TupleExpression, current) {
            doWithNode(List, ((TupleExpression) current).expressions) {
                visitListOfExpressions(expression.expressions)
            }
        }
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        doWithNode(ListExpression, current) {
            def exprs = expression.expressions
            doWithNode(exprs.class, ((ListExpression) current).expressions) {
                visitListOfExpressions(exprs)
            }
        }
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        doWithNode(ArrayExpression, current) {
            def expressions = expression.expressions
            def size = expression.sizeExpression
            def cur = (ArrayExpression) current
            def curExprs = cur.expressions
            def curSize = cur.sizeExpression
            doWithNode(expressions.class, curExprs) {
                visitListOfExpressions(expressions)
            }
            doWithNode(size.class, curSize) {
                visitListOfExpressions(size)
            }
            failIfNot(expression.elementType == cur.elementType)
        }
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        doWithNode(MapExpression, current) {
            def entries = expression.mapEntryExpressions
            def curEntries = ((MapExpression) current).mapEntryExpressions
            doWithNode(entries.class, curEntries) {
                visitListOfExpressions(entries)
            }
        }
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        doWithNode(MapEntryExpression, current) {
            def key = expression.keyExpression
            def value = expression.valueExpression
            def cur = (MapEntryExpression) current
            def curKey = cur.keyExpression
            def curValue = cur.valueExpression
            doWithNode(key.class, curKey) {
                key.visit(this)
            }
            doWithNode(value.class, curValue) {
                value.visit(this)
            }
        }
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        doWithNode(RangeExpression, current) {
            def from = expression.from
            def to = expression.to
            def cur = (RangeExpression) current
            def curFrom = cur.from
            def curTo = cur.to
            doWithNode(from.class, curFrom) {
                from.visit(this)
            }
            doWithNode(to.class, curTo) {
                to.visit(this)
            }
        }
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        doWithNode(SpreadExpression, current) {
            def expr = expression.expression
            doWithNode(expr.class, ((SpreadExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        doWithNode(MethodPointerExpression, current) {
            def cur = (MethodPointerExpression) current
            def expr = expression.expression
            def methodName = expression.methodName
            def curExpr = cur.expression
            def curName = cur.methodName
            doWithNode(expr.class, curExpr) {
                expr.visit(this)
            }
            doWithNode(methodName.class, curName) {
                methodName.visit(this)
            }
        }
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        doWithNode(UnaryMinusExpression, current) {
            def expr = expression.expression
            doWithNode(expr.class, ((UnaryMinusExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        doWithNode(UnaryPlusExpression, current) {
            def expr = expression.expression
            doWithNode(expr.class, ((UnaryPlusExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        doWithNode(BitwiseNegationExpression, current) {
            def expr = expression.expression
            doWithNode(expr.class, ((BitwiseNegationExpression) current).expression) {
                expr.visit(this)
            }
        }
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        doWithNode(CastExpression, current) {
            def expr = expression.expression
            doWithNode(expr.class, ((CastExpression) current).expression) {
                expr.visit(this)
            }
            failIfNot(expression.type == ((CastExpression) current).type)
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitConstantExpression(final ConstantExpression expression) {
        doWithNode(ConstantExpression, current) {
            def cur = (ConstantExpression) current
            super.visitConstantExpression(expression)
            failIfNot((expression.type == cur.type) &&
                    (expression.value == cur.value))
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitClassExpression(final ClassExpression expression) {
        doWithNode(ClassExpression, current) {
            super.visitClassExpression(expression)
            def cexp = (ClassExpression) current
            failIfNot(cexp.type == expression.type)
        }
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        doWithNode(VariableExpression, current) {
            def curVar = (VariableExpression) current
            failIfNot((expression.name == curVar.name) &&
                    (expression.type == curVar.type) &&
                    (expression.originType == curVar.originType))
        }
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        doWithNode(PropertyExpression, current) {
            def currentPexp = (PropertyExpression) current
            doWithNode(expression.objectExpression.class, currentPexp.objectExpression) {
                expression.objectExpression.visit(this)
            }
            doWithNode(expression.property.class, currentPexp.property) {
                expression.property.visit(this)
            }
            failIfNot((expression.propertyAsString == currentPexp.propertyAsString) &&
                    (expression.implicitThis == currentPexp.implicitThis) &&
                    (expression.safe == currentPexp.safe) &&
                    (expression.spreadSafe == currentPexp.spreadSafe))
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        doWithNode(AttributeExpression, current) {
            def currentPexp = (AttributeExpression) current
            doWithNode(expression.objectExpression.class, currentPexp.objectExpression) {
                expression.objectExpression.visit(this)
            }
            doWithNode(expression.property.class, currentPexp.property) {
                expression.property.visit(this)
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
        doWithNode(GStringExpression, current) {
            def cur = (GStringExpression) current
            def strings = expression.strings
            def values = expression.values
            def curStrings = cur.strings
            def curValues = cur.values
            doWithNode(strings.class, curStrings) {
                visitListOfExpressions(strings)
            }
            doWithNode(values.class, curValues) {
                visitListOfExpressions(values)
            }
        }
    }

    @Override
    protected void visitListOfExpressions(final List<? extends Expression> list) {
        if (list == null) return;
        def currentExprs = (List<Expression>) current
        if (currentExprs.size() != list.size()) {
            failIfNot(false)
            return
        }
        def iter = currentExprs.iterator()
        for (Expression expression : list) {
            def next = iter.next()
            doWithNode(expression.class, next) {
                expression.visit(this)
            }
        }
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        doWithNode(ClosureListExpression, current) {
            def exprs = cle.expressions
            doWithNode(exprs.class, ((ClosureListExpression)current).expressions) {
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
        doWithNode(IfStatement, current) {
            visitStatement(ifElse)
            def cur = (IfStatement) current
            def bool = ifElse.booleanExpression
            def ifBlock = ifElse.ifBlock
            def elseBlock = ifElse.elseBlock
            doWithNode(bool.class, cur.booleanExpression) {
                bool.visit(this)
            }
            doWithNode(ifBlock.class, cur.ifBlock) {
                ifBlock.visit(this)
            }
            failIfNot(elseBlock && cur.elseBlock || !elseBlock && !cur.elseBlock)
            doWithNode(elseBlock.class, cur.elseBlock) {
                elseBlock.visit(this)
            }
        }
    }

    @Override
    void visitForLoop(final ForStatement forLoop) {
        doWithNode(ForStatement, current) {
            visitStatement(forLoop)
            def cur = (ForStatement) current
            def col = forLoop.collectionExpression
            def block = forLoop.loopBlock
            doWithNode(col.class, cur.collectionExpression) {
                col.visit(this)
            }
            doWithNode(block.class, cur.loopBlock) {
                block.visit(this)
            }
        }
    }

    @Override
    void visitWhileLoop(final WhileStatement loop) {
        doWithNode(WhileStatement, current) {
            visitStatement(loop)
            def cur = (WhileStatement) current
            def bool = loop.booleanExpression
            def block = loop.loopBlock
            doWithNode(bool.class, cur.booleanExpression) {
                bool.visit(this)
            }
            doWithNode(block.class, cur.loopBlock) {
                block.visit(this)
            }
        }
    }
}
