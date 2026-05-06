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

import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode;

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
 * <p>This node is built by the parser's {@code AstBuilder} from the
 * {@code intersectionType} rule. At parse time the components have not yet
 * been resolved to bound {@link ClassNode}s, so the {@code upper} bound
 * passed to the parent constructor is a placeholder and the
 * {@code interfaces} array is just the components in user order; resolution
 * and class-vs-interface classification are completed in later phases.
 *
 * @since 5.0.0
 */
public final class IntersectionTypeClassNode extends LowestUpperBoundClassNode {

    private final ClassNode[] components;

    public IntersectionTypeClassNode(final ClassNode[] components) {
        super("IntersectionType", ClassHelper.OBJECT_TYPE, components.clone());
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
}
