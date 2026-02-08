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
package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class BuilderGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> classWithDefaultStrategy = parseClass('''import groovy.transform.builder.*
      @Builder(builderStrategy=DefaultStrategy)
      class ClassUnderTest {
          String name
      }''')

    final Class<?> classWithSimpleStrategy = parseClass('''import groovy.transform.builder.*
      @Builder(builderStrategy=SimpleStrategy)
      class ClassUnderTest {
          String name
      }''')

    final Class<?> classWithExternalStrategy = parseClass('''
      class ClassUnderTest {
          String name
      }''')

    final Class<?> builderClassWithExternalStrategy = new GroovyClassLoader(classWithExternalStrategy.classLoader).parseClass('''import groovy.transform.builder.*
      @Builder(builderStrategy=ExternalStrategy, forClass=ClassUnderTest)
      class ClassUnderTestBuilder { }''')


    final Class<?> classWithInitializerStrategy = parseClass('''import groovy.transform.builder.*
      @Builder(builderStrategy=InitializerStrategy)
      class ClassUnderTest {
          String name
      }''')

    @Test
    void test_default_builder_is_annotated() {
        assertMethodIsAnnotated(classWithDefaultStrategy, 'builder')
    }

    @Test
    void test_default_class_is_not_annotated() {
        assertClassIsNotAnnotated(classWithDefaultStrategy)
    }

    @Test
    void test_default_builder_class_is_annotated() {
        Class<?> builderClass = classWithDefaultStrategy.classes.find { it.name.endsWith('Builder') }
        assertClassIsAnnotated(builderClass)
    }

    @Test
    void test_default_name_is_annotated() {
        Class<?> builderClass = classWithDefaultStrategy.classes.find { it.name.endsWith('Builder') }
        assertMethodIsAnnotated(builderClass, 'name', String)
    }

    @Test
    void test_default_build_is_annotated() {
        Class<?> builderClass = classWithDefaultStrategy.classes.find { it.name.endsWith('Builder') }
        assertMethodIsAnnotated(builderClass, 'build')
    }

    @Test
    void test_simple_class_is_not_annotated() {
        assertClassIsNotAnnotated(classWithSimpleStrategy)
    }

    @Test
    void test_simple_setName_is_annotated() {
        assertExactMethodIsAnnotated(classWithSimpleStrategy, 'setName', classWithSimpleStrategy, String)
    }

    @Test
    void test_external_class_is_not_annotated() {
        assertClassIsNotAnnotated(classWithExternalStrategy)
    }

    @Test
    void test_external_builder_class_is_not_annotated() {
        assertClassIsNotAnnotated(builderClassWithExternalStrategy)
    }

    @Test
    void test_external_name_is_annotated() {
        assertMethodIsAnnotated(builderClassWithExternalStrategy, 'name', String)
    }

    @Test
    void test_external_build_is_annotated() {
        assertMethodIsAnnotated(builderClassWithExternalStrategy, 'build')
    }

    @Test
    void test_initializer_class_is_not_annotated() {
        assertClassIsNotAnnotated(classWithInitializerStrategy)
    }

    @Test
    void test_initializer_class_constructor_is_annotated() {
        Class<?> initializerClass = classWithInitializerStrategy.classes.find { it.name.endsWith('Initializer') }
        assertConstructorIsAnnotated(classWithInitializerStrategy, initializerClass)
    }

    @Test
    void test_initializer_createInitializer_is_annotated() {
        assertMethodIsAnnotated(classWithInitializerStrategy, 'createInitializer')
    }

    @Test
    void test_inner_initializer_class_is_annotated() {
        Class<?> initializerClass = classWithInitializerStrategy.classes.find { it.name.endsWith('Initializer') }
        assertClassIsAnnotated(initializerClass)
    }

    @Test
    void test_inner_initializer_name_is_annotated() {
        Class<?> initializerClass = classWithInitializerStrategy.classes.find { it.name.endsWith('Initializer') }
        assertMethodIsAnnotated(initializerClass, 'name', String)
    }

    @Test
    void test_inner_initializer_create_is_annotated() {
        Class<?> initializerClass = classWithInitializerStrategy.classes.find { it.name.endsWith('Initializer') }
        assertMethodIsAnnotated(initializerClass, 'create')
    }
}