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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
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
import org.codehaus.groovy.syntax.Types;
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
        public String toString() {
            return  "optimize="+optimize+" optimizeInt="+optimizeInt+
                    " target="+target+" type="+type;
        }
    }

    private static final MethodCaller isOrigInt = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "isOrigInt");
    private boolean fastPathBlocked = false;
    private WriterController controller;

    public OptimizingStatementWriter(WriterController controller) {
        super(controller);
        this.controller = controller;
    }
    
    private boolean canNotDoFastPath(StatementMeta meta) {
        // return false if cannot do fast path and if are already on the path
        return fastPathBlocked || meta==null || !meta.optimize || controller.isFastPath();
    }
    
    private FastPathData writeGuards(StatementMeta meta, Statement statement) {
        if (canNotDoFastPath(meta)) return null;
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
    
    private boolean isNewPathFork(StatementMeta meta) {
        // meta.optimize -> can do fast path
        if (meta==null || meta.optimize==false) return false;
        // fastPathBlocked -> slow path
        if (fastPathBlocked) return false;
        // controller.isFastPath() -> fastPath
        if (controller.isFastPath()) return false;
        return true;
    }
    
    @Override
    public void writeReturn(ReturnStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
        if (isNewPathFork(meta) && writeDeclarationExtraction(statement)) {
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

    @Override
    public void writeExpressionStatement(ExpressionStatement statement) {
        StatementMeta meta = (StatementMeta) statement.getNodeMetaData(StatementMeta.class);
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
                
        // do declaration
        controller.getCompileStack().defineVariable(declaration.getVariableExpression(), false);
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
    
    public static void setNodeMeta(ClassNode classNode) {
       new OptVisitor().visitClass(classNode);   
    }
    
    private static StatementMeta addMeta(ASTNode node) {
        StatementMeta metaOld = (StatementMeta) node.getNodeMetaData(StatementMeta.class);
        StatementMeta meta = metaOld;
        if (meta==null) meta = new StatementMeta();
        meta.optimize = true;
        meta.optimizeInt = true;
        if (metaOld==null) node.setNodeMetaData(StatementMeta.class, meta);
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
        public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
            //TODO: implement int operations for this
            super.visitUnaryMinusExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = ClassHelper.OBJECT_TYPE;
        }
        
        @Override
        public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
            //TODO: implement int operations for this
            super.visitUnaryPlusExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = ClassHelper.OBJECT_TYPE;
        }
        
        @Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
            //TODO: implement int operations for this
            super.visitBitwiseNegationExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = ClassHelper.OBJECT_TYPE;
        }
        
        @Override
        public void visitPrefixExpression(PrefixExpression expression) {
            //TODO: implement int operations for this
            super.visitPrefixExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = ClassHelper.OBJECT_TYPE;
        }
        
        @Override
        public void visitPostfixExpression(PostfixExpression expression) {
            //TODO: implement int operations for this
            super.visitPostfixExpression(expression);
            StatementMeta meta = addMeta(expression);
            meta.type = ClassHelper.OBJECT_TYPE;
        }        
        
        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            Expression right = expression.getRightExpression();
            right.visit(this);
            boolean rightInt = BinaryIntExpressionHelper.isIntOperand(right, node);
            boolean leftInt = BinaryIntExpressionHelper.isIntOperand(expression.getLeftExpression(), node);
            if (!optimizeInt) {
                optimizeInt =   (leftInt && rightInt) &&
                                !(right instanceof ConstantExpression);
            }
            if (optimizeInt) {
                StatementMeta meta = addMeta(expression);
                if (leftInt && rightInt) meta.type = ClassHelper.int_TYPE;
            }
        }
        
        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitBinaryExpression(expression);
            boolean leftInt = BinaryIntExpressionHelper.isIntOperand(expression.getLeftExpression(), node);
            boolean rightInt = BinaryIntExpressionHelper.isIntOperand(expression.getRightExpression(), node);
            if (!optimizeInt) optimizeInt = (leftInt && rightInt);
            if (optimizeInt) {
                switch (expression.getOperation().getType()) {
                    case Types.DIVIDE: case Types.POWER: 
                    case Types.MULTIPLY: case Types.PLUS_PLUS: 
                    case Types.MINUS_MINUS:
                        optimizeInt = false;
                        break;
                    case Types.COMPARE_EQUAL: 
                    case Types.COMPARE_LESS_THAN:
                    case Types.COMPARE_LESS_THAN_EQUAL:
                    case Types.COMPARE_GREATER_THAN:
                    case Types.COMPARE_GREATER_THAN_EQUAL:
                    case Types.COMPARE_NOT_EQUAL:
                    case Types.LOGICAL_AND:
                    case Types.LOGICAL_OR:
                        expression.setType(ClassHelper.boolean_TYPE);
                        break;
                    case Types.BITWISE_AND:
                    case Types.BITWISE_OR:
                    case Types.BITWISE_XOR:
                        expression.setType(ClassHelper.int_TYPE);
                        break;
                    default:
                }   
            }
                
            if (optimizeInt) {
                StatementMeta meta = addMeta(expression);
                if (leftInt && rightInt) meta.type = ClassHelper.int_TYPE;
            }
        }
        
        @Override
        public void visitExpressionStatement(ExpressionStatement statement) {
            if (statement.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitExpressionStatement(statement);
            if (optimizeInt) addMeta(statement);
        }
        
        @Override
        public void visitBlockStatement(BlockStatement block) {
            boolean optAll = true;
            for (Statement statement : block.getStatements()) {
                optimizeInt = false;
                statement.visit(this);
                optAll = optAll && optimizeInt;
            }
            optAll = optAll && !block.isEmpty();
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
