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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.isGenerated;
import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.markAsGenerated;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Utility class for working with ClassNodes
 */
public class ClassNodeUtils {

    /**
     * Formats a type name into a human readable version. For arrays, appends "[]" to the formatted
     * type name of the component. For unit class nodes, uses the class node name.
     *
     * @param cNode the type to format
     * @return a human readable version of the type name (java.lang.String[] for example)
     */
    public static String formatTypeName(ClassNode cNode) {
        if (cNode.isArray()) {
            ClassNode it = cNode;
            int dim = 0;
            while (it.isArray()) {
                dim++;
                it = it.getComponentType();
            }
            StringBuilder sb = new StringBuilder(it.getName().length() + 2 * dim);
            sb.append(it.getName());
            for (int i = 0; i < dim; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return cNode.getName();
    }

    /**
     * Return an existing method if one exists or else create a new method and mark it as {@code @Generated}.
     *
     * @see ClassNode#addMethod(String, int, ClassNode, Parameter[], ClassNode[], Statement)
     */
    public static MethodNode addGeneratedMethod(ClassNode cNode, String name,
                                int modifiers,
                                ClassNode returnType,
                                Parameter[] parameters,
                                ClassNode[] exceptions,
                                Statement code) {
        MethodNode existing = cNode.getDeclaredMethod(name, parameters);
        if (existing != null) return existing;
        MethodNode result = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
        addGeneratedMethod(cNode, result);
        return result;
    }

    /**
     * Add a method and mark it as {@code @Generated}.
     *
     * @see ClassNode#addMethod(MethodNode)
     */
    public static void addGeneratedMethod(ClassNode cNode, MethodNode mNode) {
        cNode.addMethod(mNode);
        markAsGenerated(cNode, mNode);
    }

    /**
     * Add an inner class that is marked as {@code @Generated}.
     *
     * @see org.codehaus.groovy.ast.ModuleNode#addClass(ClassNode)
     */
    public static void addGeneratedInnerClass(ClassNode cNode, ClassNode inner) {
        cNode.getModule().addClass(inner);
        markAsGenerated(cNode, inner);
    }

    /**
     * Add a method that is marked as {@code @Generated}.
     *
     * @see ClassNode#addConstructor(int, Parameter[], ClassNode[], Statement)
     */
    public static ConstructorNode addGeneratedConstructor(ClassNode classNode, int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        ConstructorNode consNode = classNode.addConstructor(modifiers, parameters, exceptions, code);
        markAsGenerated(classNode, consNode);
        return consNode;
    }

    /**
     * Add a method that is marked as {@code @Generated}.
     *
     * @see ClassNode#addConstructor(ConstructorNode)
     */
    public static void addGeneratedConstructor(ClassNode classNode, ConstructorNode consNode) {
        classNode.addConstructor(consNode);
        markAsGenerated(classNode, consNode);
    }

    /**
     * Add methods from the super class.
     *
     * @param cNode The ClassNode
     * @return A map of methods
     */
    public static Map<String, MethodNode> getDeclaredMethodsFromSuper(ClassNode cNode) {
        ClassNode parent = cNode.getSuperClass();
        if (parent == null) {
            return new HashMap<>();
        }
        return parent.getDeclaredMethodsMap();
    }

    /**
     * Adds methods from all interfaces. Existing entries in the methods map
     * take precedence. Methods from interfaces visited early take precedence
     * over later ones.
     *
     * @param cNode The ClassNode
     * @param methodsMap A map of existing methods to alter
     */
    public static void addDeclaredMethodsFromInterfaces(ClassNode cNode, Map<String, MethodNode> methodsMap) {
        for (ClassNode iface : cNode.getInterfaces()) {
            Map<String, MethodNode> declaredMethods = iface.getDeclaredMethodsMap();
            for (Map.Entry<String, MethodNode> entry : declaredMethods.entrySet()) {
                if (entry.getValue().getDeclaringClass().isInterface() && (entry.getValue().getModifiers() & ACC_SYNTHETIC) == 0) {
                    methodsMap.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Gets methods from all interfaces. Methods from interfaces visited early
     * take precedence over later ones.
     *
     * @param cNode The ClassNode
     * @return A map of methods
     */
    public static Map<String, MethodNode> getDeclaredMethodsFromInterfaces(ClassNode cNode) {
        Map<String, MethodNode> methodsMap = new HashMap<>();
        addDeclaredMethodsFromInterfaces(cNode, methodsMap);
        return methodsMap;
    }

    /**
     * Adds methods from interfaces and parent interfaces. Existing entries in the methods map take precedence.
     * Methods from interfaces visited early take precedence over later ones.
     *
     * @param cNode The ClassNode
     * @param methodsMap A map of existing methods to alter
     */
    public static void addDeclaredMethodsFromAllInterfaces(ClassNode cNode, Map<String, MethodNode> methodsMap) {
        List<?> cnInterfaces = Arrays.asList(cNode.getInterfaces());
        ClassNode parent = cNode.getSuperClass();
        while (parent != null && !parent.equals(ClassHelper.OBJECT_TYPE)) {
            ClassNode[] interfaces = parent.getInterfaces();
            for (ClassNode iface : interfaces) {
                if (!cnInterfaces.contains(iface)) {
                    methodsMap.putAll(iface.getDeclaredMethodsMap());
                }
            }
            parent = parent.getSuperClass();
        }
    }

    /**
     * Returns true if the given method has a possibly matching static method with the given name and arguments.
     * Handles default arguments and optionally spread expressions.
     *
     * @param cNode     the ClassNode of interest
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @param trySpread whether to try to account for SpreadExpressions within the arguments
     * @return true if a matching method was found
     */
    public static boolean hasPossibleStaticMethod(ClassNode cNode, String name, Expression arguments, boolean trySpread) {
        int count = 0;
        boolean foundSpread = false;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            for (Expression arg : tuple.getExpressions()) {
                if (arg instanceof SpreadExpression) {
                    foundSpread = true;
                } else {
                    count++;
                }
            }
        } else if (arguments instanceof MapExpression) {
            count = 1;
        }

        for (MethodNode method : cNode.getMethods(name)) {
            if (method.isStatic()) {
                Parameter[] parameters = method.getParameters();
                // do fuzzy match for spread case: count will be number of non-spread args
                if (trySpread && foundSpread && parameters.length >= count) return true;

                if (parameters.length == count) return true;

                // handle varargs case
                if (parameters.length > 0 && parameters[parameters.length - 1].getType().isArray()) {
                    if (count >= parameters.length - 1) return true;
                    // fuzzy match any spread to a varargs
                    if (trySpread && foundSpread) return true;
                }

                // handle parameters with default values
                int nonDefaultParameters = 0;
                for (Parameter parameter : parameters) {
                    if (!parameter.hasInitialExpression()) {
                        nonDefaultParameters++;
                    }
                }

                if (count < parameters.length && nonDefaultParameters <= count) {
                    return true;
                }
                // TODO handle spread with nonDefaultParams?
            }
        }
        return false;
    }

    /**
     * Return true if we have a static accessor
     */
    public static boolean hasPossibleStaticProperty(ClassNode cNode, String methodName) {
        // assume explicit static method call checked first so we can assume a simple check here
        if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
            return false;
        }
        String propName = getPropNameForAccessor(methodName);
        PropertyNode pNode = getStaticProperty(cNode, propName);
        return pNode != null && (methodName.startsWith("get") || boolean_TYPE.equals(pNode.getType()));
    }

    /**
     * Returns the property name, e.g. age, given an accessor name, e.g. getAge.
     * Returns the original if a valid prefix cannot be removed.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return the property name, e.g. age, or original if not a valid property accessor name
     */
    public static String getPropNameForAccessor(String accessorName) {
        if (!isValidAccessorName(accessorName)) return accessorName;
        int prefixLength = accessorName.startsWith("is") ? 2 : 3;
        return String.valueOf(accessorName.charAt(prefixLength)).toLowerCase() + accessorName.substring(prefixLength + 1);
    }

    /**
     * Detect whether the given accessor name starts with "get", "set" or "is" followed by at least one character.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return true if a valid prefix is found
     */
    public static boolean isValidAccessorName(String accessorName) {
        if (accessorName.startsWith("get") || accessorName.startsWith("is") || accessorName.startsWith("set")) {
            int prefixLength = accessorName.startsWith("is") ? 2 : 3;
            return accessorName.length() > prefixLength;
        }
        return false;
    }

    public static boolean hasStaticProperty(ClassNode cNode, String propName) {
        return getStaticProperty(cNode, propName) != null;
    }

    /**
     * Detect whether a static property with the given name is within the class
     * or a super class.
     *
     * @param cNode the ClassNode of interest
     * @param propName the property name
     * @return the static property if found or else null
     */
    public static PropertyNode getStaticProperty(ClassNode cNode, String propName) {
        ClassNode classNode = cNode;
        while (classNode != null) {
            for (PropertyNode pn : classNode.getProperties()) {
                if (pn.getName().equals(propName) && pn.isStatic()) return pn;
            }
            classNode = classNode.getSuperClass();
        }
        return null;
    }

    /**
     * Detect whether a given ClassNode is a inner class (non-static).
     *
     * @param cNode the ClassNode of interest
     * @return true if the given node is a (non-static) inner class, else false
     */
    public static boolean isInnerClass(ClassNode cNode) {
        return cNode.redirect().getOuterClass() != null
                && !Modifier.isStatic(cNode.getModifiers());
    }

    private ClassNodeUtils() { }

    public static boolean hasNoArgConstructor(ClassNode cNode) {
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        for (ConstructorNode next : constructors) {
            if (next.getParameters().length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if an explicit (non-generated) constructor is in the class.
     *
     * @param xform if non null, add an error if an explicit constructor is found
     * @param cNode the type of the containing class
     * @return true if an explicit (non-generated) constructor was found
     */
    public static boolean hasExplicitConstructor(AbstractASTTransformation xform, ClassNode cNode) {
        List<ConstructorNode> declaredConstructors = cNode.getDeclaredConstructors();
        for (ConstructorNode constructorNode : declaredConstructors) {
            // allow constructors added by other transforms if flagged as Generated
            if (isGenerated(constructorNode)) {
                continue;
            }
            if (xform != null) {
                xform.addError("Error during " + xform.getAnnotationName() +
                        " processing. Explicit constructors not allowed for class: " +
                        cNode.getNameWithoutPackage(), constructorNode);
            }
            return true;
        }
        return false;
    }

    /**
     * Determine if the given ClassNode values have the same package name.
     *
     * @param first a ClassNode
     * @param second a ClassNode
     * @return true if both given nodes have the same package name
     * @throws NullPointerException if either of the given nodes are null
     */
    public static boolean samePackageName(ClassNode first, ClassNode second) {
        return Objects.equals(first.getPackageName(), second.getPackageName());
    }

    /**
     * Return the (potentially inherited) field of the classnode.
     *
     * @param classNode the classnode
     * @param fieldName the name of the field
     * @return the field or null if not found
     */
    public static FieldNode getField(ClassNode classNode, String fieldName) {
        ClassNode node = classNode;
        Set<String> visited = new HashSet<>();
        while (node != null) {
            FieldNode fn = node.getDeclaredField(fieldName);
            if (fn != null) return fn;
            ClassNode[] interfaces = node.getInterfaces();
            for (ClassNode iNode : interfaces) {
                if (visited.contains(iNode.getName())) continue;
                FieldNode ifn = getField(iNode, fieldName);
                visited.add(iNode.getName());
                if (ifn != null) return ifn;
            }
            node = node.getSuperClass();
        }
        return null;
    }
}
