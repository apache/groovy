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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.vmplugin.JavaFeatureVersion
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * An exhaustive switch expression can match nothing at run time only when an
 * enum constant or a sealed subtype was added after it was compiled. On a
 * Java 21+ target that throws {@code MatchException}, as in Java (JEP 441);
 * below that the enum case keeps {@code IncompatibleClassChangeError} and the
 * sealed case {@code IllegalStateException}. A {@code null} selector is matched
 * only by an explicit {@code case null} arm; without one it throws
 * {@code IllegalStateException} on every target.
 */
final class SwitchMatchExceptionTest {

    private static final String ENUM_V1 = 'enum Flag { ON, OFF }'
    private static final String ENUM_V2 = 'enum Flag { ON, OFF, MAYBE }'
    private static final String ENUM_USER = '''
        @groovy.transform.CompileStatic
        class User {
            static String strict(Flag f) {
                return switch (f) {
                    case Flag.ON  -> 'on'
                    case Flag.OFF -> 'off'
                }
            }
        }
    '''

    private static final String SEALED_V1 = '''
        sealed interface Shape permits Circle, Square {}
        record Circle(double radius) implements Shape {}
        record Square(double side) implements Shape {}
    '''
    private static final String SEALED_V2 = '''
        sealed interface Shape permits Circle, Square, Triangle {}
        record Circle(double radius) implements Shape {}
        record Square(double side) implements Shape {}
        record Triangle(double base) implements Shape {}
    '''

    private static String sealedUser(final String mode) {
        """
            @groovy.transform.$mode
            class User {
                static String strict(Shape s) {
                    return switch (s) {
                        case Circle c -> 'circle'
                        case Square q -> 'square'
                    }
                }
            }
        """
    }

    @Test
    void enumConstantAddedLaterThrowsMatchExceptionOnJava21Target() {
        assumeTrue(JavaFeatureVersion.current() >= 21)
        withUser('21', [ENUM_V1, ENUM_USER], [ENUM_V2]) { Class user ->
            def maybe = Enum.valueOf(user.classLoader.loadClass('Flag'), 'MAYBE')
            def err = shouldFail(matchException()) { user.strict(maybe) }
            assert err.message == 'the switch expression does not cover the value MAYBE'
        }
    }

    @Test
    void enumConstantAddedLaterKeepsIncompatibleClassChangeErrorOnJava17Target() {
        withUser('17', [ENUM_V1, ENUM_USER], [ENUM_V2]) { Class user ->
            def maybe = Enum.valueOf(user.classLoader.loadClass('Flag'), 'MAYBE')
            shouldFail(IncompatibleClassChangeError) { user.strict(maybe) }
        }
    }

    @Test
    void sealedSubtypeAddedLaterThrowsMatchExceptionOnJava21Target() {
        assumeTrue(JavaFeatureVersion.current() >= 21)
        for (mode in ['TypeChecked', 'CompileStatic']) {
            withUser('21', [SEALED_V1, sealedUser(mode)], [SEALED_V2]) { Class user ->
                def triangle = user.classLoader.loadClass('Triangle').newInstance(2.0d)
                def err = shouldFail(matchException()) { user.strict(triangle) }
                assert err.message.startsWith('the switch expression does not cover the value Triangle')
            }
        }
    }

    @Test
    void sealedSubtypeAddedLaterKeepsIllegalStateExceptionOnJava17Target() {
        for (mode in ['TypeChecked', 'CompileStatic']) {
            withUser('17', [SEALED_V1, sealedUser(mode)], [SEALED_V2]) { Class user ->
                def triangle = user.classLoader.loadClass('Triangle').newInstance(2.0d)
                shouldFail(IllegalStateException) { user.strict(triangle) }
            }
        }
    }

    @Test
    void nullSelectorStillThrowsIllegalStateExceptionOnJava21Target() {
        assumeTrue(JavaFeatureVersion.current() >= 21)
        for (mode in ['TypeChecked', 'CompileStatic']) {
            withUser('21', [SEALED_V1, sealedUser(mode)], []) { Class user ->
                def err = shouldFail(IllegalStateException) { user.strict(null) }
                assert err.message == 'the switch expression does not cover the value null'
            }
        }
        withUser('21', [ENUM_V1, ENUM_USER], []) { Class user ->
            shouldFail(IllegalStateException) { user.strict(null) }
        }
    }

    @Test
    void nullSelectorMatchesAnExplicitCaseNullArmOnJava21Target() {
        assumeTrue(JavaFeatureVersion.current() >= 21)
        for (mode in ['TypeChecked', 'CompileStatic']) {
            def user = """
                @groovy.transform.$mode
                class User {
                    static String strict(Shape s) {
                        return switch (s) {
                            case null     -> 'none'
                            case Circle c -> 'circle'
                            case Square q -> 'square'
                        }
                    }
                }
            """
            withUser('21', [SEALED_V1, user], [SEALED_V2]) { Class loaded ->
                assert loaded.strict(null) == 'none'
                def triangle = loaded.classLoader.loadClass('Triangle').newInstance(2.0d)
                shouldFail(matchException()) { loaded.strict(triangle) }
            }
        }
    }

    @Test
    void matchedSelectorIsUnaffectedOnJava21Target() {
        assumeTrue(JavaFeatureVersion.current() >= 21)
        withUser('21', [SEALED_V1, sealedUser('CompileStatic')], []) { Class user ->
            def square = user.classLoader.loadClass('Square').newInstance(2.0d)
            assert user.strict(square) == 'square'
        }
    }

    //--------------------------------------------------------------------------

    /** The Java 21 exception class, looked up by name so that this test compiles on Java 17. */
    private static Class<? extends Throwable> matchException() {
        Class.forName('java.lang.MatchException')
    }

    /**
     * Compiles the two source sets to separate directories for the given target
     * bytecode and loads {@code User} with the second set ahead of the first, so
     * that it sees the later versions of the types it was compiled against.
     */
    private static void withUser(final String target, final List<String> v1Sources, final List<String> v2Sources, final Closure body) {
        File v1 = File.createTempDir()
        File v2 = File.createTempDir()
        try {
            compileTo(v1, target, v1Sources)
            if (v2Sources) compileTo(v2, target, v2Sources)
            def loader = new URLClassLoader([v2, v1]*.toURI()*.toURL() as URL[], SwitchMatchExceptionTest.classLoader)
            body(loader.loadClass('User'))
        } finally {
            v1.deleteDir()
            v2.deleteDir()
        }
    }

    private static void compileTo(final File dir, final String target, final List<String> sources) {
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: dir, targetBytecode: target))
        sources.eachWithIndex { String source, int i -> unit.addSource("Source${i}.groovy", source) }
        unit.compile()
    }
}
