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
package groovy.ginq.transform;

import org.apache.groovy.ginq.provider.collection.runtime.Queryable;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to make a method call returning GINQ result
 *
 * @since 4.0.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.apache.groovy.ginq.transform.GinqASTTransformation")
public @interface GQ {
    /**
     * Specify the result type
     */
    Class<?> value() default Queryable.class;

    /**
     * Whether to optimize the GINQ AST
     */
    boolean optimize() default true;

    /**
     * Whether to enable parallel querying
     */
    boolean parallel() default false;

    /**
     * Specify the GINQ AST walker to customize GINQ behaviour.
     * <p>
     * This is an advanced extension point: the named class must implement the
     * {@link org.apache.groovy.ginq.dsl.GinqAstVisitor} provider SPI, which is still
     * {@link Incubating} and may change in a future release.
     */
    @Incubating
    String astWalker() default "org.apache.groovy.ginq.provider.collection.GinqAstWalker";

    /**
     * Specify the GINQ provider by name, e.g. {@code collection} (the default) or a
     * provider registered via the {@link org.apache.groovy.ginq.provider.GinqProvider}
     * SPI such as {@code native-sql}.
     * <p>
     * Note that database-backed providers also require a {@code dataSource}, which
     * cannot be supplied via an annotation; use the macro form instead, e.g.
     * {@code GQ(provider: 'native-sql', dataSource: db) {...}}.
     *
     * @since 6.0.0
     */
    @Incubating
    String provider() default "collection";
}
