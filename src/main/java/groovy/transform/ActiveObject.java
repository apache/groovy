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

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an active object whose {@link ActiveMethod}-annotated
 * methods are automatically routed through an internal actor for
 * thread-safe, serialised execution.
 * <p>
 * All {@code @ActiveMethod} calls on the same instance are processed
 * one at a time — no locks needed.
 *
 * <pre>{@code
 * {@literal @}ActiveObject
 * class Account {
 *     private double balance = 0
 *
 *     {@literal @}ActiveMethod
 *     void deposit(double amount) { balance += amount }
 *
 *     {@literal @}ActiveMethod(blocking = false)
 *     Awaitable<Double> getBalance() { balance }
 * }
 * }</pre>
 *
 * @see ActiveMethod
 * @since 6.0.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ActiveObjectASTTransformation")
public @interface ActiveObject {

    /**
     * The name of the generated actor field.
     */
    String actorName() default "internalActiveObjectActor";
}
