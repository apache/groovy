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
package groovy.transform.stc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation aimed at helping IDEs or the static type checker to infer the
 * parameter types of a closure. Without this annotation, a method signature may look like
 * this:<p>
 * <code>public &lt;T,R&gt; List&lt;R&gt; doSomething(List&lt;T&gt; source, Closure&lt;R&gt; consumer)</code>
 * <p>
 * <p>The problem this annotation tries to solve is to define the expected parameter types of the
 * <i>consumer</i> closure. The generics type defined in <code>Closure&lt;R&gt;</code> correspond to the
 * result type of the closure, but tell nothing about what the closure must accept as arguments.</p>
 * <p></p>
 * <p>There's no way in Java or Groovy to express the type signature of the expected closure call method from
 * outside the closure itself, so we rely on an annotation here. Unfortunately, annotations also have limitations
 * (like not being able to use generics placeholder as annotation values) that prevent us from expressing the
 * type directly.</p>
 * <p>Additionally, closures are polymorphic. This means that a single closure can be used with different, valid,
 * parameter signatures. A typical use case can be found when a closure accepts either a {@link java.util.Map.Entry}
 * or a (key,value) pair, like the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#each(java.util.Map, groovy.lang.Closure)}
 * method.</p>
 * <p>For those reasons, the {@link ClosureParams} annotation takes these arguments:
 * <ul>
 *     <li>{@link ClosureParams#value()} defines a {@link groovy.transform.stc.ClosureSignatureHint} hint class
 *     that the compiler will use to infer the parameter types</li>
 *     <li>{@link ClosureParams#conflictResolutionStrategy()} defines a {@link groovy.transform.stc.ClosureSignatureConflictResolver} resolver
 *     class that the compiler will use to potentially reduce ambiguities remaining after initial inference calculations</li>
 *     <li>{@link ClosureParams#options()}, a set of options that are passed to the hint when the type is inferred (and also available to the resolver)</li>
 * </ul>
 * </p>
 * <p>As a result, the previous signature can be written like this:</p>
 * <code>public &lt;T,R&gt; List&lt;R&gt; doSomething(List&lt;T&gt; source, @ClosureParams(FirstParam.FirstGenericType.class) Closure&lt;R&gt; consumer)</code>
 * <p>Which uses the {@link FirstParam.FirstGenericType} first generic type of the first argument</p> hint to tell that the only expected
 * argument type corresponds to the type of the first generic argument type of the first method parameter.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClosureParams {
    Class<? extends ClosureSignatureHint> value();
    Class<? extends ClosureSignatureConflictResolver> conflictResolutionStrategy() default ClosureSignatureConflictResolver.class;
    String[] options() default {};
}
