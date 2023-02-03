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

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import groovy.transform.DefaultsMode;
import groovy.transform.TupleConstructor;
import groovy.transform.options.PropertyHandler;
import groovy.transform.stc.POJO;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.apache.groovy.ast.tools.ExpressionUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static groovy.transform.DefaultsMode.OFF;
import static groovy.transform.DefaultsMode.ON;
import static java.util.stream.Collectors.joining;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasExplicitConstructor;
import static org.apache.groovy.ast.tools.ConstructorNodeUtils.checkPropNamesS;
import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.copyStatementsWithSuperAdjustment;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;
import static org.codehaus.groovy.transform.ImmutableASTTransformation.makeImmutable;
import static org.codehaus.groovy.transform.NamedVariantASTTransformation.processImplicitNamedParam;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Handles generation of code for the @TupleConstructor annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class TupleConstructorASTTransformation extends AbstractASTTransformation implements CompilationUnitAware, TransformWithPriority {

    private CompilationUnit compilationUnit;

    static final Class<?> MY_CLASS = TupleConstructor.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    private static final String NAMED_ARGS = "__namedArgs";
    private static final ClassNode LHMAP_TYPE = ClassHelper.makeWithoutCaching(LinkedHashMap.class, false);
    private static final ClassNode POJO_TYPE = ClassHelper.make(POJO.class);

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        compilationUnit = unit;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            boolean includeFields = memberHasValue(anno, "includeFields", Boolean.TRUE);
            boolean includeProperties = !memberHasValue(anno, "includeProperties", Boolean.FALSE);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", Boolean.TRUE);
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", Boolean.TRUE);
            boolean allProperties = memberHasValue(anno, "allProperties", Boolean.TRUE);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            boolean allNames = memberHasValue(anno, "allNames", Boolean.TRUE);
            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes, "includes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties, includeSuperFields, false))
                return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties, includeSuperFields, false))
                return;
            GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
            PropertyHandler handler = PropertyHandler.createPropertyHandler(this, classLoader, cNode);
            if (handler == null || !handler.validateAttributes(this, anno))
                return;

            Expression pre = anno.getMember("pre");
            if (pre != null && !(pre instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'pre'. Found " + pre, cNode);
                return;
            }
            Expression post = anno.getMember("post");
            if (post != null && !(post instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'post'. Found " + post, cNode);
                return;
            }

            createConstructor(this, anno, cNode, includeFields, includeProperties, includeSuperFields, includeSuperProperties,
                    excludes, includes, allNames, allProperties,
                    sourceUnit, handler, (ClosureExpression) pre, (ClosureExpression) post);

            if (pre != null) {
                anno.setMember("pre", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
            }
            if (post != null) {
                anno.setMember("post", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
            }
        }
    }

    private static void createConstructor(final AbstractASTTransformation xform, final AnnotationNode anno, final ClassNode cNode, final boolean includeFields,
                                          final boolean includeProperties, final boolean includeSuperFields, final boolean includeSuperProperties,
                                          final List<String> excludes, final List<String> includes, final boolean allNames, final boolean allProperties,
                                          final SourceUnit sourceUnit, final PropertyHandler handler, final ClosureExpression pre, final ClosureExpression post) {
        boolean namedVariant = xform.memberHasValue(anno, "namedVariant", Boolean.TRUE);
        boolean callSuper = xform.memberHasValue(anno, "callSuper", Boolean.TRUE);
        DefaultsMode defaultsMode = maybeDefaultsMode(anno, "defaultsMode");
        if (defaultsMode == null) {
            boolean defaults = anno.getMember("defaults") == null
                    || !xform.memberHasValue(anno, "defaults", Boolean.FALSE);
            defaultsMode = defaults ? ON : OFF;
        }
        boolean force = xform.memberHasValue(anno, "force", Boolean.TRUE);
        boolean makeImmutable = makeImmutable(cNode);

        // no processing if explicit constructor(s) found, unless forced or ImmutableBase is in play
        if (!force && !makeImmutable && hasExplicitConstructor(null, cNode)) return;

        boolean includePseudoGetters = false, includePseudoSetters = allProperties, skipReadOnly = true;
        Set<String> names = new HashSet<>();
        List<PropertyNode> superList;
        if (includeSuperProperties || includeSuperFields) {
            superList = getAllProperties(names, cNode.getSuperClass(), includeSuperProperties, includeSuperFields, includePseudoGetters, includePseudoSetters, /*super*/true, skipReadOnly);
        } else {
            superList = new ArrayList<>();
        }
        List<PropertyNode> list = getAllProperties(names, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, /*super*/false, skipReadOnly);

        List<Parameter> params = new ArrayList<>();
        List<Expression> superParams = new ArrayList<>();
        BlockStatement preBody = new BlockStatement();
        boolean superInPre = false;
        if (pre != null) {
            superInPre = copyStatementsWithSuperAdjustment(pre, preBody);
            if (superInPre && callSuper) {
                xform.addError("Error during " + MY_TYPE_NAME + " processing, can't have a super call in 'pre' " +
                        "closure and also 'callSuper' enabled", cNode);
            }
        }

        BlockStatement body = new BlockStatement();

        if (!handler.validateProperties(xform, body, cNode, plus(list, superList))) {
            return;
        }

        boolean specialNamedArgCase = (superList.isEmpty() && ImmutableASTTransformation.isSpecialNamedArgCase(list, defaultsMode == OFF))
                || (list.isEmpty() && ImmutableASTTransformation.isSpecialNamedArgCase(superList, defaultsMode == OFF));

        for (PropertyNode pNode : superList) {
            String name = pNode.getName();
            FieldNode fNode = pNode.getField();
            if (!shouldSkipUndefinedAware(name, excludes, includes, allNames)) {
                params.add(createParam(fNode, name, defaultsMode, xform, makeImmutable));
                if (callSuper) {
                    superParams.add(varX(name));
                } else if (!superInPre && !specialNamedArgCase) {
                    Statement propInit = handler.createPropInit(xform, anno, cNode, pNode, null);
                    if (propInit != null) {
                        body.addStatement(propInit);
                    }
                }
            }
        }
        if (callSuper) {
            body.addStatement(stmt(ctorX(ClassNode.SUPER, args(superParams))));
        }
        if (!preBody.isEmpty()) {
            body.addStatements(preBody.getStatements());
        }
        for (PropertyNode pNode : list) {
            String name = pNode.getName();
            if (shouldSkipUndefinedAware(name, excludes, includes, allNames)) continue;
            Statement propInit = handler.createPropInit(xform, anno, cNode, pNode, null);
            if (propInit != null) {
                body.addStatement(propInit);
            }
            FieldNode fNode = pNode.getField();
            Parameter param = createParam(fNode, name, defaultsMode, xform, makeImmutable);
            if (cNode.getNodeMetaData("_RECORD_HEADER") != null) {
                param.addAnnotations(pNode.getAnnotations());
                param.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
                fNode.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
            }
            params.add(param);
        }
        if (post != null) {
            body.addStatement(post.getCode());
        }

        if (includes != null) {
            params.sort(Comparator.comparingInt(p -> includes.indexOf(p.getName())));
        }

        int modifiers = getVisibility(anno, cNode, ConstructorNode.class, ACC_PUBLIC);
        Parameter[] signature = params.toArray(Parameter.EMPTY_ARRAY);
        if (cNode.getDeclaredConstructor(signature) != null) {
            if (sourceUnit != null) {
                String warning = String.format(
                    "%s specifies duplicate constructor: %s(%s)",
                    xform.getAnnotationName(), cNode.getNameWithoutPackage(),
                    params.stream().map(Parameter::getType).map(ClassNodeUtils::formatTypeName).collect(joining(",")));
                sourceUnit.addWarning(warning, anno.getLineNumber() > 0 ? anno : cNode);
            }
        } else {
            // add main tuple constructor; if any parameters have default values, then Verifier will generate the variants
            ConstructorNode tupleCtor = addGeneratedConstructor(cNode, modifiers, signature, ClassNode.EMPTY_ARRAY, body);
            if (cNode.getNodeMetaData("_RECORD_HEADER") != null) {
                tupleCtor.addAnnotations(cNode.getAnnotations());
                tupleCtor.putNodeMetaData("_SKIPPABLE_ANNOTATIONS", Boolean.TRUE);
            }
            if (namedVariant) {
                BlockStatement inner = new BlockStatement();
                Parameter mapParam = param(ClassHelper.MAP_TYPE.getPlainNodeReference(), NAMED_ARGS);
                List<Parameter> genParams = new ArrayList<>();
                genParams.add(mapParam);
                ArgumentListExpression args = new ArgumentListExpression();
                List<String> propNames = new ArrayList<>();
                Map<Parameter, Expression> seen = new HashMap<>();
                for (Parameter p : params) {
                    if (!processImplicitNamedParam(xform, tupleCtor, mapParam, inner, args, propNames, p, false, seen)) return;
                }
                NamedVariantASTTransformation.createMapVariant(xform, tupleCtor, anno, mapParam, genParams, cNode, inner, args, propNames);
            }

            if (sourceUnit != null && !body.isEmpty()) {
                new VariableScopeVisitor(sourceUnit).visitClass(cNode);
            }

            if (body.isEmpty()) { // GROOVY-8868: retain empty constructor
                body.addStatement(stmt(ConstantExpression.EMPTY_EXPRESSION));
            }
        }
        // If the first param is def or a Map, named args might not work as expected so we add a hard-coded map constructor in this case
        // we don't do it for LinkedHashMap for now (would lead to duplicate signature)
        // or if there is only one Map property (for backwards compatibility)
        // or if there is already a @MapConstructor annotation
        if (!params.isEmpty() && defaultsMode != OFF && specialNamedArgCase
                && !AnnotatedNodeUtils.hasAnnotation(cNode, MapConstructorASTTransformation.MY_TYPE)) {
            ClassNode firstParamType = params.get(0).getType();
            if (params.size() > 1 || ClassHelper.isObjectType(firstParamType)) {
                String message = "The class " + cNode.getName() + " was incorrectly initialized via the map constructor with null.";
                addSpecialMapConstructors(modifiers, cNode, message, false);
            }
        }
    }

    private static Parameter createParam(final FieldNode fNode, final String name, final DefaultsMode defaultsMode, final AbstractASTTransformation xform, final boolean makeImmutable) {
        ClassNode fType = fNode.getType();
        ClassNode type = fType.getPlainNodeReference();
        type.setGenericsTypes(fType.getGenericsTypes());
        type.setGenericsPlaceHolder(fType.isGenericsPlaceHolder());

        Expression init = fNode.getInitialExpression();
        Parameter param = new Parameter(type, name);
        switch (defaultsMode) {
          case ON:
              if (init == null || (ClassHelper.isPrimitiveType(fType) && ExpressionUtils.isNullConstant(init)))
                  init = defaultValueX(fType);
              // falls through
          case AUTO:
              if (init != null) {
                  param.setInitialExpression(init);
              }
            break;
          default:
            if (init != null && !makeImmutable) {
                xform.addError("Error during " + MY_TYPE_NAME + " processing, default value processing disabled but default value found for '" + fNode.getName() + "'", fNode);
            }
        }
        return param;
    }

    public static void addSpecialMapConstructors(final int modifiers, final ClassNode cNode, final String message, final boolean addNoArg) {
        Parameter[] parameters = params(new Parameter(LHMAP_TYPE, NAMED_ARGS));
        BlockStatement code = new BlockStatement();
        VariableExpression namedArgs = varX(NAMED_ARGS);
        namedArgs.setAccessedVariable(parameters[0]);
        code.addStatement(ifElseS(equalsNullX(namedArgs),
                throwS(ctorX(ClassHelper.make(IllegalArgumentException.class), args(constX(message)))),
                processNamedArgs(cNode, namedArgs)));
        addGeneratedConstructor(cNode, modifiers, parameters, ClassNode.EMPTY_ARRAY, code);
        // potentially add a no-arg constructor too
        if (addNoArg) {
            code = new BlockStatement();
            code.addStatement(stmt(ctorX(ClassNode.THIS, ctorX(LHMAP_TYPE))));
            addGeneratedConstructor(cNode, modifiers, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code);
        }
    }

    private static BlockStatement processNamedArgs(final ClassNode cNode, final VariableExpression namedArgs) {
        BlockStatement block = new BlockStatement();
        List<PropertyNode> props = new ArrayList<>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (pNode.isStatic()) continue;

            // if (namedArgs.containsKey(propertyName)) propertyNode = namedArgs.propertyName;
            MethodCallExpression containsProperty = callX(namedArgs, "containsKey", constX(pNode.getName()));
            containsProperty.setImplicitThis(false);
            block.addStatement(ifS(containsProperty, assignS(varX(pNode), propX(namedArgs, pNode.getName()))));
            props.add(pNode);
        }
        boolean pojo = !cNode.getAnnotations(POJO_TYPE).isEmpty();
        block.addStatement(checkPropNamesS(namedArgs, pojo, props));
        return block;
    }

    private static DefaultsMode maybeDefaultsMode(final AnnotationNode node, final String name) {
        if (node != null) {
            final Expression member = node.getMember(name);
            if (member instanceof ConstantExpression) {
                ConstantExpression ce = (ConstantExpression) member;
                if (ce.getValue() instanceof DefaultsMode) {
                    return (DefaultsMode) ce.getValue();
                }
            } else if (member instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) member;
                Expression oe = prop.getObjectExpression();
                if (oe instanceof ClassExpression) {
                    ClassExpression ce = (ClassExpression) oe;
                    if (ce.getType().getName().equals("groovy.transform.DefaultsMode")) {
                        return DefaultsMode.valueOf(prop.getPropertyAsString());
                    }
                }
            }
        }
        return null;
    }
}
