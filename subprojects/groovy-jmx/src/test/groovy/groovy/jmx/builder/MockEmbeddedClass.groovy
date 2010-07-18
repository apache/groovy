/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.jmx.builder

class BaseEmbeddedClass {
    int id
    String name
    def location
    boolean available

    def doNothing() {
        // doing nothing
    }

    def doTwoThings(def thing1, String thing2) {
        // doing thing one and two
    }

    def doThreeThings(def thing1, boolean thing2, int thing3) {
        // what to do
    }

    String doSomethingElse(int whatToDo) {
        // what to do
    }

}

class EmbeddedNameOnly extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject"
    ]
}

class EmbeddedAllAttribsOnly extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            attributes: "*"
    ]
}

class EmbeddedAttribsListOnly extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            attributes: ["id", "name"]
    ]
}

class EmbeddedAttribsDescriptorOnly extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            attributes: [
                    id: "*",
                    name: [desc: "Name Description", readable: true, writable: true]
            ]
    ]
}

class EmbeddedConstructors extends BaseEmbeddedClass {
    def EmbeddedConstructors() {
        name = "EmbeddedConstructor"
    }

    def EmbeddedConstructors(int idee) {
        id = idee
    }

    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            constructors: [
                    ctor1: [desc: "ctor1"],
                    ctor2: [desc: "ctor2", params: ["int": [name: "Id", desc: "Identification"]]]
            ]
    ]
}

class EmbeddedAllOps extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            operations: "*"
    ]
}

class EmbeddedOpsList extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            operations: ["doNothing", "doThreeThings"]
    ]
}

class EmbeddedOpsDescriptor extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            operations: [
                    doNothing: "*",
                    doTwoThings: ["Object", "String"],
                    doThreeThings: [
                            description: "Do Three Things",
                            params: [
                                    "Object": [desc: "thing1"],
                                    "boolean": "*",
                                    "int": "*"
                            ]
                    ]
            ]
    ]
}


class EmbeddedAttribEventListener extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            attributes: [
                    "name": [onChange: {-> println "attrib name changed"}],
                    "id": [onChange: this.&attribChangeHandler]
            ]
    ]

    def attribChangeHandler() {
        println "attrib changed"
    }
}

class EmbeddedOpEventListener extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            ops: [
                    "doNothing": [onCall: {-> println "op doNothing() called."}],
                    "doTwoThings": [params: ["Object", "String"], onCall: this.&attribChangeHandler]
            ]
    ]

    def opCallHandler() {
        println "op called"
    }
}

class EmbeddedEventListener extends BaseEmbeddedClass {
    static descriptor = [
            name: "jmx.builder:type=EmbeddedObject",
            listeners: [
                    heartbeat: [event: "event.heartbeat", from: "some:type=object1", call: {-> "event.heartbeat detected"}],
                    timer: [event: "event.timer", from: "some:type=object2", call: this.&eventHandler]
            ]
    ]

    def eventHandler() {
        println "op called"
    }
}