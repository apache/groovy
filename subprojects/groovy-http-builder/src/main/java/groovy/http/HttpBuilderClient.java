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
package groovy.http;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a declarative HTTP client. An implementation class
 * is generated at compile time via AST transform, using {@link HttpBuilder}
 * for request execution.
 * <p>
 * Example:
 * <pre>
 * {@code @HttpBuilderClient}('https://api.example.com')
 * interface MyApi {
 *     {@code @Get}('/users/{id}')
 *     Map getUser(String id)
 * }
 *
 * def api = MyApi.create()
 * def user = api.getUser('123')
 * </pre>
 *
 * @since 6.0.0
 */
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("groovy.http.HttpBuilderClientTransform")
public @interface HttpBuilderClient {
    /** The base URL for all requests. */
    String value();
}
