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
 * Specification tests for records
 */
class RecordSpecificationTest extends GroovyTestCase {

    void testSimpleRecordKeyword() {
        assertScript '''
// tag::record_message_defn[]
record Message(String from, String to, String body) { }
// end::record_message_defn[]
// tag::record_message_usage[]
def msg = new Message('me@myhost.com', 'you@yourhost.net', 'Hello!')
assert msg.toString() == 'Message[from=me@myhost.com, to=you@yourhost.net, body=Hello!]'
// end::record_message_usage[]
'''
        def equiv = '''
// tag::record_message_equivalent[]
final class Message extends Record {
    private final String from
    private final String to
    private final String body
    private static final long serialVersionUID = 0

    /* constructor(s) */

    final String toString() { /*...*/ }

    final boolean equals(Object other) { /*...*/ }

    final int hashCode() { /*...*/ }
}
// end::record_message_equivalent[]
'''
    }

    void testRecordDefaultsAndNamedArguments() {
        assertScript '''
import static groovy.test.GroovyAssert.shouldFail
import groovy.transform.*

// tag::record_point_defn[]
record ColoredPoint(int x, int y = 0, String color = 'white') {}
// end::record_point_defn[]

// tag::record_point_defaults[]
assert new ColoredPoint(5, 5, 'black').toString() == 'ColoredPoint[x=5, y=5, color=black]'
assert new ColoredPoint(5, 5).toString() == 'ColoredPoint[x=5, y=5, color=white]'
assert new ColoredPoint(5).toString() == 'ColoredPoint[x=5, y=0, color=white]'
// end::record_point_defaults[]

// tag::record_point_named_args[]
assert new ColoredPoint(x: 5).toString() == 'ColoredPoint[x=5, y=0, color=white]'
assert new ColoredPoint(y: 5).toString() == 'ColoredPoint[x=0, y=5, color=white]'
// end::record_point_named_args[]
assert new ColoredPoint(y: null).toString() == 'ColoredPoint[x=0, y=0, color=white]'
def ex = shouldFail { new ColoredPoint(z: 5) }
assert ex.message.contains('Unrecognized namedArgKey: z')
'''
    }

    void testOverrideToString() {
        assertScript '''
// tag::record_point3d[]
record Point3D(int x, int y, int z) {
    String toString() {
        "Point3D[coords=$x,$y,$z]"
    }
}

assert new Point3D(10, 20, 30).toString() == 'Point3D[coords=10,20,30]'
// end::record_point3d[]
'''
    }

    void testRecordCompactConstructor() {
        assertScript '''
// tag::record_compact_constructor[]
public record Warning(String message) {
    public Warning {
        Objects.requireNonNull(message)
        message = message.toUpperCase()
    }
}

def w = new Warning('Help')
assert w.message() == 'HELP'
// end::record_compact_constructor[]
'''
    }

    void testToStringAnnotation() {
        assertScript '''
// tag::record_point3d_tostring_annotation[]
package threed

import groovy.transform.ToString

@ToString(ignoreNulls=true, cache=true, includeNames=true,
          leftDelimiter='[', rightDelimiter=']', nameValueSeparator='=')
record Point(Integer x, Integer y, Integer z=null) { }

assert new Point(10, 20).toString() == 'threed.Point[x=10, y=20]'
// end::record_point3d_tostring_annotation[]
'''
        assertScript '''
// tag::record_point2d_tostring_annotation[]
package twod

import groovy.transform.ToString

@ToString(ignoreNulls=true, cache=true, includeNames=true,
          leftDelimiter='[', rightDelimiter=']', nameValueSeparator='=')
record Point(Integer x, Integer y) { }

assert new Point(10, 20).toString() == 'twod.Point[x=10, y=20]'
// end::record_point2d_tostring_annotation[]
'''
    }

    void testSimpleRecordAnnotation() {
        assertScript '''
import groovy.transform.RecordType
// tag::record_message_annotation_defn[]
@RecordType
class Message {
    String from
    String to
    String body
}
// end::record_message_annotation_defn[]
def msg = new Message('me@myhost.com', 'you@yourhost.net', 'Hello!')
assert msg.toString() == 'Message[from=me@myhost.com, to=you@yourhost.net, body=Hello!]'
'''
    }
}
