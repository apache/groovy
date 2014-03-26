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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.GeneratedClosure;
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

public abstract class AbstractASTTransformation implements Opcodes, ASTTransformation {
    public static final ClassNode RETENTION_CLASSNODE = ClassHelper.makeWithoutCaching(Retention.class);

    private SourceUnit sourceUnit;

    protected void init(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }
        this.sourceUnit = sourceUnit;
    }

    protected boolean memberHasValue(AnnotationNode node, String name, Object value) {
        final Expression member = node.getMember(name);
        return member != null && member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(value);
    }

    protected Object getMemberValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member != null && member instanceof ConstantExpression) return ((ConstantExpression) member).getValue();
        return null;
    }

    protected String getMemberStringValue(AnnotationNode node, String name, String defaultValue) {
        final Expression member = node.getMember(name);
        if (member != null && member instanceof ConstantExpression) {
            Object result = ((ConstantExpression) member).getValue();
            if (result != null) return result.toString();
        }
        return defaultValue;
    }

    protected String getMemberStringValue(AnnotationNode node, String name) {
        return getMemberStringValue(node, name, null);
    }

    protected List<String> getMemberList(AnnotationNode anno, String name) {
        List<String> list;
        Expression expr = anno.getMember(name);
        if (expr != null && expr instanceof ListExpression) {
            list = new ArrayList<String>();
            final ListExpression listExpression = (ListExpression) expr;
            for (Expression itemExpr : listExpression.getExpressions()) {
                if (itemExpr != null && itemExpr instanceof ConstantExpression) {
                    Object value = ((ConstantExpression) itemExpr).getValue();
                    if (value != null) list.add(value.toString());
                }
            }
        } else {
            list = tokenize(getMemberStringValue(anno, name));
        }
        return list;
    }

    protected List<ClassNode> getClassList(AnnotationNode anno, String name) {
        List<ClassNode> list = new ArrayList<ClassNode>();
        Expression expr = anno.getMember(name);
        if (expr != null && expr instanceof ListExpression) {
            final ListExpression listExpression = (ListExpression) expr;
            for (Expression itemExpr : listExpression.getExpressions()) {
                if (itemExpr != null && itemExpr instanceof ClassExpression) {
                    ClassNode cn = itemExpr.getType();
                    if (cn != null) list.add(cn);
                }
            }
        } else if (expr != null && expr instanceof ClassExpression) {
            ClassNode cn = expr.getType();
            if (cn != null) list.add(cn);
        }
        return list;
    }

    protected void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()),
                sourceUnit)
        );
    }

    protected void checkNotInterface(ClassNode cNode, String annotationName) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() + "'. " +
                    annotationName + " not allowed for interfaces.", cNode);
        }
    }

    protected boolean hasAnnotation(ClassNode cNode, ClassNode annotation) {
        List annots = cNode.getAnnotations(annotation);
        return (annots != null && annots.size() > 0);
    }

    protected List<String> tokenize(String rawExcludes) {
        return rawExcludes == null ? new ArrayList<String>() : StringGroovyMethods.tokenize(rawExcludes, ", ");
    }

    public static boolean deemedInternalName(String name) {
        return name.contains("$");
    }

    public static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return (excludes != null && excludes.contains(name)) || deemedInternalName(name) || (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    public static boolean shouldSkipOnDescriptor(Map genericsSpec, String descriptor, List<ClassNode> excludeTypes, List<ClassNode> includeTypes) {
            for (ClassNode cn : excludeTypes) {
                List<ClassNode> remaining = new LinkedList<ClassNode>();
                remaining.add(cn);
                Map updatedGenericsSpec = new HashMap(genericsSpec);
                while (!remaining.isEmpty()) {
                    ClassNode next = remaining.remove(0);
                    if (!next.equals(ClassHelper.OBJECT_TYPE)) {
                        updatedGenericsSpec = Verifier.createGenericsSpec(next, updatedGenericsSpec);
                        for (MethodNode mn : next.getMethods()) {
                            String md = correctToGenericsSpec(updatedGenericsSpec, mn).getTypeDescriptor();
                            if (md.equals(descriptor)) return true;
                        }
                        remaining.addAll(Arrays.asList(next.getInterfaces()));
                    }
                }
            }
            if (includeTypes.isEmpty()) return false;
            for (ClassNode cn : includeTypes) {
                List<ClassNode> remaining = new LinkedList<ClassNode>();
                remaining.add(cn);
                Map updatedGenericsSpec = new HashMap(genericsSpec);
                while (!remaining.isEmpty()) {
                    ClassNode next = remaining.remove(0);
                    if (!next.equals(ClassHelper.OBJECT_TYPE)) {
                        updatedGenericsSpec = Verifier.createGenericsSpec(next, updatedGenericsSpec);
                        for (MethodNode mn : next.getMethods()) {
                            String md = correctToGenericsSpec(updatedGenericsSpec, mn).getTypeDescriptor();
                            if (md.equals(descriptor)) return false;
                        }
                        remaining.addAll(Arrays.asList(next.getInterfaces()));
                    }
                }
            }
            return true;
    }

    protected void checkIncludeExclude(AnnotationNode node, List<String> excludes, List<String> includes, String typeName) {
        if (includes != null && !includes.isEmpty() && excludes != null && !excludes.isEmpty()) {
            addError("Error during " + typeName + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", node);
        }
    }

    protected void checkIncludeExclude(AnnotationNode node, List<String> excludes, List<String> includes, List<ClassNode> excludeTypes, List<ClassNode> includeTypes, String typeName) {
        int found = 0;
        if (includes != null && !includes.isEmpty()) found++;
        if (excludes != null && !excludes.isEmpty()) found++;
        if (includeTypes != null && !includeTypes.isEmpty()) found++;
        if (excludeTypes != null && !excludeTypes.isEmpty()) found++;
        if (found > 1) {
            addError("Error during " + typeName + " processing: Only one of 'includes', 'excludes', 'includeTypes' and 'excludeTypes' should be supplied.", node);
        }
    }

    public static ClassNode nonGeneric(ClassNode type) {
        if (type.isUsingGenerics()) {
            final ClassNode nonGen = ClassHelper.makeWithoutCaching(type.getName());
            nonGen.setRedirect(type);
            nonGen.setGenericsTypes(null);
            nonGen.setUsingGenerics(false);
            return nonGen;
        }
        if (type.isArray() && type.getComponentType().isUsingGenerics()) {
            return type.getComponentType().getPlainNodeReference().makeArray();
        }
        return type;
    }

    public static ClassNode newClass(ClassNode type) {
        return type.getPlainNodeReference();
    }

    public static ClassNode makeClassSafe0(ClassNode type, GenericsType... genericTypes) {
        ClassNode plainNodeReference = newClass(type);
        if (genericTypes != null && genericTypes.length > 0) plainNodeReference.setGenericsTypes(genericTypes);
        return plainNodeReference;
    }


    public static ClassNode makeClassSafeWithGenerics(ClassNode type, GenericsType... genericTypes) {
        if (type.isArray()) {
            return makeClassSafeWithGenerics(type.getComponentType(), genericTypes).makeArray();
        }
        GenericsType[] gtypes = new GenericsType[0];
        if (genericTypes != null) {
            gtypes = new GenericsType[genericTypes.length];
            System.arraycopy(genericTypes, 0, gtypes, 0, gtypes.length);
        }
        return makeClassSafe0(type, gtypes);
    }

    public static MethodNode correctToGenericsSpec(Map genericsSpec, MethodNode mn) {
        ClassNode correctedType = correctToGenericsSpecRecurse(genericsSpec, mn.getReturnType());
        Parameter[] origParameters = mn.getParameters();
        Parameter[] newParameters = new Parameter[origParameters.length];
        for (int i = 0; i < origParameters.length; i++) {
            Parameter origParameter = origParameters[i];
            newParameters[i] = new Parameter(correctToGenericsSpecRecurse(genericsSpec, origParameter.getType()), origParameter.getName(), origParameter.getInitialExpression());
        }
        return new MethodNode(mn.getName(), mn.getModifiers(), correctedType, newParameters, mn.getExceptions(), mn.getCode());
    }

    public static ClassNode correctToGenericsSpecRecurse(Map genericsSpec, ClassNode type) {
        if (type.isGenericsPlaceHolder()) {
            String name = type.getGenericsTypes()[0].getName();
            type = (ClassNode) genericsSpec.get(name);
        }
        if (type == null) type = ClassHelper.OBJECT_TYPE;
        GenericsType[] oldgTypes = type.getGenericsTypes();
        GenericsType[] newgTypes = new GenericsType[0];
        if (oldgTypes != null) {
            newgTypes = new GenericsType[oldgTypes.length];
            for (int i = 0; i < newgTypes.length; i++) {
                GenericsType oldgType = oldgTypes[i];
                if (oldgType.isPlaceholder() ) {
                    if (genericsSpec.get(oldgType.getName())!=null) {
                        newgTypes[i] = new GenericsType((ClassNode) genericsSpec.get(oldgType.getName()));
                    } else {
                        newgTypes[i] = new GenericsType(ClassHelper.OBJECT_TYPE);
                    }
                } else if (oldgType.isWildcard()) {
                    ClassNode oldLower = oldgType.getLowerBound();
                    ClassNode lower = oldLower!=null?correctToGenericsSpecRecurse(genericsSpec, oldLower):null;
                    ClassNode[] oldUpper = oldgType.getUpperBounds();
                    ClassNode[] upper = null;
                    if (oldUpper!=null) {
                        upper = new ClassNode[oldUpper.length];
                        for (int j = 0; j < oldUpper.length; j++) {
                            upper[j] = correctToGenericsSpecRecurse(genericsSpec,oldUpper[j]);
                        }
                    }
                    GenericsType fixed = new GenericsType(oldgType.getType(), upper, lower);
                    fixed.setWildcard(true);
                    newgTypes[i] = fixed;
                } else {
                    newgTypes[i] = new GenericsType(Verifier.correctToGenericsSpec(genericsSpec, oldgType));
                }
            }
        }
        return makeClassSafeWithGenerics(type, newgTypes);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported by now.
     */
    public static void copyAnnotatedNodeAnnotations(final List<AnnotationNode> candidateAnnotations, final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, List<AnnotationNode> notCopied) {
        for (AnnotationNode annotation : candidateAnnotations)  {

            List<AnnotationNode> annotations = annotation.getClassNode().getAnnotations(RETENTION_CLASSNODE);
            if (annotations.isEmpty()) continue;

            if (hasClosureMember(annotation)) {
                notCopied.add(annotation);
                continue;
            }

            AnnotationNode retentionPolicyAnnotation = annotations.get(0);
            Expression valueExpression = retentionPolicyAnnotation.getMember("value");
            if (!(valueExpression instanceof PropertyExpression)) continue;

            PropertyExpression propertyExpression = (PropertyExpression) valueExpression;
            boolean processAnnotation =
                    propertyExpression.getProperty() instanceof ConstantExpression &&
                            (
                                    "RUNTIME".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue()) ||
                                    "CLASS".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue())
                            );

            if (processAnnotation)  {
                AnnotationNode newAnnotation = new AnnotationNode(annotation.getClassNode());
                for (Map.Entry<String, Expression> member : annotation.getMembers().entrySet())  {
                    newAnnotation.addMember(member.getKey(), member.getValue());
                }
                newAnnotation.setSourcePosition(annotatedNode);

                copied.add(newAnnotation);
            }
        }
    }

    private static boolean hasClosureMember(AnnotationNode annotation) {

        Map<String, Expression> members = annotation.getMembers();
        for (Map.Entry<String, Expression> member : members.entrySet())  {
            if (member.getValue() instanceof ClosureExpression) return true;

            if (member.getValue() instanceof ClassExpression)  {
                ClassExpression classExpression = (ClassExpression) member.getValue();
                Class<?> typeClass = classExpression.getType().isResolved() ? classExpression.getType().redirect().getTypeClass() : null;
                if (typeClass != null && GeneratedClosure.class.isAssignableFrom(typeClass)) return true;
            }
        }

        return false;
    }
}
