/*
 * Copyright 2003-2012 the original author or authors.
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
import groovy.lang.MetaClass;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.ClassNodeSkip;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.reflection.ClassInfo;
import org.objectweb.asm.Label;
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

    public static final String STATIC_METACLASS_BOOL = "__$stMC";
    public static final String SWAP_INIT = "__$swapInit";
    public static final String INITIAL_EXPRESSION = "INITIAL_EXPRESSION";

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
    private static final Parameter[] SET_METACLASS_PARAMS = new Parameter[]{
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

    private FieldNode setMetaClassFieldIfNotExists(ClassNode node, FieldNode metaClassField) {
        if (metaClassField != null) return metaClassField;
        final String classInternalName = BytecodeHelper.getClassInternalName(node);
        metaClassField =
            node.addField("metaClass", ACC_PRIVATE | ACC_TRANSIENT | ACC_SYNTHETIC, ClassHelper.METACLASS_TYPE,
                    new BytecodeExpression(ClassHelper.METACLASS_TYPE) {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "$getStaticMetaClass", "()Lgroovy/lang/MetaClass;");
                        }});
        metaClassField.setSynthetic(true);
        return metaClassField;
    }

    private FieldNode getMetaClassField(ClassNode node) {
        FieldNode ret = node.getDeclaredField("metaClass");
        if (ret != null) {
            ClassNode mcFieldType = ret.getType();
            if (!mcFieldType.equals(ClassHelper.METACLASS_TYPE)) {
                throw new RuntimeParserException("The class " + node.getName() +
                        " cannot declare field 'metaClass' of type " + mcFieldType.getName() + " as it needs to be of " +
                        "the type " + ClassHelper.METACLASS_TYPE.getName() + " for internal groovy purposes", ret);
            }
            return ret;
        }
        ClassNode current = node;
        while (current != ClassHelper.OBJECT_TYPE) {
            current = current.getSuperClass();
            if (current == null) break;
            ret = current.getDeclaredField("metaClass");
            if (ret == null) continue;
            if (Modifier.isPrivate(ret.getModifiers())) continue;
            return ret;
        }
        return null;
    }

    /**
     * add code to implement GroovyObject
     *
     * @param node
     */
    public void visitClass(final ClassNode node) {
        this.classNode = node;

        if ((classNode.getModifiers() & Opcodes.ACC_INTERFACE) > 0) {
            //interfaces have no constructors, but this code expects one,
            //so create a dummy and don't add it to the class node
            ConstructorNode dummy = new ConstructorNode(0, null);
            addInitialization(node, dummy);
            node.visitContents(this);
            if (classNode.getNodeMetaData(ClassNodeSkip.class)==null) {
                classNode.setNodeMetaData(ClassNodeSkip.class,true);
            }
            return;
        }

        ClassNode[] classNodes = classNode.getInterfaces();
        List<String> interfaces = new ArrayList<String>();
        for (ClassNode classNode : classNodes) {
            interfaces.add(classNode.getName());
        }
        Set<String> interfaceSet = new HashSet<String>(interfaces);
        if (interfaceSet.size() != interfaces.size()) {
            throw new RuntimeParserException("Duplicate interfaces in implements list: " + interfaces, classNode);
        }

        addDefaultParameterMethods(node);
        addDefaultParameterConstructors(node);

        final String classInternalName = BytecodeHelper.getClassInternalName(node);

        addStaticMetaClassField(node, classInternalName);

        boolean knownSpecialCase =
                node.isDerivedFrom(ClassHelper.GSTRING_TYPE)
                        || node.isDerivedFrom(ClassHelper.GROOVY_OBJECT_SUPPORT_TYPE);

        addFastPathHelperFieldsAndHelperMethod(node, classInternalName, knownSpecialCase);
        if (!knownSpecialCase) addGroovyObjectInterfaceAndMethods(node, classInternalName);

        addDefaultConstructor(node);

        // add a static timestamp field to the class
        if (!(node instanceof InnerClassNode)) addTimeStamp(node);

        addInitialization(node);
        checkReturnInObjectInitializer(node.getObjectInitializerStatements());
        node.getObjectInitializerStatements().clear();
        node.visitContents(this);
        addCovariantMethods(node);
    }

    private FieldNode checkFieldDoesNotExist(ClassNode node, String fieldName) {
        FieldNode ret = node.getDeclaredField(fieldName);
        if (ret != null) {
            if (    Modifier.isPublic(ret.getModifiers()) &&
                    ret.getType().redirect()==ClassHelper.boolean_TYPE) {
                return ret;
            }
            throw new RuntimeParserException("The class " + node.getName() +
                    " cannot declare field '"+fieldName+"' as this" +
                    " field is needed for internal groovy purposes", ret);
        }
        return null;
    }

    private void addFastPathHelperFieldsAndHelperMethod(ClassNode node, final String classInternalName, boolean knownSpecialCase) {
        if (node.getNodeMetaData(ClassNodeSkip.class)!=null) return;
        FieldNode stMCB = checkFieldDoesNotExist(node,STATIC_METACLASS_BOOL);
        if (stMCB==null) {
            stMCB = node.addField(
                    STATIC_METACLASS_BOOL,
                    ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC | ACC_TRANSIENT,
                    ClassHelper.boolean_TYPE, null);
            stMCB.setSynthetic(true);
        }
    }

    protected void addDefaultConstructor(ClassNode node) {
        if (!node.getDeclaredConstructors().isEmpty()) return;

        BlockStatement empty = new BlockStatement();
        empty.setSourcePosition(node);
        ConstructorNode constructor = new ConstructorNode(ACC_PUBLIC, empty);
        constructor.setSourcePosition(node);
        constructor.setHasNoRealSourcePosition(true);
        node.addConstructor(constructor);
    }

    private static boolean isInnerClassOf(ClassNode a, ClassNode b) {
        if (a.redirect()==b) return true;
        if (b.redirect() instanceof InnerClassNode) return isInnerClassOf(a, b.redirect().getOuterClass());
        return false;
    }
    
    private void addStaticMetaClassField(final ClassNode node, final String classInternalName) {
        String _staticClassInfoFieldName = "$staticClassInfo";
        while (node.getDeclaredField(_staticClassInfoFieldName) != null)
            _staticClassInfoFieldName = _staticClassInfoFieldName + "$";
        final String staticMetaClassFieldName = _staticClassInfoFieldName;

        FieldNode staticMetaClassField = node.addField(staticMetaClassFieldName, ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, ClassHelper.make(ClassInfo.class, false), null);
        staticMetaClassField.setSynthetic(true);

        node.addSyntheticMethod(
                "$getStaticMetaClass",
                ACC_PROTECTED,
                ClassHelper.make(MetaClass.class),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new BytecodeSequence(new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        if (BytecodeHelper.isClassLiteralPossible(node) || BytecodeHelper.isSameCompilationUnit(classNode, node)) {
                            BytecodeHelper.visitClassLiteral(mv,node);
                        } else {
                            mv.visitMethodInsn(INVOKESTATIC, classInternalName, "$get$$class$" + classInternalName.replaceAll("\\/", "\\$"), "()Ljava/lang/Class;");
                        }
                        Label l1 = new Label();
                        mv.visitJumpInsn(IF_ACMPEQ, l1);

                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ScriptBytecodeAdapter", "initMetaClass", "(Ljava/lang/Object;)Lgroovy/lang/MetaClass;");
                        mv.visitInsn(ARETURN);

                        mv.visitLabel(l1);

                        mv.visitFieldInsn(GETSTATIC, classInternalName, staticMetaClassFieldName, "Lorg/codehaus/groovy/reflection/ClassInfo;");
                        mv.visitVarInsn(ASTORE, 1);
                        mv.visitVarInsn(ALOAD, 1);
                        Label l0 = new Label();
                        mv.visitJumpInsn(IFNONNULL, l0);

                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/reflection/ClassInfo", "getClassInfo", "(Ljava/lang/Class;)Lorg/codehaus/groovy/reflection/ClassInfo;");
                        mv.visitInsn(DUP);
                        mv.visitVarInsn(ASTORE, 1);
                        mv.visitFieldInsn(PUTSTATIC, classInternalName, staticMetaClassFieldName, "Lorg/codehaus/groovy/reflection/ClassInfo;");

                        mv.visitLabel(l0);

                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/ClassInfo", "getMetaClass", "()Lgroovy/lang/MetaClass;");
                        mv.visitInsn(ARETURN);

                    }
                })
        );
    }

    protected void addGroovyObjectInterfaceAndMethods(ClassNode node, final String classInternalName) {
        if (!node.isDerivedFromGroovyObject()) node.addInterface(ClassHelper.make(GroovyObject.class));
        FieldNode metaClassField = getMetaClassField(node);

        if (!node.hasMethod("getMetaClass", Parameter.EMPTY_ARRAY)) {
            metaClassField = setMetaClassFieldIfNotExists(node, metaClassField);
            addMethod(node, !Modifier.isAbstract(node.getModifiers()),
                    "getMetaClass",
                    ACC_PUBLIC,
                    ClassHelper.METACLASS_TYPE,
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY,
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            Label nullLabel = new Label();
                            /**
                             *  the code is:
                             *  if (this.metaClass==null) {
                             *      this.metaClass = this.$getStaticMetaClass
                             *      return this.metaClass
                             *  } else {
                             *      return this.metaClass
                             *  }
                             *  with the optimization that the result of the
                             *  first this.metaClass is duped on the operand
                             *  stack and reused for the return in the else part
                             */
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                            mv.visitInsn(DUP);
                            mv.visitJumpInsn(IFNULL, nullLabel);
                            mv.visitInsn(ARETURN);

                            mv.visitLabel(nullLabel);
                            mv.visitInsn(POP);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitInsn(DUP);
                            mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "$getStaticMetaClass", "()Lgroovy/lang/MetaClass;");
                            mv.visitFieldInsn(PUTFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                            mv.visitInsn(ARETURN);
                        }
                    })
            );
        }

        Parameter[] parameters = new Parameter[]{new Parameter(ClassHelper.METACLASS_TYPE, "mc")};
        if (!node.hasMethod("setMetaClass", parameters)) {
            metaClassField = setMetaClassFieldIfNotExists(node, metaClassField);
            Statement setMetaClassCode;
            if (Modifier.isFinal(metaClassField.getModifiers())) {
                ConstantExpression text = new ConstantExpression("cannot set read-only meta class");
                ConstructorCallExpression cce = new ConstructorCallExpression(ClassHelper.make(IllegalArgumentException.class), text);
                setMetaClassCode = new ExpressionStatement(cce);
            } else {
                List list = new ArrayList();
                list.add(new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        /**
                         * the code is (meta class is stored in 1):
                         * this.metaClass = <1>
                         */
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, classInternalName,
                                "metaClass", "Lgroovy/lang/MetaClass;");
                        mv.visitInsn(RETURN);
                    }
                });
                setMetaClassCode = new BytecodeSequence(list);
            }

            addMethod(node, !Modifier.isAbstract(node.getModifiers()),
                    "setMetaClass",
                    ACC_PUBLIC, ClassHelper.VOID_TYPE,
                    SET_METACLASS_PARAMS, ClassNode.EMPTY_ARRAY,
                    setMetaClassCode
            );
        }

        if (!node.hasMethod("invokeMethod", INVOKE_METHOD_PARAMS)) {
            VariableExpression vMethods = new VariableExpression("method");
            VariableExpression vArguments = new VariableExpression("arguments");
            VariableScope blockScope = new VariableScope();
            blockScope.putReferencedLocalVariable(vMethods);
            blockScope.putReferencedLocalVariable(vArguments);

            addMethod(node, !Modifier.isAbstract(node.getModifiers()),
                    "invokeMethod",
                    ACC_PUBLIC,
                    ClassHelper.OBJECT_TYPE, INVOKE_METHOD_PARAMS,
                    ClassNode.EMPTY_ARRAY,
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "invokeMethod", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
                            mv.visitInsn(ARETURN);
                        }
                    })
            );
        }

        if (!node.hasMethod("getProperty", GET_PROPERTY_PARAMS)) {
            addMethod(node, !Modifier.isAbstract(node.getModifiers()),
                    "getProperty",
                    ACC_PUBLIC,
                    ClassHelper.OBJECT_TYPE,
                    GET_PROPERTY_PARAMS,
                    ClassNode.EMPTY_ARRAY,
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "getProperty", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
                            mv.visitInsn(ARETURN);
                        }
                    })
            );
        }

        if (!node.hasMethod("setProperty", SET_PROPERTY_PARAMS)) {
            addMethod(node, !Modifier.isAbstract(node.getModifiers()),
                    "setProperty",
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    SET_PROPERTY_PARAMS,
                    ClassNode.EMPTY_ARRAY,
                    new BytecodeSequence(new BytecodeInstruction() {
                        public void visit(MethodVisitor mv) {
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");
                            mv.visitInsn(RETURN);
                        }
                    })
            );
        }
    }

    /**
     * Helper method to add a new method to a ClassNode.  Depending on the shouldBeSynthetic flag the
     * call will either be made to ClassNode.addSyntheticMethod() or ClassNode.addMethod(). If a non-synthetic method
     * is to be added the ACC_SYNTHETIC modifier is removed if it has been accidentally supplied.
     */
    protected void addMethod(ClassNode node, boolean shouldBeSynthetic, String name, int modifiers, ClassNode returnType, Parameter[] parameters,
                             ClassNode[] exceptions, Statement code) {
        if (shouldBeSynthetic) {
            node.addSyntheticMethod(name, modifiers, returnType, parameters, exceptions, code);
        } else {
            node.addMethod(name, modifiers & ~ACC_SYNTHETIC, returnType, parameters, exceptions, code);
        }
    }

    protected void addTimeStamp(ClassNode node) {
        if (node.getDeclaredField(Verifier.__TIMESTAMP) == null) { // in case if verifier visited the call already
            FieldNode timeTagField = new FieldNode(
                    Verifier.__TIMESTAMP,
                    ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                    ClassHelper.long_TYPE,
                    //"",
                    node,
                    new ConstantExpression(System.currentTimeMillis()));
            // alternatively, FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
            timeTagField.setSynthetic(true);
            node.addField(timeTagField);

            timeTagField = new FieldNode(
                    Verifier.__TIMESTAMP__ + String.valueOf(System.currentTimeMillis()),
                    ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                    ClassHelper.long_TYPE,
                    //"",
                    node,
                    new ConstantExpression((long) 0));
            // alternatively, FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
            timeTagField.setSynthetic(true);
            node.addField(timeTagField);
        }
    }

    private void checkReturnInObjectInitializer(List init) {
        CodeVisitorSupport cvs = new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                // return is OK in closures in object initializers
            }

            public void visitReturnStatement(ReturnStatement statement) {
                throw new RuntimeParserException("'return' is not allowed in object initializer", statement);
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
            String type = null;

            public void visitMethodCallExpression(MethodCallExpression call) {
                if (!firstMethodCall) return;
                firstMethodCall = false;
                String name = call.getMethodAsString();
                // the name might be null if the method name is a GString for example
                if (name == null) return;
                if (!name.equals("super") && !name.equals("this")) return;
                type = name;
                call.getArguments().visit(this);
                type = null;
            }

            public void visitConstructorCallExpression(ConstructorCallExpression call) {
                if (!call.isSpecialCall()) return;
                type = call.getText();
                call.getArguments().visit(this);
                type = null;
            }

            public void visitVariableExpression(VariableExpression expression) {
                if (type == null) return;
                String name = expression.getName();
                if (!name.equals("this") && !name.equals("super")) return;
                throw new RuntimeParserException("cannot reference " + name + " inside of " + type + "(....) before supertype constructor has been called", expression);
            }
        };
        Statement s = node.getCode();
        if (s == null) {
            return;
        } else {
            s.visit(new VerifierCodeVisitor(this));
        }
        s.visit(checkSuper);
    }

    public void visitMethod(MethodNode node) {
        //GROOVY-3712 - if it's an MOP method, it's an error as they aren't supposed to exist before ACG is invoked
        if(MopWriter.isMopMethod(node.getName())) {
            throw new RuntimeParserException("Found unexpected MOP methods in the class node for " + classNode.getName() +
                    "(" + node.getName() + ")", classNode);
        }
        this.methodNode = node;
        adjustTypesIfStaticMainMethod(node);
        addReturnIfNeeded(node);
        Statement statement;
        statement = node.getCode();
        if (statement != null) statement.visit(new VerifierCodeVisitor(this));
    }

    private void adjustTypesIfStaticMainMethod(MethodNode node) {
        if (node.getName().equals("main") && node.isStatic()) {
            Parameter[] params = node.getParameters();
            if (params.length == 1) {
                Parameter param = params[0];
                if (param.getType() == null || param.getType() == ClassHelper.OBJECT_TYPE) {
                    param.setType(ClassHelper.STRING_TYPE.makeArray());
                    ClassNode returnType = node.getReturnType();
                    if (returnType == ClassHelper.OBJECT_TYPE) {
                        node.setReturnType(ClassHelper.VOID_TYPE);
                    }
                }
            }
        }
    }

    protected void addReturnIfNeeded(MethodNode node) {
        ReturnAdder adder = new ReturnAdder();
        adder.visitMethod(node);
    }

    public void visitField(FieldNode node) {
    }

    private boolean methodNeedsReplacement(MethodNode m) {
        // no method found, we need to replace
        if (m == null) return true;
        // method is in current class, nothing to be done
        if (m.getDeclaringClass() == this.getClassNode()) return false;
        // do not overwrite final
        if ((m.getModifiers() & ACC_FINAL) != 0) return false;
        return true;
    }

    public void visitProperty(PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();
        int propNodeModifiers = node.getModifiers();

        String getterName = "get" + capitalize(name);
        String setterName = "set" + capitalize(name);

        // GROOVY-3726: clear volatile, transient modifiers so that they don't get applied to methods
        if ((propNodeModifiers & Modifier.VOLATILE) != 0) {
            propNodeModifiers = propNodeModifiers - Modifier.VOLATILE;
        }
        if ((propNodeModifiers & Modifier.TRANSIENT) != 0) {
            propNodeModifiers = propNodeModifiers - Modifier.TRANSIENT;
        }

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            MethodNode getter = classNode.getGetterMethod(getterName);
            if (getter == null && ClassHelper.boolean_TYPE == node.getType()) {
                String secondGetterName = "is" + capitalize(name);
                getter = classNode.getGetterMethod(secondGetterName);
            }
            if (!node.isPrivate() && methodNeedsReplacement(getter)) {
                getterBlock = createGetterBlock(node, field);
            }
        }
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null) {
            // 2nd arg false below: though not usual, allow setter with non-void return type
            MethodNode setter = classNode.getSetterMethod(setterName, false);
            if (!node.isPrivate() &&
                    (propNodeModifiers & ACC_FINAL) == 0 &&
                    methodNeedsReplacement(setter)) {
                setterBlock = createSetterBlock(node, field);
            }
        }

        if (getterBlock != null) {
            MethodNode getter =
                    new MethodNode(getterName, propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
            getter.setSynthetic(true);
            addPropertyMethod(getter);
            visitMethod(getter);

            if (ClassHelper.boolean_TYPE == node.getType() || ClassHelper.Boolean_TYPE == node.getType()) {
                String secondGetterName = "is" + capitalize(name);
                MethodNode secondGetter =
                        new MethodNode(secondGetterName, propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
                secondGetter.setSynthetic(true);
                addPropertyMethod(secondGetter);
                visitMethod(secondGetter);
            }
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = {new Parameter(node.getType(), "value")};
            MethodNode setter =
                    new MethodNode(setterName, propNodeModifiers, ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
            setter.setSynthetic(true);
            addPropertyMethod(setter);
            visitMethod(setter);
        }
    }

    protected void addPropertyMethod(MethodNode method) {
        classNode.addMethod(method);
        // GROOVY-4415 / GROOVY-4645: check that there's no abstract method which corresponds to this one
        List<MethodNode> abstractMethods = classNode.getAbstractMethods();
        if (abstractMethods==null) return;
        String methodName = method.getName();
        Parameter[] parameters = method.getParameters();
        ClassNode methodReturnType = method.getReturnType();
        for (MethodNode node : abstractMethods) {
            if (!node.getDeclaringClass().equals(classNode)) continue;
            if (node.getName().equals(methodName)
                    && node.getParameters().length==parameters.length) {
                if (parameters.length==1) {
                    // setter
                    ClassNode abstractMethodParameterType = node.getParameters()[0].getType();
                    ClassNode methodParameterType = parameters[0].getType();
                    if (!methodParameterType.isDerivedFrom(abstractMethodParameterType) && !methodParameterType.implementsInterface(abstractMethodParameterType)) {
                        continue;
                    }
                }
                ClassNode nodeReturnType = node.getReturnType();
                if (!methodReturnType.isDerivedFrom(nodeReturnType) && !methodReturnType.implementsInterface(nodeReturnType)) {
                    continue;
                }
                // matching method, remove abstract status and use the same body
                node.setModifiers(node.getModifiers() ^ ACC_ABSTRACT);
                node.setCode(method.getCode());
            }
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    public interface DefaultArgsAction {
        void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method);
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions
     */
    protected void addDefaultParameterMethods(final ClassNode node) {
        List methods = new ArrayList(node.getMethods());
        addDefaultParameters(methods, new DefaultArgsAction() {
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                final BlockStatement code = new BlockStatement();

                MethodNode newMethod = new MethodNode(method.getName(), method.getModifiers(), method.getReturnType(), newParams, method.getExceptions(), code);

                // GROOVY-5681 and GROOVY-5632
                for (Expression argument : arguments.getExpressions()) {
                    if (argument instanceof CastExpression) {
                        argument = ((CastExpression) argument).getExpression();
                    }
                    if (argument instanceof ConstructorCallExpression) {
                        ClassNode type = argument.getType();
                        if (type instanceof InnerClassNode && ((InnerClassNode) type).isAnonymous()) {
                            type.setEnclosingMethod(newMethod);
                        }
                    }

                    // check whether closure shared variables refer to params with default values (GROOVY-5632)
                    if (argument instanceof ClosureExpression)  {
                        final List<Parameter> newMethodNodeParameters = Arrays.asList(newParams);

                        CodeVisitorSupport visitor = new CodeVisitorSupport() {
                            @Override
                            public void visitVariableExpression(VariableExpression expression) {
                                Variable v = expression.getAccessedVariable();
                                if (!(v instanceof Parameter)) return;

                                Parameter param = (Parameter) v;
                                if (param.hasInitialExpression() && code.getVariableScope().getDeclaredVariable(param.getName()) == null && !newMethodNodeParameters.contains(param))  {

                                    VariableExpression localVariable = new VariableExpression(param.getName(), ClassHelper.makeReference());
                                    DeclarationExpression declarationExpression = new DeclarationExpression(localVariable, Token.newSymbol(Types.EQUAL, -1, -1), new ConstructorCallExpression(ClassHelper.makeReference(), param.getInitialExpression()));

                                    code.addStatement(new ExpressionStatement(declarationExpression));
                                    code.getVariableScope().putDeclaredVariable(localVariable);
                                }
                            }
                        };

                        visitor.visitClosureExpression((ClosureExpression) argument);
                    }
                }

                MethodCallExpression expression = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, method.getName(), arguments);
                expression.setMethodTarget(method);
                expression.setImplicitThis(true);

                if (method.isVoidMethod()) {
                    code.addStatement(new ExpressionStatement(expression));
                } else {
                    code.addStatement(new ReturnStatement(expression));
                }

                List<AnnotationNode> annotations = method.getAnnotations();
                if(annotations != null) {
                    newMethod.addAnnotations(annotations);
                }
                MethodNode oldMethod = node.getDeclaredMethod(method.getName(), newParams);
                if (oldMethod != null) {
                    throw new RuntimeParserException(
                            "The method with default parameters \"" + method.getTypeDescriptor() +
                                    "\" defines a method \"" + newMethod.getTypeDescriptor() +
                                    "\" that is already defined.",
                            method);
                }
                addPropertyMethod(newMethod);
                newMethod.setGenericsTypes(method.getGenericsTypes());
            }
        });
    }

    protected void addDefaultParameterConstructors(final ClassNode node) {
        List methods = new ArrayList(node.getDeclaredConstructors());
        addDefaultParameters(methods, new DefaultArgsAction() {
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                ConstructorNode ctor = (ConstructorNode) method;
                ConstructorCallExpression expression = new ConstructorCallExpression(ClassNode.THIS, arguments);
                Statement code = new ExpressionStatement(expression);
                addConstructor(newParams, ctor, code, node);
            }
        });
    }

    protected void addConstructor(Parameter[] newParams, ConstructorNode ctor, Statement code, ClassNode node) {
        node.addConstructor(ctor.getModifiers(), newParams, ctor.getExceptions(), code);
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions
     */
    protected void addDefaultParameters(List methods, DefaultArgsAction action) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (method.hasDefaultValue()) {
                addDefaultParameters(action, method);
            }
        }
    }

    protected void addDefaultParameters(DefaultArgsAction action, MethodNode method) {
        Parameter[] parameters = method.getParameters();
        int counter = 0;
        List paramValues = new ArrayList();
        int size = parameters.length;
        for (int i = size - 1; i >= 0; i--) {
            Parameter parameter = parameters[i];
            if (parameter != null && parameter.hasInitialExpression()) {
                paramValues.add(Integer.valueOf(i));
                paramValues.add(
                        new CastExpression(
                                parameter.getType(),
                                parameter.getInitialExpression()
                        )
                );
                counter++;
            }
        }

        for (int j = 1; j <= counter; j++) {
            Parameter[] newParams = new Parameter[parameters.length - j];
            ArgumentListExpression arguments = new ArgumentListExpression();
            int index = 0;
            int k = 1;
            for (int i = 0; i < parameters.length; i++) {
                if (k > counter - j && parameters[i] != null && parameters[i].hasInitialExpression()) {
                    arguments.addExpression(
                            new CastExpression(
                                    parameters[i].getType(),
                                    parameters[i].getInitialExpression()
                            )
                    );
                    k++;
                } else if (parameters[i] != null && parameters[i].hasInitialExpression()) {
                    newParams[index++] = parameters[i];
                    arguments.addExpression(
                            new CastExpression(
                                    parameters[i].getType(),
                                    new VariableExpression(parameters[i].getName())
                            )
                    );
                    k++;
                } else {
                    newParams[index++] = parameters[i];
                    arguments.addExpression(
                            new CastExpression(
                                    parameters[i].getType(),
                                    new VariableExpression(parameters[i].getName())
                            )
                    );
                }
            }
            action.call(arguments, newParams, method);
        }

        for (Parameter parameter : parameters) {
            // remove default expression and store it as node metadata
            parameter.putNodeMetaData(Verifier.INITIAL_EXPRESSION, parameter.getInitialExpression());
            parameter.setInitialExpression(null);
        }
    }

    protected void addClosureCode(InnerClassNode node) {
        // add a new invoke
    }

    protected void addInitialization(final ClassNode node) {
        boolean addSwapInit = moveOptimizedConstantsInitialization(node);

        for (ConstructorNode cn : node.getDeclaredConstructors()) {
            addInitialization(node, cn);
        }

        if (addSwapInit) {
            BytecodeSequence seq = new BytecodeSequence(
                    new BytecodeInstruction() {
                        @Override
                        public void visit(MethodVisitor mv) {
                            mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(node), SWAP_INIT, "()V");
                        }
                    });

            List<Statement> swapCall= new ArrayList<Statement>(1);
            swapCall.add(seq);
            node.addStaticInitializerStatements(swapCall, true);
        }
    }

    protected void addInitialization(ClassNode node, ConstructorNode constructorNode) {
        Statement firstStatement = constructorNode.getFirstStatement();
        // if some transformation decided to generate constructor then it probably knows who it does
        if (firstStatement instanceof BytecodeSequence)
            return;

        ConstructorCallExpression first = getFirstIfSpecialConstructorCall(firstStatement);

        // in case of this(...) let the other constructor do the init
        if (first != null && (first.isThisCall())) return;

        List<Statement> statements = new ArrayList<Statement>();
        List<Statement> staticStatements = new ArrayList<Statement>();
        final boolean isEnum = node.isEnum();
        List<Statement> initStmtsAfterEnumValuesInit = new ArrayList<Statement>();
        Set<String> explicitStaticPropsInEnum = new HashSet<String>();
        if (isEnum) {
            for (PropertyNode propNode : node.getProperties()) {
                if (!propNode.isSynthetic() && propNode.getField().isStatic()) {
                    explicitStaticPropsInEnum.add(propNode.getField().getName());
                }
            }
            for (FieldNode fieldNode : node.getFields()) {
                if (!fieldNode.isSynthetic() && fieldNode.isStatic() && fieldNode.getType() != node) {
                    explicitStaticPropsInEnum.add(fieldNode.getName());
                }
            }
        }

        for (FieldNode fn : node.getFields()) {
            addFieldInitialization(statements, staticStatements, fn, isEnum,
                    initStmtsAfterEnumValuesInit, explicitStaticPropsInEnum);
        }

        statements.addAll(node.getObjectInitializerStatements());

        Statement code = constructorNode.getCode();
        BlockStatement block = new BlockStatement();
        List<Statement> otherStatements = block.getStatements();
        if (code instanceof BlockStatement) {
            block = (BlockStatement) code;
            otherStatements = block.getStatements();
        } else if (code != null) {
            otherStatements.add(code);
        }
        if (!otherStatements.isEmpty()) {
            if (first != null) {
                // it is super(..) since this(..) is already covered
                otherStatements.remove(0);
                statements.add(0, firstStatement);
            }
            Statement stmtThis$0 = getImplicitThis$0StmtIfInnerClass(otherStatements);
            if (stmtThis$0 != null) {
                // since there can be field init statements that depend on method/property dispatching
                // that uses this$0, it needs to bubble up before the super call itself (GROOVY-4471)
                statements.add(0, stmtThis$0);
            }
            statements.addAll(otherStatements);
        }
        BlockStatement newBlock = new BlockStatement(statements, block.getVariableScope());
        newBlock.setSourcePosition(block);
        constructorNode.setCode(newBlock);



        if (!staticStatements.isEmpty()) {
            if (isEnum) {
                /*
                 * GROOVY-3161: initialize statements for explicitly declared static fields
                 * inside an enum should come after enum values are initialized
                 */
                staticStatements.removeAll(initStmtsAfterEnumValuesInit);
                node.addStaticInitializerStatements(staticStatements, true);
                if (!initStmtsAfterEnumValuesInit.isEmpty()) {
                    node.positionStmtsAfterEnumInitStmts(initStmtsAfterEnumValuesInit);
                }
            } else {
                node.addStaticInitializerStatements(staticStatements, true);
            }
        }
    }

    /*
    *  when InnerClassVisitor adds this.this$0 = $p$n, it adds it as a BlockStatement having that
    *  ExpressionStatement
    */
    private Statement getImplicitThis$0StmtIfInnerClass(List<Statement> otherStatements) {
        if (!(classNode instanceof InnerClassNode)) return null;
        for (Statement stmt : otherStatements) {
            if (stmt instanceof BlockStatement) {
                List<Statement> stmts = ((BlockStatement) stmt).getStatements();
                for (Statement bstmt : stmts) {
                    if (bstmt instanceof ExpressionStatement) {
                        if (extractImplicitThis$0StmtIfInnerClassFromExpression(stmts, bstmt)) return bstmt;
                    }
                }
            } else if (stmt instanceof ExpressionStatement) {
                if (extractImplicitThis$0StmtIfInnerClassFromExpression(otherStatements, stmt)) return stmt;
            }
        }
        return null;
    }

    private boolean extractImplicitThis$0StmtIfInnerClassFromExpression(final List<Statement> stmts, final Statement bstmt) {
        Expression expr = ((ExpressionStatement) bstmt).getExpression();
        if (expr instanceof BinaryExpression) {
            Expression lExpr = ((BinaryExpression) expr).getLeftExpression();
            if (lExpr instanceof FieldExpression) {
                if ("this$0".equals(((FieldExpression) lExpr).getFieldName())) {
                    stmts.remove(bstmt); // remove from here and let the caller reposition it
                    return true;
                }
            }
        }
        return false;
    }

    private ConstructorCallExpression getFirstIfSpecialConstructorCall(Statement code) {
        if (code == null || !(code instanceof ExpressionStatement)) return null;

        Expression expression = ((ExpressionStatement) code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        if (cce.isSpecialCall()) return cce;
        return null;
    }

    protected void addFieldInitialization(List list, List staticList, FieldNode fieldNode,
                                          boolean isEnumClassNode, List initStmtsAfterEnumValuesInit, Set explicitStaticPropsInEnum) {
        Expression expression = fieldNode.getInitialExpression();
        if (expression != null) {
            final FieldExpression fe = new FieldExpression(fieldNode);
            if (fieldNode.getType().equals(ClassHelper.REFERENCE_TYPE) && ((fieldNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0)) {
                fe.setUseReferenceDirectly(true);
            }
            ExpressionStatement statement =
                    new ExpressionStatement(
                            new BinaryExpression(
                                    fe,
                                    Token.newSymbol(Types.EQUAL, fieldNode.getLineNumber(), fieldNode.getColumnNumber()),
                                    expression));
            if (fieldNode.isStatic()) {
                // GROOVY-3311: pre-defined constants added by groovy compiler for numbers/characters should be
                // initialized first so that code dependent on it does not see their values as empty
                Expression initialValueExpression = fieldNode.getInitialValueExpression();
                if (initialValueExpression instanceof ConstantExpression) {
                    ConstantExpression cexp = (ConstantExpression) initialValueExpression;
                    cexp = transformToPrimitiveConstantIfPossible(cexp);
                    if (fieldNode.isFinal() && ClassHelper.isStaticConstantInitializerType(cexp.getType()) && cexp.getType().equals(fieldNode.getType())) {
                        return; // GROOVY-5150: primitive type constants will be initialized directly
                    }
                    staticList.add(0, statement);
                } else {
                    staticList.add(statement);
                }
                fieldNode.setInitialValueExpression(null); // to avoid double initialization in case of several constructors
                /*
                 * If it is a statement for an explicitly declared static field inside an enum, store its
                 * reference. For enums, they need to be handled differently as such init statements should
                 * come after the enum values have been initialized inside <clinit> block. GROOVY-3161.
                 */
                if (isEnumClassNode && explicitStaticPropsInEnum.contains(fieldNode.getName())) {
                    initStmtsAfterEnumValuesInit.add(statement);
                }
            } else {
                list.add(statement);
            }
        }
    }

    /**
     * Capitalizes the start of the given bean property name
     */
    public static String capitalize(String name) {
        return MetaClassHelper.capitalize(name);
    }

    protected Statement createGetterBlock(PropertyNode propertyNode, final FieldNode field) {
        return new BytecodeSequence(new BytecodeInstruction() {
            public void visit(MethodVisitor mv) {
                if (field.isStatic()) {
                    mv.visitFieldInsn(GETSTATIC, BytecodeHelper.getClassInternalName(classNode), field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(classNode), field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                }
                BytecodeHelper.doReturn(mv, field.getType());
            }
        });
    }

    protected Statement createSetterBlock(PropertyNode propertyNode, final FieldNode field) {
        return new BytecodeSequence(new BytecodeInstruction() {
            public void visit(MethodVisitor mv) {
                if (field.isStatic()) {
                    BytecodeHelper.load(mv, field.getType(), 0);
                    mv.visitFieldInsn(PUTSTATIC, BytecodeHelper.getClassInternalName(classNode), field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    BytecodeHelper.load(mv, field.getType(), 1);
                    mv.visitFieldInsn(PUTFIELD, BytecodeHelper.getClassInternalName(classNode), field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                }
                mv.visitInsn(RETURN);
            }
        });
    }

    public void visitGenericType(GenericsType genericsType) {

    }

    public static long getTimestamp(Class clazz) {
        if (clazz.getClassLoader() instanceof GroovyClassLoader.InnerLoader) {
            GroovyClassLoader.InnerLoader innerLoader = (GroovyClassLoader.InnerLoader) clazz.getClassLoader();
            return innerLoader.getTimeStamp();
        }

        final Field[] fields = clazz.getFields();
        for (int i = 0; i != fields.length; ++i) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                final String name = fields[i].getName();
                if (name.startsWith(__TIMESTAMP__)) {
                    try {
                        return Long.decode(name.substring(__TIMESTAMP__.length())).longValue();
                    } catch (NumberFormatException e) {
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

        // unimplemented abstract methods from interfaces
        Map abstractMethods = new HashMap();
        Map<String, MethodNode> allInterfaceMethods = new HashMap<String, MethodNode>();
        ClassNode[] interfaces = classNode.getInterfaces();
        for (ClassNode iface : interfaces) {
            Map ifaceMethodsMap = iface.getDeclaredMethodsMap();
            abstractMethods.putAll(ifaceMethodsMap);
            allInterfaceMethods.putAll(ifaceMethodsMap);
        }

        collectSuperInterfaceMethods(classNode, allInterfaceMethods);

        List<MethodNode> declaredMethods = new ArrayList<MethodNode>(classNode.getMethods());
        // remove all static, private and package private methods
        for (Iterator methodsIterator = declaredMethods.iterator(); methodsIterator.hasNext();) {
            MethodNode m = (MethodNode) methodsIterator.next();
            abstractMethods.remove(m.getTypeDescriptor());
            if (m.isStatic() || !(m.isPublic() || m.isProtected())) {
                methodsIterator.remove();
            }
            MethodNode intfMethod = allInterfaceMethods.get(m.getTypeDescriptor());
            if (intfMethod != null && ((m.getModifiers() & ACC_SYNTHETIC) == 0)
                    && !m.isPublic() && !m.isStaticConstructor()) {
                throw new RuntimeParserException("The method " + m.getName() +
                        " should be public as it implements the corresponding method from interface " +
                        intfMethod.getDeclaringClass(), m);

            }
        }

        addCovariantMethods(classNode, declaredMethods, abstractMethods, methodsToAdd, genericsSpec);

        Map<String, MethodNode> declaredMethodsMap = new HashMap<String, MethodNode>();
        if (methodsToAdd.size() > 0) {
            for (MethodNode mn : declaredMethods) {
                declaredMethodsMap.put(mn.getTypeDescriptor(), mn);
            }
        }

        for (Object o : methodsToAdd.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            MethodNode method = (MethodNode) entry.getValue();
            // we skip bridge methods implemented in current class already
            MethodNode mn = declaredMethodsMap.get(entry.getKey());
            if (mn != null && mn.getDeclaringClass().equals(classNode)) continue;
            addPropertyMethod(method);
        }
    }

    private void collectSuperInterfaceMethods(ClassNode cn, Map<String, MethodNode> allInterfaceMethods) {
        List cnInterfaces = Arrays.asList(cn.getInterfaces());
        ClassNode sn = cn.getSuperClass();
        while (sn != null && !sn.equals(ClassHelper.OBJECT_TYPE)) {
            ClassNode[] interfaces = sn.getInterfaces();
            for (ClassNode iface : interfaces) {
                if (!cnInterfaces.contains(iface)) {
                    Map<String, MethodNode> ifaceMethodsMap = iface.getDeclaredMethodsMap();
                    allInterfaceMethods.putAll(ifaceMethodsMap);
                }
            }
            sn = sn.getSuperClass();
        }
    }

    private void addCovariantMethods(ClassNode classNode, List declaredMethods, Map abstractMethods, Map methodsToAdd, Map oldGenericsSpec) {
        ClassNode sn = classNode.getUnresolvedSuperClass(false);

        if (sn != null) {
            Map genericsSpec = createGenericsSpec(sn, oldGenericsSpec);
            List<MethodNode> classMethods = sn.getMethods();
            // original class causing bridge methods for methods in super class
            for (Object declaredMethod : declaredMethods) {
                MethodNode method = (MethodNode) declaredMethod;
                if (method.isStatic()) continue;
                storeMissingCovariantMethods(classMethods, method, methodsToAdd, genericsSpec);
            }
            // super class causing bridge methods for abstract methods in original class
            if (!abstractMethods.isEmpty()) {
                for (Object classMethod : classMethods) {
                    MethodNode method = (MethodNode) classMethod;
                    if (method.isStatic()) continue;
                    storeMissingCovariantMethods(abstractMethods.values(), method, methodsToAdd, Collections.EMPTY_MAP);
                }
            }

            addCovariantMethods(sn.redirect(), declaredMethods, abstractMethods, methodsToAdd, genericsSpec);
        }

        ClassNode[] interfaces = classNode.getInterfaces();
        for (ClassNode anInterface : interfaces) {
            List interfacesMethods = anInterface.getMethods();
            Map genericsSpec = createGenericsSpec(anInterface, oldGenericsSpec);
            for (Object declaredMethod : declaredMethods) {
                MethodNode method = (MethodNode) declaredMethod;
                if (method.isStatic()) continue;
                storeMissingCovariantMethods(interfacesMethods, method, methodsToAdd, genericsSpec);
            }
            addCovariantMethods(anInterface, declaredMethods, abstractMethods, methodsToAdd, genericsSpec);
        }

    }

    private MethodNode getCovariantImplementation(final MethodNode oldMethod, final MethodNode overridingMethod, Map genericsSpec) {
        // method name
        if (!oldMethod.getName().equals(overridingMethod.getName())) return null;
        if ((overridingMethod.getModifiers() & ACC_BRIDGE) != 0) return null;

        // parameters
        boolean normalEqualParameters = equalParametersNormal(overridingMethod, oldMethod);
        boolean genericEqualParameters = equalParametersWithGenerics(overridingMethod, oldMethod, genericsSpec);
        if (!normalEqualParameters && !genericEqualParameters) return null;

        // return type
        ClassNode mr = overridingMethod.getReturnType();
        ClassNode omr = oldMethod.getReturnType();
        boolean equalReturnType = mr.equals(omr);
        if (equalReturnType && normalEqualParameters) return null;

        // if we reach this point we have at least one parameter or return type, that
        // is different in its specified form. That means we have to create a bridge method!
        ClassNode testmr = correctToGenericsSpec(genericsSpec, omr);
        if (!isAssignable(mr, testmr)) {
            throw new RuntimeParserException(
                    "The return type of " +
                            overridingMethod.getTypeDescriptor() +
                            " in " + overridingMethod.getDeclaringClass().getName() +
                            " is incompatible with " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName(),
                    overridingMethod);
        }
        if ((oldMethod.getModifiers() & ACC_FINAL) != 0) {
            throw new RuntimeParserException(
                    "Cannot override final method " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName(),
                    overridingMethod);
        }
        if (oldMethod.isStatic() != overridingMethod.isStatic()) {
            throw new RuntimeParserException(
                    "Cannot override method " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName() +
                            " with disparate static modifier",
                    overridingMethod);
        }
        if (!equalReturnType) {
            boolean oldM = ClassHelper.isPrimitiveType(oldMethod.getReturnType());
            boolean newM = ClassHelper.isPrimitiveType(overridingMethod.getReturnType());
            if (oldM || newM) {
                String message="";
                if (oldM && newM) {
                    message = " with old and new method having different primitive return types";
                } else if (newM) {
                    message = " with new method having a primitive return type and old method not";
                } else if (oldM) {
                    message = " with old method having a primitive return type and new method not";
                }
                throw new RuntimeParserException(
                        "Cannot override method " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName() +
                            message,
                        overridingMethod);
            }
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
        instructions.add(
                new BytecodeInstruction() {
                    public void visit(MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD,0);
                        Parameter[] para = oldMethod.getParameters();
                        Parameter[] goal = overridingMethod.getParameters();
                        int doubleSlotOffset = 0;
                        for (int i = 0; i < para.length; i++) {
                            ClassNode type = para[i].getType();
                            BytecodeHelper.load(mv, type, i+1+doubleSlotOffset);
                            if (type.redirect()==ClassHelper.double_TYPE ||
                                type.redirect()==ClassHelper.long_TYPE)
                            {
                                doubleSlotOffset++;
                            }
                            if (!type.equals(goal[i].getType())) {
                                BytecodeHelper.doCast(mv,goal[i].getType());
                            }
                        }
                        mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                BytecodeHelper.getClassInternalName(classNode),
                                overridingMethod.getName(),
                                BytecodeHelper.getMethodDescriptor(overridingMethod.getReturnType(), overridingMethod.getParameters()));

                        BytecodeHelper.doReturn(mv, oldMethod.getReturnType());
                    }
                }

        );
        newMethod.setCode(new BytecodeSequence(instructions));
        return newMethod;
    }

    private boolean isAssignable(ClassNode node, ClassNode testNode) {
        if (testNode.isInterface()) {
            if (node.equals(testNode) || node.implementsInterface(testNode)) return true;
        } else {
            if (node.isDerivedFrom(testNode)) return true;
        }
        return false;
    }

    private Parameter[] cleanParameters(Parameter[] parameters) {
        Parameter[] params = new Parameter[parameters.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = new Parameter(parameters[i].getType().getPlainNodeReference(), parameters[i].getName());
        }
        return params;
    }

    private void storeMissingCovariantMethods(Collection methods, MethodNode method, Map methodsToAdd, Map genericsSpec) {
        for (Object method1 : methods) {
            MethodNode toOverride = (MethodNode) method1;
            MethodNode bridgeMethod = getCovariantImplementation(toOverride, method, genericsSpec);
            if (bridgeMethod == null) continue;
            methodsToAdd.put(bridgeMethod.getTypeDescriptor(), bridgeMethod);
            return;
        }
    }

    private ClassNode correctToGenericsSpec(Map genericsSpec, GenericsType type) {
        ClassNode ret = null;
        if (type.isPlaceholder()) {
            String name = type.getName();
            ret = (ClassNode) genericsSpec.get(name);
        }
        if (ret == null) ret = type.getType();
        return ret;
    }

    private ClassNode correctToGenericsSpec(Map genericsSpec, ClassNode type) {
        if (type.isGenericsPlaceHolder()) {
            String name = type.getGenericsTypes()[0].getName();
            type = (ClassNode) genericsSpec.get(name);
        }
        if (type == null) type = ClassHelper.OBJECT_TYPE;
        return type;
    }

    private boolean equalParametersNormal(MethodNode m1, MethodNode m2) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length != p2.length) return false;
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
        if (p1.length != p2.length) return false;
        for (int i = 0; i < p2.length; i++) {
            ClassNode type = p2[i].getType();
            ClassNode genericsType = correctToGenericsSpec(genericsSpec, type);
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
        if (sgts != null) {
            ClassNode[] spec = new ClassNode[sgts.length];
            for (int i = 0; i < spec.length; i++) {
                spec[i] = correctToGenericsSpec(ret, sgts[i]);
            }
            GenericsType[] newGts = current.redirect().getGenericsTypes();
            if (newGts == null) return ret;
            ret.clear();
            for (int i = 0; i < spec.length; i++) {
                ret.put(newGts[i].getName(), spec[i]);
            }
        }
        return ret;
    }

    private boolean moveOptimizedConstantsInitialization(final ClassNode node) {
        if (node.isInterface()) return false;

        final int mods = Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC| Opcodes.ACC_PUBLIC;
        String name = SWAP_INIT;
        BlockStatement methodCode = new BlockStatement();
        node.addSyntheticMethod(
                name, mods, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodCode);

        methodCode.addStatement(new SwapInitStatement());
        for (FieldNode fn : node.getFields()) {
            if (!fn.isStatic() || !fn.isSynthetic() || !fn.getName().startsWith("$const$")) continue;
            if (fn.getInitialExpression()==null) continue;
            final FieldExpression fe = new FieldExpression(fn);
            if (fn.getType().equals(ClassHelper.REFERENCE_TYPE)) fe.setUseReferenceDirectly(true);
            ConstantExpression init = (ConstantExpression) fn.getInitialExpression();
            ExpressionStatement statement =
                    new ExpressionStatement(
                            new BinaryExpression(
                                    fe,
                                    Token.newSymbol(Types.EQUAL, fn.getLineNumber(), fn.getColumnNumber()),
                                    init));
            fn.setInitialValueExpression(null);
            init.setConstantName(null);
            methodCode.addStatement(statement);
        }

        return true;
    }

    /**
     * When constant expressions are created, the value is always wrapped to a non primitive type.
     * Some constant expressions are optimized to return primitive types, but not all primitives are
     * handled. This method guarantees to return a similar constant expression but with a primitive type
     * instead of a boxed type.
     *
     * Additionaly, single char strings are converted to 'char' types.
     *
     * @param constantExpression a constant expression
     * @return the same instance of constant expression if the type is already primitive, or a primitive
     * constant if possible.
     */
    public static ConstantExpression transformToPrimitiveConstantIfPossible(ConstantExpression constantExpression) {
        Object value = constantExpression.getValue();
        if (value ==null) return constantExpression;
        ConstantExpression result;
        ClassNode type = constantExpression.getType();
        if (ClassHelper.isPrimitiveType(type)) return constantExpression;
        if (value instanceof String && ((String)value).length()==1) {
            result = new ConstantExpression(((String)value).charAt(0));
            result.setType(ClassHelper.char_TYPE);
        } else {
            type = ClassHelper.getUnwrapper(type);
            result = new ConstantExpression(value, true);
            result.setType(type);
        }
        return result;
    }

    private static class SwapInitStatement extends BytecodeSequence {

        private WriterController controller;

        public SwapInitStatement() {
            super(new SwapInitInstruction());
            ((SwapInitInstruction)getInstructions().get(0)).statement = this;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                AsmClassGenerator generator = (AsmClassGenerator) visitor;
                controller = generator.getController();
            }
            super.visit(visitor);
        }

        private static class SwapInitInstruction extends BytecodeInstruction {
            SwapInitStatement statement;

            @Override
            public void visit(final MethodVisitor mv) {
                statement.controller.getCallSiteWriter().makeCallSiteArrayInitializer();
            }
        }
    }

}
