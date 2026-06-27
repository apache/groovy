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
package org.apache.groovy.docgenerator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class MockSourceGeneratorTest {

    @Test
    void generatesMockSourcesFromJavaParserModel(@TempDir File tempDir) {
        def sourceDir = new File(tempDir, 'src').tap { mkdirs() }
        def outputDir = new File(tempDir, 'out').tap { mkdirs() }

        def instanceExtensions = new File(sourceDir, 'demo/SampleMethods.java')
        instanceExtensions.parentFile.mkdirs()
        instanceExtensions.text = '''\
            package demo;

            import java.util.List;

            public class SampleMethods {
                /**
                 * See {@link #append(List, String)}.
                 *
                 * @param self receiver
                 * @param suffix suffix
                 * @return appended value
                 */
                public static String append(List<String> self, String suffix) {
                    return suffix;
                }

                @Deprecated
                public static String skip(String self) {
                    return self;
                }
            }
            '''.stripIndent()

        def staticExtensions = new File(sourceDir, 'demo/SampleStaticMethods.java')
        staticExtensions.parentFile.mkdirs()
        staticExtensions.text = '''\
            package demo;

            import java.io.IOException;
            import java.util.Map;

            public class SampleStaticMethods {
                /**
                 * Combines entries.
                 *
                 * @param self receiver
                 * @param other other entry
                 * @return combined entry
                 * @throws IOException when broken
                 */
                public static <T extends Map.Entry<String, Integer>> T combine(Map.Entry<String, Integer> self, T other) throws IOException {
                    return other;
                }

                /**
                 * Counts values.
                 *
                 * @param self values
                 * @return value count
                 */
                public static int count(int[] self) {
                    return self.length;
                }
            }
            '''.stripIndent()

        new MockSourceGenerator([instanceExtensions, staticExtensions], outputDir).generateAll()

        def listMock = new File(outputDir, 'java/util/List.java').text
        assert listMock.contains('public interface List {')
        assert listMock.contains('See {@link List#append(String)}.')
        assert listMock.contains('@param suffix suffix')
        assert !listMock.contains('@param self receiver')
        assert listMock.contains('public java.lang.String append(java.lang.String suffix);')

        def mapMock = new File(outputDir, 'java/util/Map.java').text
        assert mapMock.contains('public interface Map {')
        assert mapMock.contains('public static interface Entry {')
        assert mapMock.contains('public static <T extends java.util.Map.Entry<java.lang.String, java.lang.Integer>> T combine(java.util.Map.Entry<java.lang.String, java.lang.Integer> self, T other) throws java.io.IOException { throw new UnsupportedOperationException(); }')
        assert mapMock.contains('@param self receiver')
        assert mapMock.contains('@param other other entry')

        def primitiveMock = new File(outputDir, 'primitives/PrimitiveIntArray.java').text
        assert primitiveMock.contains('@displayName int[]')
        assert primitiveMock.contains('public class PrimitiveIntArray {')
        assert primitiveMock.contains('public static int count(int[] self) { throw new UnsupportedOperationException(); }')

        assert !new File(outputDir, 'java/lang/String.java').exists()

        def manifest = new File(outputDir, MockSourceGenerator.MANIFEST_FILE).text
        assert manifest.contains('primitives\tPrimitiveIntArray\tprimitives-and-primitive-arrays\tint[]')
    }

    @Test
    void coversUtilityAndCliPaths(@TempDir File tempDir) {
        assert MockSourceGenerator.rewriteLinks('See {@link #noop()}.') == 'See {@link #noop()}.'
        assert MockSourceGenerator.rewriteLinks('See {@link #touch(String)}.') == 'See {@link String#touch()}.'
        assert MockSourceGenerator.resolveJdkClassName('A') == 'java.lang.Object'
        assert MockSourceGenerator.resolveJdkClassName('B[]') == 'java.lang.Object[]'
        assert MockSourceGenerator.resolveJdkClassName('java.lang.String') == 'java.lang.String'

        assert MockSourceGenerator.displayPackageFor('NoPackage', null) == ''
        assert MockSourceGenerator.mockPackageFor('NoPackage', null) == ''
        assert MockSourceGenerator.mockPackageFor('demo.Value', new ReceiverTypeInfo(primitive: true)) == MockSourceGenerator.PRIMITIVES_PKG
        assert MockSourceGenerator.mockClassNameFor('demo.Value', new ReceiverTypeInfo(primitive: true)) == 'PrimitiveValue'
        assert MockSourceGenerator.mockClassNameFor('demo.Value[][]', null) == 'ValueArrayArray'
        assert MockSourceGenerator.outerClassFqcn('demo.lower.name') == null
        assert MockSourceGenerator.outerClassFqcn('demo.Outer') == null
        assert MockSourceGenerator.outerClassFqcn('demo.Outer.Inner') == 'demo.Outer'

        def generator = new MockSourceGenerator([], tempDir)
        def serializeMethod = MockSourceGenerator.getDeclaredMethod('serializeMethod', JavaExtensionMethod, boolean, String)
        serializeMethod.accessible = true
        def rendered = serializeMethod.invoke(generator, new JavaExtensionMethod(
                name: 'touch',
                declaringClassName: 'TouchMethods',
                parameters: [new JavaExtensionParameter(type: 'java.lang.String', name: 'self')],
                returnType: null,
                javadoc: null
        ), false, '')
        assert !rendered.contains('/**')
        assert rendered.contains('public void touch() {  }')

        def bodyFor = MockSourceGenerator.getDeclaredMethod('bodyFor', JavaExtensionMethod)
        bodyFor.accessible = true
        assert bodyFor.invoke(null, new JavaExtensionMethod(returnType: null)) == ''
        assert bodyFor.invoke(null, new JavaExtensionMethod(returnType: 'void')) == ''
        assert bodyFor.invoke(null, new JavaExtensionMethod(returnType: 'int')) == 'throw new UnsupportedOperationException();'

        def sourceFile = new File(tempDir, 'demo/CliStaticMethods.java')
        sourceFile.parentFile.mkdirs()
        sourceFile.text = '''\
            package demo;

            public class CliStaticMethods {
                public static int inc(int self) {
                    return self + 1;
                }
            }
            '''.stripIndent()

        def outputDir = new File(tempDir, 'cli-out')

        MockSourceGenerator.main('--help')
        MockSourceGenerator.main('--outputDir', outputDir.absolutePath, sourceFile.absolutePath, 'org.codehaus.groovy.runtime.DefaultGroovyMethods')

        assert new File(outputDir, MockSourceGenerator.MANIFEST_FILE).exists()
        assert new File(outputDir, 'primitives/PrimitiveInt.java').exists()
    }
}
