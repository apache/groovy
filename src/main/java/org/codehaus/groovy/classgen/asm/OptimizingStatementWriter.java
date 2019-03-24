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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.LinkedList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GROOVY_INTERCEPTABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.tools.WideningCategories.isBigDecCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isDoubleCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isFloatingCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isIntCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isLongCategory;
import static org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher.typeMap;
import static org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher.typeMapKeyNames;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

/**
 * A class to write out the optimized statements
 */
public class OptimizingStatementWriter extends StatementWriter {

    private static class FastPathData {
        private Label pathStart = new Label();
        private Label afterPath = new Label();
    }

    public static class ClassNodeSkip{}

    public static class StatementMeta {
        private boolean optimize=false;
        protected MethodNode target;
        protected ClassNode type;
        protected VariableExpression declaredVariableExpression;
        protected boolean[] involvedTypes = new boolean[typeMapKeyNames.length];
        public void chainInvolvedTypes(OptimizeFlagsCollector opt) {
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (opt.current.involvedTypes[i]) {
                    this.involvedTypes[i] = true;
                }
            }
        }
        public String toString() {
            StringBuilder ret = new StringBuilder("optimize=" + optimize + " target=" + target + " type=" + type + " involvedTypes=");
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (involvedTypes[i]) ret.append(" ").append(typeMapKeyNames[i]);
            }
            return ret.toString();
        }
    }

    private static final MethodCaller[] guards = {
        null,
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigInt"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigL"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigD"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigC"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigB"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigS"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigF"),
        MethodCaller.newStatic(BytecodeInterface8.class, "isOrigZ"),
    };

    private static final MethodCaller disabledStandardMetaClass = MethodCaller.newStatic(BytecodeInterface8.class, "disabledStandardMetaClass");
    private boolean fastPathBlocked = false;
    private final WriterController controller;

    public OptimizingStatementWriter(WriterController controller) {
        super(controller);
        this.controller = controller;
    }

    private boolean notEnableFastPath(StatementMeta meta) {
        // return false if cannot do fast path and if are already on the path
        return fastPathBlocked || meta==null || !meta.optimize || controller.isFastPath();
    }

    private FastPathData writeGuards(StatementMeta meta, Statement statement) {
        if (notEnableFastPath(meta)) return null;
        controller.getAcg().onLineNumber(statement, null);
        MethodVisitor mv = controller.getMethodVisitor();
        FastPathData fastPathData = new FastPathData();
        Label slowPath = new Label();

        for (int i=0; i<guards.length; i++) {
            if (meta.involvedTypes[i]) {
                guards[i].call(mv);
                mv.visitJumpInsn(IFEQ, slowPath);
            }
        }

        // meta class check with boolean holder
        String owner = BytecodeHelper.getClassInternalName(controller.getClassNode());
        MethodNode mn = controller.getMethodNode();
        if (mn!=null) {
            mv.visitFieldInsn(GETSTATIC, owner, Verifier.STATIC_METACLASS_BOOL, "Z");
            mv.visitJumpInsn(IFNE, slowPath);
        }

        //standard metaclass check
        disabledStandardMetaClass.call(mv);
        mv.visitJumpInsn(IFNE, slowPath);

        // other guards here

        mv.visitJumpInsn(GOTO, fastPathData.pathStart);
        mv.visitLabel(slowPath);

        return fastPathData;
    }

    private void writeFastPathPrelude(FastPathData meta) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitJumpInsn(GOTO, meta.afterPath);
        mv.visitLabel(meta.pathStart);
        controller.switchToFastPath();
    }

    private void writeFastPathEpilogue(FastPathData meta) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitLabel(meta.afterPath);
        controller.switchToSlowPath();
    }

    @Override
    public void writeBlockStatement(BlockStatement statement) {
        StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
        FastPathData fastPathData = writeGuards(meta, statement);

        if (fastPathData==null) {
            // normal mode with different paths
            // important is to not to have a fastpathblock here,
            // otherwise the per expression statement improvement 
            // is impossible
            super.writeBlockStatement(statement);
        } else {
            // fast/slow path generation
            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeBlockStatement(statement);
            fastPathBlocked = oldFastPathBlock;

            writeFastPathPrelude(fastPathData);
            super.writeBlockStatement(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    @Override
    public void writeDoWhileLoop(DoWhileStatement statement) {
        if (controller.isFastPath()) {
            super.writeDoWhileLoop(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            FastPathData fastPathData = writeGuards(meta, statement);

            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeDoWhileLoop(statement);
            fastPathBlocked = oldFastPathBlock;

            if (fastPathData==null) return;
            writeFastPathPrelude(fastPathData);
            super.writeDoWhileLoop(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    @Override
    protected void writeIteratorHasNext(MethodVisitor mv) {
        if (controller.isFastPath()) {
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        } else {
            super.writeIteratorHasNext(mv);
        }
    }

    @Override
    protected void writeIteratorNext(MethodVisitor mv) {
        if (controller.isFastPath()) {
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        } else {
            super.writeIteratorNext(mv);
        }
    }

    @Override
    protected void writeForInLoop(ForStatement statement) {
        if (controller.isFastPath()) {
            super.writeForInLoop(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            FastPathData fastPathData = writeGuards(meta, statement);

            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeForInLoop(statement);
            fastPathBlocked = oldFastPathBlock;

            if (fastPathData==null) return;
            writeFastPathPrelude(fastPathData);
            super.writeForInLoop(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    @Override
    protected void writeForLoopWithClosureList(ForStatement statement) {
        if (controller.isFastPath()) {
            super.writeForLoopWithClosureList(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            FastPathData fastPathData = writeGuards(meta, statement);

            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeForLoopWithClosureList(statement);
            fastPathBlocked = oldFastPathBlock;

            if (fastPathData==null) return;
            writeFastPathPrelude(fastPathData);
            super.writeForLoopWithClosureList(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    @Override
    public void writeWhileLoop(WhileStatement statement) {
        if (controller.isFastPath()) {
            super.writeWhileLoop(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            FastPathData fastPathData = writeGuards(meta, statement);

            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeWhileLoop(statement);
            fastPathBlocked = oldFastPathBlock;

            if (fastPathData==null) return;
            writeFastPathPrelude(fastPathData);
            super.writeWhileLoop(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    @Override
    public void writeIfElse(IfStatement statement) {
        StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
        FastPathData fastPathData = writeGuards(meta, statement);

        if (fastPathData==null) {
            super.writeIfElse(statement);
        } else {
            boolean oldFastPathBlock = fastPathBlocked;
            fastPathBlocked = true;
            super.writeIfElse(statement);
            fastPathBlocked = oldFastPathBlock;

            writeFastPathPrelude(fastPathData);
            super.writeIfElse(statement);
            writeFastPathEpilogue(fastPathData);
        }
    }

    private boolean isNewPathFork(StatementMeta meta) {
        // meta.optimize -> can do fast path
        if (meta==null || !meta.optimize) return false;
        // fastPathBlocked -> slow path
        if (fastPathBlocked) return false;
        // controller.isFastPath() -> fastPath
        return !controller.isFastPath();
    }

    @Override
    public void writeReturn(ReturnStatement statement) {
        if (controller.isFastPath()) {
            super.writeReturn(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            if (isNewPathFork(meta) && writeDeclarationExtraction(statement)) {
                if (meta.declaredVariableExpression != null) {
                    // declaration was replaced by assignment so we need to define the variable
                    controller.getCompileStack().defineVariable(meta.declaredVariableExpression, false);
                }
                FastPathData fastPathData = writeGuards(meta, statement);

                boolean oldFastPathBlock = fastPathBlocked;
                fastPathBlocked = true;
                super.writeReturn(statement);
                fastPathBlocked = oldFastPathBlock;

                if (fastPathData==null) return;
                writeFastPathPrelude(fastPathData);
                super.writeReturn(statement);
                writeFastPathEpilogue(fastPathData);
            } else {
                super.writeReturn(statement);
            }
        }
    }

    @Override
    public void writeExpressionStatement(ExpressionStatement statement) {
        if (controller.isFastPath()) {
            super.writeExpressionStatement(statement);
        } else {
            StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
            // we have to have handle DelcarationExpressions special, since their 
            // entry should be outside the optimization path, we have to do that of
            // course only if we are actually going to do two different paths, 
            // otherwise it is not needed
            //
            // there are several cases to be considered now.
            // (1) no fast path possible, so just do super
            // (2) fast path possible, and at path split point (meaning not in 
            //     fast path and not in slow path). Here we have to extract the 
            //     Declaration and replace by an assignment
            // (3) fast path possible and in slow or fastPath. Nothing to do here.
            //
            // the only case we need to handle is then (2).

            if (isNewPathFork(meta) && writeDeclarationExtraction(statement)) {
                if (meta.declaredVariableExpression != null) {
                    // declaration was replaced by assignment so we need to define the variable
                    controller.getCompileStack().defineVariable(meta.declaredVariableExpression, false);
                }
                FastPathData fastPathData = writeGuards(meta, statement);

                boolean oldFastPathBlock = fastPathBlocked;
                fastPathBlocked = true;
                super.writeExpressionStatement(statement);
                fastPathBlocked = oldFastPathBlock;

                if (fastPathData==null) return;
                writeFastPathPrelude(fastPathData);
                super.writeExpressionStatement(statement);
                writeFastPathEpilogue(fastPathData);
            } else {
                super.writeExpressionStatement(statement);
            }
        }
    }

    private boolean writeDeclarationExtraction(Statement statement) {
        Expression ex = null;
        if (statement instanceof ReturnStatement) {
            ReturnStatement rs = (ReturnStatement) statement;
            ex = rs.getExpression();
        } else if (statement instanceof ExpressionStatement) {
            ExpressionStatement es = (ExpressionStatement) statement;
            ex = es.getExpression();
        } else {
            throw new GroovyBugError("unknown statement type :"+statement.getClass());
        }
        if (!(ex instanceof DeclarationExpression)) return true;
        DeclarationExpression declaration = (DeclarationExpression) ex;
        ex = declaration.getLeftExpression();
        if (ex instanceof TupleExpression) return false;

        // stash declared variable in case we do subsequent visits after we
        // change to assignment only
        StatementMeta meta = statement.getNodeMetaData(StatementMeta.class);
        if (meta != null) {
            meta.declaredVariableExpression = declaration.getVariableExpression();
        }

        // change statement to do assignment only
        BinaryExpression assignment = new BinaryExpression(
                declaration.getLeftExpression(),
                declaration.getOperation(),
                declaration.getRightExpression());
        assignment.setSourcePosition(declaration);
        assignment.copyNodeMetaData(declaration);
        // replace statement code
        if (statement instanceof ReturnStatement) {
            ReturnStatement rs = (ReturnStatement) statement;
            rs.setExpression(assignment);
        } else if (statement instanceof ExpressionStatement) {
            ExpressionStatement es = (ExpressionStatement) statement;
            es.setExpression(assignment);
        } else {
            throw new GroovyBugError("unknown statement type :"+statement.getClass());
        }
        return true;
    }

    public static void setNodeMeta(TypeChooser chooser, ClassNode classNode) {
        if (classNode.getNodeMetaData(ClassNodeSkip.class)!=null) return;
        new OptVisitor(chooser).visitClass(classNode);
    }

    private static StatementMeta addMeta(ASTNode node) {
        StatementMeta metaOld = node.getNodeMetaData(StatementMeta.class);
        StatementMeta meta = metaOld;
        if (meta==null) meta = new StatementMeta();
        meta.optimize = true;
        if (metaOld==null) node.setNodeMetaData(StatementMeta.class, meta);
        return meta;
    }

    private static StatementMeta addMeta(ASTNode node, OptimizeFlagsCollector opt) {
        StatementMeta meta = addMeta(node);
        meta.chainInvolvedTypes(opt);
        return meta;
    }

    private static class OptimizeFlagsCollector {
        private static class OptimizeFlagsEntry {
            private boolean canOptimize = false;
            private boolean shouldOptimize = false;
            private boolean[] involvedTypes = new boolean[typeMapKeyNames.length];
        }
        private OptimizeFlagsEntry current = new OptimizeFlagsEntry();
        private final LinkedList<OptimizeFlagsEntry> olderEntries = new LinkedList<OptimizeFlagsEntry>();
        public void push() {
            olderEntries.addLast(current);
            current = new OptimizeFlagsEntry();
        }
        public void pop(boolean propagateFlags){
            OptimizeFlagsEntry old = current;
            current = olderEntries.removeLast();
            if (propagateFlags) {
                chainCanOptimize(old.canOptimize);
                chainShouldOptimize(old.shouldOptimize);
                for (int i=0; i<typeMapKeyNames.length; i++) current.involvedTypes[i] |= old.involvedTypes[i];
            }
        }
        public String toString() {
            StringBuilder ret;
            if (current.shouldOptimize) {
                ret = new StringBuilder("should optimize, can = " + current.canOptimize);
            } else if (current.canOptimize) {
                ret = new StringBuilder("can optimize");
            } else {
                ret = new StringBuilder("don't optimize");
            }
            ret.append(" involvedTypes =");
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (current.involvedTypes[i]) ret.append(" ").append(typeMapKeyNames[i]);
            }
            return ret.toString();
        }
        /**
         * @return true iff we should Optimize - this is almost seen as must
         */
        private boolean shouldOptimize() {
            return current.shouldOptimize;
        }
        /**
         * @return true iff we can optimize, but not have to
         */
        private boolean canOptimize() {
            return current.canOptimize || current.shouldOptimize;
        }
        /**
         * set "should" to true, if not already
         */
        public void chainShouldOptimize(boolean opt) {
            current.shouldOptimize = shouldOptimize() || opt;
        }
        /**
         * set "can" to true, if not already
         */
        public void chainCanOptimize(boolean opt) {
            current.canOptimize = current.canOptimize || opt;
        }
        public void chainInvolvedType(ClassNode type) {
            Integer res = typeMap.get(type);
            if (res==null) return;
            current.involvedTypes[res] = true;
        }
        public void reset() {
            current.canOptimize = false;
            current.shouldOptimize = false;
            current.involvedTypes = new boolean[typeMapKeyNames.length];
        }
    }

    private static class OptVisitor extends ClassCodeVisitorSupport {
        private final TypeChooser typeChooser;

        public OptVisitor(final TypeChooser chooser) {
            this.typeChooser = chooser;
        }

        @Override protected SourceUnit getSourceUnit() {return null;}

        private ClassNode node;
        private OptimizeFlagsCollector opt = new OptimizeFlagsCollector();
        private boolean optimizeMethodCall = true;
        private VariableScope scope;
        private static final VariableScope nonStaticScope = new VariableScope();

        @Override
        public void visitClass(ClassNode node) {
            this.optimizeMethodCall = !node.implementsInterface(GROOVY_INTERCEPTABLE_TYPE);
            this.node = node;
            this.scope = nonStaticScope;
            super.visitClass(node);
            this.scope=null;
            this.node=null;
        }

        @Override
        public void visitMethod(MethodNode node) {
            scope = node.getVariableScope();
            super.visitMethod(node);
            opt.reset();
        }

        @Override
        public void visitConstructor(ConstructorNode node) {
            scope = node.getVariableScope();
            super.visitConstructor(node);
        }

        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            opt.push();
            super.visitReturnStatement(statement);
            if (opt.shouldOptimize()) addMeta(statement,opt);
            opt.pop(opt.shouldOptimize());
        }

        @Override
        public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
            //TODO: implement int operations for this
            super.visitUnaryMinusExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = OBJECT_TYPE;
        }

        @Override
        public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
            //TODO: implement int operations for this
            super.visitUnaryPlusExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = OBJECT_TYPE;
        }

        @Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
            //TODO: implement int operations for this
            super.visitBitwiseNegationExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = OBJECT_TYPE;
        }

        private void addTypeInformation(Expression expression, Expression orig) {
            ClassNode type = typeChooser.resolveType(expression, node);
            if (isPrimitiveType(type)) {
                StatementMeta meta = addMeta(orig);
                meta.type = type;
                opt.chainShouldOptimize(true);
                opt.chainInvolvedType(type);
            }
        }

        @Override
        public void visitPrefixExpression(PrefixExpression expression) {
            super.visitPrefixExpression(expression);
            addTypeInformation(expression.getExpression(),expression);
        }

        @Override
        public void visitPostfixExpression(PostfixExpression expression) {
            super.visitPostfixExpression(expression);
            addTypeInformation(expression.getExpression(),expression);
        }

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            Expression right = expression.getRightExpression();
            right.accept(this);

            ClassNode leftType = typeChooser.resolveType(expression.getLeftExpression(), node);
            Expression rightExpression = expression.getRightExpression();
            ClassNode rightType = optimizeDivWithIntOrLongTarget(rightExpression, leftType);
            if (rightType==null) rightType = typeChooser.resolveType(expression.getRightExpression(), node);
            if (isPrimitiveType(leftType) && isPrimitiveType(rightType)) {
                // if right is a constant, then we optimize only if it makes
                // a block complete, so we set a maybe
                if (right instanceof ConstantExpression) {
                    opt.chainCanOptimize(true);
                } else {
                    opt.chainShouldOptimize(true);
                }
                StatementMeta meta = addMeta(expression);
                ClassNode declarationType = typeChooser.resolveType(expression, node);
                meta.type = declarationType!=null?declarationType:leftType;
                opt.chainInvolvedType(leftType);
                opt.chainInvolvedType(rightType);
            }
        }

        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitBinaryExpression(expression);

            ClassNode leftType = typeChooser.resolveType(expression.getLeftExpression(), node);
            ClassNode rightType = typeChooser.resolveType(expression.getRightExpression(), node);
            ClassNode resultType = null;
            int operation = expression.getOperation().getType();

            if (operation==Types.LEFT_SQUARE_BRACKET && leftType.isArray()) {
                opt.chainShouldOptimize(true);
                resultType = leftType.getComponentType();
            } else {
                switch (operation) {
                    case Types.COMPARE_EQUAL:
                    case Types.COMPARE_LESS_THAN:
                    case Types.COMPARE_LESS_THAN_EQUAL:
                    case Types.COMPARE_GREATER_THAN:
                    case Types.COMPARE_GREATER_THAN_EQUAL:
                    case Types.COMPARE_NOT_EQUAL:
                        if (isIntCategory(leftType) && isIntCategory(rightType)) {
                            opt.chainShouldOptimize(true);
                        } else if (isLongCategory(leftType) && isLongCategory(rightType)) {
                            opt.chainShouldOptimize(true);
                        } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                            opt.chainShouldOptimize(true);
                        } else {
                            opt.chainCanOptimize(true);
                        }
                        resultType = boolean_TYPE;
                        break;
                    case Types.LOGICAL_AND: case Types.LOGICAL_AND_EQUAL:
                    case Types.LOGICAL_OR: case Types.LOGICAL_OR_EQUAL:
                        if (boolean_TYPE.equals(leftType) && boolean_TYPE.equals(rightType)) {
                            opt.chainShouldOptimize(true);
                        } else {
                            opt.chainCanOptimize(true);
                        }
                        expression.setType(boolean_TYPE);
                        resultType = boolean_TYPE;
                        break;
                    case Types.DIVIDE: case Types.DIVIDE_EQUAL:
                        if (isLongCategory(leftType) && isLongCategory(rightType)) {
                            resultType = BigDecimal_TYPE;
                            opt.chainShouldOptimize(true);
                        } else if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) {
                            // no optimization for BigDecimal yet
                            //resultType = BigDecimal_TYPE;
                        } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                            resultType = double_TYPE;
                            opt.chainShouldOptimize(true);
                        }
                        break;
                    case Types.POWER: case Types.POWER_EQUAL:
                        //TODO: implement
                        break;
                    case Types.ASSIGN:
                        resultType = optimizeDivWithIntOrLongTarget(expression.getRightExpression(), leftType);
                        opt.chainCanOptimize(true);
                        break;
                    default:
                        if (isIntCategory(leftType) && isIntCategory(rightType)) {
                            resultType = int_TYPE;
                            opt.chainShouldOptimize(true);
                        } else if (isLongCategory(leftType) && isLongCategory(rightType)) {
                            resultType = long_TYPE;
                            opt.chainShouldOptimize(true);
                        } else if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) {
                            // no optimization for BigDecimal yet
                            //resultType = BigDecimal_TYPE;
                        } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                            resultType = double_TYPE;
                            opt.chainShouldOptimize(true);
                        }
                }
            }

            if (resultType!=null) {
                StatementMeta meta = addMeta(expression);
                meta.type = resultType;
                opt.chainInvolvedType(resultType);
                opt.chainInvolvedType(leftType);
                opt.chainInvolvedType(rightType);
            }
        }

        /**
         * method to optimize Z = X/Y with Z being int or long style
         * @returns null if the optimization cannot be applied, otherwise it
         * will return the new target type
         */
        private ClassNode optimizeDivWithIntOrLongTarget(Expression rhs, ClassNode assignmentTartgetType) {
            if (!(rhs instanceof BinaryExpression)) return null;
            BinaryExpression binExp = (BinaryExpression) rhs;
            int op = binExp.getOperation().getType();
            if (op!=Types.DIVIDE && op!=Types.DIVIDE_EQUAL) return null;

            ClassNode originalResultType = typeChooser.resolveType(binExp, node);
            if (    !originalResultType.equals(BigDecimal_TYPE) ||
                    !(isLongCategory(assignmentTartgetType) || isFloatingCategory(assignmentTartgetType))
            ) {
                return null;
            }

            ClassNode leftType = typeChooser.resolveType(binExp.getLeftExpression(), node);
            if (!isLongCategory(leftType)) return null;
            ClassNode rightType = typeChooser.resolveType(binExp.getRightExpression(), node);
            if (!isLongCategory(rightType)) return null;

            ClassNode target;
            if (isIntCategory(leftType) && isIntCategory(rightType)) {
                target = int_TYPE;
            } else if (isLongCategory(leftType) && isLongCategory(rightType)) {
                target = long_TYPE;
            } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                target = double_TYPE;
            } else {
                return null;
            }
            StatementMeta meta = addMeta(rhs);
            meta.type = target;
            opt.chainInvolvedType(target);
            return target;
        }

        @Override
        public void visitExpressionStatement(ExpressionStatement statement) {
            if (statement.getNodeMetaData(StatementMeta.class)!=null) return;
            opt.push();
            super.visitExpressionStatement(statement);
            if (opt.shouldOptimize()) addMeta(statement,opt);
            opt.pop(opt.shouldOptimize());
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            opt.push();
            boolean optAll = true;
            for (Statement statement : block.getStatements()) {
                opt.push();
                statement.accept(this);
                optAll = optAll && opt.canOptimize();
                opt.pop(true);
            }
            if (block.isEmpty()) {
                opt.chainCanOptimize(true);
                opt.pop(true);
            } else {
                opt.chainShouldOptimize(optAll);
                if (optAll) addMeta(block,opt);
                opt.pop(optAll);
            }
        }

        @Override
        public void visitIfElse(IfStatement statement) {
            opt.push();
            super.visitIfElse(statement);
            if (opt.shouldOptimize()) addMeta(statement,opt);
            opt.pop(opt.shouldOptimize());
        }

        /*@Override
        public void visitConstantExpression(ConstantExpression expression) {
            super.visitConstantExpression(expression);
            opt.chainShouldOptimize(true);
        }*/

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitStaticMethodCallExpression(expression);

            setMethodTarget(expression,expression.getMethod(), expression.getArguments(), true);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitMethodCallExpression(expression);

            Expression object = expression.getObjectExpression();
            boolean setTarget = AsmClassGenerator.isThisExpression(object);
            if (!setTarget) {
                if (!(object instanceof ClassExpression)) return;
                setTarget = object.equals(node);
            }

            if (!setTarget) return;
            setMethodTarget(expression, expression.getMethodAsString(), expression.getArguments(), true);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            if (call.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitConstructorCallExpression(call);

            // we cannot a target for the constructor call, since we cannot easily
            // check the meta class of the other class
            // setMethodTarget(call, "<init>", call.getArguments(), false);
        }

        private void setMethodTarget(Expression expression, String name, Expression callArgs, boolean isMethod) {
            if (name==null) return;
            if (!optimizeMethodCall) return;
            if (AsmClassGenerator.containsSpreadExpression(callArgs)) return;
            // find method call target
            Parameter[] paraTypes = null;
            if (callArgs instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) callArgs;
                int size = args.getExpressions().size();
                paraTypes = new Parameter[size];
                int i=0;
                for (Expression exp: args.getExpressions()) {
                    ClassNode type = typeChooser.resolveType(exp, node);
                    if (!validTypeForCall(type)) return;
                    paraTypes[i] = new Parameter(type,"");
                    i++;
                }
            } else {
                ClassNode type = typeChooser.resolveType(callArgs, node);
                if (!validTypeForCall(type)) return;
                paraTypes = new Parameter[]{new Parameter(type,"")};
            }

            MethodNode target;
            ClassNode type;
            if (isMethod) {
                target = node.getMethod(name, paraTypes);
                if (target==null) return;
                if (!target.getDeclaringClass().equals(node)) return;
                if (scope.isInStaticContext() && !target.isStatic()) return;
                type = target.getReturnType().redirect();
            } else {
                type = expression.getType();
                target = selectConstructor(type, paraTypes);
                if (target==null) return;
            }

            StatementMeta meta = addMeta(expression);
            meta.target = target;
            meta.type = type;
            opt.chainShouldOptimize(true);
        }

        private static MethodNode selectConstructor(ClassNode node, Parameter[] paraTypes) {
            List<ConstructorNode> cl = node.getDeclaredConstructors();
            MethodNode res = null;
            for (ConstructorNode cn : cl) {
                if (ParameterUtils.parametersEqual(cn.getParameters(), paraTypes)) {
                    res = cn;
                    break;
                }
            }
            if (res !=null && res.isPublic()) return res;
            return null;
        }

        private static boolean validTypeForCall(ClassNode type) {
            // do call only for final classes and primitive types
            if (isPrimitiveType(type)) return true;
            return (type.getModifiers() & ACC_FINAL) > 0;
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
        }

        @Override
        public void visitForLoop(ForStatement statement) {
            opt.push();
            super.visitForLoop(statement);
            if (opt.shouldOptimize()) addMeta(statement,opt);
            opt.pop(opt.shouldOptimize());
        }
    }


}
