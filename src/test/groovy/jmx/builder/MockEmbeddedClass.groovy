
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

    def doThreeThings(def thing1, boolean thing2, int thing3){
        // what to do
    }

    String doSomethingElse(int whatToDo) {
        // what to do
    }

}

class EmbeddedNameOnly extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject"
    ]
}

class EmbeddedAllAttribsOnly extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        attributes:"*"
    ]
}

class EmbeddedAttribsListOnly extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        attributes:["id","name"]
    ]
}

class EmbeddedAttribsDescriptorOnly extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        attributes:[
            id:"*",
            name: [desc: "Name Description", readable: true, writable: true]
        ]
    ]
}

class EmbeddedConstructors extends BaseEmbeddedClass {
    def EmbeddedConstructors()  {
        name = "EmbeddConstructor"
    }
    def EmbeddedConstructors(int idee){
        id = idee
    }
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        constructors:[
            ctor1:[desc:"ctor1"],
            ctor2: [desc:"ctor2", params: ["int":[name:"Id", desc:"Identification"]]]
        ]
    ]
}

class EmbeddedAllOps extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        operations: "*"
    ]
}

class EmbeddedOpsList extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        operations: ["doNothing", "doThreeThings"]
    ]
}

class EmbeddedOpsDescriptor extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        operations: [
            doNothing:"*",
            doTwoThings:["Object","String"],
            doThreeThings:[
                description:"Do Three Things",
                params:[
                    "Object":[desc:"thing1"],
                    "boolean":"*",
                    "int":"*"
                ]
            ]
        ]
    ]
}


class EmbeddedAttribEventListener extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        attributes: [
            "name":[onChange:{-> println"attrib name changed"}],
            "id":[onChange:this.&attribChangeHandler]
        ]
    ]

    def attribChangeHandler() {
        println "attrib changed"
    }
}

class EmbeddedOpEventListener extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        ops: [
            "doNothing":[onCall:{-> println"op doNothing() called."}],
            "doTwoThings":[params:["Object","String"], onCall:this.&attribChangeHandler]
        ]
    ]

    def opCallHandler() {
        println "op called"
    }
}

class EmbeddedEventListener extends BaseEmbeddedClass {
    static descriptor = [
        name : "jmx.builder:type=EmbeddedObject",
        listeners: [
            heartbeat: [event: "event.heartbeat", from: "some:type=object1", call: {-> "event.heartbeat detected"}],
            timer: [event: "event.timer", from: "some:type=object2", call:this.&eventHandler]
        ]
    ]

    def eventHandler() {
        println "op called"
    }
}