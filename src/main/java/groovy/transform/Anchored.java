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
package groovy.transform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a trait method as <em>anchored to the trait</em> — dispatch is fixed
 * to the trait's own definition rather than routed through the implementing
 * class.
 *
 * <p>Applied to a trait <code>static</code> method, this opts out of the
 * default per-implementer override dispatch (the Groovy distinctive that the
 * Grails <code>Validateable.defaultNullable()</code> pattern depends on) and
 * substitutes the JVM interface-static dispatch model: the implementer's
 * same-signature static is an independent method, not an override; the trait
 * body always calls the trait's own copy.
 *
 * <p>By default the marker also promotes the annotated method onto the
 * generated trait interface as a JVM-native public static. That makes
 * external <code>Trait.m()</code> calls and from-trait <code>T.m()</code>
 * calls both resolve at the JVM level rather than throwing
 * <code>MissingMethodException</code>. The {@link #inInterface()} attribute
 * is provided as an opt-out for the narrow case where dispatch should be
 * trait-anchored but the method should <em>not</em> be published on the
 * interface (e.g. a soft-deprecated trait-internal helper that needs to
 * remain publicly callable for backward compatibility but should not gain a
 * fresh Java-visible API surface).
 *
 * <p>Use this marker for trait statics that are part of the trait's published
 * contract — guaranteed-invariant utilities the trait author does not want
 * implementers to redefine. Continue to use plain <code>static</code> when
 * the trait offers a default that implementers may legitimately override.
 *
 * @since 6.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Anchored {
    /**
     * Whether the annotated method should also be promoted onto the generated
     * trait interface. Default <code>true</code>: the marker bundles
     * declarer-bound dispatch with external interface visibility (the
     * coherent JVM interface-static model). Set to <code>false</code> for the
     * narrow opt-out described above — dispatch stays trait-anchored but the
     * method is not exposed on the interface.
     */
    boolean inInterface() default true;
}
