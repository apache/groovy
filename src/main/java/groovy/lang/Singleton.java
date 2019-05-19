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
package groovy.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation to make a singleton class. The singleton is obtained through normal property access using the singleton property (defaults to "instance").
 *
 * Such classes can be initialized during normal static initialization of the class or lazily (on first access).
 * To make the singleton lazy use {@code @Singleton(lazy=true)}.
 * Lazy singletons are implemented with double-checked locking and a volatile backing field.
 * By default, no explicit constructors are allowed. To create one or more explicit constructors
 * use {@code @Singleton(strict=false)}.
 * This could be used to:
 * <ul>
 * <li>provide your own custom initialization logic in your own no-arg constructor - you
 * will be responsible for the entire code (the {@code @Singleton} annotation becomes merely documentation)</li>
 * <li>provide one or more constructors with arguments for a quasi-singleton - these constructors will be used
 * to create instances that are independent of the singleton instance returned by the singleton property</li>
 * </ul>
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.SingletonASTTransformation")
public @interface Singleton {
    /**
     * @return if this singleton should be lazy
     */
    boolean lazy() default false;

    /**
     * @return if this singleton should have strict semantics
     */
    boolean strict() default true;

    /**
     * @return the singleton property name
     */
    String property() default "instance";
}
