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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.EnumConstantClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class EnumVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;

    public EnumVisitor(final CompilationUnit cu, final SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(final ClassNode node) {
        if (node.isEnum()) completeEnum(node);
    }

    private void completeEnum(final ClassNode enumClass) {
        // create MIN_VALUE, MAX_VALUE and $VALUES fields
        FieldNode minValue = null, maxValue = null, values = null;

        boolean isAIC = isAnonymousInnerClass(enumClass);
        if (!isAIC) {
            ClassNode enumPlain = enumClass.getPlainNodeReference();
            minValue = new FieldNode("MIN_VALUE", ACC_FINAL | ACC_PUBLIC | ACC_STATIC, enumPlain, enumClass, null);
            maxValue = new FieldNode("MAX_VALUE", ACC_FINAL | ACC_PUBLIC | ACC_STATIC, enumPlain, enumClass, null);
            values = new FieldNode("$VALUES", ACC_FINAL | ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, enumPlain.makeArray(), enumClass, null);
            values.setSynthetic(true);

            for (ConstructorNode ctor : enumClass.getDeclaredConstructors()) {
                if (ctor.isSyntheticPublic()) {
                    ctor.setSyntheticPublic(false);
                    ctor.setModifiers((ctor.getModifiers() | ACC_PRIVATE) & ~ACC_PUBLIC);
                } else if (!ctor.isPrivate()) {
                    addError(ctor, "Illegal modifier for the enum constructor; only private is permitted.");
                }
            }

            addMethods(enumClass, values, minValue, maxValue);
            checkForAbstractMethods(enumClass);
        }

        addInit(enumClass, minValue, maxValue, values, isAIC);
    }

    private static void checkForAbstractMethods(final ClassNode enumClass) {
        for (MethodNode method : enumClass.getMethods()) {
            if (method.isAbstract()) {
                // make the class abstract also; see Effective Java p.152
                enumClass.setModifiers(enumClass.getModifiers() | ACC_ABSTRACT);
                break;
            }
        }
    }

    private static void addMethods(final ClassNode enumClass, final FieldNode values, FieldNode minValue, FieldNode maxValue) {

        boolean hasNext = false;
        boolean hasPrevious = false;
        for (MethodNode m : enumClass.getMethods()) {
            if (m.getName().equals("next") && m.getParameters().length == 0) hasNext = true;
            if (m.getName().equals("previous") && m.getParameters().length == 0) hasPrevious = true;
            if (hasNext && hasPrevious) break;
        }
        boolean empty = true;
        for (FieldNode f : enumClass.getFields()) {
            if (f.isEnum()) {
                empty = false;
                break;
            }
        }

        ClassNode enumRef = enumClass.getPlainNodeReference();

        {
            // create values() method
            MethodNode valuesMethod = new MethodNode("values", ACC_FINAL | ACC_PUBLIC | ACC_STATIC, enumRef.makeArray(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
            valuesMethod.setSynthetic(true);
            MethodCallExpression cloneCall = callX(fieldX(values), "clone");
            cloneCall.setMethodTarget(values.getType().getMethod("clone", Parameter.EMPTY_ARRAY));
            valuesMethod.setCode(block(returnS(cloneCall)));
            addGeneratedMethod(enumClass, valuesMethod);
        }

        if (!hasNext) {
            // create next() method, code:
            //     Enum next() {
            //        int ordinal = ordinal() + 1
            //        if (ordinal >= values().size()) return MIN_VALUE
            //        return values()[ordinal]
            //     }
            MethodNode nextMethod = new MethodNode("next", ACC_PUBLIC, enumRef, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
            nextMethod.setSynthetic(true);
            VariableExpression ordinal = localVarX("ordinal", int_TYPE);
            nextMethod.setCode(empty ? block(returnS(nullX())) : block(
                declS(ordinal, plusX(callThisX("ordinal"), constX(1, true))),
                ifS(geX(ordinal, propX(fieldX(values), "length")), returnS(varX(minValue))),
                returnS(indexX(fieldX(values), ordinal))
            ));
            addGeneratedMethod(enumClass, nextMethod);
        }

        if (!hasPrevious) {
            // create previous() method, code:
            //    Enum previous() {
            //        int ordinal = ordinal() - 1
            //        if (ordinal < 0) return MAX_VALUE
            //        return values()[ordinal]
            //    }
            MethodNode prevMethod = new MethodNode("previous", ACC_PUBLIC, enumRef, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
            prevMethod.setSynthetic(true);
            VariableExpression ordinal = localVarX("ordinal", int_TYPE);
            prevMethod.setCode(empty ? block(returnS(nullX())) : block(
                declS(ordinal, minusX(callThisX("ordinal"), constX(1, true))),
                ifS(ltX(ordinal, constX(0, true)), returnS(varX(maxValue))),
                returnS(indexX(fieldX(values), ordinal))
            ));
            addGeneratedMethod(enumClass, prevMethod);
        }

        {
            // create valueOf
            Parameter stringParameter = param(ClassHelper.STRING_TYPE, "name");
            MethodNode valueOfMethod = new MethodNode("valueOf", ACC_PUBLIC | ACC_STATIC, enumRef, params(stringParameter), ClassNode.EMPTY_ARRAY, null);
            valueOfMethod.setCode(block(returnS(
                callX(ClassHelper.Enum_Type, "valueOf", args(classX(enumClass), varX("name")))
            )));
            valueOfMethod.setSynthetic(true);
            addGeneratedMethod(enumClass, valueOfMethod);
        }
    }

    private void addInit(final ClassNode enumClass, final FieldNode minValue, final FieldNode maxValue, final FieldNode values, final boolean isAIC) {
        // constructor helper
        // This method is used instead of calling the constructor as
        // calling the constructor may require a table with MetaClass
        // selecting the constructor for each enum value. So instead we
        // use this method to have a central point for constructor selection
        // and only one table. The whole construction is needed because
        // Reflection forbids access to the enum constructor.
        // code:
        // def $INIT(Object[] para) {
        //  return this(*para)
        // }
        ClassNode enumRef = enumClass.getPlainNodeReference();
        Parameter[] parameter = params(param(ClassHelper.OBJECT_TYPE.makeArray(), "para"));
        MethodNode initMethod = new MethodNode("$INIT", ACC_FINAL | ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, enumRef, parameter, ClassNode.EMPTY_ARRAY, null);
        initMethod.setSynthetic(true);
        ConstructorCallExpression cce = ctorThisX(args(spreadX(varX("para"))));
        initMethod.setCode(block(returnS(cce)));
        addGeneratedMethod(enumClass, initMethod);

        // static init
        List<FieldNode> fields = enumClass.getFields();
        List<Expression> arrayInit = new ArrayList<>();
        int value = -1;
        List<Statement> block = new ArrayList<>();
        FieldNode tempMin = null;
        FieldNode tempMax = null;
        for (FieldNode field : fields) {
            if (!field.isEnum()) continue;
            value += 1;
            if (tempMin == null) tempMin = field;
            tempMax = field;

            ClassNode enumBase = enumClass;
            ArgumentListExpression args = args(constX(field.getName()), constX(value));
            if (field.getInitialExpression() == null) {
                if (enumClass.isAbstract()) {
                    addError(field, "The enum constant " + field.getName() + " must override abstract methods from " + enumBase.getName() + ".");
                    continue;
                }
            } else {
                ListExpression oldArgs = (ListExpression) field.getInitialExpression();
                List<MapEntryExpression> savedMapEntries = new ArrayList<>();
                for (Expression exp : oldArgs.getExpressions()) {
                    if (exp instanceof MapEntryExpression) {
                        savedMapEntries.add((MapEntryExpression) exp);
                        continue;
                    }

                    InnerClassNode inner = null;
                    if (exp instanceof ClassExpression) {
                        ClassExpression clazzExp = (ClassExpression) exp;
                        ClassNode ref = clazzExp.getType();
                        if (ref instanceof EnumConstantClassNode) {
                            inner = (InnerClassNode) ref;
                        }
                    }
                    if (inner != null) {
                        List<MethodNode> baseMethods = enumBase.getMethods();
                        for (MethodNode methodNode : baseMethods) {
                            if (!methodNode.isAbstract()) continue;
                            MethodNode enumConstMethod = inner.getMethod(methodNode.getName(), methodNode.getParameters());
                            if (enumConstMethod == null || enumConstMethod.isAbstract()) {
                                addError(field, "Can't have an abstract method in enum constant " + field.getName() + ". Implement method '" + methodNode.getTypeDescriptor() + "'.");
                            }
                        }
                        if (inner.getVariableScope() == null) {
                            enumBase = inner;
                            /*
                             * GROOVY-3985: Remove the final modifier from $INIT method in this case
                             * so that subclasses of enum generated for enum constants (aic) can provide
                             * their own $INIT method
                             */
                            initMethod.setModifiers(initMethod.getModifiers() & ~ACC_FINAL);
                            continue;
                        }
                    }
                    args.addExpression(exp);
                }
                if (!savedMapEntries.isEmpty()) {
                    args.getExpressions().add(2, mapX(savedMapEntries));
                }
            }
            field.setInitialValueExpression(null);
            block.add(assignS(fieldX(field), callX(enumBase, "$INIT", args)));
            arrayInit.add(fieldX(field));
        }

        if (!isAIC) {
            if (tempMin != null) {
                block.add(assignS(fieldX(minValue), fieldX(tempMin)));
                block.add(assignS(fieldX(maxValue), fieldX(tempMax)));
                enumClass.addField(minValue);
                enumClass.addField(maxValue);
            }

            block.add(assignS(fieldX(values), arrayX(enumClass, arrayInit)));
            enumClass.addField(values);
        }
        enumClass.addStaticInitializerStatements(block, true);
    }

    private void addError(final AnnotatedNode exp, final String msg) {
        getSourceUnit().getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(
                        new SyntaxException(msg + '\n', exp),
                        getSourceUnit()
                )
        );
    }

    static boolean isAnonymousInnerClass(final ClassNode enumClass) {
        return enumClass instanceof EnumConstantClassNode
            && ((EnumConstantClassNode) enumClass).getVariableScope() == null;
    }
}
