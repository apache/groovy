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

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher.*;
import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.ast.tools.WideningCategories.*;

/**
 * A class to write out the optimized statements
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
        protected MethodNode target;
        protected ClassNode type;
        protected boolean[] involvedTypes = new boolean[typeMapKeyNames.length];
        public void chainInvolvedTypes(OptimizeFlagsCollector opt) {
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (opt.current.involvedTypes[i]) {
                    this.involvedTypes[i] = true;
                }
            }
        }
        public String toString() {
            String ret = "optimize="+optimize+" target="+target+" type="+type+" involvedTypes=";
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (involvedTypes[i]) ret += " "+typeMapKeyNames[i];
            }
            return ret;
        }
    }

    private static MethodCaller[] guards = {
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
    
    public static void setNodeMeta(TypeChooser chooser, ClassNode classNode) {
        if (classNode.getNodeMetaData(ClassNodeSkip.class)!=null) return;
        new OptVisitor(chooser).visitClass(classNode);
    }
    
    private static StatementMeta addMeta(ASTNode node) {
        StatementMeta metaOld = (StatementMeta) node.getNodeMetaData(StatementMeta.class);
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
        private LinkedList<OptimizeFlagsEntry> olderEntries = new LinkedList<OptimizeFlagsEntry>();
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
            String ret = "";
            if (current.shouldOptimize) {
                ret = "should optimize, can = "+current.canOptimize;
            } else if (current.canOptimize) {
                ret = "can optimize";
            } else {
                ret = "don't optimize";
            }
            ret += " involvedTypes =";
            for (int i=0; i<typeMapKeyNames.length; i++) {
                if (current.involvedTypes[i]) ret += " "+typeMapKeyNames[i];
            }
            return ret;
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
        private final static VariableScope nonStaticScope = new VariableScope(); 
        
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
            right.visit(this);
            
            ClassNode leftType = typeChooser.resolveType(expression.getLeftExpression(), node);
            ClassNode rightType = typeChooser.resolveType(expression.getRightExpression(), node);
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
                    case Types.LOGICAL_AND: case Types.LOGICAL_AND_EQUAL:
                    case Types.LOGICAL_OR: case Types.LOGICAL_OR_EQUAL:
                        expression.setType(boolean_TYPE);
                        resultType = boolean_TYPE;
                        break;
                    case Types.DIVIDE: case Types.DIVIDE_EQUAL:
                        if (isLongCategory(leftType) && isLongCategory(rightType)) {
                            resultType = BigDecimal_TYPE;
                        } else if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) {
                            resultType = BigDecimal_TYPE;
                        } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                            resultType = double_TYPE;
                        }
                        break;
                    case Types.POWER: case Types.POWER_EQUAL:
                        //TODO: implement
                        break;
                    case Types.ASSIGN:
                        opt.chainCanOptimize(true);
                        break;
                    default:
                        if (isIntCategory(leftType) && isIntCategory(rightType)) {
                            resultType = int_TYPE;
                        } else if (isLongCategory(leftType) && isLongCategory(rightType)) {
                            resultType = long_TYPE;
                        } else if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) {
                            resultType = BigDecimal_TYPE;
                        } else if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) {
                            resultType = double_TYPE;
                        }
                }
            }
            
            if (resultType!=null) {
                StatementMeta meta = addMeta(expression);
                meta.type = resultType;
                opt.chainShouldOptimize(true);
                opt.chainInvolvedType(resultType);
                opt.chainInvolvedType(leftType);
                opt.chainInvolvedType(rightType);
            }
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
                statement.visit(this);
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
                if (parametersEqual(cn.getParameters(), paraTypes)) {
                    res = cn;
                    break;
                }
            }
            if (res !=null && res.isPublic()) return res;
            return null;
        }

        private static boolean parametersEqual(Parameter[] a, Parameter[] b) {
            if (a.length == b.length) {
                boolean answer = true;
                for (int i = 0; i < a.length; i++) {
                    if (!a[i].getType().equals(b[i].getType())) {
                        answer = false;
                        break;
                    }
                }
                return answer;
            }
            return false;
        }
        
        private static boolean validTypeForCall(ClassNode type) {
            // do call only for final classes and primitive types
            if (isPrimitiveType(type)) return true;
            if ((type.getModifiers() & ACC_FINAL)>0) return true;
            return false;
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            return;
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
