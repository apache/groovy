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
 * Marks a public trait <code>static</code> method as <em>virtual</em>:
 * trait-body calls to this method dispatch through the implementing class
 * at runtime, so a same-signature static method declared on the
 * implementing class overrides the trait's default.
 *
 * <p>This is the opt-in counterpart to the default declarer-bound
 * dispatch for trait static methods, where trait-body calls invoke the
 * trait's own copy regardless of any same-named static on the
 * implementer. The opt-in restores the per-implementer override pattern
 * used by Grails' {@code Validateable.defaultNullable()} and similar
 * framework hooks.
 *
 * <p>The marker is <em>per-callee</em>, not per-caller: it changes how
 * trait code invokes the annotated method, regardless of which method
 * inside the trait does the calling.
 *
 * <p>Valid only on public, non-abstract <code>static</code> trait
 * methods. Applying it to an instance method, a private method, an
 * abstract method, or anything outside a trait is a compile-time error.
 *
 * @since 5.0.7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Virtual {
}
