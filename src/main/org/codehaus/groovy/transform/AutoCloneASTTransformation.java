/*
 * Copyright 2008-2014 the original author or authors.
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
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @AutoClone annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AutoCloneASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = AutoClone.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode CLONEABLE_TYPE = make(Cloneable.class);
    private static final ClassNode BAOS_TYPE = make(ByteArrayOutputStream.class);
    private static final ClassNode BAIS_TYPE = make(ByteArrayInputStream.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
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
        final Expression baos = varX("baos");
        body.addStatement(declS(baos, ctorX(BAOS_TYPE)));

        // baos.withObjectOutputStream{ it.writeObject(this) }
        final Expression it = varX("it");
        ClosureExpression writeClosure = new ClosureExpression(Parameter.EMPTY_ARRAY, block(
                stmt(callX(it, "writeObject", varX("this")))));
        writeClosure.setVariableScope(new VariableScope());
        body.addStatement(stmt(callX(baos, "withObjectOutputStream", args(writeClosure))));

        // def bais = new ByteArrayInputStream(baos.toByteArray())
        final Expression bais = varX("bais");
        ConstructorCallExpression bytes = ctorX(BAIS_TYPE, args(callX(baos, "toByteArray")));
        body.addStatement(declS(bais, bytes));

        // return bais.withObjectInputStream(getClass().classLoader){ it.readObject() }
        ClosureExpression readClosure = new ClosureExpression(new Parameter[]{}, block(
                stmt(callX(it, "readObject"))));
        readClosure.setVariableScope(new VariableScope());
        Expression klass = callThisX("getClass");
        Expression classLoader = callX(klass, "getClassLoader");
        Expression result = callX(bais, "withObjectInputStream", args(classLoader, readClosure));
        body.addStatement(returnS(result));

        ClassNode[] exceptions = {make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, exceptions, body);
    }

    private void createCloneCopyConstructor(ClassNode cNode, List<FieldNode> list, List<String> excludes) {
        BlockStatement initBody = new BlockStatement();
        if (cNode.getDeclaredConstructors().size() == 0) {
            // add no-arg constructor
            initBody.addStatement(EmptyStatement.INSTANCE);
            cNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, initBody);
            initBody = new BlockStatement();
        }
        Parameter initParam = new Parameter(GenericsUtils.nonGeneric(cNode), "other");
        final Expression other = varX(initParam);
        boolean hasParent = cNode.getSuperClass() != ClassHelper.OBJECT_TYPE;
        if (hasParent) {
            initBody.addStatement(stmt(ctorX(ClassNode.SUPER, other)));
        }
        for (FieldNode fieldNode : list) {
            String name = fieldNode.getName();
            if (excludes.contains(name)) continue;
            Expression direct = propX(other, name);
            Expression cloned = callX(direct, "clone");
            Expression to = propX(varX("this"), name);
            Statement assignCloned = assignS(to, cloned);
            Statement assignDirect = assignS(to, direct);
            initBody.addStatement(ifElseS(isInstanceOfX(direct, CLONEABLE_TYPE), assignCloned, assignDirect));
        }
        ClassNode[] exceptions = {make(CloneNotSupportedException.class)};
        cNode.addConstructor(ACC_PROTECTED, params(initParam), ClassNode.EMPTY_ARRAY, initBody);
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, exceptions, block(
                stmt(ctorX(cNode, args(varX("this"))))));
    }

    private void createSimpleClone(ClassNode cNode, List<FieldNode> fieldNodes, List<String> excludes) {
        if (cNode.getDeclaredConstructors().size() == 0) {
            // add no-arg constructor
            cNode.addConstructor(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block(EmptyStatement.INSTANCE));
        }
        final BlockStatement cloneBody = new BlockStatement();
        final Expression result = varX("_result");
        final Expression noarg = ctorX(cNode);
        cloneBody.addStatement(declS(result, noarg));
        addSimpleCloneHelperMethod(cNode, fieldNodes, excludes);
        cloneBody.addStatement(stmt(callThisX("cloneOrCopyMembers", args(result))));
        cloneBody.addStatement(returnS(result));
        ClassNode[] exceptions = {make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, exceptions, cloneBody);
    }

    private void addSimpleCloneHelperMethod(ClassNode cNode, List<FieldNode> fieldNodes, List<String> excludes) {
        Parameter methodParam = new Parameter(GenericsUtils.nonGeneric(cNode), "other");
        final Expression other = varX(methodParam);
        boolean hasParent = cNode.getSuperClass() != ClassHelper.OBJECT_TYPE;
        BlockStatement methodBody = new BlockStatement();
        if (hasParent) {
            methodBody.addStatement(stmt(callSuperX("cloneOrCopyMembers", args(other))));
        }
        for (FieldNode fieldNode : fieldNodes) {
            String name = fieldNode.getName();
            if (excludes.contains(name)) continue;
            Expression direct = propX(varX("this"), name);
            Expression cloned = callX(direct, "clone");
            Expression to = propX(other, name);
            Statement assignCloned = assignS(to, cloned);
            Statement assignDirect = assignS(to, direct);
            methodBody.addStatement(ifElseS(isInstanceOfX(direct, CLONEABLE_TYPE), assignCloned, assignDirect));
        }
        ClassNode[] exceptions = {make(CloneNotSupportedException.class)};
        cNode.addMethod("cloneOrCopyMembers", ACC_PROTECTED, ClassHelper.VOID_TYPE, params(methodParam), exceptions, methodBody);
    }

    private void createClone(ClassNode cNode, List<FieldNode> fieldNodes, List<String> excludes) {
        final BlockStatement body = new BlockStatement();
        final Expression result = varX("_result");
        body.addStatement(declS(result, callSuperX("clone")));
        for (FieldNode fieldNode : fieldNodes) {
            if (excludes.contains(fieldNode.getName())) continue;
            Expression fieldExpr = varX(fieldNode);
            Expression from = callX(fieldExpr, "clone");
            Expression to = propX(result, fieldNode.getName());
            Statement doClone = assignS(to, from);
            body.addStatement(ifS(isInstanceOfX(fieldExpr, CLONEABLE_TYPE), doClone));
        }
        body.addStatement(returnS(result));
        ClassNode[] exceptions = {make(CloneNotSupportedException.class)};
        cNode.addMethod("clone", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, exceptions, body);
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
