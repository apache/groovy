/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import groovy.lang.Delegate;
import groovy.transform.Trait;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Handles generation of code for the @Trait annotation.
 * A class annotated with @Trait will generate, instead:
 * <ul>
 *     <li>an <i>interface</i> with the same name</li>
 *     <li>an utility inner class that will be used by the compiler to handle the trait</li>
 * </ul>
 *
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class TraitASTTransformation extends AbstractASTTransformation {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    private static @interface AbstractMethod {}

    static final ClassNode ABSTRACT_METHOD = ClassHelper.make(AbstractMethod.class);
    static final Class MY_CLASS = Trait.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    static final String TRAIT_HELPER = "$Trait$Helper";
    static final ClassNode DELEGATE_NODE = ClassHelper.make(Delegate.class);

    private SourceUnit unit;
    private FieldNode mixinField;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;
        unit = source;
        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            checkNoConstructor(cNode);
            createHelperClass(cNode);
        }
    }

    private void checkNoConstructor(final ClassNode cNode) {
        if (!cNode.getDeclaredConstructors().isEmpty()) {
            addError("Error processing trait '" + cNode.getName() + "'. " +
                    " Constructors are not allowed.", cNode);
        }
    }

    private void createHelperClass(final ClassNode cNode) {
        ClassNode helper = new InnerClassNode(
                cNode,
                cNode.getName()+ TRAIT_HELPER,
                ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
                ClassHelper.OBJECT_TYPE,
                new ClassNode[]{cNode},
                null
        );
        mixinField = new FieldNode(
                "mixin",
                ACC_PRIVATE | ACC_FINAL,
                cNode,
                helper,
                null
        );
        helper.addField(mixinField);
        cNode.setModifiers(ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT);
        Map<String,MethodNode> methods = cNode.getDeclaredMethodsMap();
        for (MethodNode methodNode : methods.values()) {
            if (methodNode.getDeclaringClass()==cNode) {
                helper.addMethod(processMethod(methodNode));
            }
        }
        Parameter owner = new Parameter(cNode, "owner");
        ConstructorNode constructorNode = new ConstructorNode(
                ACC_PUBLIC,
                new Parameter[]{
                        owner
                },
                ClassNode.EMPTY_ARRAY,
                new ExpressionStatement(
                        new BinaryExpression(
                                new FieldExpression(mixinField),
                                Token.newSymbol(Types.EQUAL, -1, -1),
                                new VariableExpression(owner)
                        )
                )
        );
        helper.addConstructor(constructorNode);
        unit.getAST().addClass(helper);
    }

    private MethodNode processMethod(final MethodNode methodNode) {
        MethodNode mNode = new MethodNode(
                methodNode.getName(),
                ACC_PUBLIC,
                methodNode.getReturnType(),
                methodNode.getParameters(),
                methodNode.getExceptions(),
                processBody(methodNode.getCode())
        );
        if (methodNode.getCode()==null) {
            methodNode.addAnnotation(new AnnotationNode(ABSTRACT_METHOD));
        } else {
            methodNode.setCode(null);
        }
        methodNode.setModifiers(ACC_PUBLIC | ACC_ABSTRACT);
        return mNode;
    }

    private Statement processBody(final Statement code) {
        if (code==null) return null;
        ReceiverTransformer trn = new ReceiverTransformer();
        code.visit(trn);
        return code;
    }

    private class ReceiverTransformer extends ClassCodeExpressionTransformer {

        @Override
        protected SourceUnit getSourceUnit() {
            return unit;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp instanceof MethodCallExpression) {
                MethodCallExpression call = (MethodCallExpression) exp;
                Expression obj = call.getObjectExpression();
                if (call.isImplicitThis() || obj.getText().equals("this")) {
                    MethodCallExpression transformed = new MethodCallExpression(
                            new VariableExpression(mixinField),
                            call.getMethod(),
                            call.getArguments()
                    );
                    transformed.setSourcePosition(call);
                    transformed.setSafe(call.isSafe());
                    transformed.setSpreadSafe(call.isSpreadSafe());
                    return transformed;
                }
            }
            return super.transform(exp);
        }
    }

    public static void doExtendTraits(ClassNode cNode, SourceUnit unit) {
        ClassNode[] interfaces = cNode.getInterfaces();
        for (ClassNode trait : interfaces) {
            List<AnnotationNode> traitAnn = trait.getAnnotations(MY_TYPE);
            if (traitAnn !=null && !traitAnn.isEmpty() && !cNode.getNameWithoutPackage().endsWith(TRAIT_HELPER)) {
                DelegateASTTransformation delegateASTTransformation = new DelegateASTTransformation() {
                    @Override
                    protected List<MethodNode> getAllMethods(final ClassNode type) {
                        List<MethodNode> allMethods = super.getAllMethods(type);
                        Iterator<MethodNode> iterator = allMethods.iterator();
                        while (iterator.hasNext()) {
                            MethodNode next = iterator.next();
                            List<AnnotationNode> annotations = next.getAnnotations(ABSTRACT_METHOD);
                            if (annotations!=null && !annotations.isEmpty()) {
                                iterator.remove();
                            }
                        }
                        return allMethods;
                    }
                };
                AnnotationNode delegate = new AnnotationNode(
                        DELEGATE_NODE
                );
                Iterator<InnerClassNode> innerClasses = trait.redirect().getInnerClasses();
                ClassNode icn;
                if (innerClasses!=null && innerClasses.hasNext()) {
                    // trait defined in same source unit
                    icn = innerClasses.next();
                } else {
                    // precompiled trait
                    try {
                        ClassLoader classLoader = trait.getTypeClass().getClassLoader();
                        Class traitHelper = classLoader.loadClass(trait.getName()+TRAIT_HELPER);
                        icn = ClassHelper.make(traitHelper);
                    } catch (ClassNotFoundException e) {
                        throw new GroovyBugError(e);
                    }
                }
                FieldNode delegateNode = new FieldNode(
                        "delegate$"+trait.getNameWithoutPackage()+"$trait",
                        ACC_PRIVATE,
                        trait,
                        cNode,
                        new ConstructorCallExpression(
                                icn,
                                new ArgumentListExpression(new VariableExpression("this"))
                        )
                );
                cNode.addField(delegateNode);
                delegateASTTransformation.visit(new ASTNode[]{delegate, delegateNode}, unit);
            }
        }
    }
}
