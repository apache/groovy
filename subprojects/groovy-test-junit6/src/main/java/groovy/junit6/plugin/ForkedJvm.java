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
package groovy.junit6.plugin;

import org.apache.groovy.lang.annotation.Incubating;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs the annotated test method (or every test method on the annotated class)
 * in a freshly forked JVM, optionally configured with extra system properties
 * and JVM arguments.
 * <p>
 * Useful for testing behaviour gated by JVM startup state that cannot be
 * toggled at runtime, e.g. {@code static final} fields initialised from
 * {@code System.getProperty(...)} at class load time
 * (such as {@code groovy.val.enabled}).
 * <p>
 * System properties are supplied as {@code "key=value"} strings:
 * <pre>
 * &#64;Test
 * &#64;ForkedJvm(systemProperties = {"groovy.val.enabled=false"})
 * void runsInChildJvm() {
 *     assert System.getProperty("groovy.val.enabled").equals("false");
 * }
 * </pre>
 * <p>
 * JVM arguments are passed verbatim:
 * <pre>
 * &#64;Test
 * &#64;ForkedJvm(jvmArgs = {"--add-opens=java.base/java.lang=ALL-UNNAMED"})
 * void withModuleAccess() { ... }
 * </pre>
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ForkedJvmExtension.class)
public @interface ForkedJvm {
    /**
     * System properties to set on the forked JVM, each as a {@code "key=value"} string.
     */
    String[] systemProperties() default {};

    /**
     * Raw JVM arguments to prepend to the forked JVM's command line, e.g.
     * {@code "-Xmx128m"} or {@code "--add-opens=java.base/java.lang=ALL-UNNAMED"}.
     */
    String[] jvmArgs() default {};
}
