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
package groovy.contracts;

import org.apache.groovy.contracts.annotations.meta.AnnotationProcessorImplementation;
import org.apache.groovy.contracts.annotations.meta.Postcondition;
import org.apache.groovy.contracts.common.impl.EnsuresAnnotationProcessor;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a <b>method postcondition</b>.
 * </p>
 * <p>
 * A postcondition is a condition that is guaranteed to be fulfilled by suppliers.
 * </p>
 * <p>
 * A method's postcondition is executed <i>after</i> a method call
 * has finished. A successor's postcondition strengthens the postcondition of its parent class, e.g. if A.someMethod
 * declares a postcondition and B.someMethod overrides the method the postconditions are combined with a boolean AND.
 * </p>
 * <p>
 * Compared to pre-conditions, postcondition annotation closures are optionally called with two additional
 * closure arguments: <tt>result</tt> and <tt>old</tt>.
 * </p>
 * <p>
 * <tt>result</tt> is available if the corresponding method has a non-void return-type and holds the
 * result of the method call. Be aware that modifying the internal state of a reference type can lead
 * to side-effects. Groovy-contracts does not keep track of any sort of modifications, neither any conversion to
 * immutability.
 * </p>
 * <p>
 * <tt>old</tt> is available in every postcondition. It is a {@link java.util.Map} which holds the values
 * of value types and {@link Cloneable} types before the method has been executed.
 * </p>
 * <p>
 * Examples:
 * <p>
 * Accessing the <tt>result</tt> closure parameter:
 *
 * <pre>
 *   &#064;Ensures({ result -&gt; result != argument1 })
 *   def T someOperation(def argument1, def argument2)  {
 *     ...
 *   }
 * </pre>
 * <p>
 * Accessing the <tt>old</tt> closure parameter:
 *
 * <pre>
 *   &#064;Ensures({ old -&gt; old.counter + 1 == counter })
 *   def T someOperation(def argument1, def argument2)  {
 *     ...
 *   }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Incubating
@Postcondition
@AnnotationProcessorImplementation(EnsuresAnnotationProcessor.class)
public @interface Ensures {
    Class value();
}