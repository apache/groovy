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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

@CompileStatic
class RecordTest {
    @Test
    void testRecordOnJDK16plus() {
        assumeTrue(isAtLeastJdk('16.0'))
        assertScript(new GroovyShell(new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK16)), '''
            record RecordJDK16plus(String name) {}
            assert java.lang.Record == RecordJDK16plus.class.getSuperclass()
        ''')
    }

    @Test
    void testRecordOnJDK16plusWhenDisabled() {
        assumeTrue(isAtLeastJdk('16.0'))
        def configuration = new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK16, recordsNative: false)
        assertScript(new GroovyShell(configuration), '''
            record RecordJDK16plus2(String name) {}
            assert java.lang.Record != RecordJDK16plus2.class.getSuperclass()
        ''')
    }

    @Test
    void testInnerRecordIsImplicitlyStatic() {
        assertScript '''
            class Test {
                record Point(int i, int j) {}
            }
            assert java.lang.reflect.Modifier.isStatic(Test$Point.modifiers)
        '''
    }

    @Test
    void testNativeRecordOnJDK16plus() {
        assumeTrue(isAtLeastJdk('16.0'))
        assertScript(new GroovyShell(new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK16)), '''
            import java.lang.annotation.Annotation
            import java.lang.annotation.Documented
            import java.lang.annotation.ElementType
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target
            import java.lang.reflect.RecordComponent
            
            @Documented
            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.RECORD_COMPONENT])
            @interface NotNull {}
            
            record Person(@NotNull String name, int age, List<String> locations, String[] titles) {}
            
            RecordComponent[] rcs = Person.class.getRecordComponents()
            assert 4 == rcs.length
            
            assert 'name' == rcs[0].name && String.class == rcs[0].type
            Annotation[] annotations = rcs[0].getAnnotations()
            assert 1 == annotations.length
            assert NotNull.class == annotations[0].annotationType()
            
            assert 'age' == rcs[1].name && int.class == rcs[1].type
            
            assert 'locations' == rcs[2].name && List.class == rcs[2].type
            assert 'Ljava/util/List<Ljava/lang/String;>;' == rcs[2].genericSignature
            assert 'java.util.List<java.lang.String>' == rcs[2].genericType.toString()
            
            assert 'titles' == rcs[3].name && String[].class == rcs[3].type
        ''')
    }

    @Test
    @CompileDynamic
    void testNativeRecordOnJDK16plus_java() {
        assumeTrue(isAtLeastJdk('16.0'))

        def config = new CompilerConfiguration(
                targetDirectory: File.createTempDir(),
                jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            def b = new File(parentDir, 'Person.java')
            b.write '''
                import java.lang.annotation.*;
                
                public record Person(@NotNull String name, int age, java.util.List<String> locations, String[] titles) {}
                
                @Documented
                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.RECORD_COMPONENT})
                @interface NotNull {}
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(b)
            cu.compile()

            Class personClazz = loader.loadClass("Person")
            Class notNullClazz = loader.loadClass("NotNull")

            def rcs = personClazz.getRecordComponents()
            assert 4 == rcs.length

            assert 'name' == rcs[0].name && String.class == rcs[0].type
            def annotations = rcs[0].getAnnotations()
            assert 1 == annotations.length
            assert notNullClazz == annotations[0].annotationType()

            assert 'age' == rcs[1].name && int.class == rcs[1].type

            assert 'locations' == rcs[2].name && List.class == rcs[2].type
            assert 'Ljava/util/List<Ljava/lang/String;>;' == rcs[2].genericSignature
            assert 'java.util.List<java.lang.String>' == rcs[2].genericType.toString()

            assert 'titles' == rcs[3].name && String[].class == rcs[3].type

            ClassNode personClassNode = ClassHelper.make(personClazz)
            assert personClassNode.isRecord()
            def rcns = personClassNode.getRecordComponentNodes()
            assert 4 == rcns.size()
            assert 'name' == rcns[0].name && ClassHelper.STRING_TYPE == rcns[0].type
            List<AnnotationNode> annotationNodes = rcns[0].getAnnotations()
            assert 1 == annotationNodes.size()
            assert ClassHelper.make(notNullClazz) == annotationNodes[0].getClassNode()

            assert 'age' == rcns[1].name && ClassHelper.int_TYPE == rcns[1].type

            assert 'locations' == rcns[2].name && ClassHelper.LIST_TYPE == rcns[2].type
            def genericsTypes = rcns[2].type.genericsTypes
            assert 1 == genericsTypes.size()
            assert ClassHelper.STRING_TYPE == genericsTypes[0].type

            assert 'titles' == rcns[3].name && ClassHelper.make(String[].class) == rcns[3].type
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    private static void doTestNativeRecordClassNode(ClassNode personClassNode, ClassNode notNullClassNode) {
        assert personClassNode.isRecord()
        def rcns = personClassNode.getRecordComponentNodes()
        assert 4 == rcns.size()
        assert 'name' == rcns[0].name && ClassHelper.STRING_TYPE == rcns[0].type
        List<AnnotationNode> annotationNodes = rcns[0].getAnnotations()
        assert 1 == annotationNodes.size()
        assert notNullClassNode == annotationNodes[0].getClassNode()

        assert 'age' == rcns[1].name && ClassHelper.int_TYPE == rcns[1].type

        assert 'locations' == rcns[2].name && ClassHelper.LIST_TYPE == rcns[2].type
        def genericsTypes = rcns[2].type.genericsTypes
        assert 1 == genericsTypes.size()
        assert ClassHelper.STRING_TYPE == genericsTypes[0].type

        assert 'titles' == rcns[3].name && ClassHelper.make(String[].class) == rcns[3].type
    }

    @Test
    void testNativeRecordOnJDK16plus2_java() {
        assumeTrue(isAtLeastJdk('16.0'))
        assertScript '''
            import org.codehaus.groovy.ast.*
            
            def cn = ClassHelper.make(jdk.net.UnixDomainPrincipal.class)
            assert cn.isRecord()
            def rcns = cn.getRecordComponentNodes()
            assert 2 == rcns.size()
            assert 'user' == rcns[0].name && 'java.nio.file.attribute.UserPrincipal' == rcns[0].type.name
            assert 'group' == rcns[1].name && 'java.nio.file.attribute.GroupPrincipal' == rcns[1].type.name
        '''
    }
}
