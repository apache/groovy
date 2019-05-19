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
 * Variable annotation used for changing the scope of a variable within a script from
 * being within the run method of the script to being at the class level for the script.
 * <p>
 * The annotated variable will become a private field of the script class.
 * The type of the field will be the same as the type of the variable. Example usage:
 * <pre class="groovyTestCase">
 * import groovy.transform.Field
 * {@code @Field} List awe = [1, 2, 3]
 * def awesum() { awe.sum() }
 * assert awesum() == 6
 * </pre>
 * In this example, without the annotation, variable <code>awe</code> would be
 * a local script variable (technically speaking it will be a local variable within
 * the <code>run</code> method of the script class). Such a local variable would
 * not be visible inside the <code>awesum</code> method. With the annotation,
 * <code>awe</code> becomes a private List field in the script class and is
 * visible within the <code>awesum</code> method.
 *
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.FieldASTTransformation")
public @interface Field {
}
