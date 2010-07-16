/*
 * Copyright 2008-2010 the original author or authors.
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

import groovy.transform.TupleConstructor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.*;

/**
 * Handles generation of code for the @TupleConstructor annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class TupleConstructorASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = TupleConstructor.class;
    static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static Map<Class<?>, Expression> primitivesInitialValues;

    static {
        final ConstantExpression zero = new ConstantExpression(0);
        final ConstantExpression zeroDecimal = new ConstantExpression(.0);
        primitivesInitialValues = new HashMap<Class<?>, Expression>();
        primitivesInitialValues.put(int.class, zero);
        primitivesInitialValues.put(long.class, zero);
        primitivesInitialValues.put(short.class, zero);
        primitivesInitialValues.put(byte.class, zero);
        primitivesInitialValues.put(char.class, zero);
        primitivesInitialValues.put(float.class, zeroDecimal);
        primitivesInitialValues.put(double.class, zeroDecimal);
        primitivesInitialValues.put(boolean.class, ConstantExpression.FALSE);
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            boolean includeProperties = !memberHasValue(anno, "includeProperties", false);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", true);
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", true);
            boolean callSuper = memberHasValue(anno, "callSuper", true);
            boolean force = memberHasValue(anno, "force", true);
            List<String> excludes = tokenize((String) getMemberValue(anno, "excludes"));
            createConstructor(cNode, includeFields, includeProperties, includeSuperFields, includeSuperProperties, callSuper, force, excludes);
        }
    }

    public static void createConstructor(ClassNode cNode, boolean includeFields, boolean includeProperties, boolean includeSuperFields, boolean includeSuperProperties, boolean callSuper, boolean force, List<String> excludes) {
        // no processing if existing constructors found
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        if (constructors.size() > 1 && !force) return;
        boolean foundEmpty = constructors.size() == 1 && constructors.get(0).getFirstStatement() == null;
        if (constructors.size() == 1 && !foundEmpty && !force) return;
        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        if (foundEmpty) constructors.remove(0);

        List<FieldNode> superList = new ArrayList<FieldNode>();
        if (includeSuperProperties) {
            superList.addAll(getSuperPropertyFields(cNode.getSuperClass()));
        }
        if (includeSuperFields) {
            superList.addAll(getSuperNonPropertyFields(cNode.getSuperClass()));
        }

        List<FieldNode> list = new ArrayList<FieldNode>();
        if (includeProperties) {
            list.addAll(getInstancePropertyFields(cNode));
        }
        if (includeFields) {
            list.addAll(getInstanceNonPropertyFields(cNode));
        }

        final List<Parameter> params = new ArrayList<Parameter>();
        final List<Expression> superParams = new ArrayList<Expression>();
        final BlockStatement body = new BlockStatement();
        for (FieldNode fNode : superList) {
            String name = fNode.getName();
            if (excludes.contains(name) || name.contains("$")) continue;
            params.add(createParam(fNode, name));
            if (callSuper) {
                superParams.add(new VariableExpression(name));
            } else {
                body.addStatement(assignStatement(new PropertyExpression(VariableExpression.THIS_EXPRESSION, name), new VariableExpression(name)));
            }
        }
        if (callSuper) {
            body.addStatement(new ExpressionStatement(new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(superParams))));
        }
        for (FieldNode fNode : list) {
            String name = fNode.getName();
            if (excludes.contains(name) || name.contains("$")) continue;
            params.add(createParam(fNode, name));
            body.addStatement(assignStatement(new FieldExpression(fNode), new VariableExpression(name)));
        }
        cNode.addConstructor(new ConstructorNode(ACC_PUBLIC, params.toArray(new Parameter[params.size()]), ClassNode.EMPTY_ARRAY, body));
    }

    private static Parameter createParam(FieldNode fNode, String name) {
        Parameter param = new Parameter(fNode.getType(), name);
        param.setInitialExpression(providedOrDefaultInitialValue(fNode));
        return param;
    }

    private static Expression providedOrDefaultInitialValue(FieldNode fNode) {
        Expression initialExp = fNode.getInitialExpression() != null ? fNode.getInitialExpression() : ConstantExpression.NULL;
        final ClassNode paramType = fNode.getType();
        if (ClassHelper.isPrimitiveType(paramType) && initialExp.equals(ConstantExpression.NULL)) {
            initialExp = primitivesInitialValues.get(paramType.getTypeClass());
        }
        return initialExp;
    }

}
