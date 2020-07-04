import groovy.test.GroovyTestCase

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
class DifferencesFromJavaTest extends GroovyTestCase {
    void testMultiMethods() {
        assertScript '''import static org.junit.Assert.*
// tag::multimethods[]
int method(String arg) {
    return 1;
}
int method(Object arg) {
    return 2;
}
Object o = "Object";
int result = method(o);
// end::multimethods[]
/*
// With Java:
// tag::multimethods_java[]
assertEquals(2, result);
// end::multimethods_java[]
*/
// With Groovy:
// tag::multimethods_groovy[]
assertEquals(1, result);
// end::multimethods_groovy[]
'''
    }

    void testArrayCreation() {
        shouldFail {
            assertScript '''
                // tag::arraycreate_fail[]
                int[] array = {1, 2, 3};             // Java array initializer shorthand syntax
                int[] array2 = new int[] {4, 5, 6};  // Java array initializer long syntax
                // end::arraycreate_fail[]
            '''
        }
        assertScript '''
            // tag::arraycreate_success[]
            int[] array = [1, 2, 3]
            // end::arraycreate_success[]
            assert array instanceof int[]
            // tag::arraycreate3_success[]
            def array2 = new int[] {1, 2, 3} // Groovy 3.0+ supports the Java-style array initialization long syntax
            // end::arraycreate3_success[]
            assert array2 instanceof int[]
        '''
    }

    void testPackagePrivate() {
        assertScript '''import groovy.transform.ASTTest
import org.codehaus.groovy.control.CompilePhase

import java.lang.reflect.Modifier

@ASTTest(phase = CompilePhase.CLASS_GENERATION, value = {
    def field = node.getField('name')
    assert field.modifiers == Modifier.PRIVATE
})
// tag::packageprivate_property[]
class Person {
    String name
}
// end::packageprivate_property[]
new Person()        '''

        assertScript '''import groovy.transform.ASTTest
import groovy.transform.PackageScope
import org.codehaus.groovy.control.CompilePhase

import java.lang.reflect.Modifier

@ASTTest(phase = CompilePhase.CLASS_GENERATION, value = {
    def field = node.getField('name')
    assert field.modifiers == 0
})
// tag::packageprivate_field[]
class Person {
    @PackageScope String name
}
// end::packageprivate_field[]
new Person()        '''
    }

    void testAnonymousAndNestedClasses() {
        assertScript '''
// tag::innerclass_1[]
class A {
    static class B {}
}

new A.B()
// end::innerclass_1[]
'''
        assertScript '''
// tag::innerclass_2[]
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

CountDownLatch called = new CountDownLatch(1)

Timer timer = new Timer()
timer.schedule(new TimerTask() {
    void run() {
        called.countDown()
    }
}, 0)

assert called.await(10, TimeUnit.SECONDS)
// end::innerclass_2[]
'''
        assertScript '''
/*
// tag::innerclass_3_java[]
public class Y {
    public class X {}
    public X foo() {
        return new X();
    }
    public static X createX(Y y) {
        return y.new X();
    }
}
// end::innerclass_3_java[]
*/
// tag::innerclass_3[]
public class Y {
    public class X {}
    public X foo() {
        return new X()
    }
    public static X createX(Y y) {
        return new X(y)
    }
}
// end::innerclass_3[]
def y = new Y()
def x = Y.createX(y)
assert (x.'this$0').is(y)
'''
    }

    void testStringsAndCharsGotchas() {
        assertScript '''
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

// tag::type_depends_on_quoting_AND_whether_we_actually_interpolate[]
assert 'c'.getClass()==String
assert "c".getClass()==String
assert "c${1}".getClass() in GString
// end::type_depends_on_quoting_AND_whether_we_actually_interpolate[]
// tag::single_char_strings_are_autocasted[]
char a='a'
assert Character.digit(a, 16)==10 : 'But Groovy does boxing'
assert Character.digit((char) 'a', 16)==10

try {
  assert Character.digit('a', 16)==10
  assert false: 'Need explicit cast'
} catch(MissingMethodException e) {
}
// end::single_char_strings_are_autocasted[]
// tag::chars_c_vs_groovy_cast[]
// for single char strings, both are the same
assert ((char) "c").class==Character
assert ("c" as char).class==Character

// for multi char strings they are not
try {
  ((char) 'cx') == 'c'
  assert false: 'will fail - not castable'
} catch(GroovyCastException e) {
}
assert ('cx' as char) == 'c'
assert 'cx'.asType(char) == 'c'
// end::chars_c_vs_groovy_cast[]
        '''

    }
}
