/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Unit tests for static type checking : miscellaneous tests.
 *
 * @author Cedric Champeau
 */
class GetAnnotationStaticCompileTest extends AbstractBytecodeTestCase {

    void testGetAnnotationShouldNotProduceProxy() {
        def bytecode = compile([method:'m'],'''import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target
            import java.lang.annotation.ElementType
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.TYPE])
            public @interface MyAnnotation {
                String value() default 'hello'
            }

            @MyAnnotation
            class AnnotatedClass {}

            @groovy.transform.CompileStatic
            static String m(Class clazz) {
                MyAnnotation annotation = clazz.getAnnotation(MyAnnotation)
                annotation?.value()
            }
            m(AnnotatedClass)
        ''')
        println bytecode
        clazz.newInstance().main()
    }

}

