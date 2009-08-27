package org.codehaus.groovy.classgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InnerClassVisitor extends ClassCodeVisitorSupport implements Opcodes {

    private final SourceUnit sourceUnit;
    private ClassNode classNode;
    
    
    public InnerClassVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }
    
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
    
    @Override
    public void visitClass(ClassNode node) {
        this.classNode = node;
        super.visitClass(node);
        if (node.isEnum() || node.isInterface()) return;
        addDispatcherMethods();
        if (!(node instanceof InnerClassNode)) return;
        if (node.getSuperClass().isInterface()) {
            node.addInterface(node.getUnresolvedSuperClass());
            node.setUnresolvedSuperClass(ClassHelper.OBJECT_TYPE);
        }
        addDefaultMethods((InnerClassNode)node);
    }
    
    private void addDefaultMethods(InnerClassNode node) {
        if(node.getVariableScope()==null) return;
        final boolean isStatic = node.getVariableScope().isInStaticContext();
        
        final String classInternalName = BytecodeHelper.getClassInternalName(node);
        final String outerClassInternalName = getInternalName(node.getOuterClass(),isStatic);
        final String outerClassDescriptor = getTypeDescriptor(node.getOuterClass(),isStatic);
        final int objectDistance = getObjectDistance(node.getOuterClass());
        
        // add method dispatcher
        Parameter[] parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "args")
        };
        MethodNode method = node.addSyntheticMethod(
                "methodMissing", 
                Opcodes.ACC_PUBLIC, 
                ClassHelper.OBJECT_TYPE, 
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );

        BlockStatement block = new BlockStatement();
        if (isStatic) {
        	setMethodDispatcherCode(block, new ClassExpression(node.getOuterClass()), parameters);
        } else {
	        block.addStatement(
	                new BytecodeSequence(new BytecodeInstruction() {
	                    public void visit(MethodVisitor mv) {
	                        mv.visitVarInsn(ALOAD, 0);
	                        mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
	                        mv.visitVarInsn(ALOAD, 1);
	                        mv.visitVarInsn(ALOAD, 2);
	                        mv.visitMethodInsn( INVOKEVIRTUAL, 
	                                            outerClassInternalName, 
	                                            "this$dist$invoke$"+objectDistance, 
	                                            "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
	                        mv.visitInsn(ARETURN);
	                    }
	                })
	        );
        }
        method.setCode(block);
        
        // add property getter dispatcher
        parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "val")
        };
        method = node.addSyntheticMethod(
                "propertyMissing", 
                Opcodes.ACC_PUBLIC, 
                ClassHelper.VOID_TYPE,
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );
        
        block = new BlockStatement();
        if (isStatic) {
        	setPropertySetDispatcher(block, new ClassExpression(node.getOuterClass()), parameters);
	    } else {
	        block.addStatement(
	                new BytecodeSequence(new BytecodeInstruction() {
	                    public void visit(MethodVisitor mv) {
	                        mv.visitVarInsn(ALOAD, 0);
	                        mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
	                        mv.visitVarInsn(ALOAD, 1);
	                        mv.visitVarInsn(ALOAD, 2);
	                        mv.visitMethodInsn( INVOKEVIRTUAL, 
	                                            outerClassInternalName, 
	                                            "this$dist$set$"+objectDistance,
	                                            "(Ljava/lang/String;Ljava/lang/Object;)V");
	                        mv.visitInsn(RETURN);
	                    }
	                })
	        );
	    }
        method.setCode(block);
        
        // add property setter dispatcher
        parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name")
        };
        method = node.addSyntheticMethod(
                "propertyMissing", 
                Opcodes.ACC_PUBLIC, 
                ClassHelper.OBJECT_TYPE, 
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );
        
        block = new BlockStatement();
	    if (isStatic) {
	    	setPropertyGetterDispatcher(block, new ClassExpression(node.getOuterClass()), parameters);
	    } else {
	        block.addStatement(
	                new BytecodeSequence(new BytecodeInstruction() {
	                    public void visit(MethodVisitor mv) {
	                        mv.visitVarInsn(ALOAD, 0);
	                        mv.visitFieldInsn(GETFIELD, classInternalName, "this$0", outerClassDescriptor);
	                        mv.visitVarInsn(ALOAD, 1);
	                        mv.visitMethodInsn( INVOKEVIRTUAL, 
	                                            outerClassInternalName, 
	                                            "this$dist$get$"+objectDistance, 
	                                            "(Ljava/lang/String;)Ljava/lang/Object;");
	                        mv.visitInsn(ARETURN);
	                    }
	                })
	        );
	    }
        method.setCode(block);
    }

    private String getTypeDescriptor(ClassNode node, boolean isStatic) {
    	return BytecodeHelper.getTypeDescription(getClassNode(node,isStatic));
	}

	private String getInternalName(ClassNode node, boolean isStatic) {
    	return BytecodeHelper.getClassInternalName(getClassNode(node,isStatic));
	}

	@Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        if (!call.isUsingAnnonymousInnerClass()) return;
        
        InnerClassNode innerClass = (InnerClassNode) call.getType();
        if (!innerClass.getDeclaredConstructors().isEmpty()) return;
        if ((innerClass.getModifiers() & ACC_STATIC)!=0) return;
        
        VariableScope scope = innerClass.getVariableScope();
        if (scope==null) return;
        
        
        boolean isStatic = scope.isInStaticContext();
        // expressions = constructor call arguments
        List<Expression> expressions = ((TupleExpression) call.getArguments()).getExpressions();
        // block = init code for the constructor we produce
        BlockStatement block = new BlockStatement();
        // parameters = parameters of the constructor
        List parameters = new ArrayList(expressions.size()+1+scope.getReferencedLocalVariablesCount());
        // superCallArguments = arguments for the super call == the constructor call arguments
        List superCallArguments = new ArrayList(expressions.size());
        
        // first we add a super() call for all expressions given in the 
        // constructor call expression
        int pCount = 0;
        for (Expression expr : expressions) {
            pCount++;
            // add one parameter for each expression in the 
            // constructor call
            Parameter param = new Parameter(ClassHelper.OBJECT_TYPE,"p"+pCount);
            parameters.add(param);
            // add to super call
            superCallArguments.add(new VariableExpression(param));
        }
        
        // add the super call
        ConstructorCallExpression cce = new ConstructorCallExpression(
                ClassNode.SUPER,
                new TupleExpression(superCallArguments)
        );
        block.addStatement(new ExpressionStatement(cce));
        
        // we need to add "this" to access unknown methods/properties
        // this is saved in a field named this$0
        expressions.add(VariableExpression.THIS_EXPRESSION);
        pCount++;
        ClassNode outerClassType = getClassNode(innerClass.getOuterClass(),isStatic);
        Parameter thisParameter = new Parameter(outerClassType,"p"+pCount);
        parameters.add(thisParameter);
        int privateSynthetic = Opcodes.ACC_PRIVATE+Opcodes.ACC_SYNTHETIC;
        FieldNode thisField = innerClass.addField("this$0", privateSynthetic, outerClassType, null);
        addFieldInit(thisParameter,thisField,block,false);

        // for each shared variable we add a reference and save it as field
        for (Iterator it=scope.getReferencedLocalVariablesIterator(); it.hasNext();) {
            pCount++;
            org.codehaus.groovy.ast.Variable var = (org.codehaus.groovy.ast.Variable) it.next();
            VariableExpression ve = new VariableExpression(var);
            ve.setClosureSharedVariable(true);
            ve.setUseReferenceDirectly(true);
            expressions.add(ve);

            Parameter p = new Parameter(ClassHelper.REFERENCE_TYPE,"p"+pCount);
            //p.setClosureSharedVariable(true);
            parameters.add(p);
            final VariableExpression initial = new VariableExpression(p);
            initial.setUseReferenceDirectly(true);
            final FieldNode pField = innerClass.addFieldFirst(ve.getName(), privateSynthetic, ClassHelper.REFERENCE_TYPE, initial);
            final int finalPCount = pCount;
//            pField.setInitialValueExpression(initial);
            pField.setHolder(true);
//            addFieldInit(p,pField,block,true);
        }
        
        innerClass.addConstructor(ACC_PUBLIC, (Parameter[]) parameters.toArray(new Parameter[0]), ClassNode.EMPTY_ARRAY, block);
        
    }
    
    private ClassNode getClassNode(ClassNode node, boolean isStatic) {
    	if (isStatic) node = ClassHelper.CLASS_Type;
    	return node;
	}

	private void addDispatcherMethods() {
        final int objectDistance = getObjectDistance(classNode);
        
        // since we added an anonymous inner class we should also
        // add the dispatcher methods
        
        // add method dispatcher
        Parameter[] parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "args")
        };
        MethodNode method = classNode.addSyntheticMethod(
                "this$dist$invoke$"+objectDistance, 
                ACC_PUBLIC+ACC_BRIDGE+ACC_SYNTHETIC, 
                ClassHelper.OBJECT_TYPE, 
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );

        BlockStatement block = new BlockStatement();
        setMethodDispatcherCode(block, VariableExpression.THIS_EXPRESSION, parameters);
        method.setCode(block);
        
        // add property setter
        parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name"),
                new Parameter(ClassHelper.OBJECT_TYPE, "value")
        };
        method = classNode.addSyntheticMethod(
                "this$dist$set$"+objectDistance, 
                ACC_PUBLIC+ACC_BRIDGE+ACC_SYNTHETIC, 
                ClassHelper.VOID_TYPE, 
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );

        block = new BlockStatement();
        setPropertySetDispatcher(block,VariableExpression.THIS_EXPRESSION,parameters);
        method.setCode(block);

        // add property getter
        parameters = new Parameter[] {
                new Parameter(ClassHelper.STRING_TYPE, "name")
        };
        method = classNode.addSyntheticMethod(
                "this$dist$get$"+objectDistance, 
                ACC_PUBLIC+ACC_BRIDGE+ACC_SYNTHETIC, 
                ClassHelper.OBJECT_TYPE, 
                parameters, 
                ClassNode.EMPTY_ARRAY, 
                null
        );

        block = new BlockStatement();
        setPropertyGetterDispatcher(block, VariableExpression.THIS_EXPRESSION, parameters);
        method.setCode(block);
    }

    private void setPropertyGetterDispatcher(BlockStatement block, Expression thiz, Parameter[] parameters) {
    	List gStringStrings = new ArrayList();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List gStringValues = new ArrayList();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ReturnStatement(
                        new AttributeExpression(
                                thiz,
                                new GStringExpression("$name",
                                        gStringStrings,
                                        gStringValues
                                )
                        )
                )
        );		
	}

	private void setPropertySetDispatcher(BlockStatement block, Expression thiz, Parameter[] parameters) {
    	List gStringStrings = new ArrayList();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List gStringValues = new ArrayList();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ExpressionStatement(
                        new BinaryExpression(
                                new AttributeExpression(
                                        thiz,
                                        new GStringExpression("$name",
                                                gStringStrings,
                                                gStringValues
                                        )
                                ),
                                Token.newSymbol(Types.ASSIGN, -1, -1),
                                new VariableExpression(parameters[1])
                        )
                )
        );
	}

	private void setMethodDispatcherCode(BlockStatement block, Expression thiz, Parameter[] parameters) {
        List gStringStrings = new ArrayList();
        gStringStrings.add(new ConstantExpression(""));
        gStringStrings.add(new ConstantExpression(""));
        List gStringValues = new ArrayList();
        gStringValues.add(new VariableExpression(parameters[0]));
        block.addStatement(
                new ReturnStatement(
                        new MethodCallExpression(
                               thiz,
                               new GStringExpression("$name",
                                       gStringStrings,
                                       gStringValues
                               ),
                               new ArgumentListExpression(
                                       new SpreadExpression(new VariableExpression(parameters[1]))
                               )
                        )
                )
        );
	}

	private static void addFieldInit(Parameter p, FieldNode fn, BlockStatement block, boolean ref) {
        VariableExpression ve = new VariableExpression(p);
        ve.setUseReferenceDirectly(ref);
        FieldExpression fe = new FieldExpression(fn);
        fe.setUseReferenceDirectly(ref);
        block.addStatement(new ExpressionStatement(
                new BinaryExpression(
                        fe,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        ve
                )
        ));
    }
    
    private int getObjectDistance(ClassNode node) {
        int count = 1;
        while (node!=null && node!=ClassHelper.OBJECT_TYPE) {
            count++;
            node = node.getSuperClass();
        }
        return count;
    }
    
}
