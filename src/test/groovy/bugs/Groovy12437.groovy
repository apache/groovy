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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * A type variable of a superclass read from bytecode (or via reflection) had an
 * unresolved placeholder in its generics, so resolving the computed type of an
 * expression over an inherited generic field, e.g. a ternary or unary minus,
 * reported "unable to resolve class E" in the subclass.
 */
final class Groovy12437 {

    private static final String BASE = '''
        class Base<E, N extends Number> {
            E value
            N num
            List<E> list = []
            Map<String, E> map = [:]
            Map<String, E> map2 = [:]
            def take(Object... args) { args }
        }
    '''

    private static GroovyClassLoader compileAgainstPrecompiledBase(String childSource) {
        File baseDir = File.createTempDir(), childDir = File.createTempDir()
        try {
            def cu = new CompilationUnit(new CompilerConfiguration(targetDirectory: baseDir))
            cu.addSource('Base.groovy', BASE)
            cu.compile()

            def config = new CompilerConfiguration(targetDirectory: childDir, classpathList: [baseDir.path])
            cu = new CompilationUnit(config)
            cu.addSource('Child.groovy', childSource)
            cu.compile()

            def loader = new GroovyClassLoader(Groovy12437.classLoader)
            loader.addURL(baseDir.toURI().toURL())
            loader.addURL(childDir.toURI().toURL())
            // load eagerly, so the directories can be removed
            loader.loadClass('Base'); loader.loadClass('Child')
            loader
        } finally {
            baseDir.deleteDir()
            childDir.deleteDir()
        }
    }

    @Test
    void testTernaryOverInheritedGenericProperties() {
        def loader = compileAgainstPrecompiledBase '''
            @groovy.transform.CompileStatic
            class Child extends Base<String, Integer> {
                Map<String, String> pick(boolean flag) {
                    flag ? map : map2
                }
            }
        '''
        def child = loader.loadClass('Child').getDeclaredConstructor().newInstance()
        child.map.a = 'x'
        child.map2.b = 'y'
        assert child.pick(true) == [a: 'x']
        assert child.pick(false) == [b: 'y']
    }

    @ParameterizedTest
    @ValueSource(strings = [
        'f ? map : map2',
        'f ? value : value',
        'f ? num : num',
        'map ?: map2',
        '++value',
        'num++',
        '-value',
        '-list',
        '+num',
        '~value',
        '~list',
        '[*list]',
        'take(*list)',
        '[*:map]',
        '{ -> f ? map : map2 }()'
    ])
    void testExpressionOverInheritedGenericField(String expression) {
        compileAgainstPrecompiledBase """
            class Child extends Base<String, Integer> {
                def m(boolean f) { $expression }
            }
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['f ? dataModel : dataModel', 'dataModel ?: dataModel', '-dataModel'])
    void testExpressionOverInheritedJdkGenericField(String expression) {
        new GroovyClassLoader().parseClass """
            class Child extends javax.swing.JComboBox<String> {
                def m(boolean f) { $expression }
            }
        """
    }
}
