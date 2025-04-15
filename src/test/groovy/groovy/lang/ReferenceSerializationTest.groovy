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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * GROOVY-4305: Make groovy.lang.Reference implement Serializable
 */
class ReferenceSerializationTest extends GroovyTestCase implements Serializable {

    private static final long serialVersionUID = 10L

    private serializeDeserialize(obj) {
        // serialize
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(out)
        oos.writeObject(obj)
        oos.close()

        //deserialize
        byte[] bytes = out.toByteArray()
        InputStream is = new ByteArrayInputStream(bytes)
        ObjectInputStream ois = new ObjectInputStream(is)

        // return back what's read from the stream
        return ois.readObject()
    }

    void testSimplePogoSerializationToObjectOutputStream() {
        int age = 33
        String name = "Guillaume"

        def person = new CustomPogoPerson(name: name, age: age, pet: new CustomPogoPet(nickname: "Minou", kind: "cat"))

        def personDeserialized = serializeDeserialize(person)

        assert personDeserialized.name == name
        assert personDeserialized.age == age
        assert personDeserialized.pet.nickname == "Minou"
        assert personDeserialized.pet.kind == "cat"
    }

    void testClosureSerializationWithAReferenceToALocalVariable() {
        int number = 2
        def doubler = { it * number }

        def closure = serializeDeserialize(doubler)

        assert closure(2) == 4
        assert closure(3) == 6
    }

    void testAICReferencingLocalVariableTest() {
        long count = 0
        def button = new Button()
        button.listener = new ClickAdapter() {
            long onClick() {
                count++
                return count
            }
        }
        assert button.listener.onClick() == 1
        assert button.listener.onClick() == 2

        def buttonCopy = serializeDeserialize(button)
        assert buttonCopy.listener.onClick() == 3
        assert buttonCopy.listener.onClick() == 4
    }
}

class CustomPogoPerson implements Serializable {
    private static final long serialVersionUID = 1L

    String name
    int age
    CustomPogoPet pet
}

class CustomPogoPet implements Serializable {
    private static final long serialVersionUID = 2L

    String nickname
    String kind
}

class Button implements Serializable {
    private static final long serialVersionUID = 3L
    ClickListener listener
}

interface ClickListener {
    long onClick()
}

abstract class ClickAdapter implements ClickListener, Serializable {
    private static final long serialVersionUID = 4L
}