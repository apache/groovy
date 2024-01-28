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
package org.codehaus.groovy.transform.trait;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import groovy.transform.CompilationUnitAware;
import groovy.transform.Sealed;
import groovy.transform.Trait;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformationVisitor;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedInnerClass;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OVERRIDE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SEALED_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperBoolean;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classList2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.forS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.trait.SuperCallTraitTransformer.UNRESOLVED_HELPER_CLASS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Handles generation of code for the traits (trait keyword is equivalent to using the @Trait annotation).
 * A class annotated with @Trait will generate, instead:
 * <ul>
 * <li>an <i>interface</i> with the same name</li>
 * <li>a utility inner class that will be used by the compiler to implement the trait</li>
 * <li>potentially a utility inner class to assist with implementing trait fields</li>
 * </ul>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class TraitASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    public static final String DO_DYNAMIC = TraitReceiverTransformer.class + ".doDynamic";
    public static final String POST_TYPECHECKING_REPLACEMENT = TraitReceiverTransformer.class + ".replacement";

    private CompilationUnit compilationUnit;

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!anno.getClassNode().equals(Traits.TRAIT_CLASSNODE)) return;
        init(nodes, source);
        if (node instanceof ClassNode) {
            ClassNode cNode = (ClassNode) node;
            if (!checkNotInterface(cNode, Traits.TRAIT_TYPE_NAME)) return;

            checkInnerClasses(cNode);
            checkNoConstructor(cNode);
            checkExtendsClause(cNode);
            replaceExtendsByImplements(cNode);
            generateMethodsWithDefaultArgs(cNode);
            resolveHelperClassIfNecessary(createHelperClass(cNode));
        }
    }

    private void checkInnerClasses(final ClassNode cNode) {
        for (Iterator<InnerClassNode> it = cNode.getInnerClasses(); it.hasNext(); ) { InnerClassNode ic = it.next();
            if ((ic.getModifiers() & ACC_STATIC) == 0) {
                sourceUnit.addError(new SyntaxException("Cannot have non-static inner class inside a trait (" + ic.getName() + ")", ic.getLineNumber(), ic.getColumnNumber()));
            }
        }
    }

    private void checkNoConstructor(final ClassNode cNode) {
        if (!cNode.getDeclaredConstructors().isEmpty()) {
            addError("Error processing trait '" + cNode.getName() + "'. Constructors are not allowed.", cNode);
        }
    }

    private void checkExtendsClause(final ClassNode cNode) {
        ClassNode superClass = cNode.getUnresolvedSuperClass(false);
        if (superClass.isInterface() && !Traits.isTrait(superClass)) {
            addError("A trait cannot extend an interface. Use 'implements' instead.", cNode);
        }
    }

    private static void replaceExtendsByImplements(final ClassNode cNode) {
        ClassNode superClass = cNode.getUnresolvedSuperClass(false);
        if (Traits.isTrait(superClass)) {
            // move from super class to interface
            cNode.setSuperClass(OBJECT_TYPE);
            cNode.addInterface(superClass);
        }
    }

    private static void generateMethodsWithDefaultArgs(final ClassNode cNode) {
        new Verifier() {
            @Override
            public void addDefaultParameterMethods(final ClassNode cn) {
                setClassNode(cn); super.addDefaultParameterMethods(cn);
            }
        }.addDefaultParameterMethods(cNode);
    }

    private ClassNode createHelperClass(final ClassNode cNode) {
        cNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE);

        ClassNode helper = new InnerClassNode(
                cNode,
                Traits.helperClassName(cNode),
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                OBJECT_TYPE,
                ClassNode.EMPTY_ARRAY,
                null
        );
        helper.setStaticClass(true); // GROOVY-7242, GROOVY-7456, etc.

        MethodNode initializer = createInitMethod(false, helper);
        MethodNode staticInitializer = createInitMethod(true, helper);

        // apply the verifier to have the property nodes generated
        for (PropertyNode pNode : cNode.getProperties()) {
            processProperty(cNode, pNode);
        }

        // prepare fields
        List<FieldNode> fields = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();
        boolean hasStatic = false;
        for (FieldNode field : cNode.getFields()) {
            if (!"metaClass".equals(field.getName()) && (!field.isSynthetic() || field.getName().indexOf('$') < 0)) {
                fields.add(field);
                fieldNames.add(field.getName());
                if (field.isStatic()) {
                    hasStatic = true;
                }
            }
        }
        ClassNode fieldHelper = null;
        ClassNode staticFieldHelper = null;
        if (!fields.isEmpty()) {
            fieldHelper = new InnerClassNode(
                    cNode,
                    Traits.fieldHelperClassName(cNode),
                    ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_SYNTHETIC,
                    OBJECT_TYPE
            );
            fieldHelper.setStaticClass(true);
            if (hasStatic) {
                staticFieldHelper = new InnerClassNode(
                        cNode,
                        Traits.staticFieldHelperClassName(cNode),
                        ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE | ACC_SYNTHETIC,
                        OBJECT_TYPE
                );
                staticFieldHelper.setStaticClass(true);
            }
        }

        // add fields
        for (FieldNode field : fields) {
            processField(field, initializer, staticInitializer, fieldHelper, helper, staticFieldHelper, cNode, fieldNames);
        }

        // add methods
        List<MethodNode> nonPublicAPIMethods = new ArrayList<>();
        List<Statement> staticInitStatements = null;
        for (MethodNode methodNode : cNode.getMethods()) {
            if (!methodNode.isSynthetic() && (methodNode.isProtected() || methodNode.isPackageScope())) {
                sourceUnit.addError(new SyntaxException("Cannot have protected/package-private method in a trait (" + cNode.getName() + "#" + methodNode.getTypeDescriptor() + ")",
                        methodNode.getLineNumber(), methodNode.getColumnNumber()));
                return null;
            }
            if (!methodNode.isAbstract()) {
                MethodNode newMethod = processMethod(cNode, helper, methodNode, fieldHelper, fieldNames);
                if (methodNode.isStaticConstructor()) {
                    staticInitStatements = getCodeAsBlock(newMethod).getStatements();
                } else {
                    // add non-abstract methods; abstract methods covered from trait interface
                    helper.addMethod(newMethod);
                }
            }
            if (methodNode.isPrivate() || methodNode.isStatic()) {
                nonPublicAPIMethods.add(methodNode);
            }
        }

        // remove methods which should not appear in the trait interface
        for (MethodNode privateMethod : nonPublicAPIMethods) {
            cNode.removeMethod(privateMethod);
        }

        // copy statements from static and instance init blocks
        if (staticInitStatements != null) {
            BlockStatement toBlock = getBlockStatement(staticInitializer, staticInitializer.getCode());
            for (Statement next : staticInitStatements) {
                toBlock.addStatement(next);
            }
        }
        List<Statement> initStatements = cNode.getObjectInitializerStatements();
        Statement toCode = initializer.getCode();
        BlockStatement toBlock = getBlockStatement(initializer, toCode);
        for (Statement next : initStatements) {
            Parameter selfParam = createSelfParameter(cNode, false);
            toBlock.addStatement(processBody(varX(selfParam), next, cNode, helper, fieldHelper, fieldNames));
        }
        initStatements.clear();

        // clear properties to avoid generation of methods
        cNode.getProperties().clear();

        fields = new ArrayList<>(cNode.getFields()); // reuse the full list of fields
        for (FieldNode field : fields) {
            cNode.removeField(field.getName());
        }

        addMopMethods(helper);
        copyClassAnnotations(helper);
        registerASTTransformations(helper);

        addGeneratedInnerClass(cNode, helper);
        if (fieldHelper != null) {
            addGeneratedInnerClass(cNode, fieldHelper);
            if (staticFieldHelper != null) {
                addGeneratedInnerClass(cNode, staticFieldHelper);
            }
        }

        // resolve scope (for closures)
        resolveScope(helper);
        if (fieldHelper != null) {
            resolveScope(fieldHelper);
            if (staticFieldHelper != null) {
                resolveScope(staticFieldHelper);
            }
        }

        return helper;
    }

    private void resolveHelperClassIfNecessary(final ClassNode helperClass) {
        for (ClassNode cn : sourceUnit.getAST().getClasses()) {
            ClassNode unresolvedHelperClass = cn.getNodeMetaData(UNRESOLVED_HELPER_CLASS);
            if (unresolvedHelperClass != null && unresolvedHelperClass.getName().equals(helperClass.getName())) {
                unresolvedHelperClass.setRedirect(helperClass);
            }
        }
    }

    //--------------------------------------------------------------------------

    private void resolveScope(final ClassNode cNode) {
        new VariableScopeVisitor(sourceUnit).visitClass(cNode);
    }

    private static void addMopMethods(final ClassNode helper) {
        var interfaces = helper.getOuterClass().getInterfaces();
        var delegates = new ArrayList<String>(interfaces.length);
        for (int i = interfaces.length; i > 0; ) { var maybeTrait = interfaces[--i];
            if (Traits.isTrait(maybeTrait)) delegates.add(Traits.helperClassName(maybeTrait));
        }
        if (!delegates.isEmpty()) {
            var bs = new BlockStatement();
            var e1 = localVarX("e1", classX(MissingMethodException.class).getType());
            var e2 = new Parameter(  classX(GroovyRuntimeException.class).getType(), "e2");
            Parameter[] params = {new Parameter(STRING_TYPE, "name"), new Parameter(OBJECT_TYPE, "args")};
            helper.addSyntheticMethod("$static_methodMissing", ACC_PUBLIC | ACC_STATIC, OBJECT_TYPE, params, new ClassNode[]{e1.getType()}, bs);

            /* public static Object $static_methodMissing(String name, Object args) throws MissingMethodException {
             *   MissingMethodException e1 = new MissingMethodException(name, TheTrait.class, args.tail())
             *   for (Class delegate in [...]) {
             *     try {
             *       delegate.(name)(*args)
             *     } catch (GroovyRuntimeException e2) {
             *       e1.addSuppressed(e2)
             *     }
             *   }
             *   throw e1
             * }
             */

            bs.addStatement(declS(e1, ctorX(e1.getType(), args(varX(params[0]), classX(helper.getOuterClass()), callX(varX(params[1]), "tail")))));

            Parameter delegate = new Parameter(CLASS_Type, "delegate");
            Statement callName = returnS(callX(varX(delegate), varX(params[0]), new SpreadExpression(varX(params[1]))));
            bs.addStatement(forS(delegate, classList2args(delegates), tryCatchS(callName).addCatch(catchS(e2, stmt(callX(varX(e1), "addSuppressed", varX(e2)))))));

            bs.addStatement(throwS(varX(e1)));
        }
    }

    /**
     * Copies annotations from the trait to the helper, excluding non-applicable
     * items such as {@link Trait @Trait} and {@link Sealed @Sealed}.
     */
    private static void copyClassAnnotations(final ClassNode helper) {
        for (AnnotationNode annotation : helper.getOuterClass().getAnnotations()) {
            ClassNode annotationType = annotation.getClassNode();
            if (!annotationType.equals(Traits.TRAIT_CLASSNODE)
                    && !annotationType.equals(SEALED_TYPE)) {
                helper.addAnnotation(annotation);
            }
        }
    }

    private /****/ void registerASTTransformations(final ClassNode helper) {
        ASTTransformationVisitor.addNewPhaseOperation(compilationUnit, sourceUnit, helper);
        // perform an additional operation which has to be done *after* static type checking
        compilationUnit.addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            if (classNode == helper) {
                GroovyClassVisitor visitor = new PostTypeCheckingExpressionReplacer(source);
                visitor.visitClass(helper);
            }
        }, CompilePhase.INSTRUCTION_SELECTION.getPhaseNumber());
    }

    private static MethodNode createInitMethod(final boolean isStatic, final ClassNode helper) {
        MethodNode initializer = new MethodNode(
                isStatic ? Traits.STATIC_INIT_METHOD : Traits.INIT_METHOD,
                ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                VOID_TYPE,
                new Parameter[]{createSelfParameter(helper.getOuterClass(), isStatic)},
                ClassNode.EMPTY_ARRAY,
                new BlockStatement()
        );
        helper.addMethod(initializer);

        // Cannot add static compilation of init method because of GROOVY-7217, see example 2 of test case
        //AnnotationNode an = new AnnotationNode(TraitComposer.COMPILESTATIC_CLASSNODE);
        //initializer.addAnnotation(an);
        //cNode.addTransform(StaticCompileTransformation.class, an);

        return initializer;
    }

    private static ClassNode createReceiverType(final boolean isStatic, final ClassNode cNode) {
        ClassNode type;
        if (isStatic) {
            // Class<TraitClass>
            type = GenericsUtils.makeClassSafe0(CLASS_Type, new GenericsType(cNode));
        } else {
            // TraitClass
            type = cNode;
        }
        return type;
    }

    private static Parameter createSelfParameter(final ClassNode cNode, final boolean isStatic) {
        ClassNode type = createReceiverType(isStatic, cNode.getPlainNodeReference());
        return new Parameter(type, isStatic ? Traits.STATIC_THIS_OBJECT : Traits.THIS_OBJECT);
    }

    private static BlockStatement getBlockStatement(final MethodNode initMethod, final Statement initCode) {
        BlockStatement block;
        if (initCode instanceof BlockStatement) {
            block = (BlockStatement) initCode;
        } else {
            block = new BlockStatement();
            block.addStatement(initCode);
            initMethod.setCode(block);
        }
        return block;
    }

    //

    /**
     * Mostly copied from the {@link Verifier} class but does *not* generate bytecode.
     */
    private static void processProperty(final ClassNode cNode, final PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();
        int propNodeModifiers = node.getModifiers() & 0x1F; // GROOVY-3726

        String getterName = node.getGetterNameOrDefault();
        String setterName = node.getSetterNameOrDefault();

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            MethodNode getter = cNode.getGetterMethod(getterName);
            if (getter == null && isPrimitiveBoolean(node.getType())) {
                getter = cNode.getGetterMethod("is" + capitalize(name));
            }
            if (!node.isPrivate() && methodNeedsReplacement(cNode, getter)) {
                getterBlock = stmt(fieldX(field));
            }
        }
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null) {
            // 2nd arg false below: though not usual, allow setter with non-void return type
            MethodNode setter = cNode.getSetterMethod(setterName, false);
            if (!node.isPrivate() && (propNodeModifiers & ACC_FINAL) == 0
                    && methodNeedsReplacement(cNode, setter)) {
                setterBlock = assignS(fieldX(field), varX(name));
            }
        }

        if (getterBlock != null) {
            MethodNode getter = new MethodNode(getterName, propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
            getter.setSynthetic(true);
            addGeneratedMethod(cNode, getter);

            if (node.getGetterName() == null && getterName.startsWith("get") && (isPrimitiveBoolean(node.getType()) || isWrapperBoolean(node.getType()))) {
                MethodNode secondGetter = new MethodNode("is" + capitalize(name), propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
                if (methodNeedsReplacement(cNode, secondGetter)) {
                    secondGetter.setSynthetic(true);
                    addGeneratedMethod(cNode, secondGetter);
                }
            }
        }
        if (setterBlock != null) {
            VariableExpression var = (VariableExpression) ((BinaryExpression) ((ExpressionStatement) setterBlock).getExpression()).getRightExpression();
            Parameter setterParameter = new Parameter(node.getType(), name);
            var.setAccessedVariable(setterParameter);

            MethodNode setter = new MethodNode(setterName, propNodeModifiers, VOID_TYPE, new Parameter[]{setterParameter}, ClassNode.EMPTY_ARRAY, setterBlock);
            setter.setSynthetic(true);
            addGeneratedMethod(cNode, setter);
        }
    }

    private static boolean methodNeedsReplacement(final ClassNode cNode, final MethodNode mNode) {
        // no method found, we need to replace
        if (mNode == null) return true;
        // method is in current class, nothing to be done
        if (mNode.getDeclaringClass() == cNode) return false;
        // do not overwrite final
        if ((mNode.getModifiers() & ACC_FINAL) != 0) return false;
        return true;
    }

    private void processField(final FieldNode field, final MethodNode initializer, final MethodNode staticInitializer,
                              final ClassNode fieldHelper, final ClassNode helper, final ClassNode staticFieldHelper, final ClassNode trait,
                              final Set<String> knownFields) {
        if (field.isProtected()) {
            sourceUnit.addError(new SyntaxException("Cannot have protected field in a trait (" + trait.getName() + "#" + field.getName() + ")",
                    field.getLineNumber(), field.getColumnNumber()));
            return;
        }

        Expression initialExpression = field.getInitialExpression();
        MethodNode selectedMethod = field.isStatic() ? staticInitializer : initializer;
        ClassNode target = field.isStatic() && staticFieldHelper != null ? staticFieldHelper : fieldHelper;
        if (initialExpression != null) {
            VariableExpression thisObject = varX(selectedMethod.getParameters()[0]);
            ExpressionStatement initCode = new ExpressionStatement(initialExpression);
            processBody(thisObject, initCode, trait, helper, fieldHelper, knownFields);
            if (field.isFinal()) {
                String baseName = field.isStatic() ? Traits.STATIC_INIT_METHOD : Traits.INIT_METHOD;
                MethodNode fieldInitializer = new MethodNode(
                        baseName + Traits.remappedFieldName(trait, field.getName()),
                        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                        field.getOriginType(),
                        new Parameter[]{createSelfParameter(trait, field.isStatic())},
                        ClassNode.EMPTY_ARRAY,
                        returnS(initCode.getExpression())
                );
                helper.addMethod(fieldInitializer);
            } else {
                BlockStatement code = (BlockStatement) selectedMethod.getCode();
                MethodCallExpression mce;
                if (field.isStatic()) {
                    if (staticFieldHelper != null) {
                        target = staticFieldHelper;
                    }
                    mce = callX(
                            classX(InvokerHelper.class),
                            "invokeStaticMethod",
                            args(
                                    thisObject,
                                    constX(Traits.helperSetterName(field)),
                                    initCode.getExpression()
                            )
                    );
                } else {
                    mce = callX(
                            castX(createReceiverType(field.isStatic(), fieldHelper), thisObject),
                            Traits.helperSetterName(field),
                            castX(field.getOriginType(), initCode.getExpression())
                    );
                }
                mce.setImplicitThis(false);
                mce.setSourcePosition(initialExpression);
                code.addStatement(stmt(mce));
            }
        }
        // define setter/getter helper methods (setter added even for final fields for legacy compatibility)
        addGeneratedMethod(target,
                Traits.helperSetterName(field),
                ACC_PUBLIC | ACC_ABSTRACT,
                field.getOriginType(),
                new Parameter[]{new Parameter(field.getOriginType(), "val")},
                ClassNode.EMPTY_ARRAY,
                null
        );
        addGeneratedMethod(target,
                Traits.helperGetterName(field),
                ACC_PUBLIC | ACC_ABSTRACT,
                field.getOriginType(),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                null
        );

        // dummy fields are only used to carry annotations if instance field
        // and to differentiate from static fields otherwise
        int mods = field.getModifiers() & Traits.FIELD_PREFIX_MASK;
        String dummyFieldName = String.format("$0x%04x", mods) + Traits.remappedFieldName(field.getOwner(), field.getName());
        FieldNode dummyField = new FieldNode(
                dummyFieldName,
                ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC,
                field.getOriginType(),
                fieldHelper,
                null
        );
        dummyField.setSynthetic(true);
        // copy annotations from field to dummy field
        List<AnnotationNode> copy = new ArrayList<>();
        List<AnnotationNode> skip = new ArrayList<>();
        GeneralUtils.copyAnnotatedNodeAnnotations(field, copy, skip);
        dummyField.addAnnotations(copy);
        fieldHelper.addField(dummyField);

        // retain legacy field (will be given lower precedence than above)
        dummyFieldName = (field.isStatic() ? Traits.STATIC_FIELD_PREFIX : Traits.FIELD_PREFIX) +
                (field.isPublic() ? Traits.PUBLIC_FIELD_PREFIX : Traits.PRIVATE_FIELD_PREFIX) +
                Traits.remappedFieldName(field.getOwner(), field.getName());
        dummyField = new FieldNode(
                dummyFieldName,
                ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC,
                field.getOriginType().getPlainNodeReference(),
                fieldHelper,
                null
        );
        dummyField.setSynthetic(true);
        dummyField.addAnnotations(copy);
        fieldHelper.addField(dummyField);
    }

    private MethodNode processMethod(final ClassNode traitClass, final ClassNode traitHelperClass, final MethodNode methodNode, final ClassNode fieldHelper, final Collection<String> knownFields) {
        Parameter[] methodParams = methodNode.getParameters();
        Parameter[] helperParams = new Parameter[methodParams.length + 1];
        helperParams[0] = createSelfParameter(traitClass,methodNode.isStatic());
        System.arraycopy(methodParams, 0, helperParams, 1, methodParams.length);

        MethodNode mNode = new MethodNode(
                methodNode.getName(),
                (methodNode.isPrivate() ? ACC_PRIVATE : ACC_PUBLIC) | (methodNode.isFinal() ? ACC_FINAL : 0) | ACC_STATIC,
                methodNode.getReturnType(),
                helperParams,
                methodNode.getExceptions(),
                processBody(varX(helperParams[0]), methodNode.getCode(), traitClass, traitHelperClass, fieldHelper, knownFields)
        );
        for (AnnotationNode annotation : methodNode.getAnnotations()) {
            if (!annotation.getClassNode().equals(OVERRIDE_TYPE)) {
                mNode.addAnnotation(annotation);
            }
        }
        mNode.setGenericsTypes(methodNode.getGenericsTypes());
        mNode.setSourcePosition(methodNode);
        if (methodNode.isAbstract()) {
            mNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT);
        } else {
            methodNode.addAnnotation(new AnnotationNode(Traits.IMPLEMENTED_CLASSNODE));
        }
        if (!methodNode.isPrivate() && !methodNode.isStatic()) {
            methodNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT);
        }
        methodNode.setCode(null);

        return mNode;
    }

    private Statement processBody(final VariableExpression thisObject, final Statement code, final ClassNode trait, final ClassNode traitHelper, final ClassNode fieldHelper, final Collection<String> knownFields) {
        if (code != null) {
            code.visit(new NAryOperationRewriter(sourceUnit, knownFields));
            code.visit(new SuperCallTraitTransformer(sourceUnit));
            code.visit(new TraitReceiverTransformer(thisObject, sourceUnit, trait, traitHelper, fieldHelper, knownFields));
        }
        return code;
    }

    //--------------------------------------------------------------------------

    private static class PostTypeCheckingExpressionReplacer extends ClassCodeExpressionTransformer {
        private final SourceUnit sourceUnit;

        PostTypeCheckingExpressionReplacer(final SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp != null) {
                Expression replacement = exp.getNodeMetaData(POST_TYPECHECKING_REPLACEMENT);
                if (replacement != null) {
                    return replacement;
                }
            }
            return super.transform(exp);
        }
    }
}
