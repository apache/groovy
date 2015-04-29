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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used to assist in the creation of {@code Externalizable} classes.
 * The {@code @ExternalizeVerifier} annotation instructs the compiler to check
 * that a class has {@code writeExternal()} and {@code readExternal()} methods,
 * implements the {@code Externalizable} interface and that each property (and optionally field) is not final
 * and, optionally for non-primitives, has a type which is either {@code Externalizable} or {@code Serializable}.
 * Properties or fields marked as {@code transient} are ignored.
 * This annotation is typically used in conjunction with the {@code @ExternalizeMethods} annotation but
 * most usually not directly but rather via {@code @AutoExternalizable} which is a shortcut for both annotations.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ExternalizeVerifierASTTransformation")
public @interface ExternalizeVerifier {
    /**
     * Comma separated list of property names to exclude from externalization verification.
     * For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * Include fields as well as properties when verifying externalization properties.
     */
    boolean includeFields() default false;

    /**
     * Turns on strict type checking for property (or field) types. In strict mode, such types must also implement Serializable or Externalizable.
     * If your properties have interface types that don't implement Serializable but all the concrete implementations do, or the
     * type is of a non-Serializable class but the property will be null at runtime, then your instances will still be serializable
     * but you can't turn on strict checking.
     */
    boolean checkPropertyTypes() default false;
}
