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
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Represents a user-written intersection type used as the target of a cast
 * expression or {@code as} coercion, e.g.
 * <pre>
 *     (Runnable &amp; Serializable) () -&gt; ...
 *     value as (A &amp; B)
 * </pre>
 *
 * <p>Distinct from the implicit lowest-upper-bound nodes that
 * {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor}
 * synthesises during inference: an instance of this class records the ordered
 * list of components exactly as written by the user. That ordering is needed
 * for cast-conversion checks, error messages and (in later phases) bytecode
 * generation via {@code LambdaMetafactory.altMetafactory} markers.
 *
 * <p>Lifecycle: at parse time the components have not yet been resolved to
 * bound {@link ClassNode}s, so the constructor places all components in the
 * inherited {@link #getInterfaces() interfaces} array with {@code Object} as
 * the placeholder superclass. After {@code ResolveVisitor} resolves each
 * component it should call {@link #reclassifyComponents()} so that the
 * interfaces array contains only interface components and the superclass is
 * the (at most one) class component.
 *
 * @since 5.0.0
 */
public final class IntersectionTypeClassNode extends ClassNode {

    private final ClassNode[] components;

    public IntersectionTypeClassNode(final ClassNode[] components) {
        super("IntersectionType", ACC_PUBLIC | ACC_FINAL, ClassHelper.OBJECT_TYPE, components.clone(), MixinNode.EMPTY_ARRAY);
        if (components.length < 2) {
            throw new IllegalArgumentException("IntersectionTypeClassNode requires at least two components");
        }
        this.components = components.clone();
    }

    /**
     * Returns the components of this intersection type in user-written order.
     */
    public ClassNode[] getComponents() {
        return components.clone();
    }

    /**
     * Reclassifies the components after resolution: separates the (at most
     * one) class component from the interface components and updates the
     * inherited superclass and interfaces accordingly. Components are
     * resolved in place — callers do not need to substitute new instances.
     */
    public void reclassifyComponents() {
        ClassNode klass = null;
        List<ClassNode> ifaces = new ArrayList<>(components.length);
        for (ClassNode c : components) {
            if (c.isInterface()) {
                ifaces.add(c);
            } else {
                klass = c; // STC will validate "at most one" elsewhere
            }
        }
        setSuperClass(klass != null ? klass : ClassHelper.OBJECT_TYPE);
        setInterfaces(ifaces.toArray(ClassNode.EMPTY_ARRAY));
    }

    @Override
    public String getText() {
        StringJoiner sj = new StringJoiner(" & ", "(", ")");
        for (ClassNode c : components) sj.add(c.toString(false));
        return sj.toString();
    }

    @Override
    public String toString(final boolean showRedirect) {
        return getText();
    }
}
