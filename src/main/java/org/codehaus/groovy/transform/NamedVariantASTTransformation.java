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
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;
import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.entryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.list2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class NamedVariantASTTransformation extends AbstractASTTransformation {

    private static final ClassNode NAMED_VARIANT_TYPE = make(NamedVariant.class);
    private static final String NAMED_VARIANT = "@" + NAMED_VARIANT_TYPE.getNameWithoutPackage();
    private static final ClassNode NAMED_PARAM_TYPE = makeWithoutCaching(NamedParam.class, false);
    private static final ClassNode NAMED_DELEGATE_TYPE = makeWithoutCaching(NamedDelegate.class, false);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        MethodNode mNode = (MethodNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!NAMED_VARIANT_TYPE.equals(anno.getClassNode())) return;

        Parameter[] fromParams = mNode.getParameters();
        if (fromParams.length == 0) {
            addError("Error during " + NAMED_VARIANT + " processing. No-args method not supported.", mNode);
            return;
        }

        boolean autoDelegate = memberHasValue(anno, "autoDelegate", true);
        Parameter mapParam = param(GenericsUtils.nonGeneric(MAP_TYPE), "__namedArgs");
        List<Parameter> genParams = new ArrayList<>();
        genParams.add(mapParam);
        ClassNode cNode = mNode.getDeclaringClass();
        BlockStatement inner = new BlockStatement();
        ArgumentListExpression args = new ArgumentListExpression();
        List<String> propNames = new ArrayList<>();

        // first pass, just check for absence of annotations of interest
        boolean annoFound = false;
        for (Parameter fromParam : fromParams) {
            if (AnnotatedNodeUtils.hasAnnotation(fromParam, NAMED_PARAM_TYPE) || AnnotatedNodeUtils.hasAnnotation(fromParam, NAMED_DELEGATE_TYPE)) {
                annoFound = true;
            }
        }

        if (!annoFound && autoDelegate) {
            // assume the first param is the delegate by default
            processDelegateParam(mNode, mapParam, args, propNames, fromParams[0]);
        } else {
            for (Parameter fromParam : fromParams) {
                if (!annoFound) {
                    if (!processImplicitNamedParam(mNode, mapParam, args, propNames, fromParam)) return;
                } else if (AnnotatedNodeUtils.hasAnnotation(fromParam, NAMED_PARAM_TYPE)) {
                    if (!processExplicitNamedParam(mNode, mapParam, inner, args, propNames, fromParam)) return;
                } else if (AnnotatedNodeUtils.hasAnnotation(fromParam, NAMED_DELEGATE_TYPE)) {
                    if (!processDelegateParam(mNode, mapParam, args, propNames, fromParam)) return;
                } else {
                    args.addExpression(varX(fromParam));
                    if (hasDuplicates(mNode, propNames, fromParam.getName())) return;
                    genParams.add(fromParam);
                }
            }
        }
        createMapVariant(mNode, anno, mapParam, genParams, cNode, inner, args, propNames);
    }

    private boolean processImplicitNamedParam(final MethodNode mNode, final Parameter mapParam, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam) {
        boolean required = fromParam.hasInitialExpression();
        String name = fromParam.getName();
        if (hasDuplicates(mNode, propNames, name)) return false;
        AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
        namedParam.addMember("value", constX(name));
        namedParam.addMember("type", classX(fromParam.getType()));
        namedParam.addMember("required", constX(required, true));
        mapParam.addAnnotation(namedParam);
        args.addExpression(propX(varX(mapParam), name));
        return true;
    }

    private boolean processExplicitNamedParam(final MethodNode mNode, final Parameter mapParam, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam) {
        AnnotationNode namedParam = fromParam.getAnnotations(NAMED_PARAM_TYPE).get(0);
        boolean required = memberHasValue(namedParam, "required", true);
        if (getMemberStringValue(namedParam, "value") == null) {
            namedParam.addMember("value", constX(fromParam.getName()));
        }
        String name = getMemberStringValue(namedParam, "value");
        if (getMemberValue(namedParam, "type") == null) {
            namedParam.addMember("type", classX(fromParam.getType()));
        }
        if (hasDuplicates(mNode, propNames, name)) return false;
        // TODO: Check specified type is assignable from declared param type?
        //ClassNode type = getMemberClassValue(namedParam, "type");
        if (required) {
            if (fromParam.hasInitialExpression()) {
                addError("Error during " + NAMED_VARIANT + " processing. A required parameter can't have an initial value.", mNode);
                return false;
            }
            inner.addStatement(new AssertStatement(boolX(callX(varX(mapParam), "containsKey", args(constX(name)))),
                    plusX(constX("Missing required named argument '" + name + "'. Keys found: "), callX(varX(mapParam), "keySet"))));
        }
        args.addExpression(propX(varX(mapParam), name));
        mapParam.addAnnotation(namedParam);
        fromParam.getAnnotations().remove(namedParam);
        return true;
    }

    private boolean processDelegateParam(final MethodNode mNode, final Parameter mapParam, final ArgumentListExpression args, final List<String> propNames, final Parameter fromParam) {
        if (isInnerClass(fromParam.getType())) {
            if (mNode.isStatic()) {
                addError("Error during " + NAMED_VARIANT + " processing. Delegate type '" + fromParam.getType().getNameWithoutPackage() + "' is an inner class which is not supported.", mNode);
                return false;
            }
        }

        Set<String> names = new HashSet<>();
        List<PropertyNode> props = getAllProperties(names, fromParam.getType(), true, false, false, true, false, true);
        for (String next : names) {
            if (hasDuplicates(mNode, propNames, next)) return false;
        }
        List<MapEntryExpression> entries = new ArrayList<>();
        for (PropertyNode pNode : props) {
            String name = pNode.getName();
            // create entry [name: __namedArgs.getOrDefault('name', initialValue)]
            Expression defaultValue = Optional.ofNullable(pNode.getInitialExpression()).orElseGet(() -> getDefaultExpression(pNode.getType()));
            entries.add(entryX(constX(name), callX(varX(mapParam), "getOrDefault", args(constX(name), defaultValue))));
            // create annotation @NamedParam(value='name', type=DelegateType)
            AnnotationNode namedParam = new AnnotationNode(NAMED_PARAM_TYPE);
            namedParam.addMember("value", constX(name));
            namedParam.addMember("type", classX(pNode.getType()));
            mapParam.addAnnotation(namedParam);
        }
        Expression delegateMap = mapX(entries);
        args.addExpression(castX(fromParam.getType(), delegateMap));
        return true;
    }

    private Expression getDefaultExpression(ClassNode pType) {
        if (isPrimitiveType(pType)) {
            if (boolean_TYPE.equals(pType)) {
                return constX(Boolean.FALSE);
            }
            return castX(getWrapper(pType), constX(0));
        }
        return nullX();
    }

    private boolean hasDuplicates(final MethodNode mNode, final List<String> propNames, final String next) {
        if (propNames.contains(next)) {
            addError("Error during " + NAMED_VARIANT + " processing. Duplicate property '" + next + "' found.", mNode);
            return true;
        }
        propNames.add(next);
        return false;
    }

    private void createMapVariant(final MethodNode mNode, final AnnotationNode anno, final Parameter mapParam, final List<Parameter> genParams, final ClassNode cNode, final BlockStatement inner, final ArgumentListExpression args, final List<String> propNames) {
        Parameter namedArgKey = param(STRING_TYPE, "namedArgKey");
        inner.addStatement(
                new ForStatement(
                        namedArgKey,
                        callX(varX(mapParam), "keySet"),
                        new AssertStatement(boolX(callX(list2args(propNames), "contains", varX(namedArgKey))), plusX(constX("Unrecognized namedArgKey: "), varX(namedArgKey)))
                ));

        Parameter[] genParamsArray = genParams.toArray(Parameter.EMPTY_ARRAY);
        // TODO account for default params giving multiple signatures
        if (cNode.hasMethod(mNode.getName(), genParamsArray)) {
            addError("Error during " + NAMED_VARIANT + " processing. Class " + cNode.getNameWithoutPackage() +
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
}
