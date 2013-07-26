/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform;

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
 * The idea for this AST transformation originated in {@link groovy.util.GroovyTestCase#notYetImplemented()}.
 *
 * @author Dierk König
 * @author Andre Steingress
 * @author Ilinca V. Hallberg
 * @author Björn Westlin
 * @since 2.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.NotYetImplementedASTTransformation")
public @interface NotYetImplemented {
}
