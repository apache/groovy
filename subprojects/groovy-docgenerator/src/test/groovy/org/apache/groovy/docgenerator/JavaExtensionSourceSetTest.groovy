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

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.type.IntersectionType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.ast.type.UnionType
import com.github.javaparser.ast.type.VoidType
import com.github.javaparser.ast.type.WildcardType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertThrows

class JavaExtensionSourceSetTest {

    @Test
    void buildsMethodViewsAndTypeInfo(@TempDir File tempDir) {
        def sourceSet = new JavaExtensionSourceSet()
        sourceSet.addSource(null)
        sourceSet.addSource(new File(tempDir, 'missing/Nope.java'))

        def complex = new File(tempDir, 'demo/ComplexMethods.java')
        complex.parentFile.mkdirs()
        complex.text = '''\
            package demo;

            import java.io.IOException;
            import java.util.List;

            public class ComplexMethods {
                public static void noReceiver() {}

                @Deprecated
                public static String oldOne(String self) {
                    return self;
                }

                public interface Marker {}

                public static class Nested {
                    public static String nested(Marker self) {
                        return self.toString();
                    }
                }

                /**
                 * Description.
                 * @param self receiver
                 * @param values values
                 * @return first value
                 */
                public static <T extends Number & Comparable<T>> T first(List<? extends Number> self, T... values) throws IOException {
                    return values[0];
                }
            }
            '''.stripIndent()

        def plain = new File(tempDir, 'PlainMethods.java')
        plain.text = '''\
            public class PlainMethods {
                public static int ping(int self) {
                    return self;
                }
            }
            '''.stripIndent()

        sourceSet.addSource(complex)
        sourceSet.addSource(plain)

        def methods = sourceSet.methods
        assert methods*.name.containsAll(['noReceiver', 'oldOne', 'nested', 'first', 'ping'])

        def noReceiver = methods.find { it.name == 'noReceiver' }
        assert noReceiver.receiverTypeName == null
        assert noReceiver.receiverTypeInfo == null
        assert noReceiver.returnType == 'void'

        def first = methods.find { it.name == 'first' }
        assert first.receiverTypeName == 'java.util.List'
        assert first.receiverTypeInfo.interfaceType
        assert first.parameters*.name == ['self', 'values']
        assert first.parameters[1].varArgs
        assert first.parameters[1].type == 'T'
        assert first.typeParameters == ['T extends java.lang.Number & java.lang.Comparable<T>']
        assert first.exceptions == ['java.io.IOException']

        def deprecatedMethod = methods.find { it.name == 'oldOne' }
        assert deprecatedMethod.deprecated

        assert sourceSet.methods.size() == methods.size()

        assert sourceSet.typeInfoForFqcn('demo.ComplexMethods.Marker').interfaceType
        assert !sourceSet.typeInfoForFqcn('demo.ComplexMethods').interfaceType
        assert sourceSet.typeInfoForFqcn('java.util.List').interfaceType
        assert sourceSet.typeInfoForFqcn('int').primitive
        assert sourceSet.typeInfoForFqcn('[]').canonicalName == '[]'
        assert sourceSet.typeInfoForFqcn(null).canonicalName == null
        assert !sourceSet.typeInfoForFqcn('does.not.Exist').interfaceType
    }

    @Test
    void handlesBrokenSourcesAndClassLoading(@TempDir File tempDir) {
        def broken = new File(tempDir, 'Broken.java')
        broken.bytes = [0, 1, 2] as byte[]

        def sourceSet = new JavaExtensionSourceSet()
        def error = assertThrows(IllegalStateException) {
            sourceSet.addSource(broken)
        }
        assert error.message.contains('Unable to parse')

        assert JavaExtensionSourceSet.tryLoad(null) == null
        assert JavaExtensionSourceSet.tryLoad('') == null
        assert JavaExtensionSourceSet.tryLoad('java.util.Map.Entry') == java.util.Map.Entry
        assert JavaExtensionSourceSet.tryLoad('no.such.Type') == null
    }

