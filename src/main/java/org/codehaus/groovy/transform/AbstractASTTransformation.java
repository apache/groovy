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
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static groovy.transform.Undefined.isUndefined;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFieldNames;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperNonPropertyFields;

/**
 * Abstract base class for AST transformations.
 * <p>
 * Provides utility methods for extracting annotation members, validating properties/fields,
 * and filtering members based on include/exclude specifications.
 */
public abstract class AbstractASTTransformation implements ASTTransformation, ErrorCollecting {
    /**
     * A shared ClassNode representing the {@link java.lang.annotation.Retention} annotation.
     * Used for checking retention policy of annotations during AST transformation.
     */
    public static final ClassNode RETENTION_CLASSNODE = ClassHelper.makeWithoutCaching(Retention.class);

    /**
     * The source unit associated with the compilation context.
     * Provides access to error collection and source file information during AST transformation.
     */
    protected SourceUnit sourceUnit;

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported for now.
     */
    protected List<AnnotationNode> copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, String myTypeName) {
        return copyAnnotatedNodeAnnotations(annotatedNode, myTypeName, true);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported for now.
     */
    protected List<AnnotationNode> copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, String myTypeName, boolean includeGenerated) {
        final List<AnnotationNode> copiedAnnotations = new ArrayList<>();
        final List<AnnotationNode> notCopied = new ArrayList<>();
        GeneralUtils.copyAnnotatedNodeAnnotations(annotatedNode, copiedAnnotations, notCopied, includeGenerated);
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

    /**
     * Initializes the transformation with the annotation and source unit context.
     * This method must be called before the transformation processes any AST nodes.
     *
     * @param nodes An array containing exactly 2 elements: the first must be an AnnotationNode
     *              specifying the annotation driving the transformation, and the second must be
     *              an AnnotatedNode to which the annotation is applied.
     * @param sourceUnit The source compilation unit providing error collection and source info
     * @throws GroovyBugError if nodes is null, has wrong size, or wrong element types
     */
    protected void init(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes == null || nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + (nodes == null ? null : Arrays.asList(nodes)));
        }
        this.sourceUnit = sourceUnit;
    }

    /**
     * Checks whether an annotation member has a specific value.
     *
     * @param node the annotation node to check
     * @param name the name of the annotation member
     * @param value the expected value to match against
     * @return true if the member exists and is a ConstantExpression with the given value
     */
    public boolean memberHasValue(AnnotationNode node, String name, Object value) {
        final Expression member = node.getMember(name);
        return member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(value);
    }

    /**
     * Retrieves the value of an annotation member that is a constant expression.
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @return the constant value of the member, or null if the member doesn't exist or is not a ConstantExpression
     */
    public Object getMemberValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member instanceof ConstantExpression) return ((ConstantExpression) member).getValue();
        return null;
    }

    /**
     * Retrieves the string value of an annotation member with a default fallback.
     * Returns the default value if the member is undefined (see {@link groovy.transform.Undefined}).
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @param defaultValue the value to return if the member is missing or undefined
     * @return the string value of the member, or the default value if the member is undefined or not a ConstantExpression
     */
    public static String getMemberStringValue(AnnotationNode node, String name, String defaultValue) {
        final Expression member = node.getMember(name);
        if (member instanceof ConstantExpression) {
            Object result = ((ConstantExpression) member).getValue();
            if (result instanceof String && isUndefined((String) result)) result = null;
            if (result != null) return result.toString();
        }
        return defaultValue;
    }

    /**
     * Retrieves the string value of an annotation member.
     * This is a convenience overload that calls {@link #getMemberStringValue(AnnotationNode, String, String)}
     * with null as the default value.
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @return the string value of the member, or null if the member is missing or undefined
     */
    public static String getMemberStringValue(AnnotationNode node, String name) {
        return getMemberStringValue(node, name, null);
    }

    /**
     * Retrieves the integer value of an annotation member.
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @return the integer value if the member is a ConstantExpression holding an Integer, otherwise 0
     */
    public int getMemberIntValue(AnnotationNode node, String name) {
        Object value = getMemberValue(node, name);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    /**
     * Retrieves the ClassNode value of an annotation member.
     * This is a convenience overload that calls {@link #getMemberClassValue(AnnotationNode, String, ClassNode)}
     * with null as the default value.
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @return the ClassNode value if the member is a ClassExpression with a defined type, otherwise null
     */
    public ClassNode getMemberClassValue(AnnotationNode node, String name) {
        return getMemberClassValue(node, name, null);
    }

    /**
     * Retrieves the ClassNode value of an annotation member with a default fallback.
     * Validates that the member is a ClassExpression and generates errors for invalid expression types
     * (VariableExpression or ConstantExpression).
     *
     * @param node the annotation node to query
     * @param name the name of the annotation member
     * @param defaultValue the value to return if the member is missing or not a valid ClassExpression
     * @return the ClassNode value if the member is a ClassExpression with a defined type, otherwise the default value
     */
    public ClassNode getMemberClassValue(AnnotationNode node, String name, ClassNode defaultValue) {
        final Expression member = node.getMember(name);
        if (member != null) {
            if (member instanceof ClassExpression) {
                if (!isUndefined(member.getType())) return member.getType();
            } else if (member instanceof VariableExpression) {
                addError("Expecting to find a class value for '" + name + "' but found variable: " + member.getText() + ". Missing import?", node);
                return null;
            } else if (member instanceof ConstantExpression) {
                addError("Expecting to find a class value for '" + name + "' but found constant: " + member.getText() + "!", node);
                return null;
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves a list of string values from a list-type annotation member.
     * Handles both ListExpression members and comma/space-separated string values.
     * Returns null if the member is the undefined marker.
     *
     * @param anno the annotation node to query
     * @param name the name of the annotation member
     * @return a list of strings if the member contains string values, or null if the member is missing or undefined
     */
    public static List<String> getMemberStringList(AnnotationNode anno, String name) {
        Expression expr = anno.getMember(name);
        if (expr == null) {
            return null;
        }
        if (expr instanceof ListExpression listExpression) {
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
        List<String> list = new ArrayList<>();
        for (Expression itemExpr : listExpression.getExpressions()) {
            if (itemExpr instanceof ConstantExpression) {
                Object value = ((ConstantExpression) itemExpr).getValue();
                if (value != null) list.add(value.toString());
            }
        }
        return list;
    }

    /**
     * Retrieves a list of ClassNode values from a list-type annotation member.
     * Handles both single ClassExpression members and ListExpression members.
     * Generates errors for invalid expression types and returns null if the member is undefined.
     *
     * @param anno the annotation node to query
     * @param name the name of the annotation member
     * @return a list of ClassNode values if the member contains class values, or null if missing or undefined
     */
    public List<ClassNode> getMemberClassList(AnnotationNode anno, String name) {
        List<ClassNode> list = new ArrayList<>();
        Expression expr = anno.getMember(name);
        if (expr == null) {
            return null;
        }
        if (expr instanceof ListExpression listExpression) {
            if (isUndefinedMarkerList(listExpression)) {
                return null;
            }
            list = getTypeList(anno, name, listExpression);
        } else if (expr instanceof ClassExpression) {
            ClassNode cn = expr.getType();
            if (isUndefined(cn)) return null;
            if (cn != null) list.add(cn);
        } else if (expr instanceof VariableExpression) {
            addError("Expecting to find a class value for '" + name + "' but found variable: " + expr.getText() + ". Missing import or unknown class?", anno);
        } else if (expr instanceof ConstantExpression) {
            addError("Expecting to find a class value for '" + name + "' but found constant: " + expr.getText() + "!", anno);
        }
        return list;
    }

    private List<ClassNode> getTypeList(AnnotationNode anno, String name, ListExpression listExpression) {
        List<ClassNode> list = new ArrayList<>();
        for (Expression itemExpr : listExpression.getExpressions()) {
            if (itemExpr instanceof ClassExpression) {
                ClassNode cn = itemExpr.getType();
                if (cn != null) list.add(cn);
            } else if (itemExpr instanceof VariableExpression) {
                addError("Expecting a list of class values for '" + name + "' but found variable: " + itemExpr.getText() + ". Missing import or unknown class?", anno);
            } else if (itemExpr instanceof ConstantExpression) {
                addError("Expecting a list of class values for '" + name + "' but found constant: " + itemExpr.getText() + "!", anno);
            }
        }
        return list;
    }

    /**
     * Records a transformation error message associated with an AST node.
     * The error is collected and reported at the end of compilation.
     * This method implements {@link ErrorCollecting#addError(String, ASTNode)}.
     *
     * @param msg the error message to report
     * @param node the AST node associated with the error for source location tracking
     */
    @Override
    public void addError(String msg, ASTNode node) {
        sourceUnit.getErrorCollector().addErrorAndContinue(msg + '\n', node, sourceUnit);
    }

    /**
     * Validates that the target class node is not an interface.
     * Generates an error if the target is an interface since transformation cannot be applied.
     *
     * @param cNode the class node to validate
     * @param annotationName the name of the annotation for error reporting
     * @return true if the class is not an interface, false otherwise
     */
    protected boolean checkNotInterface(ClassNode cNode, String annotationName) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() + "'. " +
                    annotationName + " not allowed for interfaces.", cNode);
            return false;
        }
        return true;
    }

    /**
     * Checks whether a class node has a specific annotation.
     *
     * @param node the class node to check
     * @param annotation the annotation ClassNode to look for
     * @return true if the node is annotated with the given annotation
     */
    public boolean hasAnnotation(ClassNode node, ClassNode annotation) {
        return AnnotatedNodeUtils.hasAnnotation(node, annotation);
    }

    /**
     * Tokenizes a string into a list of individual tokens using comma and space as delimiters.
     *
     * @param rawExcludes a comma or space-separated string, or null
     * @return a list of tokens, or an empty list if the input is null
     */
    public static List<String> tokenize(String rawExcludes) {
        return rawExcludes == null ? new ArrayList<>() : StringGroovyMethods.tokenize(rawExcludes, ", ");
    }

    /**
     * @see org.apache.groovy.ast.tools.AnnotatedNodeUtils#markAsInternal(AnnotatedNode)
     * @since 6.0.0
     */
    public static void markAsInternal(AnnotatedNode node) {
        AnnotatedNodeUtils.markAsInternal(node);
    }

    /**
     * Determines whether a name is considered internal and should typically be excluded.
     * A name is considered internal if it contains a dollar sign ('$'),
     * which is commonly used for generated member names.
     *
     * @param name the name to check
     * @return true if the name contains a dollar sign, false otherwise
     */
    public static boolean deemedInternalName(String name) {
        return name.contains("$");
    }

    /**
     * @see org.apache.groovy.ast.tools.AnnotatedNodeUtils#deemedInternal(AnnotatedNode)
     * @since 6.0.0
     */
    public static boolean deemedInternal(AnnotatedNode node) {
        return AnnotatedNodeUtils.deemedInternal(node);
    }

    /**
     * Determines whether a name should be skipped based on include/exclude filters
     * and the undefined marker semantics.
     * This method provides a convenient overload that calls {@link #shouldSkipUndefinedAware(String, List, List, boolean)}
     * with allNames set to false.
     *
     * @param name the name to check
     * @param excludes a list of names to exclude, or null
     * @param includes a list of names to include, or null
     * @return true if the name should be skipped (in excludes, internal, or not in includes when includes is specified)
     */
    public static boolean shouldSkipUndefinedAware(String name, List<String> excludes, List<String> includes) {
        return shouldSkipUndefinedAware(name, excludes, includes, false);
    }

    /**
     * Determines whether a name should be skipped based on include/exclude filters
     * with support for the undefined marker semantics.
     *
     * @param name the name to check
     * @param excludes a list of names to exclude, or null
     * @param includes a list of names to include, or null
     * @param allNames if false, also skips names that are deemed internal (containing '$');
     *                 if true, only respects explicit include/exclude lists
     * @return true if the name should be skipped
     */
    public static boolean shouldSkipUndefinedAware(String name, List<String> excludes, List<String> includes, boolean allNames) {
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternalName(name)) ||
            (includes != null && !includes.contains(name));
    }

    /**
     * Variant that checks both the name and {@link groovy.transform.Internal @Internal} annotation.
     *
     * @since 6.0.0
     */
    public static boolean shouldSkipUndefinedAware(AnnotatedNode node, List<String> excludes, List<String> includes, boolean allNames) {
        String name = nodeName(node);
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternal(node)) ||
            (includes != null && !includes.contains(name));
    }

    /**
     * Determines whether a name should be skipped based on include/exclude filters.
     * This method provides a convenient overload that calls {@link #shouldSkip(String, List, List, boolean)}
     * with allNames set to false.
     *
     * @param name the name to check
     * @param excludes a list of names to exclude, or null
     * @param includes a list of names to include, or null (if non-empty and name not in it, name is skipped)
     * @return true if the name should be skipped (in excludes, internal, or not in includes when includes is non-empty)
     */
    public static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return shouldSkip(name, excludes, includes, false);
    }

    /**
     * Determines whether a name should be skipped based on include/exclude filters.
     *
     * @param name the name to check
     * @param excludes a list of names to exclude, or null
     * @param includes a list of names to include, or null (if non-empty and name not in it, name is skipped)
     * @param allNames if false, also skips names that are deemed internal (containing '$');
     *                 if true, only respects explicit include/exclude lists
     * @return true if the name should be skipped
     */
    public static boolean shouldSkip(String name, List<String> excludes, List<String> includes, boolean allNames) {
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternalName(name)) ||
            (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    /**
     * Variant that checks both the name and {@link groovy.transform.Internal @Internal} annotation.
     *
     * @since 6.0.0
     */
    public static boolean shouldSkip(AnnotatedNode node, List<String> excludes, List<String> includes, boolean allNames) {
        String name = nodeName(node);
        return (excludes != null && excludes.contains(name)) ||
            (!allNames && deemedInternal(node)) ||
            (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    private static String nodeName(AnnotatedNode node) {
        if (node instanceof FieldNode fn) return fn.getName();
        if (node instanceof PropertyNode pn) return pn.getName();
        if (node instanceof MethodNode mn) return mn.getName();
        return "";
    }

    /**
     * Determines whether a method should be skipped based on method descriptor matching
     * against include/exclude type lists.
     * Allows filtering methods by their signature and return type for selective processing
     * during transformations that handle multiple methods.
     *
     * @param checkReturn if true, compares full method descriptors including return type;
     *                    if false, compares only parameter descriptors excluding return type
     * @param genericsSpec a map of generic type specifications for resolving generic method signatures
     * @param mNode the method node to evaluate
     * @param excludeTypes a list of ClassNodes specifying methods to exclude, or null
     * @param includeTypes a list of ClassNodes specifying methods to include, or null
     * @return true if the method should be skipped (in excludeTypes or not in includeTypes when includeTypes is specified)
     */
    public static boolean shouldSkipOnDescriptorUndefinedAware(boolean checkReturn, Map genericsSpec, MethodNode mNode,
                                                               List<ClassNode> excludeTypes, List<ClassNode> includeTypes) {
        String descriptor = mNode.getTypeDescriptor();
        String descriptorNoReturn = MethodNodeUtils.methodDescriptorWithoutReturnType(mNode);

        // Check excludes first - if any match is found, we should skip
        if (excludeTypes != null && hasMatchingMethodDescriptor(checkReturn, genericsSpec, descriptor, descriptorNoReturn, excludeTypes)) {
            return true;
        }

        // Then check includes - if any defined but none match, we should skip
        if (includeTypes != null && !hasMatchingMethodDescriptor(checkReturn, genericsSpec, descriptor, descriptorNoReturn, includeTypes)) {
            return true;
        }

        return false;
    }

    private static boolean hasMatchingMethodDescriptor(boolean checkReturn, Map genericsSpec, String descriptor,
                                                       String descriptorNoReturn, List<ClassNode> types) {
        for (ClassNode cn : types) {
            List<ClassNode> remaining = new LinkedList<>();
            remaining.add(cn);
            Map updatedGenericsSpec = new HashMap(genericsSpec);

            while (!remaining.isEmpty()) {
                ClassNode next = remaining.remove(0);
                if (!isObjectType(next)) {
                    updatedGenericsSpec = GenericsUtils.createGenericsSpec(next, updatedGenericsSpec);
                    for (MethodNode mn : next.getMethods()) {
                        MethodNode correctedMethodNode = GenericsUtils.correctToGenericsSpec(updatedGenericsSpec, mn);
                        String md;
                        String compareTo;

                        if (checkReturn) {
                            md = correctedMethodNode.getTypeDescriptor();
                            compareTo = descriptor;
                        } else {
                            md = MethodNodeUtils.methodDescriptorWithoutReturnType(correctedMethodNode);
                            compareTo = descriptorNoReturn;
                        }

                        if (md.equals(compareTo)) {
                            return true; // Found a match
                        }
                    }
                    remaining.addAll(Arrays.asList(next.getInterfaces()));
                }
            }
        }

        return false; // No match found
    }

    /**
     * Validates that only one of 'includes' or 'excludes' is specified, not both.
     * Generates an error if both are provided.
     *
     * @param node the annotation node associated with this check for error reporting
     * @param excludes the excludes list, or null
     * @param includes the includes list, or null
     * @param typeName the transformation type name for error reporting
     * @return false if both includes and excludes are non-null/non-empty, true otherwise
     */
    protected boolean checkIncludeExcludeUndefinedAware(AnnotationNode node, List<String> excludes, List<String> includes, String typeName) {
        if (includes != null && excludes != null && !excludes.isEmpty()) {
            addError("Error during " + typeName + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", node);
            return false;
        }
        return true;
    }

    /**
     * Validates that only one filtering list is specified among 'includes', 'excludes', 'includeTypes', and 'excludeTypes'.
     * Generates an error if more than one filtering specification is provided.
     *
     * @param node the annotation node associated with this check for error reporting
     * @param excludes the string excludes list, or null
     * @param includes the string includes list, or null
     * @param excludeTypes the type excludes list, or null
     * @param includeTypes the type includes list, or null
     * @param typeName the transformation type name for error reporting
     */
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

    /**
     * Validates that all property names in the given list exist in the target class.
     * This is a convenience overload that calls {@link #checkPropertyList(ClassNode, List, String, AnnotationNode, String, boolean, boolean, boolean, boolean, boolean)}
     * with defaults: includeSuperProperties=false, allProperties=false, includeSuperFields=false, includeStatic=false.
     *
     * @param cNode the class node to validate properties against
     * @param propertyNameList the list of property names to verify, or null
     * @param listName the name of the list (for error reporting, e.g., "includes" or "excludes")
     * @param anno the annotation node for error reporting
     * @param typeName the transformation type name for error reporting
     * @param includeFields whether to also check for matching field names
     * @return true if all properties exist or the list is null/empty, false if any property is not found
     */
    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields) {
        return checkPropertyList(cNode, propertyNameList, listName, anno, typeName, includeFields, false, false);
    }

    /**
     * Validates that all property names in the given list exist in the target class.
     * This is a convenience overload that calls {@link #checkPropertyList(ClassNode, List, String, AnnotationNode, String, boolean, boolean, boolean, boolean, boolean)}
     * with defaults: includeSuperFields=false, includeStatic=false.
     *
     * @param cNode the class node to validate properties against
     * @param propertyNameList the list of property names to verify, or null
     * @param listName the name of the list (for error reporting, e.g., "includes" or "excludes")
     * @param anno the annotation node for error reporting
     * @param typeName the transformation type name for error reporting
     * @param includeFields whether to also check for matching field names
     * @param includeSuperProperties whether to include inherited properties
     * @param allProperties whether to include all accessible properties
     * @return true if all properties exist or the list is null/empty, false if any property is not found
     */
    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields, boolean includeSuperProperties, boolean allProperties) {
        return checkPropertyList(cNode, propertyNameList, listName, anno, typeName, includeFields, includeSuperProperties, allProperties, false, false);
    }

    /**
     * Validates that all property names in the given list exist in the target class.
     * Checks both properties and optionally fields in the class and its superclasses.
     * Generates errors for any properties or fields that cannot be found.
     *
     * @param cNode the class node to validate properties against
     * @param propertyNameList the list of property names to verify, or null
     * @param listName the name of the list (for error reporting, e.g., "includes" or "excludes")
     * @param anno the annotation node for error reporting
     * @param typeName the transformation type name for error reporting
     * @param includeFields whether to also check for matching field names in the current class
     * @param includeSuperProperties whether to include inherited properties from superclasses
     * @param allProperties whether to include all accessible properties or only declared ones
     * @param includeSuperFields whether to also check superclass fields
     * @param includeStatic whether to include static properties
     * @return true if all properties exist or the list is null/empty, false if any property is not found
     */
    public boolean checkPropertyList(ClassNode cNode, List<String> propertyNameList, String listName, AnnotationNode anno, String typeName, boolean includeFields, boolean includeSuperProperties, boolean allProperties, boolean includeSuperFields, boolean includeStatic) {
        if (propertyNameList == null || propertyNameList.isEmpty()) {
            return true;
        }
        final List<String> pNames = new ArrayList<>();
        for (PropertyNode pNode : BeanUtils.getAllProperties(cNode, includeSuperProperties, includeStatic, allProperties)) {
            pNames.add(pNode.getName());
        }
        boolean result = true;
        if (includeFields || includeSuperFields) {
            final List<String> fNames = new ArrayList<>();
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
