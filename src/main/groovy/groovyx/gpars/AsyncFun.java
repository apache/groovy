
/*
 GPars - Groovy Parallel Systems

 Copyright © 2008-2013  The original author or authors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package groovyx.gpars;

import groovyx.gpars.util.AsyncFunASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation makes a field or local variable as an asynchronous function, and the field/variable should be
 * evaluated only within the context of a ThreadPool.
 *
 * @author Vladimir Orany
 * @author Hamlet D'Arcy
 *         Date: May 14, 2011
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass(classes = {AsyncFunASTTransformation.class})
public @interface AsyncFun {
    Class<?> value() default GParsPoolUtil.class;

    /**
     * Set to true to execute the closure in blocking mode.
     *
     * @return The current value
     */
    boolean blocking() default false;
}
