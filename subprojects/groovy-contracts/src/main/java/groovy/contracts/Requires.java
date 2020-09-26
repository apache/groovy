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
import org.apache.groovy.contracts.annotations.meta.Precondition;
import org.apache.groovy.contracts.common.impl.RequiresAnnotationProcessor;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a <b>method precondition</b>.
 * </p>
 * <p>
 * A precondition is a condition that must be met by clients of this class. Whenever the
 * precondition can be satisfied, it is guaranteed that the supplier will fulfil the method's
 * postcondition.
 * </p>
 * <p>
 * A method's precondition is executed <i>as the first statement</i> within a method call. A
 * successor's precondition weakens the precondition of its parent class, e.g. if A.someMethod
 * declares a precondition and B.someMethod overrides the method the preconditions are combined with a boolean OR.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 *   &#064;Requires({ argument1 != argument2 &amp;&amp; argument2 &gt;= 0 })
 *   void someOperation(def argument1, def argument2)  {
 *     ...
 *   }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Incubating
@Precondition
@AnnotationProcessorImplementation(RequiresAnnotationProcessor.class)
public @interface Requires {
    Class value();
}