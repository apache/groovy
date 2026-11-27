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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.lang.module.ModuleDescriptor
import java.nio.file.Files
import java.nio.file.Path

import static groovy.test.GroovyAssert.assertScript

final class PackageTest {

    @Test
    void testPackages() {
        assertScript '''
            // tag::package_statement[]
            // defining a package named com.yoursite
            package com.yoursite
            // end::package_statement[]

            class Foo { }
            def foo = new Foo()

            assert foo != null
            assert Foo.class.name == 'com.yoursite.Foo'
        '''

        assertScript '''
            //tag::import_statement[]
            // importing the class MarkupBuilder
            import groovy.xml.MarkupBuilder

            // using the imported class to create an object
            def xml = new MarkupBuilder()

            assert xml != null
            // end::import_statement[]
        '''
    }

    @Test
    void testDefaultImports() {
        assertScript '''
            // tag::default_import[]
            long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            Date date = new Date(time)
            // end::default_import[]
        '''
    }

    @Test
    void testMultipleImportsFromSamePackage() {
        assertScript '''
            // tag::multiple_import[]
            import groovy.xml.MarkupBuilder
            import groovy.xml.StreamingMarkupBuilder

            def markupBuilder = new MarkupBuilder()

            assert markupBuilder != null

            assert new StreamingMarkupBuilder() != null
            // end::multiple_import[]
        '''
    }

    @Test
    void testStarImports() {
        assertScript '''
            // tag::star_import[]
            import groovy.xml.*

            def markupBuilder = new MarkupBuilder()

            assert markupBuilder != null

            assert new StreamingMarkupBuilder() != null
            // end::star_import[]
        '''
    }

    @Test
    void testStaticImports() {
        assertScript '''
            // tag::static_imports[]
            import static java.lang.Boolean.FALSE

            assert !FALSE //use directly, without Boolean prefix!
            // end::static_imports[]
        '''
    }

    @Test
    void testStaticImportAlias() {
        assertScript '''
            // tag::static_importswithas[]
            import static Calendar.getInstance as now

            assert now().class == Calendar.getInstance().class
            // end::static_importswithas[]
        '''
    }

    @Test
    void testStaticStarImport() {
        assertScript '''
            // tag::static_importswithstar[]
            import static java.lang.Math.*

            assert sin(0) == 0.0
            assert cos(0) == 1.0
            // end::static_importswithstar[]
        '''
    }

    @Test
    void testStaticStarImportSameMethodNameDifferentParameterType() {
        assertScript '''
            // tag::static_import_same_method_name_different_parameter_type[]
            import static java.lang.String.format // <1>

            class SomeClass {

                String format(Integer i) { // <2>
                    i.toString()
                }

                static void main(String[] args) {
                    assert format('String') == 'String' // <3>
                    assert new SomeClass().format(Integer.valueOf(1)) == '1'
                }
            }
            // end::static_import_same_method_name_different_parameter_type[]
        '''
    }

    @Test
    void testAliasImport() {
        assertScript '''
            // tag::alias_import[]
            import java.util.Date
            import java.sql.Date as SQLDate

            Date utilDate = new Date(1000L)
            SQLDate sqlDate = new SQLDate(1000L)

            assert utilDate instanceof java.util.Date
            assert sqlDate instanceof java.sql.Date
            // end::alias_import[]
        '''
    }

    @Test
    void testModuleImport() {
        assertScript '''
            // tag::module_import[]
            import module java.sql

            assert Connection.name == 'java.sql.Connection'
            assert DriverManager.name == 'java.sql.DriverManager'
            // end::module_import[]
        '''
    }

    @Test
    void testModuleImportTransitive() {
        assertScript '''
            // tag::module_import_transitive[]
            import module java.sql

            // Direct exports of java.sql
            assert Connection.name == 'java.sql.Connection'

            // From java.xml (requires transitive)
            assert Source.name == 'javax.xml.transform.Source'

            // From java.logging (requires transitive)
            assert Logger.name == 'java.util.logging.Logger'
            // end::module_import_transitive[]
        '''
    }

    @Test
    void testModuleImportWithExplicit() {
        assertScript '''
            // tag::module_import_with_explicit[]
            import module java.desktop
            import java.util.List

            // The explicit single-type import wins over the
            // module-expanded java.awt.List
            assert List.name == 'java.util.List'
            // end::module_import_with_explicit[]
        '''
    }

    @Test
    void testModuleImportShadowedByStar() {
        assertScript '''
            // tag::module_import_shadowed_by_star[]
            import module java.base
            import module java.desktop
            import java.util.*

            // The explicit package star import shadows both module imports (JLS §6.4.1)
            assert List.name == 'java.util.List'
            // end::module_import_shadowed_by_star[]
        '''
    }

    @Test
    void testModuleDeclaration(@TempDir Path tempDir) {
        def config = new CompilerConfiguration()
        config.targetDirectory = tempDir.toFile()
        def unit = new CompilationUnit(config)
        unit.addSource('Greeter.groovy', '''
            package com.example.app.api
            interface Greeter { String greet() }
        ''')
        unit.addSource('DefaultGreeter.groovy', '''
            package com.example.app.internal
            import com.example.app.api.Greeter
            class DefaultGreeter implements Greeter {
                String greet() { 'hello' }
            }
        ''')
        unit.addSource('module-info.groovy', '''
            // tag::module_declaration[]
            module com.example.app {
                requires java.logging;
                exports com.example.app.api;
                opens com.example.app.internal;
                uses com.example.app.api.Greeter;
                provides com.example.app.api.Greeter
                    with com.example.app.internal.DefaultGreeter;
            }
            // end::module_declaration[]
        ''')
        unit.compile()

        def descriptor = Files.newInputStream(tempDir.resolve('module-info.class')).withCloseable {
            ModuleDescriptor.read(it)
        }
        assert descriptor.name() == 'com.example.app'
        assert descriptor.requires()*.name().containsAll(['java.base', 'java.logging'])
        assert descriptor.exports()*.source() == ['com.example.app.api']
        assert descriptor.opens()*.source() == ['com.example.app.internal']
        assert descriptor.uses() == ['com.example.app.api.Greeter'] as Set
        assert descriptor.provides()*.service() == ['com.example.app.api.Greeter']
    }
}
