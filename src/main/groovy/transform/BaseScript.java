/*
 * Copyright 2008-2013 the original author or authors.
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
 * Variable annotation used for changing the base script class of the current script.
 * <p>
 * The type of the variable annotated with {@BaseScript} must extend {@link groovy.lang.Script}.
 * It will be used as the base script class.
 * The annotated variable will become shortcut to <code>this</code> object.
 * Using this annotation will override base script set by Groovy compiler or
 * {@link org.codehaus.groovy.control.CompilerConfiguration} of {@link groovy.lang.GroovyShell}
 * Example usage:
 * <pre>
 * class CustomScript extends Script {
 *     int getTheMeaningOfLife() { 42 }
 * }
 * {@code @BaseScript} CustomScript baseScript
 * assert baseScript == this
 * assert theMeaningOfLife == 42
 * assert theMeaningOfLife == baseScript.theMeaningOfLife
 * </pre>
 * In this example, the base script of the current script will be changed to 
 * <code>CustomScript</code> allowing usage of <code>getTheMeaningOfLife()</code>
 * method. <code>baseScript</code> variable will become typed shortcut for 
 * <code>this<code> object which enables better IDE support.
 *
 * @author Paul King
 * @author Vladimir Orany
 * @since 2.2.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.BaseScriptASTTransformation")
public @interface BaseScript {
}
