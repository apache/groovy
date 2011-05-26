/*
 * Copyright 2008-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform

import groovy.transform.AutoClone
import groovy.transform.AutoExternalize
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import static groovy.transform.AutoCloneStyle.COPY_CONSTRUCTOR
import static groovy.transform.AutoCloneStyle.SERIALIZATION
import groovy.transform.ToString

/**
 * @author Paul King
 */
class CanonicalComponentsTransformTest extends GroovyShellTestCase {

    void testCloningWithFinalFields() {
        def p1 = new Person1('John', 'Smith')
        def p2 = p1.clone()
        def c1 = new Customer1('John', 'Smith', ['ipod', 'shiraz'], new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert p1 == p2
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert c1 == c2
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithNonFinalFields() {
        def p1 = new Person2(first:'John', last:'Smith')
        def p2 = p1.clone()
        def c1 = new Customer2(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithSerializable() {
        def c1 = new Customer3(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithExternalizable() {
        def c1 = new Customer4(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert c1 instanceof Externalizable
        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithDefaultObjectClone() {
        def c1 = new Customer5(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], age:25)
        def c2 = c1.clone()
        assert c1.first == c2.first
        assert c1.last == c2.last
        assert c1.favItems == c2.favItems
        // no need to use includeFields=true here because of Object clone!
        assert c1.agePeek() == c2.agePeek()
    }

    // GROOVY-4786
    void testExcludesWithEqualsAndHashCode() {
        def p1 = new PointIgnoreY(x:1, y:10)
        def p2 = new PointIgnoreY(x:1, y:100)
        assert p1 == p2
        assert p1.hashCode() == p2.hashCode()
    }

    // GROOVY-4844
    void testToStringCustomGetter() {
        def p1 = new Point(1, 2)
        def p2 = new Point(1, 1) { int getY() { 2 } }
        assert p1.toString() == 'org.codehaus.groovy.transform.Point(1, 2)'
        assert p2.toString() == 'org.codehaus.groovy.transform.Point(1, 2)'
    }
}

@TupleConstructor
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
class Person1 { final String first, last }

@TupleConstructor(includeSuperProperties=true, callSuper=true)
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
class Customer1 extends Person1 { final List favItems; final Date since }

@AutoClone(style=COPY_CONSTRUCTOR)
class Person2 { String first, last }

@AutoClone(style=COPY_CONSTRUCTOR)
class Customer2 extends Person2 { List favItems; Date since }

@AutoClone(style=SERIALIZATION)
@EqualsAndHashCode
class Customer3 implements Serializable {
    String first, last
    List favItems
    Date since
    int bonus
}

@AutoExternalize
@AutoClone(style=SERIALIZATION)
@EqualsAndHashCode
class Customer4 {
    String first, last
    List favItems
    Date since
    int bonus
}

@AutoClone
class Customer5 {
    String first, last
    List favItems
    private int age
    int agePeek() { age }
}

// GROOVY-4786
@EqualsAndHashCode(excludes="y")
class PointIgnoreY {
    int x
    int y // y coordinate excluded from Equals and hashCode
}

// GROOVY-4844
@TupleConstructor @ToString
class Point { int x, y }
