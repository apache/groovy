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
package org.apache.groovy.groovyfx

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * The transform needs only {@code javafx.base}: no toolkit, no display.
 */
final class FXBindableTest {

    private static Object evaluate(String script) {
        new GroovyShell().evaluate(script)
    }

    private static String compileError(String script) {
        shouldFail(MultipleCompilationErrorsException) { evaluate(script) }.message
    }

    @Test
    void propertyLevelAnnotationGeneratesJavaFXProperties() {
        def person = evaluate '''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Person {
                @FXBindable String name
                @FXBindable int age = 21
                String nickname
            }
            new Person(name: 'Ada')
        '''
        assert person.name == 'Ada'
        assert person.age == 21
        assert person.nameProperty() instanceof StringProperty
        assert person.ageProperty() instanceof IntegerProperty
        assert person.nameProperty().bean.is(person)
        assert person.nameProperty().name == 'name'
        assert person.nameProperty().value == 'Ada'
        assert person.ageProperty().value == 21

        // the plain property is left alone
        assert !person.respondsTo('nicknameProperty')

        // setter and property stay in step, in both directions
        person.name = 'Grace'
        assert person.nameProperty().value == 'Grace'
        person.ageProperty().set(36)
        assert person.age == 36
    }

    @Test
    void propertyIsCreatedLazilyAndRemembersValuesSetBeforehand() {
        def person = evaluate '''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Person { @FXBindable String name }
            new Person()
        '''
        person.name = 'Ada'
        assert person.@'name$property' == null
        assert person.name == 'Ada'
        def property = person.nameProperty()
        assert property.value == 'Ada'
        assert person.@'name$property'.is(property)
        assert person.nameProperty().is(property)
    }

    @Test
    void classLevelAnnotationCoversAllEligibleProperties() {
        def model = evaluate '''
            import org.apache.groovy.groovyfx.beans.FXBindable
            import javafx.collections.ObservableList
            import javafx.collections.FXCollections
            @FXBindable
            class Model {
                boolean enabled = true
                double ratio = 0.5
                ObservableList<String> items = FXCollections.observableArrayList('a')
                Date when
                static int instances = 0
                final String constant = 'fixed'
            }
            new Model()
        '''
        assert model.enabledProperty() instanceof BooleanProperty
        assert model.ratioProperty() instanceof DoubleProperty
        assert model.itemsProperty() instanceof ListProperty
        assert model.whenProperty() instanceof ObjectProperty
        assert model.enabled
        assert model.isEnabled()
        assert model.items == ['a']
        assert !model.respondsTo('instancesProperty')
        assert !model.respondsTo('constantProperty')

        model.enabledProperty().set(false)
        assert !model.enabled
        model.items = FXCollections.observableArrayList('b', 'c')
        assert model.itemsProperty().value == ['b', 'c']
    }

    @Test
    void smallIntegralTypesWidenAndCharStaysAnObject() {
        def model = evaluate '''
            import org.apache.groovy.groovyfx.beans.FXBindable
            @FXBindable
            class Model {
                short count = 3
                Byte flag = 1
                char initial = 'A'
            }
            new Model()
        '''
        assert model.countProperty() instanceof IntegerProperty
        assert model.flagProperty() instanceof IntegerProperty
        assert model.initialProperty() instanceof ObjectProperty
        assert model.countProperty().value == 3
        model.countProperty().set(5)
        assert model.count == 5 as short
        assert model.initial == 'A' as char
        assert model.initialProperty().value == 'A' as char
    }

    @Test
    void propertiesBindAndListenLikeAnyJavaFXProperty() {
        def pair = evaluate '''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Person { @FXBindable String name }
            [new Person(name: 'Ada'), new Person()]
        '''
        def (source, target) = pair
        target.nameProperty().bind(source.nameProperty())
        assert target.name == 'Ada'
        def seen = []
        source.nameProperty().addListener({ observable, oldValue, newValue -> seen << newValue } as ChangeListener)
        source.name = 'Grace'
        assert target.name == 'Grace'
        assert seen == ['Grace']
    }

    @Test
    void worksUnderStaticCompilation() {
        def result = evaluate '''
            import groovy.transform.CompileStatic
            import org.apache.groovy.groovyfx.beans.FXBindable
            import javafx.beans.property.StringProperty

            @CompileStatic
            class Person {
                @FXBindable String name
            }

            @CompileStatic
            String exercise() {
                Person p = new Person(name: 'Ada')
                StringProperty property = p.nameProperty()
                property.set(property.get() + ' Lovelace')
                p.name
            }
            exercise()
        '''
        assert result == 'Ada Lovelace'
    }

    @Test
    void rejectsStaticFinalAndNonProperties() {
        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad { @FXBindable static String name }
        ''').contains('cannot annotate a static property')

        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad { @FXBindable final String name = 'x' }
        ''').contains('cannot annotate a final property')

        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad { @FXBindable private String name }
        ''').contains('must be on a property, not a field')
    }

    @Test
    void rejectsPropertiesItCannotWrap() {
        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            import javafx.beans.property.SimpleStringProperty
            class Bad { @FXBindable SimpleStringProperty name = new SimpleStringProperty() }
        ''').contains('already a JavaFX property')

        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad {
                @FXBindable String name
                void setName(String value) { this.name = value }
            }
        ''').contains('a setter is declared explicitly')

        assert compileError('''
            import org.apache.groovy.groovyfx.beans.FXBindable
            import javafx.beans.property.StringProperty
            class Bad {
                @FXBindable String name
                StringProperty nameProperty() { null }
            }
        ''').contains('nameProperty() is declared explicitly')
    }

    @Test
    void rejectsCombinationWithBindableInEitherOrder() {
        def expected = "cannot annotate property 'name': it is also @Bindable"

        assert compileError('''
            import groovy.beans.Bindable
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad { @Bindable @FXBindable String name }
        ''').contains(expected)

        assert compileError('''
            import groovy.beans.Bindable
            import org.apache.groovy.groovyfx.beans.FXBindable
            class Bad { @FXBindable @Bindable String name }
        ''').contains(expected)

        assert compileError('''
            import groovy.beans.Bindable
            import org.apache.groovy.groovyfx.beans.FXBindable
            @Bindable class Bad { @FXBindable String name }
        ''').contains(expected)

        assert compileError('''
            import groovy.beans.Bindable
            import org.apache.groovy.groovyfx.beans.FXBindable
            @FXBindable class Bad { @Bindable String name }
        ''').contains(expected)
    }

    @Test
    void bindablePropertyCanBeAdaptedToAJavaFXProperty() {
        def result = evaluate '''
            import groovy.beans.Bindable
            import javafx.beans.property.SimpleStringProperty
            import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder

            class Person { @Bindable String name }

            def person = new Person(name: 'Ada')
            def name = JavaBeanStringPropertyBuilder.create().bean(person).name('name').build()
            def mirror = new SimpleStringProperty()
            mirror.bind(name)
            person.name = 'Ada Lovelace'
            name.set('Lady Lovelace')
            [mirror.get(), person.name]
        '''
        assert result == ['Lady Lovelace', 'Lady Lovelace']
    }
}
