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

import groovy.test.GroovyTestCase

/**
 * Specification tests for the traits feature
 */
class SealedSpecificationTest extends GroovyTestCase {

    void testSealedADT() {
        assertScript '''
// tag::sealed_ADT[]
import groovy.transform.*

@Sealed interface Tree<T> {}
@Singleton final class Empty implements Tree {
    String toString() { 'Empty' }
}
@Canonical final class Node<T> implements Tree<T> {
    T value
    Tree<T> left, right
}

Tree<Integer> tree = new Node<>(42, new Node<>(0, Empty.instance, Empty.instance), Empty.instance)
assert tree.toString() == 'Node(42, Node(0, Empty, Empty), Empty)'
// end::sealed_ADT[]
'''
    }

    void testSimpleSealedHierarchyInterfaces() {
        assertScript '''
import groovy.transform.Sealed

// tag::simple_interface[]
@Sealed(permittedSubclasses=[Circle,Square]) interface ShapeI { }
final class Circle implements ShapeI { }
final class Square implements ShapeI { }
// end::simple_interface[]

assert [new Circle(), new Square()]*.class.name == ['Circle', 'Square']
'''
    }

    void testSimpleSealedHierarchyClasses() {
        assertScript '''
import groovy.transform.Sealed
import groovy.transform.NonSealed

// tag::general_sealed_class[]
@Sealed(permittedSubclasses=[Circle,Polygon,Rectangle]) class Shape { }

final class Circle extends Shape { }

class Polygon extends Shape { }
@NonSealed class RegularPolygon extends Polygon { }
final class Hexagon extends Polygon { }

@Sealed(permittedSubclasses=Square) class Rectangle extends Shape { }
final class Square extends Rectangle { }
// end::general_sealed_class[]

assert [new Circle(), new Square(), new Hexagon()]*.class.name == ['Circle', 'Square', 'Hexagon']
'''
    }

    void testEnum() {
        assertScript '''
// tag::weather_enum[]
enum Weather { Rainy, Cloudy, Sunny }
def forecast = [Weather.Rainy, Weather.Sunny, Weather.Cloudy]
assert forecast.toString() == '[Rainy, Sunny, Cloudy]'
// end::weather_enum[]
'''
    }

    void testSealedWeather() {
        assertScript '''
import groovy.transform.*

// tag::weather_sealed[]
@Sealed abstract class Weather { }
@Immutable(includeNames=true) class Rainy extends Weather { Integer expectedRainfall }
@Immutable(includeNames=true) class Sunny extends Weather { Integer expectedTemp }
@Immutable(includeNames=true) class Cloudy extends Weather { Integer expectedUV }
def forecast = [new Rainy(12), new Sunny(35), new Cloudy(6)]
assert forecast.toString() == '[Rainy(expectedRainfall:12), Sunny(expectedTemp:35), Cloudy(expectedUV:6)]'
// end::weather_sealed[]
'''
    }
}
