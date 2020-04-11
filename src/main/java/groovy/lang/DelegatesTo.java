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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used by API or DSL writers to document parameters which accept a closure.
 * In that case, using this annotation, you can specify what the delegate type of the closure will
 * be. This is important for IDE support.
 * <p>
 * This annotation can also be used to help the type checker ({@link groovy.transform.TypeChecked})
 * which would not report errors then if the delegate is of the documented type. Of course, it is
 * also compatible with {@link groovy.transform.CompileStatic}.
 * <p>
 * Example:
 * <pre>
 * // Document the fact that the delegate of the closure will be an ExecSpec
 * ExecResult exec(@DelegatesTo(ExecSpec) Closure closure) { ... }
 * </pre>
 *
 * @since 2.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DelegatesTo {
    Class value() default Target.class;

    /**
     * The {@link Closure#resolveStrategy} used by the closure.
     */
    int strategy() default Closure.OWNER_FIRST;

    /**
     * The index of the generic type that will be the type of the closure's delegate.
     * The generic types are considered with respect to the {@code @DelegatesTo.Target} annotated
     * parameter for this usage, with the index starting at 0.
     */
    int genericTypeIndex() default -1;

    /**
     * In cases when there are multiple {@code @DelegatesTo.Target} annotated parameters, this
     * member should be set to the {@link DelegatesTo.Target#value()} of the correct target.
     */
    String target() default "";

    /**
     * The type member should be used when the type of the delegate cannot
     * be represented with {@link #value()}, {@link #genericTypeIndex()} or
     * {@link #target()}. In this case, it is possible to use a String to represent
     * the type, at the cost of potential uncaught errors at compile time if the
     * type is invalid and increased compile time.
     *
     * @return a String representation of a type
     * @since 2.4.0
     */
    String type() default "";

    /**
     * Parameter annotation used to specify the delegate for a {@code @DelegatesTo} annotated
     * parameter of the same method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({ElementType.PARAMETER})
    @interface Target {

        /**
         * An identifier that should be used to disambiguate targets when there are
         * multiple {@code @DelegatesTo.Target} annotated parameters.
         */
        String value() default "";
    }
}