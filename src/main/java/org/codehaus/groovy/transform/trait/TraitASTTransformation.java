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

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.markAsGenerated;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.trait.SuperCallTraitTransformer.UNRESOLVED_HELPER_CLASS;

/**
 * Handles generation of code for the traits (trait keyword is equivalent to using the @Trait annotation).
 * A class annotated with @Trait will generate, instead:
 * <ul>
 * <li>an <i>interface</i> with the same name</li>
 * <li>a utility inner class that will be used by the compiler to implement the trait</li>
 * <li>potentially a utility inner class to assist with implementing trait fields</li>
 * </ul>
 *
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class TraitASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    public static final String DO_DYNAMIC = TraitReceiverTransformer.class+".doDynamic";
    public static final String POST_TYPECHECKING_REPLACEMENT = TraitReceiverTransformer.class+".replacement";

    private static final ClassNode INVOKERHELPER_CLASSNODE = ClassHelper.make(InvokerHelper.class);

    private static final ClassNode OVERRIDE_CLASSNODE = ClassHelper.make(Override.class);

    private SourceUnit unit;
    private CompilationUnit compilationUnit;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!Traits.TRAIT_CLASSNODE.equals(anno.getClassNode())) return;
        unit = source;
        init(nodes, source);
        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, Traits.TRAIT_TYPE_NAME)) return;
            checkNoConstructor(cNode);
            checkExtendsClause(cNode);
            generateMethodsWithDefaultArgs(cNode);
            replaceExtendsByImplements(cNode);
            ClassNode helperClassNode = createHelperClass(cNode);
            resolveHelperClassIfNecessary(helperClassNode);
        }
    }

    private void resolveHelperClassIfNecessary(final ClassNode helperClassNode) {
        if (helperClassNode == null) {
            return;
        }
        for (ClassNode cNode : unit.getAST().getClasses()) {
            ClassNode unresolvedHelperNode = cNode.getNodeMetaData(UNRESOLVED_HELPER_CLASS);
            if (unresolvedHelperNode != null
                    && unresolvedHelperNode.getName().equals(helperClassNode.getName())) {
                unresolvedHelperNode.setRedirect(helperClassNode);
            }
        }
    }

    private static void generateMethodsWithDefaultArgs(final ClassNode cNode) {
        DefaultArgsMethodsAdder adder = new DefaultArgsMethodsAdder();
        adder.addDefaultParameterMethods(cNode);
    }

    private void checkExtendsClause(final ClassNode cNode) {
        ClassNode superClass = cNode.getSuperClass();
        if (superClass.isInterface() && !Traits.isTrait(superClass)) {
            addError("Trait cannot extend an interface. Use 'implements' instead", cNode);
        }
    }

    private void replaceExtendsByImplements(final ClassNode cNode) {
        ClassNode superClass = cNode.getUnresolvedSuperClass();
        if (Traits.isTrait(superClass)) {
            // move from super class to interface;
            cNode.setSuperClass(ClassHelper.OBJECT_TYPE);
            cNode.setUnresolvedSuperClass(ClassHelper.OBJECT_TYPE);
            cNode.addInterface(superClass);
            resolveScope(cNode);
        }
    }

    private void resolveScope(final ClassNode cNode) {
        // we need to resolve again!
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(unit);
        scopeVisitor.visitClass(cNode);
    }

    private void checkNoConstructor(final ClassNode cNode) {
        if (!cNode.getDeclaredConstructors().isEmpty()) {
            addError("Error processing trait '" + cNode.getName() + "'. " +
                    " Constructors are not allowed.", cNode);
        }
    }

    private ClassNode createHelperClass(final ClassNode cNode) {
        ClassNode helper = new InnerClassNode(
                cNode,
                Traits.helperClassName(cNode),
                ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                ClassHelper.OBJECT_TYPE,
                ClassNode.EMPTY_ARRAY,
                null
        );
        cNode.setModifiers(ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT);

        checkInnerClasses(cNode);

        MethodNode initializer = createInitMethod(false, cNode, helper);
        MethodNode staticInitializer = createInitMethod(true, cNode, helper);

        // apply the verifier to have the property nodes generated
        generatePropertyMethods(cNode);

        // prepare fields
        List<FieldNode> fields = new ArrayList<FieldNode>();
        Set<String> fieldNames = new HashSet<String>();
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
                    ACC_STATIC | ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC,
                    ClassHelper.OBJECT_TYPE
            );
            if (hasStatic) {
                staticFieldHelper = new InnerClassNode(
                        cNode,
                        Traits.staticFieldHelperClassName(cNode),
                        ACC_STATIC | ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC,
                        ClassHelper.OBJECT_TYPE
                );
            }
        }

        // add methods
        List<MethodNode> methods = new ArrayList<MethodNode>(cNode.getMethods());
        List<MethodNode> nonPublicAPIMethods = new LinkedList<MethodNode>();
        for (final MethodNode methodNode : methods) {
            boolean declared = methodNode.getDeclaringClass() == cNode;
            if (declared) {
                if (!methodNode.isSynthetic() && (methodNode.isProtected() || methodNode.getModifiers()==0)) {
                    unit.addError(new SyntaxException("Cannot have protected/package private method in a trait (" + cNode.getName() + "#" + methodNode.getTypeDescriptor() + ")",
                            methodNode.getLineNumber(), methodNode.getColumnNumber()));
                    return null;
                }
                if (!methodNode.isAbstract()) {
                    // add non-abstract methods; abstract methods covered from trait interface
                    helper.addMethod(processMethod(cNode, helper, methodNode, fieldHelper, fieldNames));
                }
                if (methodNode.isPrivate() || methodNode.isStatic()) {
                    nonPublicAPIMethods.add(methodNode);
                }
            }
        }

        // remove methods which should not appear in the trait interface
        for (MethodNode privateMethod : nonPublicAPIMethods) {
            cNode.removeMethod(privateMethod);
        }

        // add fields
        for (FieldNode field : fields) {
            processField(field, initializer, staticInitializer, fieldHelper, helper, staticFieldHelper, cNode, fieldNames);
        }

        // clear properties to avoid generation of methods
        cNode.getProperties().clear();

        // copy annotations
        copyClassAnnotations(cNode, helper);
        markAsGenerated(cNode, helper);

        fields = new ArrayList<FieldNode>(cNode.getFields()); // reuse the full list of fields
        for (FieldNode field : fields) {
            cNode.removeField(field.getName());
        }

        // visit AST xforms
        registerASTTransformations(helper);

        unit.getAST().addClass(helper);
        if (fieldHelper != null) {
            unit.getAST().addClass(fieldHelper);
            if (staticFieldHelper != null) {
                unit.getAST().addClass(staticFieldHelper);
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

    private static MethodNode createInitMethod(final boolean isStatic, final ClassNode cNode, final ClassNode helper) {
        MethodNode initializer = new MethodNode(
                isStatic?Traits.STATIC_INIT_METHOD:Traits.INIT_METHOD,
                ACC_STATIC | ACC_PUBLIC | ACC_SYNTHETIC,
                ClassHelper.VOID_TYPE,
                new Parameter[]{createSelfParameter(cNode, isStatic)},
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

    private void registerASTTransformations(final ClassNode helper) {
        ASTTransformationCollectorCodeVisitor collector = new ASTTransformationCollectorCodeVisitor(
                unit, compilationUnit.getTransformLoader()
        );
        collector.visitClass(helper);
        // Perform an additional phase which has to be done *after* type checking
        compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            @Override
            public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
                if (classNode==helper) {
                    PostTypeCheckingExpressionReplacer replacer = new PostTypeCheckingExpressionReplacer(source);
                    replacer.visitClass(helper);
                }
            }
        }, CompilePhase.INSTRUCTION_SELECTION.getPhaseNumber());
    }

    /**
     * Copies annotation from the trait to the helper, excluding the trait annotation itself
     * @param cNode the trait class node
     * @param helper the helper class node
     */
    private static void copyClassAnnotations(final ClassNode cNode, final ClassNode helper) {
        List<AnnotationNode> annotations = cNode.getAnnotations();
        for (AnnotationNode annotation : annotations) {
            if (!annotation.getClassNode().equals(Traits.TRAIT_CLASSNODE)) {
                helper.addAnnotation(annotation);
            }
        }
    }

    private void checkInnerClasses(final ClassNode cNode) {
        Iterator<InnerClassNode> it = cNode.getInnerClasses();
        while (it.hasNext()) {
            InnerClassNode origin = it.next();
            if ((origin.getModifiers() & ACC_STATIC) == 0) {
                unit.addError(new SyntaxException("Cannot have non-static inner class inside a trait ("+origin.getName()+")", origin.getLineNumber(), origin.getColumnNumber()));
            }
        }
    }

    private static void generatePropertyMethods(final ClassNode cNode) {
        for (PropertyNode node : cNode.getProperties()) {
            processProperty(cNode, node);
        }
    }

    /**
     * Mostly copied from the {@link Verifier} class but does *not* generate bytecode
     *
     * @param cNode
     * @param node
     */
    private static void processProperty(final ClassNode cNode, PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();
        int propNodeModifiers = node.getModifiers();

        String getterName = "get" + Verifier.capitalize(name);
        String setterName = "set" + Verifier.capitalize(name);

        // GROOVY-3726: clear volatile, transient modifiers so that they don't get applied to methods
        if ((propNodeModifiers & Modifier.VOLATILE) != 0) {
            propNodeModifiers = propNodeModifiers - Modifier.VOLATILE;
        }
        if ((propNodeModifiers & Modifier.TRANSIENT) != 0) {
            propNodeModifiers = propNodeModifiers - Modifier.TRANSIENT;
        }

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            MethodNode getter = cNode.getGetterMethod(getterName);
            if (getter == null && ClassHelper.boolean_TYPE == node.getType()) {
                String secondGetterName = "is" + Verifier.capitalize(name);
                getter = cNode.getGetterMethod(secondGetterName);
            }
            if (!node.isPrivate() && methodNeedsReplacement(cNode, getter)) {
                getterBlock = new ExpressionStatement(new FieldExpression(field));
            }
        }
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null) {
            // 2nd arg false below: though not usual, allow setter with non-void return type
            MethodNode setter = cNode.getSetterMethod(setterName, false);
            if (!node.isPrivate() &&
                    (propNodeModifiers & ACC_FINAL) == 0 &&
                    methodNeedsReplacement(cNode, setter)) {
                setterBlock = new ExpressionStatement(
                        new BinaryExpression(
                                new FieldExpression(field),
                                Token.newSymbol(Types.EQUAL, 0, 0),
                                new VariableExpression("value")
                        )
                );
            }
        }

        if (getterBlock != null) {
            MethodNode getter =
                    new MethodNode(getterName, propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
            getter.setSynthetic(true);
            cNode.addMethod(getter);

            if (ClassHelper.boolean_TYPE == node.getType() || ClassHelper.Boolean_TYPE == node.getType()) {
                String secondGetterName = "is" + Verifier.capitalize(name);
                MethodNode secondGetter =
                        new MethodNode(secondGetterName, propNodeModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
                secondGetter.setSynthetic(true);
                cNode.addMethod(secondGetter);
            }
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = {new Parameter(node.getType(), "value")};
            VariableExpression var = (VariableExpression) ((BinaryExpression) ((ExpressionStatement) setterBlock).getExpression()).getRightExpression();
            var.setAccessedVariable(setterParameterTypes[0]);
            MethodNode setter =
                    new MethodNode(setterName, propNodeModifiers, ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
            setter.setSynthetic(true);
            cNode.addMethod(setter);
        }
    }

    private static boolean methodNeedsReplacement(ClassNode classNode, MethodNode m) {
        // no method found, we need to replace
        if (m == null) return true;
        // method is in current class, nothing to be done
        if (m.getDeclaringClass() == classNode) return false;
        // do not overwrite final
        if ((m.getModifiers() & ACC_FINAL) != 0) return false;
        return true;
    }

    private void processField(final FieldNode field, final MethodNode initializer, final MethodNode staticInitializer,
                              final ClassNode fieldHelper, final ClassNode helper, final ClassNode staticFieldHelper, final ClassNode trait,
                              final Set<String> knownFields) {
        if (field.isProtected()) {
            unit.addError(new SyntaxException("Cannot have protected field in a trait (" + trait.getName() + "#" + field.getName() + ")",
                    field.getLineNumber(), field.getColumnNumber()));
            return;
        }

        Expression initialExpression = field.getInitialExpression();
        MethodNode selectedMethod = field.isStatic()?staticInitializer:initializer;
        ClassNode target = field.isStatic() && staticFieldHelper != null ? staticFieldHelper : fieldHelper;
        if (initialExpression != null) {
            VariableExpression thisObject = new VariableExpression(selectedMethod.getParameters()[0]);
            ExpressionStatement initCode = new ExpressionStatement(initialExpression);
            processBody(thisObject, initCode, trait, helper, fieldHelper, knownFields);
            if (field.isFinal()) {
                String baseName = field.isStatic() ? Traits.STATIC_INIT_METHOD : Traits.INIT_METHOD;
                MethodNode fieldInitializer = new MethodNode(
                        baseName + Traits.remappedFieldName(trait, field.getName()),
                        ACC_STATIC | ACC_PUBLIC | ACC_SYNTHETIC,
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
                    mce = new MethodCallExpression(
                            new ClassExpression(INVOKERHELPER_CLASSNODE),
                            "invokeStaticMethod",
                            new ArgumentListExpression(
                                    thisObject,
                                    new ConstantExpression(Traits.helperSetterName(field)),
                                    initCode.getExpression()
                            )
                    );
                } else {
                    mce = new MethodCallExpression(
                            new CastExpression(createReceiverType(field.isStatic(), fieldHelper), thisObject),
                            Traits.helperSetterName(field),
                            new CastExpression(field.getOriginType(),initCode.getExpression())
                    );
                }
                mce.setImplicitThis(false);
                mce.setSourcePosition(initialExpression);
                code.addStatement(new ExpressionStatement(mce));
            }
        }
        // define setter/getter helper methods (setter added even for final fields for legacy compatibility)
        target.addMethod(
                Traits.helperSetterName(field),
                ACC_PUBLIC | ACC_ABSTRACT,
                field.getOriginType(),
                new Parameter[]{new Parameter(field.getOriginType(), "val")},
                ClassNode.EMPTY_ARRAY,
                null
        );
        target.addMethod(
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
                ACC_STATIC | ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC,
                field.getOriginType(),
                fieldHelper,
                null
        );
        // copy annotations from field to dummy field
        List<AnnotationNode> copied = new LinkedList<AnnotationNode>();
        List<AnnotationNode> notCopied = new LinkedList<AnnotationNode>();
        GeneralUtils.copyAnnotatedNodeAnnotations(field, copied, notCopied);
        dummyField.addAnnotations(copied);
        fieldHelper.addField(dummyField);

        // retain legacy field (will be given lower precedence than above)
        dummyFieldName = (field.isStatic() ? Traits.STATIC_FIELD_PREFIX : Traits.FIELD_PREFIX) +
                (field.isPublic()? Traits.PUBLIC_FIELD_PREFIX : Traits.PRIVATE_FIELD_PREFIX)+
                Traits.remappedFieldName(field.getOwner(), field.getName());
        dummyField = new FieldNode(
                dummyFieldName,
                ACC_STATIC | ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC,
                field.getOriginType(),
                fieldHelper,
                null
        );
        // copy annotations from field to legacy dummy field
        copied = new LinkedList<AnnotationNode>();
        notCopied = new LinkedList<AnnotationNode>();
        GeneralUtils.copyAnnotatedNodeAnnotations(field, copied, notCopied);
        dummyField.addAnnotations(copied);
        fieldHelper.addField(dummyField);
    }

    private MethodNode processMethod(ClassNode traitClass, ClassNode traitHelperClass, MethodNode methodNode, ClassNode fieldHelper, Collection<String> knownFields) {
        Parameter[] initialParams = methodNode.getParameters();
        Parameter[] newParams = new Parameter[initialParams.length + 1];
        newParams[0] = createSelfParameter(traitClass, methodNode.isStatic());
        System.arraycopy(initialParams, 0, newParams, 1, initialParams.length);
        final int mod = methodNode.isPrivate() ? ACC_PRIVATE : ACC_PUBLIC | (methodNode.isFinal() ? ACC_FINAL : 0);
        MethodNode mNode = new MethodNode(
                methodNode.getName(),
                mod | ACC_STATIC,
                methodNode.getReturnType(),
                newParams,
                methodNode.getExceptions(),
                processBody(new VariableExpression(newParams[0]), methodNode.getCode(), traitClass, traitHelperClass, fieldHelper, knownFields)
        );
        mNode.setSourcePosition(methodNode);
        mNode.addAnnotations(filterAnnotations(methodNode.getAnnotations()));
        mNode.setGenericsTypes(methodNode.getGenericsTypes());
        if (methodNode.isAbstract()) {
            mNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT);
        } else {
            methodNode.addAnnotation(new AnnotationNode(Traits.IMPLEMENTED_CLASSNODE));
        }
        methodNode.setCode(null);

        if (!methodNode.isPrivate() && !methodNode.isStatic()) {
            methodNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT);
        }
        return mNode;
    }

    private static List<AnnotationNode> filterAnnotations(List<AnnotationNode> annotations) {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>(annotations.size());
        for (AnnotationNode annotation : annotations) {
            if (!OVERRIDE_CLASSNODE.equals(annotation.getClassNode())) {
                result.add(annotation);
            }
        }

        return result;
    }

    private static Parameter createSelfParameter(final ClassNode traitClass, boolean isStatic) {
        final ClassNode rawType = traitClass.getPlainNodeReference();
        ClassNode type = createReceiverType(isStatic, rawType);
        return new Parameter(type, isStatic?Traits.STATIC_THIS_OBJECT:Traits.THIS_OBJECT);
    }

    private static ClassNode createReceiverType(final boolean isStatic, final ClassNode rawType) {
        ClassNode type;
        if (isStatic) {
            // Class<TraitClass>
            type = ClassHelper.CLASS_Type.getPlainNodeReference();
            type.setGenericsTypes(new GenericsType[]{
                    new GenericsType(rawType)
            });
        } else {
            // TraitClass
            type = rawType;
        }
        return type;
    }

    private Statement processBody(VariableExpression thisObject, Statement code, ClassNode trait, ClassNode traitHelper, ClassNode fieldHelper, Collection<String> knownFields) {
        if (code == null) return null;
        NAryOperationRewriter operationRewriter = new NAryOperationRewriter(unit, knownFields);
        code.visit(operationRewriter);
        SuperCallTraitTransformer superTrn = new SuperCallTraitTransformer(unit);
        code.visit(superTrn);
        TraitReceiverTransformer trn = new TraitReceiverTransformer(thisObject, unit, trait, traitHelper, fieldHelper, knownFields);
        code.visit(trn);
        return code;
    }

    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    private static class DefaultArgsMethodsAdder extends Verifier {

        @Override
        public void addDefaultParameterMethods(final ClassNode node) {
            setClassNode(node);
            super.addDefaultParameterMethods(node);
        }
    }

    private static class PostTypeCheckingExpressionReplacer extends ClassCodeExpressionTransformer {
        private final SourceUnit sourceUnit;

        private PostTypeCheckingExpressionReplacer(final SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp != null) {
                Expression replacement = exp.getNodeMetaData(TraitASTTransformation.POST_TYPECHECKING_REPLACEMENT);
                if (replacement!=null) {
                    return replacement;
                }
            }
            return super.transform(exp);
        }
    }

}
