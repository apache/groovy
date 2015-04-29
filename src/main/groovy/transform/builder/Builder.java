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
package groovy.transform.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

import groovy.transform.Undefined;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import static org.codehaus.groovy.transform.BuilderASTTransformation.BuilderStrategy;

/**
 * The {@code @Builder} AST transformation is used to help write classes that can be created using <em>fluent</em> api calls.<!-- -->
 * The transform supports multiple building strategies to cover a range of cases and there are a number
 * of configuration options to customize the building process.
 *
 * In addition, a number of annotation attributes let you customise the building process. Not all annotation attributes
 * are supported by all strategies. See the individual strategy documentation for more details.
 * If you're an AST hacker, you can also define your own strategy class.
 *
 * The following strategies are bundled with Groovy:
 * <ul>
 *     <li>{@link SimpleStrategy} for creating chained setters</li>
 *     <li>{@link ExternalStrategy} where you annotate an explicit builder class while leaving some buildee class being built untouched</li>
 *     <li>{@link DefaultStrategy} which creates a nested helper class for instance creation</li>
 *     <li>{@link InitializerStrategy} which creates a nested helper class for instance creation which when used with {@code @CompileStatic} allows type-safe object creation</li>
 * </ul>
 *
 * Note that Groovy provides other built-in mechanisms for easy creation of objects, e.g. the named-args constructor:
 * <pre>
 * new Person(firstName: "Robert", lastName: "Lewandowski", age: 21)
 * </pre>
 * or the with statement:
 * <pre>
 * new Person().with {
 *     firstName = "Robert"
 *     lastName = "Lewandowski"
 *     age = 21
 * }
 * </pre>
 * so you might not find value in using the builder transform at all. But if you need Java integration or in some cases improved type safety, the {@code @Builder} transform might prove very useful.
 *
 * @author Marcin Grzejszczak
 * @author Paul King
 * @see groovy.transform.builder.SimpleStrategy
 * @see groovy.transform.builder.ExternalStrategy
 * @see groovy.transform.builder.DefaultStrategy
 * @see groovy.transform.builder.InitializerStrategy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.BuilderASTTransformation")
public @interface Builder {

    /**
     * A class for which builder methods should be created. It will be an error to leave
     * this attribute with its default value for some strategies.
     */
    Class forClass() default Undefined.CLASS.class;

    /**
     * A class capturing the builder strategy
     */
    Class<? extends BuilderStrategy> builderStrategy() default DefaultStrategy.class;

    /**
     * The prefix to use when creating the setter methods.
     * Default is determined by the strategy which might use "" or "set" but you can choose your own, e.g. "with".
     * If non-empty the first letter of the property will be capitalized before being appended to the prefix.
     */
    String prefix() default Undefined.STRING;

    /**
     * For strategies which create a builder helper class, the class name to use for the helper class.
     * Not used if using {@code forClass} since in such cases the builder class is explicitly supplied.
     * Default is determined by the strategy, e.g. <em>TargetClass</em> + "Builder" or <em>TargetClass</em> + "Initializer".
     */
    String builderClassName() default Undefined.STRING;

    /**
     * For strategies which create a builder helper class that creates the instance, the method name to call to create the instance.
     * Default is determined by the strategy, e.g. <em>build</em> or <em>create</em>.
     */
    String buildMethodName() default Undefined.STRING;

    /**
     * The method name to use for a builder factory method in the source class for easy access of the
     * builder helper class for strategies which create such a helper class.
     * Must not be used if using {@code forClass}.
     * Default is determined by the strategy, e.g. <em>builder</em> or <em>createInitializer</em>.
     */
    String builderMethodName() default Undefined.STRING;

    /**
     * List of field and/or property names to exclude from generated builder methods.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include within the generated builder methods.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] includes() default {};
}
