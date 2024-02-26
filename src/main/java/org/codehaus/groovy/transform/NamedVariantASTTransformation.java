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
package org.codehaus.groovy.transform;

import groovy.transform.NamedDelegate;
import groovy.transform.NamedParam;
import groovy.transform.NamedVariant;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;
import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.asX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.elvisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.list2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class NamedVariantASTTransformation extends AbstractASTTransformation {

    private static final ClassNode NAMED_PARAM_TYPE = make(NamedParam.class);
    private static final ClassNode NAMED_VARIANT_TYPE = make(NamedVariant.class);
    private static final ClassNode NAMED_DELEGATE_TYPE = make(NamedDelegate.class);
    private static final ClassNode ILLEGAL_ARGUMENT_TYPE = make(IllegalArgumentException.class);

    private static final String NAMED_VARIANT = "@" + NAMED_VARIANT_TYPE.getNameWithoutPackage();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        MethodNode mNode = (MethodNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!NAMED_VARIANT_TYPE.equals(anno.getClassNode())) return;

        Parameter[] mNodeParams = mNode.getParameters();
        if (mNodeParams.length == 0) {
            addError("Error during " + NAMED_VARIANT + " processing. No-arg methods aren't supported.", mNode);
            return;
        }

        boolean autoDelegate = memberHasValue(anno, "autoDelegate", Boolean.TRUE);
        boolean coerce = memberHasValue(anno, "coerce", Boolean.TRUE);
        Parameter mapParam = param(GenericsUtils.nonGeneric(MAP_TYPE), "namedArgs");
        List<Parameter> genParams = new ArrayList<>();
        genParams.add(mapParam);
        ClassNode cNode = mNode.getDeclaringClass();
        BlockStatement inner = new BlockStatement();
        ArgumentListExpression args = new ArgumentListExpression();
        List<String> propNames = new ArrayList<>();

        // first pass, just check for annotations of interest
        boolean annoFound = false;
        for (Parameter mNodeParam : mNodeParams) {
            if (AnnotatedNodeUtils.hasAnnotation(mNodeParam, NAMED_PARAM_TYPE) || AnnotatedNodeUtils.hasAnnotation(mNodeParam, NAMED_DELEGATE_TYPE)) {
                annoFound = true;
                break;
            }
        }

        if (!annoFound && autoDelegate) { // the first param is the delegate
            processDelegateParam(mNode, mapParam, args, propNames, mNodeParams[0], coerce);
        } else {
            Map<Parameter, Expression> seen = new HashMap<>();
            for (Parameter mNodeParam : mNodeParams) {
                if (!annoFound) {
                    if (!processImplicitNamedParam(this, mNode, mapParam, inner, args, propNames, mNodeParam, coerce, seen)) return;
                } else if (AnnotatedNodeUtils.hasAnnotation(mNodeParam, NAMED_PARAM_TYPE)) {
                    if (!processExplicitNamedParam(mNode, mapParam, inner, args, propNames, mNodeParam, coerce, seen)) return;
                } else if (AnnotatedNodeUtils.hasAnnotation(mNodeParam, NAMED_DELEGATE_TYPE)) {
                    if (!processDelegateParam(mNode, mapParam, args, propNames, mNodeParam, coerce)) return;
                } else {
                    Expression arg = varX(mNodeParam);
                    Expression argOrDefault = mNodeParam.hasInitialExpression() ? elvisX(arg, mNodeParam.getDefaultValue()) : arg;
                    args.addExpression(asType(argOrDefault, mNodeParam.getType(), coerce));
                    if (hasDuplicates(this, mNode, propNames, mNodeParam.getName())) return;
                    genParams.add(mNodeParam);
                }
            }
        }
        createMapVariant(this, mNode, anno, mapParam, genParams, cNode, inner, args, propNames);
    }

    // for backwards compatibility
    static boolean processImplicitNamedParam(final ErrorCollecting xform, final MethodNode mNode, final Parameter mapParam, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam, final boolean coerce) {
        return processImplicitNamedParam(xform, mNode, mapParam, inner, args, propNames, fromParam, coerce, null);
    }

    static boolean processImplicitNamedParam(final ErrorCollecting xform, final MethodNode mNode, final Parameter mapParam, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam, final boolean coerce, Map<Parameter, Expression> seen) {
        String name = fromParam.getName();
        ClassNode type = fromParam.getType();
        boolean required = !fromParam.hasInitialExpression();
        if (hasDuplicates(xform, mNode, propNames, name)) return false;

        AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
        namedParam.addMember("value", constX(name));
        namedParam.addMember("type", classX(type));
        namedParam.addMember("required", constX(required, true));
        mapParam.addAnnotation(namedParam);

        if (required) {
            inner.addStatement(new AssertStatement(boolX(containsKey(mapParam, name)),
                    plusX(constX("Missing required named argument '" + name + "'. Keys found: "), callX(varX(mapParam), "keySet"))));
        }
        Expression defValue = getDefaultValue(fromParam.getInitialExpression(), seen);
        Expression initExpr = namedParamValue(mapParam, name, type, coerce, defValue);
        if (seen != null) {
            seen.put(fromParam, initExpr);
        }
        args.addExpression(initExpr);
        return true;
    }

    private boolean processExplicitNamedParam(final MethodNode mNode, final Parameter mapParam, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam, final boolean coerce, Map<Parameter, Expression> seen) {
        AnnotationNode namedParam = fromParam.getAnnotations(NAMED_PARAM_TYPE).get(0);

        String name = getMemberStringValue(namedParam, "value");
        if (name == null) {
            name = fromParam.getName();
            namedParam.addMember("value", constX(name));
        }
        if (hasDuplicates(this, mNode, propNames, name)) return false;

        ClassNode type = getMemberClassValue(namedParam, "type");
        if (type == null) {
            type = fromParam.getType();
            namedParam.addMember("type", classX(type));
        } else {
            // TODO: Check attribute type is assignable to declared param type?
        }

        boolean required = memberHasValue(namedParam, "required", Boolean.TRUE);
        if (required) {
            if (fromParam.hasInitialExpression()) {
                addError("Error during " + NAMED_VARIANT + " processing. A required parameter can't have an initial value.", fromParam);
                return false;
            }
            inner.addStatement(new AssertStatement(boolX(containsKey(mapParam, name)),
                    plusX(constX("Missing required named argument '" + name + "'. Keys found: "), callX(varX(mapParam), "keySet"))));
        }
        Expression defValue = getDefaultValue(fromParam.getInitialExpression(), seen);
        Expression initExpr = namedParamValue(mapParam, name, type, coerce, defValue);
        seen.put(fromParam, initExpr);
        args.addExpression(initExpr);
        mapParam.addAnnotation(namedParam);
        fromParam.getAnnotations().remove(namedParam);
        return true;
    }

    private boolean processDelegateParam(final MethodNode mNode, final Parameter mapParam, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam, final boolean coerce) {
        if (isInnerClass(fromParam.getType()) && mNode.isStatic()) {
            addError("Error during " + NAMED_VARIANT + " processing. Delegate type '" + fromParam.getType().getNameWithoutPackage() + "' is an inner class which is not supported.", mNode);
            return false;
        }

        Set<String> names = new HashSet<>();
        List<PropertyNode> props = getAllProperties(names, fromParam.getType(), true, false, false, true, false, true);
        for (String name : names) {
            if (hasDuplicates(this, mNode, propNames, name)) return false;
        }
        for (PropertyNode prop : props) {
            // create annotation @NamedParam(value='name', type=PropertyType)
            AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
            namedParam.addMember("value", constX(prop.getName()));
            namedParam.addMember("type", classX(prop.getType()));
            mapParam.addAnnotation(namedParam);
        }

        Expression[] subMapArgs = names.stream().map(name -> constX(name)).toArray(Expression[]::new);
        Expression delegateMap = callX(varX(mapParam), "subMap", args(subMapArgs));
        args.addExpression(castX(fromParam.getType(), delegateMap));
        return true;
    }

    private static boolean hasDuplicates(final ErrorCollecting xform, final MethodNode mNode, final List<String> propNames, final String next) {
        if (propNames.contains(next)) {
            xform.addError("Error during " + NAMED_VARIANT + " processing. Duplicate property '" + next + "' found.", mNode);
            return true;
        }
        propNames.add(next);
        return false;
    }

    static void createMapVariant(final ErrorCollecting xform, final MethodNode mNode, final AnnotationNode anno, final Parameter mapParam, final List<Parameter> genParams, final ClassNode cNode, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames) {
        Parameter namedArgKey = param(STRING_TYPE, "namedArgKey");
        if (!(mNode instanceof ConstructorNode)) {
            inner.getStatements().add(0, ifS(isNullX(varX(mapParam)),
                    throwS(ctorX(ILLEGAL_ARGUMENT_TYPE, args(constX("Named parameter map cannot be null"))))));
        }
        inner.addStatement(
                new ForStatement(
                        namedArgKey,
                        callX(varX(mapParam), "keySet"),
                        new AssertStatement(boolX(callX(list2args(propNames), "contains", varX(namedArgKey))), plusX(constX("Unrecognized namedArgKey: "), varX(namedArgKey)))
                ));

        Parameter[] genParamsArray = genParams.toArray(Parameter.EMPTY_ARRAY);
        // TODO account for default params giving multiple signatures
        if (cNode.hasMethod(mNode.getName(), genParamsArray)) {
            xform.addError("Error during " + NAMED_VARIANT + " processing. Class " + cNode.getNameWithoutPackage() +
                    " already has a named-arg " + (mNode instanceof ConstructorNode ? "constructor" : "method") +
                    " of type " + genParams, mNode);
            return;
        }

        BlockStatement body = new BlockStatement();
        int modifiers = getVisibility(anno, mNode, mNode.getClass(), mNode.getModifiers());
        if (mNode instanceof ConstructorNode) {
            body.addStatement(stmt(ctorX(ClassNode.THIS, args)));
            body.addStatement(inner);
            addGeneratedConstructor(cNode,
                    modifiers,
                    genParamsArray,
                    mNode.getExceptions(),
                    body
            );
        } else {
            body.addStatement(inner);
            body.addStatement(stmt(callThisX(mNode.getName(), args)));
            addGeneratedMethod(cNode,
                    mNode.getName(),
                    modifiers,
                    mNode.getReturnType(),
                    genParamsArray,
                    mNode.getExceptions(),
                    body
            );
        }
    }

    private static Expression getDefaultValue(final Expression defaultValue, final Map<Parameter, Expression> seen) {
        if (defaultValue != null && seen != null && !seen.isEmpty()) { // GROOVY-10561, GROOVY-10889, GROOVY-11325
            ExpressionTransformer variableTransformer = (expression) -> {
                if (expression instanceof VariableExpression) { // maybe it's a reference to a previous parameter
                    return seen.getOrDefault(((VariableExpression) expression).getAccessedVariable(), expression);
                }
                return expression;
            };
            return (defaultValue instanceof VariableExpression
                    ? variableTransformer.transform(defaultValue)
                    : defaultValue.transformExpression(variableTransformer));
        }
        return defaultValue;
    }

    private static Expression namedParamValue(final Parameter mapParam, final String name, final ClassNode type, final boolean coerce, Expression defaultValue) {
        Expression value = propX(varX(mapParam), name); // TODO: "map.get(name)"
        if (defaultValue == null && isPrimitiveType(type)) {
            defaultValue = defaultValueX(type);
        }
        if (defaultValue != null) {
            value = ternaryX(containsKey(mapParam, name), value, defaultValue);
        }
        return asType(value, type, coerce);
    }

    private static Expression containsKey(final Parameter mapParam, final String name) {
        MethodCallExpression call = callX(varX(mapParam), "containsKey", constX(name));
        call.setImplicitThis(false); // required for use before super ctor call
        call.setMethodTarget(MAP_TYPE.getMethods("containsKey").get(0));
        return call;
    }

    private static Expression asType(final Expression value, final ClassNode type, final boolean coerce) {
        return coerce ? asX(type, value) : /*castX(*/value/*)*/;
    }
}
