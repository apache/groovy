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
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
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
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.BytecodeInterface8;
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
    
    public static class ClassNodeSkip{}
    
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

    private static final MethodCaller isOrigInt = MethodCaller.newStatic(BytecodeInterface8.class, "isOrigInt");
    private static final MethodCaller disabledStandardMetaClass = MethodCaller.newStatic(BytecodeInterface8.class, "disabledStandardMetaClass");
    private boolean fastPathBlocked = false;
    private WriterController controller;

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
        MethodVisitor mv = controller.getMethodVisitor();
        FastPathData fastPathData = new FastPathData();
        Label slowPath = new Label();
        
        if (meta.optimizeInt) {
            isOrigInt.call(mv);
            mv.visitJumpInsn(IFEQ, slowPath);
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
        if (controller.isFastPath()) {
            super.writeDoWhileLoop(statement);
        } else {
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
        if (controller.isFastPath()) {
            super.writeForInLoop(statement);
        } else {
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
    }

    @Override
    protected void writeForLoopWithClosureList(ForStatement statement) {
        if (controller.isFastPath()) {
            super.writeForLoopWithClosureList(statement);
        } else {
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
    }

    @Override
    public void writeWhileLoop(WhileStatement statement) {
        if (controller.isFastPath()) {
            super.writeWhileLoop(statement);
        } else {
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
    }

    @Override
    public void writeIfElse(IfStatement statement) {
        if (controller.isFastPath()) {
            super.writeIfElse(statement);
        } else {
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
        if (controller.isFastPath()) {
            super.writeReturn(statement);
        } else {
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
    }

    @Override
    public void writeExpressionStatement(ExpressionStatement statement) {
        if (controller.isFastPath()) {
            super.writeExpressionStatement(statement);
        } else {
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
        if (classNode.getNodeMetaData(ClassNodeSkip.class)!=null) return;
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
    
    private static class OptimizeFlags {
        private boolean canOptimize = false;
        private boolean shouldOptimize = false;
        public String toString() {
            if (shouldOptimize) {
                return "should optimize, can = "+canOptimize;
            } else if (canOptimize) {
                return "can optimize";
            } else {
                return "don't optimize";
            }
        }
        /**
         * @return true iff we should Optimize - this is almost seen as must
         */
        private boolean shouldOptimize() {
            return shouldOptimize;
        }
        /**
         * @return true iff we can optimize, but not have to
         */
        private boolean canOptimize() {
            return canOptimize || shouldOptimize;
        }
        /**
         * set optimization flags to false
         */
        public void reset() {
            canOptimize = false;
            shouldOptimize = false;
        }
        /**
         * set "should" to true, if not already
         */
        public void chainShouldOptimize(boolean opt) {
            shouldOptimize = shouldOptimize() || opt;
        }
        /**
         * set "can" to true, if not already
         */
        public void chainCanOptimize(boolean opt) {
            canOptimize = canOptimize || opt;
        }
    }
    
    private static class OptVisitor extends ClassCodeVisitorSupport {
        @Override protected SourceUnit getSourceUnit() {return null;}

        private ClassNode node;
        private OptimizeFlags opt = new OptimizeFlags();
        
        @Override
        public void visitClass(ClassNode node) {
            this.node = node;
            super.visitClass(node);
        }
        
        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            super.visitReturnStatement(statement);
            if (opt.shouldOptimize()) addMeta(statement);
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
            super.visitPrefixExpression(expression);
            boolean isInt = BinaryIntExpressionHelper.isIntOperand(expression.getExpression(),node); 
            if (isInt) {
                StatementMeta meta = addMeta(expression);
                meta.type = ClassHelper.int_TYPE;
                opt.chainCanOptimize(true);
            }
        }
        
        @Override
        public void visitPostfixExpression(PostfixExpression expression) {
            super.visitPostfixExpression(expression);
            boolean isInt = BinaryIntExpressionHelper.isIntOperand(expression.getExpression(),node); 
            if (isInt) {
                StatementMeta meta = addMeta(expression);
                meta.type = ClassHelper.int_TYPE;
                opt.chainCanOptimize(true);
            }
        }        
        
        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            Expression right = expression.getRightExpression();
            right.visit(this);
            boolean rightInt = BinaryIntExpressionHelper.isIntOperand(right, node);
            boolean leftInt = BinaryIntExpressionHelper.isIntOperand(expression.getLeftExpression(), node);
            boolean maybeOptimize = leftInt && rightInt;
            if (maybeOptimize) {
                // if right is a constant, then we optimize only if it makes
                // a block complete, so we set a maybe
                if (right instanceof ConstantExpression) {
                    opt.chainCanOptimize(maybeOptimize);
                } else {
                    opt.chainShouldOptimize(maybeOptimize);
                }
            }
            if (opt.shouldOptimize()) {
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
            boolean optimizeThisExpression = leftInt && rightInt;
            ClassNode type = null;
            opt.chainShouldOptimize(optimizeThisExpression);
            if (optimizeThisExpression) {
                switch (expression.getOperation().getType()) {
                    case Types.DIVIDE: case Types.POWER: 
                    case Types.MULTIPLY: case Types.PLUS_PLUS: 
                    case Types.MINUS_MINUS:
                        opt.reset();
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
                type = ClassHelper.int_TYPE;
            } else if (rightInt && expression.getOperation().getType()==Types.LEFT_SQUARE_BRACKET) {
                // maybe getting from array
                ClassNode ltype = BinaryExpressionMultiTypeDispatcher.getType(expression.getLeftExpression(), node);
                if (ltype.getComponentType()==ClassHelper.int_TYPE) {
                    opt.shouldOptimize = true;
                    optimizeThisExpression = true;
                    type = ClassHelper.int_TYPE;
                }
            }
                
            if (optimizeThisExpression) {
                StatementMeta meta = addMeta(expression);
                meta.type = type;
            }
        }
        
        @Override
        public void visitExpressionStatement(ExpressionStatement statement) {
            if (statement.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitExpressionStatement(statement);
            if (opt.shouldOptimize()) addMeta(statement);
        }
        
        @Override
        public void visitBlockStatement(BlockStatement block) {
            boolean optAll = true;
            for (Statement statement : block.getStatements()) {
                opt.reset();
                statement.visit(this);
                optAll = optAll && opt.canOptimize();
            }
            if (block.isEmpty()) {
                opt.canOptimize = true;
            } else {
                opt.shouldOptimize = optAll;                
                if (optAll) addMeta(block);
            }
        }
        
        @Override
        public void visitIfElse(IfStatement statement) {
            super.visitIfElse(statement);
            if (opt.shouldOptimize()) addMeta(statement);
        }
        
        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
            if (expression.getNodeMetaData(StatementMeta.class)!=null) return;
            super.visitStaticMethodCallExpression(expression);

            setMethodTarget(expression,expression.getMethod(), expression.getArguments());
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
            setMethodTarget(expression, expression.getMethodAsString(), expression.getArguments());
        }
        
        private void setMethodTarget(Expression expression, String name, Expression callArgs) {
            if (name==null) return;
            // find method call target
            Parameter[] paraTypes = null;
            if (callArgs instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) callArgs;
                int size = args.getExpressions().size();
                paraTypes = new Parameter[size];
                int i=0;
                for (Expression exp: args.getExpressions()) {
                    ClassNode type = BinaryExpressionMultiTypeDispatcher.getType(exp,node);
                    if (!validTypeForCall(type)) return;
                    paraTypes[i] = new Parameter(type,"");
                    i++;
                }
            } else {
                ClassNode type = BinaryExpressionMultiTypeDispatcher.getType(callArgs,node);
                paraTypes = new Parameter[]{new Parameter(type,"")};
            }
            
            MethodNode target = node.getMethod(name, paraTypes);
            if (target==null) return;
            if (!target.getDeclaringClass().equals(node)) return;
            StatementMeta meta = addMeta(expression);
            meta.target = target;
            meta.type = target.getReturnType().redirect();
            opt.shouldOptimize = true;
        }
        
        private static boolean validTypeForCall(ClassNode type) {
            // do call only for final classes and primitive types
            if (ClassHelper.isPrimitiveType(type)) return true;
            if ((type.getModifiers() & ACC_FINAL)>0) return true;
            return false;
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            return;
        }
        
        @Override
        public void visitForLoop(ForStatement statement) {
            opt.reset();
            super.visitForLoop(statement);
            if (opt.shouldOptimize()) addMeta(statement);
        }
    }    

    protected static boolean shouldOptimize(ASTNode orig) {
        StatementMeta meta = (StatementMeta) orig.getNodeMetaData(StatementMeta.class);
        if (meta==null) return false;
        if (meta.optimize=false) return false;
        if (meta.optimizeInt==true) return true;
        return false;
    }
    

}
