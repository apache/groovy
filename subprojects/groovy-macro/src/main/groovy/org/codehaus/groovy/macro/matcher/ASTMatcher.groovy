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
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.stc.StaticTypesMarker

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
        if (match && (next==null || expectedClass.isAssignableFrom(next.class))) {
            Object old = current
            current = next
            cl()
            current = old
        } else {
            match = false
        }
    }

    @Override
    public void visitClass(final ClassNode node) {
        doWithNode(ClassNode, current) {
            visitAnnotations(node)
        }
        doWithNode(PackageNode, ((ClassNode) current).package) {
            visitPackage(node.package)
        }
        doWithNode(ModuleNode, ((ClassNode) current).module) {
            visitImports(node.module)
        }
        doWithNode(ClassNode, current) {
            def cur = (ClassNode) current
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
                if (nodeFields.size()==curFields.size()) {
                    iter = curFields.iterator()
                    for (FieldNode fn : nodeFields) {
                        doWithNode(fn.class, iter.next()) {
                            visitField(fn)
                        }
                    }

                    def nodeConstructors = node.declaredConstructors
                    def curConstructors = cur.declaredConstructors
                    if (nodeConstructors.size()==curConstructors.size()) {
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


                return
            }
            match = false
        }
    }

    @Override
    protected void visitObjectInitializerStatements(final ClassNode node) {
        doWithNode(ClassNode, current) {
            def initializers = ((ClassNode)current).objectInitializerStatements
            if (initializers.size()==node.objectInitializerStatements.size()) {
                def iterator = initializers.iterator()
                for (Statement element : node.objectInitializerStatements) {
                    doWithNode(element.class, iterator.next()) {
                        element.visit(this)
                    }
                }
            } else {
                match = false
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
                    match = false
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
                    match = false
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
                    match = false
                    return
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
                    match = false
                    return
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
            if (refAnnotations.size()!=curAnnotations.size()) {
                match = false
                return
            }
            if (refAnnotations.empty) return
            def iter = curAnnotations.iterator()
            for (AnnotationNode an : refAnnotations) {
                AnnotationNode curNext = iter.next()
                // skip built-in properties
                if (an.builtIn) {
                    if (!curNext.builtIn) {
                        match = false
                        return
                    }
                    continue
                }

                def refEntrySet = an.members.entrySet()
                def curEntrySet = curNext.members.entrySet()
                if (refEntrySet.size()==curEntrySet.size()) {
                    def entryIt = curEntrySet.iterator()
                    for (Map.Entry<String, Expression> member : refEntrySet) {
                        def next = entryIt.next()
                        if (next.key==member.key) {
                            doWithNode(member.value.class, next.value) {
                                member.value.visit(this)
                            }
                        } else {
                            match = false
                        }
                    }
                } else {
                    match = false
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
            if (params.length==curParams.length) {
                params.eachWithIndex { Parameter entry, int i ->
                    doWithNode(entry.class, curParams[i]) {
                        visitAnnotations(entry)
                    }
                }
            } else {
                match = false
            }
        }
    }

    @Override
    public void visitField(final FieldNode node) {
        doWithNode(FieldNode, current) {
            visitAnnotations(node)
            def fieldNode = (FieldNode) current
            if (fieldNode.name==node.name) {
                Expression init = node.initialExpression

                Expression curInit = fieldNode.initialExpression
                if (init) {
                    if (curInit) {
                        doWithNode(init.class, curInit) {
                            init.visit(this)
                        }
                    } else {
                        match = false
                    }
                } else if (curInit) {
                    match = false
                }
            } else {
                match = false
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
            doWithNode(statement.class, curStatement) {
                visitClassCodeContainer(statement)
            }

            statement = node.setterBlock
            curStatement = pNode.setterBlock
            doWithNode(statement.class, curStatement) {
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
                    match = false
                }
            } else if (curInit) {
                match = false
            }
        }
    }

    @Override
    void visitExpressionStatement(final ExpressionStatement statement) {
        visitStatement(statement)
        doWithNode(statement.expression.class, ((ExpressionStatement)current).expression) {
            statement.expression.visit(this)
        }
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        doWithNode(BlockStatement, current) {
            def statements = ((BlockStatement) current).statements
            if (statements.size()==block.statements.size()) {
                def iter = statements.iterator()
                for (Statement statement : block.statements) {
                    doWithNode(statement.class, iter.next()) {
                        statement.visit(this)
                    }
                }
            } else {
                match = false
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
            match = match && (call.methodAsString==mce.methodAsString) &&
                    (call.safe==mce.safe) &&
                    (call.spreadSafe == mce.spreadSafe) &&
                    (call.implicitThis == mce.implicitThis)
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
            match = match &&
                    (call.type == cur.type)
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
            if (bin.operation.type!=expression.operation.type) {
                match = false
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
                match = match &&
                        (expression.operation.type==curExpr.operation.type)
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
                match = match &&
                        (expression.operation.type==curExpr.operation.type)
            }
        }
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        doWithNode(BooleanExpression, current) {
            doWithNode(expression.expression.class, ((BooleanExpression)current).expression) {
                expression.expression.visit(this)
            }
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        super.visitNotExpression(expression);
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
        if (nodeParams==null && curParams!=null || nodeParams!=null && curParams==null) {
            match = false
            return
        }
        if (nodeParams) {
            if (curParams.length==nodeParams.length) {
                for (int i = 0; i < nodeParams.length && match; i++) {
                    def n = nodeParams[i]
                    def c = curParams[i]
                    doWithNode(n.class, c) {
                        match = match &&
                                (n.name == c.name) &&
                                (n.originType == c.originType) &&
                                (n.type == c.originType)
                    }
                }
            } else {
                match = false
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
        super.visitListExpression(expression);
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        super.visitMapExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        super.visitMapEntryExpression(expression);
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        super.visitSpreadExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        super.visitMethodPointerExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        super.visitBitwiseNegationExpression(expression);
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitConstantExpression(final ConstantExpression expression) {
        doWithNode(ConstantExpression, current) {
            def cur = (ConstantExpression) current
            super.visitConstantExpression(expression)
            match = match &&
                    (expression.type==cur.type) &&
                    (expression.value==cur.value)
        }
    }

    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    public void visitClassExpression(final ClassExpression expression) {
        doWithNode(ClassExpression, current) {
            super.visitClassExpression(expression)
            def cexp = (ClassExpression) current
            match = match && (cexp.type == expression.type)
        }
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        doWithNode(VariableExpression, current) {
            def curVar = (VariableExpression) current
            match = match &&
                    (expression.name == curVar.name) &&
                    (expression.type == curVar.type) &&
                    (expression.originType == curVar.originType)
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
            match = match &&
                    (expression.propertyAsString==currentPexp.propertyAsString) &&
                    (expression.implicitThis==currentPexp.implicitThis) &&
                    (expression.safe==currentPexp.safe) &&
                    (expression.spreadSafe==currentPexp.spreadSafe)
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        super.visitAttributeExpression(expression);
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        super.visitGStringExpression(expression);
    }

    @Override
    protected void visitListOfExpressions(final List<? extends Expression> list) {
        if (list == null) return;
        def currentExprs = (List<Expression>) current
        if (currentExprs.size()!=list.size()) {
            match = false
            return
        }
        def iter = currentExprs.iterator()
        for (Expression expression : list) {
            def next = iter.next()
            if (expression instanceof SpreadExpression) {
                doWithNode(SpreadExpression, next) {
                    Expression spread = ((SpreadExpression) expression).getExpression()
                    doWithNode(Expression, ((SpreadExpression)current).expression) {
                        spread.visit(this)
                    }
                }
            } else {
                doWithNode(Expression, next) {
                    expression.visit(this)
                }
            }
        }
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        super.visitClosureListExpression(cle);
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        super.visitBytecodeExpression(cle);
    }
}
