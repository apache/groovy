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
package groovy.script

import groovy.test.GroovyShellTestCase

class StreamClassloaderInScriptTest extends GroovyShellTestCase {

    static final BLAH = """
class Blah implements Serializable {
def x, y
boolean equals(def other) { x == other.x && y == other.y }
}
"""

    void testSerializationToFileWithinScript() {
        evaluate BLAH + """
def file = new File('blahblah.dat')
file.deleteOnExit()
def data = [ new Blah(x:1, y:2), new Blah(x:'flob', y:'adob') ]
file.withObjectOutputStream { oos ->
    oos.writeObject ( data )
}
// in a script we must set the classloader
file.withObjectInputStream(getClass().classLoader){ ois ->
    def newData = ois.readObject ( )
    assert data == newData
}
        """
    }

    void testSerializationToBytesWithinScript() {
        evaluate BLAH + """
def baos = new ByteArrayOutputStream()
def data = [ new Blah(x:1, y:2), new Blah(x:'flob', y:'adob') ]
baos.withObjectOutputStream { oos ->
    oos.writeObject ( data )
}
def bais = new ByteArrayInputStream(baos.toByteArray())
// in a script we must set the classloader
bais.withObjectInputStream(getClass().classLoader){ ois ->
    def newData = ois.readObject ( )
    assert data == newData
}
        """
    }

}