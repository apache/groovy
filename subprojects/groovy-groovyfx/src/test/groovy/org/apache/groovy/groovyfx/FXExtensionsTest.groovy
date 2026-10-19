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

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.NumberBinding
import javafx.beans.binding.StringExpression
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Test

/**
 * The extension methods need only {@code javafx.base}: no toolkit, no display.
 */
final class FXExtensionsTest {

    @Test
    void arithmeticOperatorsProduceLiveBindings() {
        def width = new SimpleDoubleProperty(100)
        def height = new SimpleDoubleProperty(50)

        def area = width * height
        def perimeter = (width + height) * 2
        def half = width / 2
        def diff = width - height
        def neg = -width

        assert area instanceof NumberBinding
        assert area.value == 5000.0d
        assert perimeter.value == 300.0d
        assert half.value == 50.0d
        assert diff.value == 50.0d
        assert neg.value == -100.0d

        width.set(200)
        assert area.value == 10000.0d
        assert perimeter.value == 500.0d
        assert half.value == 100.0d
        assert diff.value == 150.0d
        assert neg.value == -200.0d
    }

    @Test
    void constantOnTheLeft() {
        def count = new SimpleIntegerProperty(4)

        def plus = 10 + count
        def minus = 10 - count
        def times = 3 * count
        def over = 12 / count

        assert plus.value == 14
        assert minus.value == 6
        assert times.value == 12
        assert over.value == 3

        count.set(6)
        assert plus.value == 16
        assert minus.value == 4
        assert times.value == 18
        assert over.value == 2
    }

    @Test
    void integerArithmeticStaysIntegral() {
        def count = new SimpleIntegerProperty(7)
        assert (count + 1).value instanceof Integer
        assert (count + 1.5d).value instanceof Double
        assert (count * 2).value == 14
    }

    @Test
    void stringConcatenationIsLive() {
        def name = new SimpleStringProperty('Groovy')
        def age = new SimpleIntegerProperty(20)

        def greeting = 'Hello ' + name
        def summary = name + ' is ' + age + ' years old'

        assert greeting instanceof StringExpression
        assert greeting.value == 'Hello Groovy'
        assert summary.value == 'Groovy is 20 years old'

        name.set('GroovyFX')
        age.set(21)
        assert greeting.value == 'Hello GroovyFX'
        assert summary.value == 'GroovyFX is 21 years old'
    }

    @Test
    void booleanOperatorsAlreadyWork() {
        def a = new SimpleBooleanProperty(true)
        def b = new SimpleBooleanProperty(false)
        def both = a & b
        def either = a | b
        assert both instanceof BooleanBinding
        assert !both.value
        assert either.value
        b.set(true)
        assert both.value
    }

    @Test
    void onChangeDispatchesByArity() {
        def value = new SimpleIntegerProperty(1)
        def seen = []
        def listener = value.onChange { newValue -> seen << newValue }
        value.onChange { oldValue, newValue -> seen << [oldValue, newValue] }
        value.onChange { observable, oldValue, newValue -> seen << observable.is(value) }

        value.set(2)
        assert seen == [2, [1, 2], true]

        value.removeListener(listener)
        value.set(3)
        assert seen == [2, [1, 2], true, [2, 3], true]
    }

    @Test
    void onInvalidateDispatchesByArity() {
        def value = new SimpleStringProperty('a')
        def hits = []
        value.onInvalidate { hits << 'plain' }
        value.onInvalidate { observable -> hits << observable.is(value) }
        value.set('b')
        assert hits == ['plain', true]
    }
}
