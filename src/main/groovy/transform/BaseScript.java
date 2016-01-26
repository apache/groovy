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

import groovy.lang.Script;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Variable annotation used for changing the base script class of the current script.
 * <p>
 * The type of the variable annotated with {@code @BaseScript} must extend {@link groovy.lang.Script}.
 * It will be used as the base script class.
 * The annotated variable will become shortcut to <code>this</code> object.
 * Using this annotation will override base script set by Groovy compiler or
 * {@link org.codehaus.groovy.control.CompilerConfiguration} of {@link groovy.lang.GroovyShell}
 * Example usage:
 * <pre>
 * class CustomScript extends Script {
 *     int getTheMeaningOfLife() { 42 }
 * }
 *
 * &#64;BaseScript CustomScript baseScript
 *
 * assert baseScript == this
 * assert theMeaningOfLife == 42
 * assert theMeaningOfLife == baseScript.theMeaningOfLife
 * </pre>
 * In this example, the base script of the current script will be changed to 
 * <code>CustomScript</code> allowing usage of <code>getTheMeaningOfLife()</code>
 * method. <code>baseScript</code> variable will become typed shortcut for 
 * <code>this</code> object which enables better IDE support.
 * </p><p>
 * The custom base script may implement the run() method and specify a different
 * method name to be used for the script body by declaring a single abstract method.
 * For example:
 * <pre>
 * abstract class CustomScriptBodyMethod extends Script {
 *     abstract def runScript()
 *     def preRun() { println "preRunning" }
 *     def postRun() { println "postRunning" }
 *     def run() {
 *         preRun()
 *         try {
 *             3.times { runScript() }
 *         } finally {
 *             postRun()
 *         }
 *     }
 * }
 *
 * {@code @BaseScript} CustomScriptBodyMethod baseScript
 * println "Script body run"
 * </pre>
 * That will produce the following output:
 * <pre>
 * preRunning
 * Script body run
 * Script body run
 * Script body run
 * postRunning
 * </pre>
 *
 * Note that while you can declare arguments for the script body's method, as
 * the AST is currently implemented they are not accessible in the script body code.
 * </p>
 * @author Paul King
 * @author Vladimir Orany
 * @author Jim White
 * @since 2.2.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE, ElementType.PACKAGE, ElementType.TYPE /*, ElementType.IMPORT*/})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.BaseScriptASTTransformation")
public @interface BaseScript {
    Class value() default Script.class;
}
