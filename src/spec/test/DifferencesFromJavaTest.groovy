/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                int[] array = { 1, 2, 3}
                // end::arraycreate_fail[]
            '''
        }
        assertScript '''
            // tag::arraycreate_success[]
            int[] array = [1,2,3]
            // end::arraycreate_success[]
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
boolean called = false

Timer timer = new Timer()
timer.schedule(new TimerTask() {
    void run() {
        called = true
    }
}, 0)
sleep 100

assert called
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
}
