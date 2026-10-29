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
import gls.CompilableTestSupport
import org.junit.jupiter.api.Test

/**
 * The documentation examples for the groovyfx chapter.
 */
final class GroovyFXSpecTest extends CompilableTestSupport {

    @Test
    void testHelloWorld() {
        shouldCompile '''
        // tag::hello_world[]
        import org.apache.groovy.groovyfx.GroovyFX

        GroovyFX.start {
            stage(title: 'Hello GroovyFX', show: true) {
                scene(width: 300, height: 120) {
                    vbox(spacing: 10, padding: 20) {
                        label('Welcome to GroovyFX')
                        button('Click me', onAction: { println 'clicked' })
                    }
                }
            }
        }
        // end::hello_world[]
        '''
    }

    @Test
    void testLayoutsAndConstraints() {
        shouldCompile '''
        // tag::layouts[]
        import org.apache.groovy.groovyfx.SceneGraphBuilder

        def builder = SceneGraphBuilder.fxBuilder {
            borderPane(id: 'root') {
                top { menuBar { menu('File') { menuItem('Exit', onAction: { System.exit(0) }) } } }
                center(margin: 10) {
                    gridPane(hgap: 8, vgap: 8) {
                        label('Name', row: 0, column: 0, halignment: 'RIGHT')
                        textField(id: 'name', row: 0, column: 1, hgrow: 'ALWAYS')
                        label('Age', row: 1, column: 0, halignment: 'RIGHT')
                        spinner(id: 'age', row: 1, column: 1)
                    }
                }
                bottom { hbox(alignment: 'CENTER_RIGHT', padding: 10) { button('Save') } }
            }
        }
        assert builder.root.center != null
        // end::layouts[]
        '''
    }

    @Test
    void testBindableModel() {
        shouldCompile '''
        // tag::fxbindable[]
        import javafx.beans.value.ChangeListener
        import org.apache.groovy.groovyfx.beans.FXBindable

        @FXBindable
        class Person {
            String name
            int age
        }

        def person = new Person(name: 'Ada', age: 36)
        person.nameProperty().addListener({ observable, oldValue, newValue ->
            println "renamed to $newValue"
        } as ChangeListener)
        person.name = 'Ada Lovelace'      // prints: renamed to Ada Lovelace
        assert person.ageProperty().value == 36
        // end::fxbindable[]
        '''
    }

    @Test
    void testBindableAdapter() {
        shouldCompile '''
        // tag::fxbindable_adapter[]
        import groovy.beans.Bindable
        import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder

        @Bindable
        class Person {
            String name
        }

        def person = new Person(name: 'Ada')
        def name = JavaBeanStringPropertyBuilder.create().bean(person).name('name').build()
        name.addListener { observable, oldValue, newValue -> println "renamed to $newValue" }
        person.name = 'Ada Lovelace'      // prints: renamed to Ada Lovelace
        name.set('Lady Lovelace')         // writes through to person.name
        // end::fxbindable_adapter[]
        '''
    }

    @Test
    void testBindingAttributes() {
        shouldCompile '''
        // tag::binding[]
        import org.apache.groovy.groovyfx.SceneGraphBuilder
        import org.apache.groovy.groovyfx.beans.FXBindable

        @FXBindable
        class Model {
            String name = 'World'
            double size = 100
        }
        def model = new Model()

        SceneGraphBuilder.fxBuilder {
            vbox {
                textField(text: model.nameProperty())                        // <1>
                label(text: bind(model.nameProperty(), converter: { "Hello, $it!" })) // <2>
                rectangle(width: model.sizeProperty(), height: model.sizeProperty() / 2) // <3>
            }
        }
        // end::binding[]
        '''
    }

    @Test
    void testOperators() {
        shouldCompile '''
        // tag::operators[]
        import javafx.beans.property.SimpleDoubleProperty
        import javafx.beans.property.SimpleStringProperty

        def width = new SimpleDoubleProperty(200)
        def height = new SimpleDoubleProperty(100)
        def title = new SimpleStringProperty('Chart')

        def area = width * height                 // NumberBinding
        def margin = (width + height) / 20        // NumberBinding
        def heading = title + ' (' + area + ')'   // StringExpression

        width.set(300)
        assert area.value == 30000.0d
        assert heading.value == 'Chart (30000.0)'

        area.onChange { newValue -> println "area is now $newValue" }
        // end::operators[]
        '''
    }

    @Test
    void testThreading() {
        shouldCompile '''
        // tag::threading[]
        import org.apache.groovy.groovyfx.SceneGraphBuilder

        def builder = new SceneGraphBuilder()
        builder.fx {                       // runs on the JavaFX application thread and waits
            stage(id: 'main') { scene { label(id: 'status', text: 'Loading') } }
        }
        Thread.start {
            def result = 'done'            // some background work
            builder.fxLater { status.text = result }
        }
        // end::threading[]
        '''
    }
}
