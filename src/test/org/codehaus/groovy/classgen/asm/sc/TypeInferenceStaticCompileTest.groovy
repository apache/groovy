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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.TypeInferenceSTCTest

/**
 * Unit tests for static type checking : type inference.
 */
class TypeInferenceStaticCompileTest extends TypeInferenceSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5655
    void testByteArrayInference() {
        assertScript '''
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE.makeArray()
                })
                def b = "foo".bytes
                new String(b)
            '''
    }

    @Override
    void testShouldNotThrowIncompatibleArgToFunVerifyError() {
        try {
            super.testShouldNotThrowIncompatibleArgToFunVerifyError()
        } finally {
//            println astTrees
        }
    }

    // GROOVY-
    void testGetAnnotationFail() {
            assertScript '''import groovy.transform.*
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD])
            @interface Ann1 {}
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD])
            @interface Ann2 {}

            class A {
                @Ann2
                String field
            }

                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    lookup('second').each {
                      assert it.expression.getNodeMetaData(INFERRED_TYPE).name == 'Ann2'
                    }
                })
                def doit(obj, String propName) {
                def field = obj.getClass().getDeclaredField propName
                println field
                if(field) {
                    @ASTTest(phase=INSTRUCTION_SELECTION,value={
                        assert node.getNodeMetaData(INFERRED_TYPE).name == 'Ann1'
                    })
                    def annotation = field.getAnnotation Ann1
                    if(true) {
                        second: annotation = field.getAnnotation Ann2
                    }
                    return annotation
                }
                return null
            }

            assert Ann2.isAssignableFrom (doit(new A(), "field").class)
            '''
    }
}

