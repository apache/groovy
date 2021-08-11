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
 * Annotation to add the final modifier to classes, methods, constructors, and fields.
 * Using `{@code final}` and directly using `{@code @Final}` will have the same result.
 * However, the intention is almost never to use `{@code @Final}` directly but rather as part
 * of an annotation collector (meta-annotation).
 *
 * If you like the behavior of an existing annotation but would really like a version that
 * also ensured the respective annotated node was final, you can create such an element, e.g.:
 * <pre>
 * &#64;AnnotationCollector
 * &#64;Canonical
 * &#64;Final
 * &#64;interface MyCanonical {}
 *
 * &#64;MyCanonical class Foo {}
 * </pre>
 * Here, class {@code Foo} will be final as well as having all the normal {@code Canonical} enhancements.
 *
 * <p>
 * Similarly, if you wanted to, you could define:
 * <pre>
 * &#64;AnnotationCollector([Singleton, Final]) &#64;interface MySingleton {}
 * </pre>
 * Classes annotated with &#64;MySingleton would be final as well as have all the {@code Singleton} enhancements.
 * </p>
 *
 * <p>
 * As another example, you could define:
 * <pre>
 * &#64;AnnotationCollector([NullCheck, Final, AutoFinal]) &#64;interface MyNullCheck {}
 * </pre>
 * Methods annotated with &#64;MyNullCheck would be final (from &#64;Final),
 * would have all parameters marked final (from &#64;AutoFinal), and
 * would have all parameters checked against {@code null} (from &#64;NullCheck).
 * </p>
 *
 * @since 4.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.FinalASTTransformation")
public @interface Final {
}
