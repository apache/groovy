/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm;


import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class OptimizingStatementWriter extends StatementWriter {
    
    private static class FastPathData {
        private Label pathStart = new Label();
        private Label afterPath = new Label();
    }
    
    public static class StatementMeta {
        private boolean optimize=false;
        private boolean optimizeInt=false;
        protected MethodNode target;
        protected ClassNode type;
    }

    private static final MethodCaller isOrigInt = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isOrigInt");
    private boolean fastPathBlocked = false;
    private WriterController controller;

    public OptimizingStatementWriter(WriterController controller) {
        super(controller);
        this.controller = controller;
    }
    
    private FastPathData writeGuards(StatementMeta meta, Statement statement) {
        if (fastPathBlocked || meta==null || !meta.optimize || controller.isFastPath()) return null;
        MethodVisitor mv = controller.getMethodVisitor();
        FastPathData fastPathData = new FastPathData();
        
        if (meta.optimizeInt) {
            isOrigInt.call(mv);
            mv.visitJumpInsn(IFNE, fastPathData.pathStart);
        } 
        // other guards here
        
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
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
        FastPathData fastPathData = writeGuards(meta, statement);
        
        boolean oldFastPathBlock = fastPathBlocked;
        fastPathBlocked = true;
        super.writeBlockStatement(statement);
        fastPathBlocked = oldFastPathBlock;
        
        if (fastPathData==null) return;
        writeFastPathPrelude(fastPathData);
        super.writeBlockStatement(statement);
        writeFastPathEpilogue(fastPathData);
    }
    
    @Override
    public void writeDoWhileLoop(DoWhileStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
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
    
    @Override
    protected void writeIteratorHasNext(MethodVisitor mv) {
        if (controller.isFastPath()) {
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        } else {
            super.writeIteratorHasNext(mv);
        }
    }
    
    @Override
    protected void writeIteratorNext(MethodVisitor mv) {
        if (controller.isFastPath()) {
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
        } else {
            super.writeIteratorNext(mv);
        }
    }
    
    @Override
    protected void writeForInLoop(ForStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
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

    @Override
    protected void writeForLoopWithClosureList(ForStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
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

    @Override
    public void writeWhileLoop(WhileStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
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

    @Override
    public void writeIfElse(IfStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
        FastPathData fastPathData = writeGuards(meta, statement);

        boolean oldFastPathBlock = fastPathBlocked;
        fastPathBlocked = true;
        super.writeIfElse(statement);
        fastPathBlocked = oldFastPathBlock;
        
        if (fastPathData==null) return;
        writeFastPathPrelude(fastPathData);
        super.writeIfElse(statement);
        writeFastPathEpilogue(fastPathData);
    }

    @Override
    public void writeExpressionStatement(ExpressionStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
        FastPathData fastPathData = writeGuards(meta, statement);

        boolean oldFastPathBlock = fastPathBlocked;
        fastPathBlocked = true;
        super.writeExpressionStatement(statement);
        fastPathBlocked = oldFastPathBlock;
        
        if (fastPathData==null) return;
        writeFastPathPrelude(fastPathData);
        super.writeExpressionStatement(statement);
        writeFastPathEpilogue(fastPathData);
    }

    public static void setNodeMeta(ClassNode classNode) {
       new OptVisitor().visitClass(classNode);   
    }
    
    private static StatementMeta addMeta(ASTNode node) {
        StatementMeta meta = new StatementMeta();
        meta.optimize = true;
        meta.optimizeInt = true;
        node.setNodeMetaData(StatementMeta.class, meta);
        return meta;
    }
    
    private static class OptVisitor extends ClassCodeVisitorSupport {
        @Override protected SourceUnit getSourceUnit() {return null;}

        private boolean optimizeInt;
        private ClassNode node;
        
        @Override
        public void visitClass(ClassNode node) {
            this.node = node;
            super.visitClass(node);
        }
        
        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            if (!optimizeInt) super.visitReturnStatement(statement);
            if (optimizeInt) addMeta(statement);
        }
        
        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitBinaryExpression(expression);
            boolean leftInt = BinaryIntExpressionHelper.isIntOperand(expression.getLeftExpression());
            boolean rightInt = BinaryIntExpressionHelper.isIntOperand(expression.getRightExpression());
            if (!optimizeInt) optimizeInt =   leftInt || rightInt;
            if (optimizeInt) {
                StatementMeta meta = addMeta(expression);
                if (leftInt && rightInt) meta.type = ClassHelper.int_TYPE;
            }
        }
        
        @Override
        public void visitBlockStatement(BlockStatement block) {
            boolean optAll = false;
            for (Statement statement : block.getStatements()) {
                optimizeInt = false;
                statement.visit(this);
                optAll = optAll || optimizeInt;
            }
            if (optAll) addMeta(block);
            optimizeInt = optAll;
        }
        
        @Override
        public void visitIfElse(IfStatement statement) {
            super.visitIfElse(statement);
            if (optimizeInt) addMeta(statement);
        }
        
        @Override
        public void visitMethodCallExpression(MethodCallExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitMethodCallExpression(expression);
            if (!AsmClassGenerator.isThisExpression(expression.getObjectExpression())) return;
            String name = expression.getMethodAsString();
            if (name==null) return;
            // find method call target
            Expression callArgs = expression.getArguments();
            Parameter[] paraTypes = null;
            if (callArgs instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) callArgs;
                int size = args.getExpressions().size();
                paraTypes = new Parameter[size];
                int i=0;
                for (Expression exp: args.getExpressions()) {
                    ClassNode type = getType(exp);
                    paraTypes[i] = new Parameter(type,"");
                    i++;
                }
            } else {
                ClassNode type = getType(callArgs);
                paraTypes = new Parameter[]{new Parameter(type,"")};
            }
            
            MethodNode target = node.getMethod(name, paraTypes);
            StatementMeta meta = addMeta(expression);
            meta.target = target;
            if (target!=null) meta.type = target.getReturnType().redirect();
            if (!optimizeInt) meta.optimizeInt =false;
        }
        
        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            return;
        }
    }

    private static ClassNode getType(Expression exp) {
        ClassNode type = exp.getType();
        StatementMeta meta = (StatementMeta) exp.getNodeMetaData(StatementMeta.class);
        if (meta==null || meta.type==null) return type;
        return meta.type;
    }
    

}
