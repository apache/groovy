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
package org.apache.groovy.contracts.generation;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

/**
 * <p>
 * Functions in this class are used to determine whether a certain AST node fulfills certain assertion
 * requirements. E.g. whether a class node is a class invariant candidate or not.
 * </p>
 */
public class CandidateChecks {

    /**
     * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is a candidate
     * for applying contracts. <p/>
     * <p>
     * If the given class node has already been processed in this compilation run, this
     * method will return <tt>false</tt>.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to be checked
     * @return whether the given <tt>type</tt> is a candidate for applying contract assertions
     */
    public static boolean isContractsCandidate(final ClassNode type) {
        return type != null && !type.isSynthetic() && !type.isInterface() && !type.isEnum() && !type.isGenericsPlaceHolder() && !type.isScript() && !type.isScriptBody() && !isRuntimeClass(type);
    }

    /**
     * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is a candidate
     * for applying interface contracts.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to be checked
     * @return whether the given <tt>type</tt> is a candidate for applying interface contract assertions
     */
    public static boolean isInterfaceContractsCandidate(final ClassNode type) {
        return type != null && type.isInterface() && !type.isSynthetic() && !type.isEnum() && !type.isGenericsPlaceHolder() && !type.isScript() && !type.isScriptBody() && !isRuntimeClass(type);
    }

    /**
     * Decides whether the given <tt>propertyNode</tt> is a candidate for class invariant injection.
     *
     * @param propertyNode the {@link org.codehaus.groovy.ast.PropertyNode} to check
     * @return whether the <tt>propertyNode</tt> is a candidate for injecting the class invariant or not
     */
    public static boolean isClassInvariantCandidate(final PropertyNode propertyNode) {
        return propertyNode != null &&
                propertyNode.isPublic() && !propertyNode.isStatic() && !propertyNode.isInStaticContext() && !propertyNode.isClosureSharedVariable();
    }

    /**
     * Decides whether the given <tt>method</tt> is a candidate for a pre- or postcondition.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for pre- or postcondition compliance
     * @return whether the given {@link org.codehaus.groovy.ast.MethodNode} is a candidate for pre- or postconditions
     */
    public static boolean isPreOrPostconditionCandidate(final ClassNode type, final MethodNode method) {
        if (!isPreconditionCandidate(type, method) && !isPostconditionCandidate(type, method)) return false;

        return true;
    }

    /**
     * Decides whether the given <tt>method</tt> is a candidate for class invariants.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for class invariant compliance
     * @return whether the given {@link org.codehaus.groovy.ast.MethodNode} is a candidate for class invariants
     */
    public static boolean isClassInvariantCandidate(final ClassNode type, final MethodNode method) {
        if (method.isSynthetic() || method.isAbstract() || method.isStatic() || !method.isPublic()) return false;
        if (method.getDeclaringClass() != type) return false;

        return true;
    }

    /**
     * Decides whether the given <tt>method</tt> is a candidate for a pre-condition.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for pre-condition compliance
     * @return whether the given {@link org.codehaus.groovy.ast.MethodNode} is a candidate for pre-conditions
     */
    public static boolean isPreconditionCandidate(final ClassNode type, final MethodNode method) {
        if (method.isSynthetic() || method.isAbstract()) return false;
        if (method.getDeclaringClass() != type) return false;

        return true;
    }

    /**
     * Decides whether the given <tt>method</tt> is a candidate for a post-condition.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for post-condition compliance
     * @return whether the given {@link org.codehaus.groovy.ast.MethodNode} is a candidate for post-conditions
     */
    public static boolean isPostconditionCandidate(final ClassNode type, final MethodNode method) {
        if (!isPreconditionCandidate(type, method)) return false;
        if (method.isStatic()) return false;

        return true;
    }

    /**
     * Checks whether the given {@link MethodNode} could be a candidate for an arbitrary {@link org.apache.groovy.contracts.annotations.meta.ContractElement}
     * annotation.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for {@link org.apache.groovy.contracts.annotations.meta.ContractElement} compliance
     * @return whether the given method node could be a candidate or not
     */
    public static boolean couldBeContractElementMethodNode(final ClassNode type, final MethodNode method) {
        if (method.isSynthetic() || !method.isPublic()) return false;
        if (method.getDeclaringClass() != null && !method.getDeclaringClass().getName().equals(type.getName()))
            return false;

        return true;
    }

    /**
     * Checks whether the given {@link ClassNode} is part of the Groovy/Java runtime.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @return <tt>true</tt> whether the current {@link org.codehaus.groovy.ast.ClassNode} is a Groovy/Java system class
     */
    public static boolean isRuntimeClass(final ClassNode type) {
        String name = type.getName();
        return name.startsWith("java.") || (name.startsWith("groovy.") && !name.startsWith("groovy.contracts."));
    }
}
