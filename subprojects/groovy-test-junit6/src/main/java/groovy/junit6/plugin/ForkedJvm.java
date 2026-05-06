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
 * <p>
 * Properties from the parent JVM can be propagated to the child via
 * {@link #inheritProperties()}. Each entry is either an exact property
 * name or a prefix pattern ending in {@code *}. This is useful when the
 * parent test JVM is configured by the build (e.g. Gradle) with a
 * property the child also needs, such as Spock's Groovy-version-check
 * override:
 * <pre>
 * &#64;Test
 * &#64;ForkedJvm(inheritProperties = {"spock.*"})
 * void seesSpockOverride() { ... }
 * </pre>
 * <p>
 * <b>Lifecycle gotcha:</b> JUnit lifecycle hooks
 * ({@code @BeforeAll}, {@code @BeforeEach}, {@code @AfterAll},
 * {@code @AfterEach}) fire in <em>both</em> the parent and the forked
 * child JVM, because the child re-runs the class lifecycle for the
 * targeted method. Setup that mutates JVM-global state — typically
 * {@code System.setProperty(...)} — therefore replays in the child and
 * can defeat tests that assert on what was (or was not) propagated
 * across the fork. Guard parent-only setup with the forked-flag check:
 * <pre>
 * &#64;BeforeAll
 * static void setUp() {
 *     if (Boolean.parseBoolean(System.getProperty("groovy.junit6.forked"))) return;
 *     // ...parent-only setup here...
 * }
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

    /**
     * Names or prefix patterns of system properties from the parent JVM to
     * propagate to the forked child. Each entry is either an exact property
     * name (e.g. {@code "spock.iKnowWhatImDoing.disableGroovyVersionCheck"})
     * or a prefix pattern ending in {@code *} (e.g. {@code "spock.*"} or
     * {@code "groovy.compiler.*"}). Patterns that match no parent property
     * are silently ignored.
     * <p>
     * Properties supplied via {@link #systemProperties()} override inherited
     * values for the same key.
     */
    String[] inheritProperties() default {};
}
