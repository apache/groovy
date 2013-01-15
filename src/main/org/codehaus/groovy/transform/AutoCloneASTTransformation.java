/*
 * Copyright 2008-2013 the original author or authors.
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
package org.codehaus.groovy.transform;

import groovy.transform.AutoClone;
import groovy.transform.AutoCloneStyle;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.assignStatement;
import static org.codehaus.groovy.transform.AbstractASTTransformUtil.getInstanceNonPropertyFields;
import static org.codehaus.groovy.transform.AbstractASTTransformUtil.getInstancePropertyFields;
import static org.codehaus.groovy.transform.AbstractASTTransformUtil.isInstanceOf;

/**
 * Handles generation of code for the @AutoClone annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AutoCloneASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = AutoClone.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode CLONEABLE_TYPE = ClassHelper.make(Cloneable.class);
    private static final ClassNode BAOS_TYPE = ClassHelper.make(ByteArrayOutputStream.class);
    private static final ClassNode BAIS_TYPE = ClassHelper.make(ByteArrayInputStream.class);
    private static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            cNode.addInterface(CLONEABLE_TYPE);
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            AutoCloneStyle style = getStyle(anno, "style");
            List<String> excludes = getMemberList(anno, "excludes");
            List<FieldNode> list = getInstancePropertyFields(cNode);
            if (includeFields) {
                list.addAll(getInstanceNonPropertyFields(cNode));
            }
            if (style == null) style = AutoCloneStyle.CLONE;
            switch (style) {
                case COPY_CONSTRUCTOR:
                    createCloneCopyConstructor(cNode, list, excludes);
                    break;
                case SERIALIZATION:
                    createCloneSerialization(cNode);
                    break;
                case CLONE:
                    createClone(cNode, list, excludes);
                    break;
                case SIMPLE:
                    createSimpleClone(cNode, list, excludes);
                    break;
            }
        }
    }

    private void createCloneSerialization(ClassNode cNode) {
        final BlockStatement body = new BlockStatement();
        // def baos = new ByteArrayOutputStream()
        final Expression baos = new VariableExpression("baos");
        body.addStatement(new ExpressionStatement(new DeclarationExpression(baos, ASSIGN, new ConstructorCallExpression(BAOS_TYPE, MethodCallExpression.NO_ARGUMENTS))));

        // baos.withObjectOutputStream{ it.writeObject(this) }
        BlockStatement writeClosureCode = new BlockStatement();
        final Expression it = new VariableExpression("it");
        writeClosureCode.addStatement(new ExpressionStatement(new MethodCallExpression(it, "writeObject", VariableExpression.THIS_EXPRESSION)));
        ClosureExpression writeClosure = new ClosureExpression(new Parameter[]{}, writeClosureCode);
        writeClosure.setVariableScope(new VariableScope());
        body.addStatement(new ExpressionStatement(new MethodCallExpression(baos, "withObjectOutputStream", new ArgumentListExpression(writeClosure))));

        // def bais = new ByteArrayInputStream(baos.toByteArray())
        final Expression bais = new VariableExpression("bais");
        ConstructorCallExpression bytes = new ConstructorCallExpression(BAIS_TYPE, new TupleExpression(new MethodCallExpression(baos, "toByteArray", MethodCallExpression.NO_ARGUMENTS)));
        body.addStatement(new ExpressionStatement(new DeclarationExpression(bais, ASSIGN, bytes)));

        // return bais.withObjectInputStream(getClass().classLoader){ it.readObject() }
        BlockStatement readClosureCode = new BlockStatement();
        readClosureCode.addStatement(new ExpressionStatement(new MethodCallExpression(it, "readObject", MethodCallExpression.NO_ARGUMENTS)));
        ClosureExpression readClosure = new ClosureExpression(new Parameter[]{}, readClosureCode);
        readClosure.setVariableScope(new VariableScope());
        Expression klass = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, "getClass", MethodCallExpression.NO_ARGUMENTS);
        Expression classLoader = new MethodCallExpression(klass, "getClassLoader", MethodCallExpression.NO_ARGUMENTS);
        Expression result = new MethodCallExpression(bais, "withObjectInputStream", new ArgumentListExpression(classLoader, readClosure));
        body.addStatement(new ReturnStatement(result));

        ClassNode[] exceptions = {ClassHelper.make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, new Parameter[0], exceptions, body);
    }

    private void createCloneCopyConstructor(ClassNode cNode, List<FieldNode> list, List<String> excludes) {
        BlockStatement initBody = new BlockStatement();
        if (cNode.getDeclaredConstructors().size() == 0) {
            // add no-arg constructor
            initBody.addStatement(new EmptyStatement());
            cNode.addConstructor(ACC_PUBLIC, new Parameter[0], ClassNode.EMPTY_ARRAY, initBody);
            initBody = new BlockStatement();
        }
        Parameter initParam = new Parameter(cNode, "other");
        final Expression other = new VariableExpression(initParam);
        boolean hasParent = cNode.getSuperClass() != ClassHelper.OBJECT_TYPE;
        if (hasParent) {
            initBody.addStatement(new ExpressionStatement(new ConstructorCallExpression(ClassNode.SUPER, other)));
        }
        for (FieldNode fieldNode : list) {
            String name = fieldNode.getName();
            if (excludes.contains(name)) continue;
            PropertyExpression direct = new PropertyExpression(other, name);
            Expression cloned = new MethodCallExpression(direct, "clone", MethodCallExpression.NO_ARGUMENTS);
            Expression to = new PropertyExpression(VariableExpression.THIS_EXPRESSION, name);
            Statement assignCloned = assignStatement(to, cloned);
            Statement assignDirect = assignStatement(to, direct);
            initBody.addStatement(new IfStatement(isInstanceOf(direct, CLONEABLE_TYPE), assignCloned, assignDirect));
        }
        ClassNode[] exceptions = {ClassHelper.make(CloneNotSupportedException.class)};
        cNode.addConstructor(ACC_PROTECTED, new Parameter[]{initParam}, ClassNode.EMPTY_ARRAY, initBody);
        final BlockStatement cloneBody = new BlockStatement();
        cloneBody.addStatement(new ExpressionStatement(new ConstructorCallExpression(cNode, new ArgumentListExpression(VariableExpression.THIS_EXPRESSION))));
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, new Parameter[0], exceptions, cloneBody);
    }

    private void createSimpleClone(ClassNode cNode, List<FieldNode> list, List<String> excludes) {
        if (cNode.getDeclaredConstructors().size() == 0) {
            // add no-arg constructor
            BlockStatement initBody = new BlockStatement();
            initBody.addStatement(new EmptyStatement());
            cNode.addConstructor(ACC_PUBLIC, new Parameter[0], ClassNode.EMPTY_ARRAY, initBody);
        }
        final BlockStatement cloneBody = new BlockStatement();
        final Expression result = new VariableExpression("_result");
        final Expression noarg = new ConstructorCallExpression(cNode, ArgumentListExpression.EMPTY_ARGUMENTS);
        cloneBody.addStatement(new ExpressionStatement(new DeclarationExpression(result, ASSIGN, noarg)));
        addSimpleCloneHelperMethod(cNode, list, excludes);
        cloneBody.addStatement(new ExpressionStatement(new MethodCallExpression(VariableExpression.THIS_EXPRESSION, "cloneOrCopyMembers", new ArgumentListExpression(result))));
        cloneBody.addStatement(new ReturnStatement(result));
        ClassNode[] exceptions = {ClassHelper.make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, new Parameter[0], exceptions, cloneBody);
    }

    private void addSimpleCloneHelperMethod(ClassNode cNode, List<FieldNode> list, List<String> excludes) {
        Parameter methodParam = new Parameter(cNode, "other");
        final Expression other = new VariableExpression(methodParam);
        boolean hasParent = cNode.getSuperClass() != ClassHelper.OBJECT_TYPE;
        BlockStatement methodBody = new BlockStatement();
        if (hasParent) {
            methodBody.addStatement(new ExpressionStatement(new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "cloneOrCopyMembers", new ArgumentListExpression(other))));
        }
        for (FieldNode fieldNode : list) {
            String name = fieldNode.getName();
            if (excludes.contains(name)) continue;
            PropertyExpression direct = new PropertyExpression(VariableExpression.THIS_EXPRESSION, name);
            Expression cloned = new MethodCallExpression(direct, "clone", MethodCallExpression.NO_ARGUMENTS);
            Expression to = new PropertyExpression(other, name);
            Statement assignCloned = assignStatement(to, cloned);
            Statement assignDirect = assignStatement(to, direct);
            methodBody.addStatement(new IfStatement(isInstanceOf(direct, CLONEABLE_TYPE), assignCloned, assignDirect));
        }
        ClassNode[] exceptions = {ClassHelper.make(CloneNotSupportedException.class)};
        cNode.addMethod("cloneOrCopyMembers", ACC_PROTECTED, ClassHelper.VOID_TYPE, new Parameter[]{methodParam}, exceptions, methodBody);
    }

    private void createClone(ClassNode cNode, List<FieldNode> list, List<String> excludes) {
        final BlockStatement body = new BlockStatement();
        final Expression result = new VariableExpression("_result");
        final Expression clone = new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "clone", MethodCallExpression.NO_ARGUMENTS);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(result, ASSIGN, clone)));
        for (FieldNode fieldNode : list) {
            if (excludes.contains(fieldNode.getName())) continue;
            Expression fieldExpr = new VariableExpression(fieldNode);
            Expression from = new MethodCallExpression(fieldExpr, "clone", MethodCallExpression.NO_ARGUMENTS);
            Expression to = new PropertyExpression(result, fieldNode.getName());
            Statement doClone = assignStatement(to, from);
            Statement doNothing = new EmptyStatement();
            body.addStatement(new IfStatement(isInstanceOf(fieldExpr, CLONEABLE_TYPE), doClone, doNothing));
        }
        body.addStatement(new ReturnStatement(result));
        ClassNode[] exceptions = {ClassHelper.make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, new Parameter[0], exceptions, body);
    }

    private AutoCloneStyle getStyle(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member != null && member instanceof PropertyExpression) {
            PropertyExpression prop = (PropertyExpression) member;
            Expression oe = prop.getObjectExpression();
            if (oe instanceof ClassExpression) {
                ClassExpression ce = (ClassExpression) oe;
                if (ce.getType().getName().equals("groovy.transform.AutoCloneStyle")) {
                    return AutoCloneStyle.valueOf(prop.getPropertyAsString());
                }
            }
        }
        return null;
    }

}