    @Test
    void resolvesContextTypesAcrossImportsAndBounds() {
        def unit = StaticJavaParser.parse('''\
            package demo.pkg;

            import foo.bar;
            import java.time.*;
            import java.util.List;
            import java.util.Map;
            import static java.util.Collections.emptyList;

            class LocalType {}
            class Another {}
            '''.stripIndent())

        Map<String, Set<String>> simpleNameIndex = [
                Unique   : ['x.y.Unique'] as Set<String>,
                Ambiguous: ['a.b.Ambiguous', 'c.d.Ambiguous'] as Set<String>
        ]
        Set<String> knownTypes = ['demo.pkg.LocalType', 'java.time.LocalDate'] as Set<String>

        def context = new JavaExtensionContext(unit, simpleNameIndex, knownTypes)

        assert context.resolveTypeName(null) == null
        assert context.resolveTypeName('int') == 'int'
        assert context.resolveTypeName('void') == 'void'
        assert context.resolveTypeName('T', ['T'] as Set<String>) == 'T'
        assert context.resolveTypeName('Map.Entry') == 'java.util.Map.Entry'
        assert context.resolveTypeName('bar.Baz') == 'foo.bar.Baz'
        assert context.resolveTypeName('demo.inner.Name') == 'demo.inner.Name'
        assert context.resolveTypeName('LocalType') == 'demo.pkg.LocalType'
        assert context.resolveTypeName('LocalDate') == 'java.time.LocalDate'
        assert context.resolveTypeName('String') == 'java.lang.String'
        assert context.resolveTypeName('Unique') == 'x.y.Unique'
        assert context.resolveTypeName('Ambiguous') == 'Ambiguous'
        assert context.resolveTypeName('Another') == 'Another'
        assert context.resolveTypeName('Unknown.Type') == 'Unknown.Type'

        assert context.renderType(new VoidType()) == 'void'
        assert context.renderType(StaticJavaParser.parseType('String[]'), Collections.emptySet(), true) == 'java.lang.String...'
        assert context.renderType(StaticJavaParser.parseType('Map')) == 'java.util.Map'
        assert context.renderType(StaticJavaParser.parseType('Map.Entry<String, Integer>')) == 'java.util.Map.Entry<java.lang.String, java.lang.Integer>'
        assert context.renderType(new WildcardType().setExtendedType(StaticJavaParser.parseClassOrInterfaceType('Number'))) == '? extends java.lang.Number'
        assert context.renderType(new WildcardType().setSuperType(StaticJavaParser.parseClassOrInterfaceType('Integer'))) == '? super java.lang.Integer'
        assert context.renderType(new WildcardType()) == '?'

        Type intersection = new IntersectionType(NodeList.nodeList(
                StaticJavaParser.parseType('Runnable'),
                StaticJavaParser.parseType('java.io.Closeable')
        ))
        assert context.renderType(intersection) == 'java.lang.Runnable & java.io.Closeable'

        Type union = new UnionType(NodeList.nodeList(
                StaticJavaParser.parseType('java.io.IOException'),
                StaticJavaParser.parseType('RuntimeException')
        ))
        assert context.renderType(union) == 'java.io.IOException | java.lang.RuntimeException'
        assert context.renderType(StaticJavaParser.parseType('int')) == 'int'

        assert context.eraseType(StaticJavaParser.parseType('List<String>[]')) == 'java.util.List[]'
        assert context.eraseType(StaticJavaParser.parseType('Map.Entry<String, Integer>')) == 'java.util.Map.Entry'

        def holder = StaticJavaParser.parse('class Holder<T extends Number & Comparable<T>, U> {}').types[0]
        assert context.renderTypeParameter(holder.typeParameters[0], [] as Set<String>) == 'T extends java.lang.Number & java.lang.Comparable<T>'
        assert context.renderTypeParameter(holder.typeParameters[1], [] as Set<String>) == 'U'
    }

    @Test
    void parsesJavadocInfoWithMalformedAndMultilineTags() {
        def declaration = StaticJavaParser.parseBodyDeclaration('''\
            /**
             * Summary line.
             * @
             * @since
             * 1.0
             * @param value first line
             * second line
             */
            void sample() {}
            '''.stripIndent())
        def info = JavadocInfo.parse(declaration.javadocComment.orElse(null))

        assert info.description == 'Summary line.'
        assert info.tags*.name == ['since', 'param']
        assert info.tags[0].value == '1.0'
        assert info.tags[1].value.trim() == 'value first line\nsecond line'
        assert JavadocInfo.parse(null).description == ''
    }
}
