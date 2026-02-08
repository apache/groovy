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
package builder

import groovy.transform.CompileStatic
import org.junit.Test
import static groovy.test.GroovyAssert.assertScript

@CompileStatic
class BuilderSpecTest {
    @Test
    void testBuilderSupport() {
        assertScript '''
// tag::define_builder1[]
class TrainingBuilder1 extends BuilderSupport {
    protected createNode(name) {
        [name: name, steps: []]
    }

    protected createNode(name, Map attributes) {
        createNode(name) + attributes
    }

    void nodeCompleted(maybeParent, node) {
        if (maybeParent) maybeParent.steps << node
    }

    // unused lifecycle methods
    protected void setParent(parent, child) { }
    protected createNode(name, Map attributes, value) { }
    protected createNode(name, value) { }
}
// end::define_builder1[]

// tag::define_total_helper1[]
def total(map) {
    if (map.distance) return map.distance
    def repeat = map.repeat ?: 1
    repeat * map.steps.sum{ total(it) }
}
// end::define_total_helper1[]

// tag::use_builder1[]
def training = new TrainingBuilder1()

def monday = training.swimming {
    warmup(repeat: 3) {
        freestyle(distance: 50)
        breaststroke(distance: 50)
    }
    endurance(repeat: 20) {
        freestyle(distance: 50, break: 15)
    }
    warmdown {
        kick(distance: 100)
        choice(distance: 100)
    }
}

assert 1500 == total(monday)
// end::use_builder1[]
'''
    }

    @Test
    void testFactoryBuilderSupport() {
        assertScript '''
// tag::define_builder2[]
import static org.apache.groovy.util.BeanUtils.capitalize

class TrainingBuilder2 extends FactoryBuilderSupport {
    def factory = new TrainingFactory(loader: getClass().classLoader)

    protected Factory resolveFactory(name, Map attrs, value) {
        factory
    }

    void nodeCompleted(maybeParent, node) {
        if (maybeParent) maybeParent.steps << node
    }
}

class TrainingFactory extends AbstractFactory {
    ClassLoader loader
    def newInstance(FactoryBuilderSupport fbs, name, value, Map attrs) {
        def clazz = loader.loadClass(capitalize(name))
        value ? clazz.newInstance(value: value) : clazz.newInstance()
    }
}
// end::define_builder2[]

// tag::define_domain_classes2[]
trait HasDistance {
    int distance
}

trait Container extends HasDistance {
    List steps = []
    int repeat
}

class Cycling implements Container { }

class Interval implements Container { }

class Sprint implements HasDistance {}

class Tempo implements HasDistance {}
// end::define_domain_classes2[]

// tag::define_total_helper2[]
def total(HasDistance c) {
    c.distance
}

def total(Container c) {
    if (c.distance) return c.distance
    def repeat = c.repeat ?: 1
    repeat * c.steps.sum{ total(it) }
}
// end::define_total_helper2[]

// tag::use_builder2[]
def training = new TrainingBuilder2()

def tuesday = training.cycling {
    interval(repeat: 5) {
        sprint(distance: 400)
        tempo(distance: 3600)
    }
}

assert 20000 == total(tuesday)
// end::use_builder2[]
'''
    }
}
