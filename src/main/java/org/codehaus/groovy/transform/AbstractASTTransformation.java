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

import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.apache.groovy.ast.tools.MethodNodeUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.BeanUtils;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static groovy.transform.Undefined.isUndefined;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFieldNames;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperNonPropertyFields;

public abstract class AbstractASTTransformation implements Opcodes, ASTTransformation, ErrorCollecting {
    public static final ClassNode RETENTION_CLASSNODE = ClassHelper.makeWithoutCaching(Retention.class);

    protected SourceUnit sourceUnit;

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported for now.
     */
    protected List<AnnotationNode> copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, String myTypeName) {
        final List<AnnotationNode> copiedAnnotations = new ArrayList<AnnotationNode>();
        final List<AnnotationNode> notCopied = new ArrayList<AnnotationNode>();
        GeneralUtils.copyAnnotatedNodeAnnotations(annotatedNode, copiedAnnotations, notCopied);
        for (AnnotationNode annotation : notCopied) {
            addError(myTypeName + " does not support keeping Closure annotation members.", annotation);
        }
        return copiedAnnotations;
    }

    /**
     * If the transform is associated with a single annotation, returns a name suitable for displaying in error messages.
     *
     * @return The simple name of the annotation including the "@" or null if no such name is defined
     */
    public String getAnnotationName() {
        return null;
    }

    protected void init(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes == null || nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + (nodes == null ? null : Arrays.asList(nodes)));
        }
        this.sourceUnit = sourceUnit;
    }

    public boolean memberHasValue(AnnotationNode node, String name, Object value) {
        final Expression member = node.getMember(name);
        return member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(value);
    }

    public Object getMemberValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member instanceof ConstantExpression) return ((ConstantExpression) member).getValue();
        return null;
    }

    public static String getMemberStringValue(AnnotationNode node, String name, String defaultValue) {
        final Expression member = node.getMember(name);
        if (member instanceof ConstantExpression) {
            Object result = ((ConstantExpression) member).getValue();
            if (result instanceof String && isUndefined((String) result)) result = null;
            if (result != null) return result.toString();
        }
        return defaultValue;
    }

    public static String getMemberStringValue(AnnotationNode node, String name) {
        return getMemberStringValue(node, name, null);
    }

    public int getMemberIntValue(AnnotationNode node, String name) {
        Object value = getMemberValue(node, name);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    public ClassNode getMemberClassValue(AnnotationNode node, String name) {
        return getMemberClassValue(node, name, null);
    }

    public ClassNode getMemberClassValue(AnnotationNode node, String name, ClassNode defaultValue) {
        final Expression member = node.getMember(name);
        if (member != null) {
            if (member instanceof ClassExpression) {
                if (!isUndefined(member.getType())) return member.getType();
            } else if (member instanceof VariableExpression) {
                addError("Error expecting to find class value for '" + name + "' but found variable: " + member.getText() + ". Missing import?", node);
                return null;
            } else if (member instanceof ConstantExpression) {
                addError("Error expecting to find class value for '" + name + "' but found constant: " + member.getText() + "!", node);
                return null;
            }
        }
        return defaultValue;
    }

    public static List<String> getMemberStringList(AnnotationNode anno, String name) {
        Expression expr = anno.getMember(name);
        if (expr == null) {
            return null;
        }
        if (expr instanceof ListExpression) {
            final ListExpression listExpression = (ListExpression) expr;
            if (isUndefinedMarkerList(listExpression)) {
                return null;
            }

            return getValueStringList(listExpression);
        }
        return tokenize(getMemberStringValue(anno, name));
    }

    private static boolean isUndefinedMarkerList(ListExpression listExpression) {
        if (listExpression.getExpressions().size() != 1) return false;
        Expression itemExpr = listExpression.getExpression(0);
        if (itemExpr == null) return false;
        if (itemExpr instanceof ConstantExpression) {
            Object value = ((ConstantExpression) itemExpr).getValue();
            if (value instanceof String && isUndefined((String)value)) return true;
        } else if (itemExpr instanceof ClassExpression && isUndefined(itemExpr.getType())) {
            return true;
        }
        return false;
    }

    private static List<String> getValueStringList(ListExpression listExpression) {
        List<String> list = new ArrayList<String>();
        for (Expression itemExpr : listExpression.getExpressions()) {
            if (itemExpr instanceof ConstantExpression) {
                Object value = ((ConstantExpression) itemExpr).getValue();
                if (value != null) list.add(value.toString());
            }
        }
        return list;
    }

    public List<ClassNode> getMemberClassList(AnnotationNode anno, String name) {
        List<ClassNode> list = new ArrayList<ClassNode>();
        Expression expr = anno.getMember(name);
        if (expr == null) {
            return null;
        }
        if (expr instanceof ListExpression) {
            final ListExpression listExpression = (ListExpression) expr;
            if (isUndefinedMarkerList(listExpression)) {
                return null;
            }
            list = getTypeList(listExpression);
        } else if (expr instanceof ClassExpression) {
            ClassNode cn = expr.getType();
            if (isUndefined(cn)) return null;
            if (cn != null) list.add(cn);
        }
        return list;
    }

    private static List<ClassNode> getTypeList(ListExpression listExpression) {
        List<ClassNode> list = new ArrayList<ClassNode>();
        for (Expression itemExpr : listExpression.getExpressions()) {
            if (itemExpr instanceof ClassExpression) {
                ClassNode cn = itemExpr.getType();
                if (cn != null) list.add(cn);
            }
        }
        return list;
    }

    public void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                        new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                                expr.getLastLineNumber(), expr.getLastColumnNumber()),
                        sourceUnit)
        );
    }

    protected boolean checkNotInterface(ClassNode cNode, String annotationName) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() + "'. " +
                    annotationName + " not allowed for interfaces.", cNode);
            return false;
        }
        return true;
    }

    public boolean hasAnnotation(ClassNode node, ClassNode annotation) {
        return AnnotatedNodeUtils.hasAnnotation(node, annotation);
    }

    public static List<String> tokenize(String rawExcludes) {
        return rawExcludes == null ? new ArrayList<String>() : StringGroovyMethods.tokenize(rawExcludes, ", ");
    }

    public static boolean deemedInternalName(String name) {
        return name.contains("$");
    }

    public static boolean shouldSkipUndefinedAware(String name, List<String> excludes, List<String> includes) {
        return shouldSkipUndefinedAware(name, excludes, includes, false);
    }

    public static boolean shouldSkipUndefinedAware(String name, List<String> excludes, List<String> includes, boolean allNames) {
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternalName(name)) ||
            (includes != null && !includes.contains(name));
    }

    public static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return shouldSkip(name, excludes, includes, false);
    }

    public static boolean shouldSkip(String name, List<String> excludes, List<String> includes, boolean allNames) {
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternalName(name)) ||
            (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    public static boolean shouldSkipOnDescriptorUndefinedAware(boolean checkReturn, Map genericsSpec, MethodNode mNode,
                                                  List<ClassNode> excludeTypes, List<ClassNode> includeTypes) {
        String descriptor = mNode.getTypeDescriptor();
        String descriptorNoReturn = MethodNodeUtils.methodDescriptorWithoutReturnType(mNode);
        if (excludeTypes != null) {
            for (ClassNode cn : excludeTypes) {
                List<ClassNode> remaining = new LinkedList<ClassNode>();
                remaining.add(cn);
                Map updatedGenericsSpec = new HashMap(genericsSpec);
                while (!remaining.isEmpty()) {
                    ClassNode next = remaining.remove(0);
                    if (!next.equals(ClassHelper.OBJECT_TYPE)) {
                        updatedGenericsSpec = GenericsUtils.createGenericsSpec(next, updatedGenericsSpec);
                        for (MethodNode mn : next.getMethods()) {
                            MethodNode correctedMethodNode = GenericsUtils.correctToGenericsSpec(updatedGenericsSpec, mn);
                            if (checkReturn) {
                                String md = correctedMethodNode.getTypeDescriptor();
                                if (md.equals(descriptor)) return true;
                            } else {
                                String md = MethodNodeUtils.methodDescriptorWithoutReturnType(correctedMethodNode);
                                if (md.equals(descriptorNoReturn)) return true;
                            }
                        }
                        remaining.addAll(Arrays.asList(next.getInterfaces()));
                    }
                }
            }
        }
        if (includeTypes == null) return false;
        for (ClassNode cn : includeTypes) {
            List<ClassNode> remaining = new LinkedList<ClassNode>();
            remaining.add(cn);
            Map updatedGenericsSpec = new HashMap(genericsSpec);
            while (!remaining.isEmpty()) {
                ClassNode next = remaining.remove(0);
                if (!next.equals(ClassHelper.OBJECT_TYPE)) {
                    updatedGenericsSpec = GenericsUtils.createGenericsSpec(next, updatedGenericsSpec);
                    for (MethodNode mn : next.getMethods()) {
                        MethodNode correctedMethodNode = GenericsUtils.correctToGenericsSpec(updatedGenericsSpec, mn);
                        if (checkReturn) {
                            String md = correctedMethodNode.getTypeDescriptor();
                            if (md.equals(descriptor)) return false;
                        } else {
                            String md = MethodNodeUtils.methodDescriptorWithoutReturnType(correctedMethodNode);
                            if (md.equals(descriptorNoReturn)) return false;
                        }
                    }
                    remaining.addAll(Arrays.asList(next.getInterfaces()));
                }
            }
        }
        return true;
    }

    protected boolean checkIncludeExcludeUndefinedAware(AnnotationNode node, List<String> excludes, List<String> includes, String typeName) {
        if (includes != null && excludes != null && !excludes.isEmpty()) {
            addError("Error during " + typeName + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", node);
            return false;
        }
        return true;
    }

    protected void checkIncludeExcludeUndefinedAware(AnnotationNode node, List<String> excludes, List<String> includes,
                                        List<ClassNode> excludeTypes, List<ClassNode> includeTypes, String typeName) {
        int found = 0;
        if (includes != null) found++;
        if (excludes != null && !excludes.isEmpty()) found++;
        if (includeTypes != null) found++;
        if (excludeTypes != null && !excludeTypes.isEmpty()) found++;
        if (found > 1) {
            addError("Error during " + typeName + " processing: Only one of 'includes', 'excludes', 'includeTypes' and 'excludeTypes' should be supplied.", node);
        }
    }

    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields) {
        return checkPropertyList(cNode, propertyNameList, listName, anno, typeName, includeFields, false, false);
    }

    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields, boolean includeSuperProperties, boolean allProperties) {
        return checkPropertyList(cNode, propertyNameList, listName, anno, typeName, includeFields, includeSuperProperties, allProperties, false, false);
    }

    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields, boolean includeSuperProperties, boolean allProperties, boolean includeSuperFields, boolean includeStatic) {
        if (propertyNameList == null || propertyNameList.isEmpty()) {
            return true;
        }
        final List<String> pNames = new ArrayList<String>();
        for (PropertyNode pNode : BeanUtils.getAllProperties(cNode, includeSuperProperties, includeStatic, allProperties)) {
            pNames.add(pNode.getField().getName());
        }
        boolean result = true;
        if (includeFields || includeSuperFields) {
            final List<String> fNames = new ArrayList<String>();
            if (includeFields) {
                fNames.addAll(getInstanceNonPropertyFieldNames(cNode));
            }
            if (includeSuperFields) {
                List<FieldNode> superNonPropertyFields = getSuperNonPropertyFields(cNode.getSuperClass());
                for (FieldNode fn : superNonPropertyFields) {
                    fNames.add(fn.getName());
                }
            }
            for (String pName : propertyNameList) {
                if (!pNames.contains(pName) && !fNames.contains(pName)) {
                    addError("Error during " + typeName + " processing: '" + listName + "' property or field '" + pName + "' does not exist.", anno);
                    result = false;
                }
            }
        } else {
            for (String pName : propertyNameList) {
                if (!pNames.contains(pName)) {
                    addError("Error during " + typeName + " processing: '" + listName + "' property '" + pName + "' does not exist.", anno);
                    result = false;
                }
            }
        }
        return result;
    }
}
