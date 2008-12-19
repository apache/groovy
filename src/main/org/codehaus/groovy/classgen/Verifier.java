/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Verifies the AST node and adds any defaulted AST code before
 * bytecode generation occurs.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Verifier implements GroovyClassVisitor, Opcodes {

    public static final String __TIMESTAMP = "__timeStamp";
    public static final String __TIMESTAMP__ = "__timeStamp__239_neverHappen";
    private static final Parameter[] INVOKE_METHOD_PARAMS = new Parameter[]{
            new Parameter(ClassHelper.STRING_TYPE, "method"),
            new Parameter(ClassHelper.OBJECT_TYPE, "arguments")
    };
    private static final Parameter[] SET_PROPERTY_PARAMS = new Parameter[]{
            new Parameter(ClassHelper.STRING_TYPE, "property"),
            new Parameter(ClassHelper.OBJECT_TYPE, "value")
    };
    private static final Parameter[] GET_PROPERTY_PARAMS = new Parameter[]{
            new Parameter(ClassHelper.STRING_TYPE, "property")
    };
    private static final Parameter[] SET_METACLASS_PARAMS = new Parameter[] {
            new Parameter(ClassHelper.METACLASS_TYPE, "mc")
    };

    private ClassNode classNode;
    private MethodNode methodNode;

    public ClassNode getClassNode() {
        return classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    private FieldNode setMetaClassFieldIfNotExists(ClassNode node, FieldNode metaClassField){
        if (metaClassField != null) return metaClassField;
        StaticMethodCallExpression initMetaClassCall = new StaticMethodCallExpression(
                ClassHelper.make(ScriptBytecodeAdapter.class),
                "initMetaClass",
                VariableExpression.THIS_EXPRESSION);
        PropertyNode metaClassProperty =
            node.addProperty("metaClass", ACC_PUBLIC, ClassHelper.make(MetaClass.class), initMetaClassCall, null, null);
        metaClassProperty.setSynthetic(true);
        FieldNode field = metaClassProperty.getField();
        field.setModifiers(field.getModifiers() | ACC_TRANSIENT);
        return field;
    }
    
    private FieldNode getMetaClassField(ClassNode node) {
        FieldNode ret = node.getField("metaClass");
        if (ret!=null) return ret;
        ClassNode current = node;
        while (current!=null && current!=ClassHelper.OBJECT_TYPE) {
            current = current.getSuperClass();
            ret = current.getField("metaClass");
            if (ret==null) continue;
            if (Modifier.isPrivate(ret.getModifiers())) continue;
            return ret;
        }
        return null;
    }
    
    /**
     * add code to implement GroovyObject
     * @param node
     */
    public void visitClass(final ClassNode node) {
        this.classNode = node;

        if ((classNode.getModifiers() & Opcodes.ACC_INTERFACE) >0) {
            //interfaces have no constructors, but this code expects one,
            //so create a dummy and don't add it to the class node
            ConstructorNode dummy = new ConstructorNode(0,null);
            addInitialization(node, dummy);
            node.visitContents(this);
            return;
        }

        ClassNode[] classNodes = classNode.getInterfaces();
        List interfaces = new ArrayList();
        for (int i = 0; i < classNodes.length; i++) {
            ClassNode classNode = classNodes[i];
            interfaces.add(classNode.getName());
        }
        Set interfaceSet = new HashSet(interfaces);
        if (interfaceSet.size() != interfaces.size()) {
            throw new RuntimeParserException("Duplicate interfaces in implements list: " + interfaces, classNode);
        }

        addDefaultParameterMethods(node);
        addDefaultParameterConstructors(node);

        boolean knownSpecialCase =
                node.isDerivedFrom(ClassHelper.GSTRING_TYPE)
                        || node.isDerivedFrom(ClassHelper.make(GroovyObjectSupport.class))
                        || node.implementsInterface(ClassHelper.METACLASS_TYPE);

        if (!knownSpecialCase) {

            if (!node.isDerivedFromGroovyObject()) node.addInterface(ClassHelper.make(GroovyObject.class));
            FieldNode metaClassField = getMetaClassField(node);
            final String classInternalName = BytecodeHelper.getClassInternalName(node);
            
            if (!node.hasMethod("getMetaClass", Parameter.EMPTY_ARRAY)) {
                metaClassField=setMetaClassFieldIfNotExists(node,metaClassField);
                StaticMethodCallExpression initMetaClassCall = new StaticMethodCallExpression(
                        ClassHelper.make(ScriptBytecodeAdapter.class),
                        "initMetaClass",
                        VariableExpression.THIS_EXPRESSION);
                FieldExpression metaClassVar = new FieldExpression(metaClassField);
                IfStatement initMetaClassField = new IfStatement(
                        new BooleanExpression(
                                new BinaryExpression(metaClassVar, Token.newSymbol(Types.COMPARE_EQUAL, -1, -1), ConstantExpression.NULL)),
                                new ExpressionStatement(new BinaryExpression(metaClassVar, Token.newSymbol(Types.EQUAL, -1, -1), initMetaClassCall)),
                                EmptyStatement.INSTANCE);
                
                node.addSyntheticMethod(
                        "getMetaClass",
                        ACC_PUBLIC,
                        ClassHelper.make(MetaClass.class),
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        new BlockStatement(new Statement[]{initMetaClassField, new ReturnStatement(metaClassVar)}, new VariableScope())
                );
            }

            if (!node.hasMethod("setMetaClass", SET_METACLASS_PARAMS)) {
                metaClassField=setMetaClassFieldIfNotExists(node,metaClassField);
                Statement setMetaClassCode;                
                if (Modifier.isFinal(metaClassField.getModifiers())) {
                    ConstantExpression text = new ConstantExpression("cannot set read-only meta class");
                    ConstructorCallExpression cce = new ConstructorCallExpression(ClassHelper.make(IllegalArgumentException.class), text);
                    setMetaClassCode = new ExpressionStatement(cce);
                } else {
                    List list = new ArrayList();
                    list.add (new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitFieldInsn(PUTFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                        }
                    });
                    setMetaClassCode = new BytecodeSequence(list);
                }
                
                node.addSyntheticMethod(
                        "setMetaClass",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        SET_METACLASS_PARAMS,
                        ClassNode.EMPTY_ARRAY,
                        setMetaClassCode
                );
            }

            if (!node.hasMethod("invokeMethod",INVOKE_METHOD_PARAMS)) {
                VariableExpression vMethods = new VariableExpression("method");
                VariableExpression vArguments = new VariableExpression("arguments");
                VariableScope blockScope = new VariableScope();
                blockScope.putReferencedLocalVariable(vMethods);
                blockScope.putReferencedLocalVariable(vArguments);
                List instructions = new ArrayList();
                instructions.add(new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "invokeMethod", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
                        mv.visitInsn(ARETURN);
                    }
                });
                node.addSyntheticMethod(
                        "invokeMethod",
                        ACC_PUBLIC,
                        ClassHelper.OBJECT_TYPE, INVOKE_METHOD_PARAMS,
                        ClassNode.EMPTY_ARRAY,
                        new BytecodeSequence(instructions)
                );
            }

            if (!node.hasMethod("getProperty", GET_PROPERTY_PARAMS)) {
                List instructions = new ArrayList();
                instructions.add(new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "getProperty", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
                        mv.visitInsn(ARETURN);
                    }
                });
                node.addSyntheticMethod(
                        "getProperty",
                        ACC_PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        GET_PROPERTY_PARAMS,
                        ClassNode.EMPTY_ARRAY,
                        new BytecodeSequence(instructions)
                );
            }

            if (!node.hasMethod("setProperty", SET_PROPERTY_PARAMS)) {
                List instructions = new ArrayList();
                instructions.add(new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");
                        mv.visitInsn(RETURN);                    }
                });
                
                node.addSyntheticMethod(
                        "setProperty",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        SET_PROPERTY_PARAMS,
                        ClassNode.EMPTY_ARRAY,
                        new BytecodeSequence(instructions)
                );
            }



        }

        if (node.getDeclaredConstructors().isEmpty()) {
            ConstructorNode constructor = new ConstructorNode(ACC_PUBLIC, null);
            constructor.setSynthetic(true);
            node.addConstructor(constructor);
        }

        if (!(node instanceof InnerClassNode)) {// add a static timestamp field to the class
            addTimeStamp(node);
        }

        addInitialization(node);
        checkReturnInObjectInitializer(node.getObjectInitializerStatements());
        node.getObjectInitializerStatements().clear();
        addCovariantMethods(node);
        node.visitContents(this);
    }

    protected void addTimeStamp(ClassNode node) {
        FieldNode timeTagField = new FieldNode(
                Verifier.__TIMESTAMP,
                Modifier.PUBLIC | Modifier.STATIC,
                ClassHelper.Long_TYPE,
                //"",
                node,
                new ConstantExpression(new Long(System.currentTimeMillis())));
        // alternatively , FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
        timeTagField.setSynthetic(true);
        node.addField(timeTagField);

        timeTagField = new FieldNode(
                Verifier.__TIMESTAMP__ + String.valueOf(System.currentTimeMillis()),
                Modifier.PUBLIC | Modifier.STATIC,
                ClassHelper.Long_TYPE,
                //"",
                node,
                new ConstantExpression(new Long(0)));
        // alternatively , FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
        timeTagField.setSynthetic(true);
        node.addField(timeTagField);
    }

    private void checkReturnInObjectInitializer(List init) {
        CodeVisitorSupport cvs = new CodeVisitorSupport() {
            public void visitReturnStatement(ReturnStatement statement) {
                throw new RuntimeParserException("'return' is not allowed in object initializer",statement);
            }
        };
        for (Iterator iterator = init.iterator(); iterator.hasNext();) {
            Statement stm = (Statement) iterator.next();
            stm.visit(cvs);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        CodeVisitorSupport checkSuper = new CodeVisitorSupport() {
            boolean firstMethodCall = true;
            String type=null;
            public void visitMethodCallExpression(MethodCallExpression call) {
                if (!firstMethodCall) return;
                firstMethodCall = false;
                String name = call.getMethodAsString();
                // the name might be null if the method name is a GString for example
                if (name==null) return;
                if (!name.equals("super") && !name.equals("this")) return;
                type=name;
                call.getArguments().visit(this);
                type=null;
            }
            public void visitVariableExpression(VariableExpression expression) {
                if (type==null) return;
                String name = expression.getName();
                if (!name.equals("this") && !name.equals("super")) return;
                throw new RuntimeParserException("cannot reference "+name+" inside of "+type+"(....) before supertype constructor has been called",expression);
            }
        };
        Statement s = node.getCode();
        if (s == null) return;
        s.visit(checkSuper);
    }

    public void visitMethod(MethodNode node) {
        this.methodNode = node;
        Statement statement = node.getCode();
        if (!node.isVoidMethod()) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expStmt = (ExpressionStatement) statement;
                Expression expr = expStmt.getExpression();
                ReturnStatement ret = new ReturnStatement(expr);
                ret.setSourcePosition(expr);
                node.setCode(ret);
            }
            else if (statement instanceof BlockStatement) {
                BlockStatement block = (BlockStatement) statement;

                // let's copy the list so we create a new block
                List list = new ArrayList(block.getStatements());
                if (!list.isEmpty()) {
                    int idx = list.size() - 1;
                    Statement last = (Statement) list.get(idx);
                    if (last instanceof ExpressionStatement) {
                        ExpressionStatement expStmt = (ExpressionStatement) last;
                        ReturnStatement ret = new ReturnStatement(expStmt);
                        ret.setSourcePosition(expStmt);
                        list.set(idx, ret);
                    } else if (!(last instanceof ReturnStatement)) {
                        list.add(new ReturnStatement(ConstantExpression.NULL));
                    }
                } else {
                    list.add(new ReturnStatement(ConstantExpression.NULL));
                }

                node.setCode(new BlockStatement(filterStatements(list),block.getVariableScope()));
            }
        } else if (!node.isAbstract()) {
        	BlockStatement newBlock = new BlockStatement();
            if (statement instanceof BlockStatement) {
                BlockStatement oldBlock = (BlockStatement)statement;
                newBlock.addStatements(filterStatements(((BlockStatement)statement).getStatements()));
            }
            else {
                newBlock.addStatement(filterStatement(statement));
            }
            newBlock.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
            newBlock.setSourcePosition(statement);
            node.setCode(newBlock);
        }
        if (node.getName().equals("main") && node.isStatic()) {
            Parameter[] params = node.getParameters();
            if (params.length == 1) {
                Parameter param = params[0];
                if (param.getType() == null || param.getType()==ClassHelper.OBJECT_TYPE) {
                    param.setType(ClassHelper.STRING_TYPE.makeArray());
                }
            }
        }
        statement = node.getCode();
        if (statement!=null) statement.visit(new VerifierCodeVisitor(this));
    }

    public void visitField(FieldNode node) {
    }

    private boolean methodNeedsReplacement(MethodNode m) {
        // no method found, we need to replace
        if (m==null) return true;
        // method is in current class, nothing to be done
        if (m.getDeclaringClass()==this.getClassNode()) return false;
        // do not overwrite final
        if ((m.getModifiers()&ACC_FINAL)!=0) return false;
        return true;
    }

    public void visitProperty(PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();

        String getterName = "get" + capitalize(name);
        String setterName = "set" + capitalize(name);

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            MethodNode getter = classNode.getGetterMethod(getterName);
            if (!node.isPrivate() && methodNeedsReplacement(getter)) {
                getterBlock = createGetterBlock(node, field);
            }
        }
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null) {
            MethodNode setter = classNode.getSetterMethod(setterName);
            if ( !node.isPrivate() &&
                 (node.getModifiers()&ACC_FINAL)==0 &&
                 methodNeedsReplacement(setter))
            {
                setterBlock = createSetterBlock(node, field);
            }
        }

        if (getterBlock != null) {
            MethodNode getter =
                new MethodNode(getterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
            getter.setSynthetic(true);
            addPropertyMethod(getter);
            visitMethod(getter);

            if (ClassHelper.boolean_TYPE==node.getType() || ClassHelper.Boolean_TYPE==node.getType()) {
                String secondGetterName = "is" + capitalize(name);
                MethodNode secondGetter =
                    new MethodNode(secondGetterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
                secondGetter.setSynthetic(true);
                addPropertyMethod(secondGetter);
                visitMethod(secondGetter);
            }
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = { new Parameter(node.getType(), "value")};
            MethodNode setter =
                new MethodNode(setterName, node.getModifiers(), ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
            setter.setSynthetic(true);
            addPropertyMethod(setter);
            visitMethod(setter);
        }
    }

    protected void addPropertyMethod(MethodNode method) {
    	classNode.addMethod(method);
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------

    private interface DefaultArgsAction {
        void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method);
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions
     */
    protected void addDefaultParameterMethods(final ClassNode node) {
        List methods = new ArrayList(node.getMethods());
        addDefaultParameters(methods, new DefaultArgsAction(){
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                MethodCallExpression expression = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, method.getName(), arguments);
                expression.setImplicitThis(true);
                Statement code = null;
                if (method.isVoidMethod()) {
                    code = new ExpressionStatement(expression);
                } else {
                    ReturnStatement ret = new ReturnStatement(expression);
                    ret.setSourcePosition(expression);
                    code = ret;
                }
                node.addMethod(method.getName(), method.getModifiers(), method.getReturnType(), newParams, method.getExceptions(), code);
            }
        });
    }

    protected void addDefaultParameterConstructors(final ClassNode node) {
        List methods = new ArrayList(node.getDeclaredConstructors());
        addDefaultParameters(methods, new DefaultArgsAction(){
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                ConstructorNode ctor = (ConstructorNode) method;
                ConstructorCallExpression expression = new ConstructorCallExpression(ClassNode.THIS, arguments);
                Statement code = new ExpressionStatement(expression);
                node.addConstructor(ctor.getModifiers(), newParams, ctor.getExceptions(), code);
            }
        });
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions
     */
    protected void addDefaultParameters(List methods, DefaultArgsAction action) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (method.hasDefaultValue()) {
                Parameter[] parameters = method.getParameters();
                int counter = 0;
                List paramValues = new ArrayList();
                int size = parameters.length;
                for (int i = size - 1; i >= 0; i--) {
                    Parameter parameter = parameters[i];
                    if (parameter != null && parameter.hasInitialExpression()) {
                        paramValues.add(new Integer(i));
                        paramValues.add(parameter.getInitialExpression());
                        counter++;
                    }
                }

                for (int j = 1; j <= counter; j++) {
                    Parameter[] newParams =  new Parameter[parameters.length - j];
                    ArgumentListExpression arguments = new ArgumentListExpression();
                    int index = 0;
                    int k = 1;
                    for (int i = 0; i < parameters.length; i++) {
                        if (k > counter - j && parameters[i] != null && parameters[i].hasInitialExpression()) {
                            arguments.addExpression(parameters[i].getInitialExpression());
                            k++;
                        }
                        else if (parameters[i] != null && parameters[i].hasInitialExpression()) {
                            newParams[index++] = parameters[i];
                            arguments.addExpression(new VariableExpression(parameters[i].getName()));
                            k++;
                        }
                        else {
                            newParams[index++] = parameters[i];
                            arguments.addExpression(new VariableExpression(parameters[i].getName()));
                        }
                    }
                    action.call(arguments,newParams,method);
                }

                for (int i = 0; i < parameters.length; i++) {
                    // remove default expression
                    parameters[i].setInitialExpression(null);
                }
            }
        }
    }

    protected void addClosureCode(InnerClassNode node) {
        // add a new invoke
    }

    protected void addInitialization(ClassNode node) {
        for (Iterator iter = node.getDeclaredConstructors().iterator(); iter.hasNext();) {
            addInitialization(node, (ConstructorNode) iter.next());
        }
    }

    protected void addInitialization(ClassNode node, ConstructorNode constructorNode) {
        Statement firstStatement = constructorNode.getFirstStatement();
        ConstructorCallExpression first = getFirstIfSpecialConstructorCall(firstStatement);

        // in case of this(...) let the other constructor do the intit
        if (first!=null && first.isThisCall()) return;

        List statements = new ArrayList();
        List staticStatements = new ArrayList();
        final boolean isEnumClassNode = isEnum(node);
        List initStmtsAfterEnumValuesInit = new ArrayList();
        Set explicitStaticPropsInEnum = new HashSet();
        if(isEnumClassNode) {
        	for (Iterator iter = node.getProperties().iterator(); iter.hasNext();) {
        		PropertyNode propNode = (PropertyNode) iter.next();
        		if(!propNode.isSynthetic() && propNode.getField().isStatic()) {
        			explicitStaticPropsInEnum.add(propNode.getField().getName());
        		}
        	}
        }
        for (Iterator iter = node.getFields().iterator(); iter.hasNext();) {
        	addFieldInitialization(statements, staticStatements, 
        			(FieldNode) iter.next(), isEnumClassNode, 
        			initStmtsAfterEnumValuesInit, explicitStaticPropsInEnum);
        }
        statements.addAll(node.getObjectInitializerStatements());
        if (!statements.isEmpty()) {
            Statement code = constructorNode.getCode();
            BlockStatement block = new BlockStatement();
            List otherStatements = block.getStatements();
            if (code instanceof BlockStatement) {
                block = (BlockStatement) code;
                otherStatements=block.getStatements();
            }
            else if (code != null) {
                otherStatements.add(code);
            }
            if (!otherStatements.isEmpty()) {
                if (first!=null) {
                    // it is super(..) since this(..) is already covered
                    otherStatements.remove(0);
                    statements.add(0, firstStatement);
                }
                statements.addAll(otherStatements);
            }
            BlockStatement newBlock = new BlockStatement(statements, block.getVariableScope());
            newBlock.setSourcePosition(block);
            constructorNode.setCode(newBlock);
        }

        if (!staticStatements.isEmpty()) {
        	if(isEnumClassNode) {
        		/*
        		 * GROOVY-3161: initialize statements for explicitly declared static fields 
        		 * inside an enum should come after enum values are initialized
        		 */
        		staticStatements.removeAll(initStmtsAfterEnumValuesInit);
        		node.addStaticInitializerStatements(staticStatements, true);
        		if(!initStmtsAfterEnumValuesInit.isEmpty()) {
        			node.addStaticInitializerStatements(initStmtsAfterEnumValuesInit, false);
        		}
        	} else {
        		node.addStaticInitializerStatements(staticStatements, true);
        	}
        }
    }
    
    private boolean isEnum(ClassNode node) {
    	return (node.getModifiers() & Opcodes.ACC_ENUM) != 0;
    }

    private ConstructorCallExpression getFirstIfSpecialConstructorCall(Statement code) {
        if (code == null || !(code instanceof ExpressionStatement)) return null;

        Expression expression = ((ExpressionStatement)code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        if (cce.isSpecialCall()) return cce;
        return null;
    }

    protected void addFieldInitialization(
        List list,
        List staticList,
        FieldNode fieldNode,
        boolean isEnumClassNode,
        List initStmtsAfterEnumValuesInit,
        Set explicitStaticPropsInEnum) {
        Expression expression = fieldNode.getInitialExpression();
        if (expression != null) {
            ExpressionStatement statement =
                new ExpressionStatement(
                    new BinaryExpression(
                        new FieldExpression(fieldNode),
                        Token.newSymbol(Types.EQUAL, fieldNode.getLineNumber(), fieldNode.getColumnNumber()),
                        expression));
            if (fieldNode.isStatic()) {
                staticList.add(statement);
                /*
                 * If it is a statement for an explicitly declared static field inside an enum, store its
                 * reference. For enums, they need to be handled differently as such init statements should
                 * come after the enum values have been initialized inside <clinit> block. GROOVY-3161.
                 */
                if(isEnumClassNode && explicitStaticPropsInEnum.contains(fieldNode.getName())) {
                	initStmtsAfterEnumValuesInit.add(statement);
                }
            }
            else {
                list.add(statement);
            }
        }
    }

    /**
     * Capitalizes the start of the given bean property name
     */
    public static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    protected Statement createGetterBlock(PropertyNode propertyNode, FieldNode field) {
        Expression expression = new FieldExpression(field);
        return new ReturnStatement(expression);
    }

    protected Statement createSetterBlock(PropertyNode propertyNode, FieldNode field) {
        Expression expression = new FieldExpression(field);
        return new ExpressionStatement(
            new BinaryExpression(expression, Token.newSymbol(Types.EQUAL, 0, 0), new VariableExpression("value")));
    }

    /**
     * Filters the given statements
     */
    protected List filterStatements(List list) {
        List answer = new ArrayList(list.size());
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            answer.add(filterStatement((Statement) iter.next()));
        }
        return answer;
    }

    protected Statement filterStatement(Statement statement) {
        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expStmt = (ExpressionStatement) statement;
            Expression expression = expStmt.getExpression();
            if (expression instanceof ClosureExpression) {
                ClosureExpression closureExp = (ClosureExpression) expression;
                if (!closureExp.isParameterSpecified()) {
                    return closureExp.getCode();
                }
            }
        }
        return statement;
    }

    public void visitGenericType(GenericsType genericsType) {

    }

    public static long getTimestamp (Class clazz) {
        if (clazz.getClassLoader() instanceof GroovyClassLoader.InnerLoader) {
            GroovyClassLoader.InnerLoader innerLoader = (GroovyClassLoader.InnerLoader) clazz.getClassLoader();
            return innerLoader.getTimeStamp();
        }

        final Field[] fields = clazz.getFields();
        for (int i = 0; i != fields.length; ++i ) {
           if (Modifier.isStatic(fields[i].getModifiers())) {
               final String name = fields[i].getName();
               if (name.startsWith(__TIMESTAMP__)) {
                 try {
                     return Long.decode(name.substring(__TIMESTAMP__.length())).longValue();
                 }
                 catch (NumberFormatException e) {
                     return Long.MAX_VALUE;
                 }
             }
           }
        }
        return Long.MAX_VALUE;
    }

    protected void addCovariantMethods(ClassNode classNode) {
        Map methodsToAdd = new HashMap();
        Map genericsSpec = new HashMap();

        List declaredMethods = classNode.getAllDeclaredMethods();
        // remove all static, private and package private methods
        // we remove also abstract methods if the current class is not abstract
        // we do remove abstract methods, because covariation may automatically implement them
        boolean isNotAbstract = (classNode.getModifiers() & ACC_ABSTRACT)==0;
        for (Iterator methodsIterator = declaredMethods.iterator(); methodsIterator.hasNext();) {
            MethodNode m = (MethodNode) methodsIterator.next();
            if (m.isStatic() || !(m.isPublic() || m.isProtected())) {
                methodsIterator.remove();
            } else if (isNotAbstract && m.isAbstract()) {
                methodsIterator.remove();
            }
        }

        addCovariantMethods(classNode, declaredMethods, methodsToAdd, genericsSpec);

        Map declaredMethodsMap = new HashMap();
        if (methodsToAdd.size()>0) {
            for (Iterator methodsIterator = declaredMethods.iterator(); methodsIterator.hasNext();) {
                MethodNode m = (MethodNode) methodsIterator.next();
                declaredMethodsMap.put(m.getTypeDescriptor(),m);
            }
        }
        
        for (Iterator it = methodsToAdd.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            MethodNode method = (MethodNode) entry.getValue();
            // we skip bridge methods implemented in current class already
            MethodNode mn = (MethodNode) declaredMethodsMap.get(entry.getKey());
            if (mn!=null && mn.getDeclaringClass().equals(classNode)) continue;
            classNode.addMethod(method);
        }
    }

    private void addCovariantMethods(ClassNode classNode, List declaredMethods, Map methodsToAdd, Map oldGenericsSpec) {
        ClassNode sn = classNode.getUnresolvedSuperClass(false);
        if (sn!=null) {
            Map genericsSpec = createGenericsSpec(sn,oldGenericsSpec);
            for (Iterator it = declaredMethods.iterator(); it.hasNext();) {
                MethodNode method = (MethodNode) it.next();
                if (method.isStatic()) continue;
                storeMissingCovariantMethods(sn,method,methodsToAdd,genericsSpec);
            }
            addCovariantMethods(sn.redirect(),declaredMethods,methodsToAdd,genericsSpec);
        }

        ClassNode[] interfaces = classNode.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            Map genericsSpec = createGenericsSpec(interfaces[i],oldGenericsSpec);
            for (Iterator it = declaredMethods.iterator(); it.hasNext();) {
                MethodNode method = (MethodNode) it.next();
                if (method.isStatic()) continue;
                storeMissingCovariantMethods(interfaces[i],method,methodsToAdd,genericsSpec);
            }
            addCovariantMethods(interfaces[i],declaredMethods,methodsToAdd,genericsSpec);
        }

    }

    private MethodNode getCovariantImplementation(final MethodNode oldMethod, final MethodNode overridingMethod, Map genericsSpec) {
        // method name
        if (!oldMethod.getName().equals(overridingMethod.getName())) return null;

        // parameters
        boolean normalEqualParamerters = equalParametersNormal(overridingMethod,oldMethod);
        boolean genericEqualParamerters = equalParametersWithGenerics(overridingMethod,oldMethod,genericsSpec);
        if (!normalEqualParamerters && !genericEqualParamerters) return null;

        // return type
        ClassNode mr = overridingMethod.getReturnType();
        ClassNode omr = oldMethod.getReturnType();
        boolean equalReturnType = mr.equals(omr);
        if (equalReturnType && normalEqualParamerters) return null;

        // if we reach this point we have at last one parameter or return type, that
        // is different in its specified form. That means we have to create a bridge method!
        ClassNode testmr = correctToGenericsSpec(genericsSpec,omr);
        if (!isAssignable(mr,testmr)){
            throw new RuntimeParserException(
                    "The return type of "+
                    overridingMethod.getTypeDescriptor()+
                    " in "+overridingMethod.getDeclaringClass().getName()+
                    " is incompatible with "+
                    oldMethod.getTypeDescriptor()+
                    " in "+oldMethod.getDeclaringClass().getName(),
                    overridingMethod);
        }
        if ((oldMethod.getModifiers()&ACC_FINAL)!=0) {
            throw new RuntimeParserException(
                    "Cannot override final method "+
                    oldMethod.getTypeDescriptor()+
                    " in "+oldMethod.getDeclaringClass().getName(),
                    overridingMethod);
        }
        if (oldMethod.isStatic() != overridingMethod.isStatic()){
            throw new RuntimeParserException(
                    "Cannot override method "+
                    oldMethod.getTypeDescriptor()+
                    " in "+oldMethod.getDeclaringClass().getName()+
                    " with disparate static modifier",
                    overridingMethod);
        }

        MethodNode newMethod = new MethodNode(
                oldMethod.getName(),
                overridingMethod.getModifiers() | ACC_SYNTHETIC | ACC_BRIDGE,
                oldMethod.getReturnType().getPlainNodeReference(),
                cleanParameters(oldMethod.getParameters()),
                oldMethod.getExceptions(),
                null
        );
        List instructions = new ArrayList(1);
        instructions.add (
                new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        BytecodeHelper helper = new BytecodeHelper(mv);
                        mv.visitVarInsn(ALOAD,0);
                        Parameter[] para = oldMethod.getParameters();
                        Parameter[] goal = overridingMethod.getParameters();
                        for (int i = 0; i < para.length; i++) {
                            helper.load(para[i].getType(), i+1);
                            if (!para[i].getType().equals(goal[i].getType())) {
                                helper.doCast(goal[i].getType());
                            }
                        }
                        mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                BytecodeHelper.getClassInternalName(classNode),
                                overridingMethod.getName(),
                                BytecodeHelper.getMethodDescriptor(overridingMethod.getReturnType(), overridingMethod.getParameters()));
                        helper.doReturn(oldMethod.getReturnType());
                    }
                }

        );
        newMethod.setCode(new BytecodeSequence(instructions));
        return newMethod;
    }

    private boolean isAssignable(ClassNode node, ClassNode testNode) {
        if (testNode.isInterface()) {
        	if(node.isInterface()) {
        		if (node.isDerivedFrom(testNode)) return true;
        	} else {
        		if (node.implementsInterface(testNode)) return true;
        	}
        } else {
            if (node.isDerivedFrom(testNode)) return true;
        }
        return false;
    }

    private Parameter[] cleanParameters(Parameter[] parameters) {
        Parameter[] params = new Parameter[parameters.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = new Parameter(parameters[i].getType().getPlainNodeReference(),parameters[i].getName());
        }
        return params;
    }

    private void storeMissingCovariantMethods(ClassNode current, MethodNode method, Map methodsToAdd, Map genericsSpec) {
        List methods = current.getMethods();
        for (Iterator sit = methods.iterator(); sit.hasNext();) {
            MethodNode toOverride = (MethodNode) sit.next();
            MethodNode bridgeMethod = getCovariantImplementation(toOverride,method,genericsSpec);
            if (bridgeMethod==null) continue;
            methodsToAdd.put (bridgeMethod.getTypeDescriptor(),bridgeMethod);
            return;
        }
    }

    private ClassNode correctToGenericsSpec(Map genericsSpec, GenericsType type) {
        ClassNode ret = null;
        if (type.isPlaceholder()){
            String name = type.getName();
            ret = (ClassNode) genericsSpec.get(name);
        }
        if (ret==null) ret = type.getType();
        return ret;
    }

    private ClassNode correctToGenericsSpec(Map genericsSpec, ClassNode type) {
        if (type.isGenericsPlaceHolder()){
            String name = type.getGenericsTypes()[0].getName();
            type = (ClassNode) genericsSpec.get(name);
        }
        if (type==null) type = ClassHelper.OBJECT_TYPE;
        return type;
    }

    private boolean equalParametersNormal(MethodNode m1, MethodNode m2) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length!=p2.length) return false;
        for (int i = 0; i < p2.length; i++) {
            ClassNode type = p2[i].getType();
            ClassNode parameterType = p1[i].getType();
            if (!parameterType.equals(type)) return false;
        }
        return true;
    }

    private boolean equalParametersWithGenerics(MethodNode m1, MethodNode m2, Map genericsSpec) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length!=p2.length) return false;
        for (int i = 0; i < p2.length; i++) {
            ClassNode type = p2[i].getType();
            ClassNode genericsType = correctToGenericsSpec(genericsSpec,type);
            ClassNode parameterType = p1[i].getType();
            if (!parameterType.equals(genericsType)) return false;
        }
        return true;
    }

    private Map createGenericsSpec(ClassNode current, Map oldSpec) {
        Map ret = new HashMap(oldSpec);
        // ret contains the type specs, what we now need is the type spec for the
        // current class. To get that we first apply the type parameters to the
        // current class and then use the type names of the current class to reset
        // the map. Example:
        //   class A<V,W,X>{}
        //   class B<T extends Number> extends A<T,Long,String> {}
        // first we have:    T->Number
        // we apply it to A<T,Long,String> -> A<Number,Long,String>
        // resulting in:     V->Number,W->Long,X->String

        GenericsType[] sgts = current.getGenericsTypes();
        if (sgts!=null) {
            ClassNode[] spec = new ClassNode[sgts.length];
            for (int i = 0; i < spec.length; i++) {
                spec[i]=correctToGenericsSpec(ret, sgts[i]);
            }
            GenericsType[] newGts = current.redirect().getGenericsTypes();
            if (newGts==null) return ret;
            ret.clear();
            for (int i = 0; i < spec.length; i++) {
                ret.put(newGts[i].getName(), spec[i]);
            }
        }
        return ret;
    }

}
