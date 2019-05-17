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
package groovy.test;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation used to invert test case results. If a JUnit 3/4 test case method is
 * annotated with {@code @NotYetImplemented} the test will fail if no test failure occurs and it will pass
 * if a test failure occurs.
 * <p>
 * This is helpful for tests that don't currently work but should work one day,
 * when the tested functionality has been implemented.
 * <p>
 * The idea for this AST transformation originated in {@link groovy.test.GroovyTestCase#notYetImplemented()}.
 *
 * @since 3.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.apache.groovy.test.transform.NotYetImplementedASTTransformation")
public @interface NotYetImplemented {
}
