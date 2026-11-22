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
package bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

/**
 * GROOVY-5400 (regression of GROOVY-3852): groovyc used to accept multiple
 * non-repeatable runtime annotations on the same element, and the JVM then
 * failed with {@code AnnotationFormatError: Duplicate annotation}. The
 * compiler must reject the duplicates.
 */
final class Groovy5400 {

    @Test
    void testDuplicateCustomRuntimeAnnotationIsACompileError() {
        def err = compileFails '''
            import java.lang.annotation.*
            import static java.lang.annotation.RetentionPolicy.*
            import static java.lang.annotation.ElementType.*

            @Retention(RUNTIME)
            @Target([TYPE])
            @interface MyAnnotation { }

            @MyAnnotation
            @MyAnnotation
            @MyAnnotation
            class IAmWayTooAnnotated {}
        '''
        assert err.contains('Cannot specify duplicate annotation on the same member')
        assert err.contains('MyAnnotation')
        assert !err.contains('AnnotationFormatError')
    }

    @Test
    void testDuplicateDeprecatedAnnotationIsACompileError() {
        def err = compileFails '''
            @Deprecated
            @Deprecated
            class TooDeprecated {}
        '''
        assert err.contains('Cannot specify duplicate annotation on the same member')
        assert err.contains('Deprecated')
    }

    private static String compileFails(String src) {
        try {
            new GroovyClassLoader().parseClass(src)
            assert false, 'expected a compilation failure'
            return ''
        } catch (MultipleCompilationErrorsException e) {
            return e.message
        }
    }
}
