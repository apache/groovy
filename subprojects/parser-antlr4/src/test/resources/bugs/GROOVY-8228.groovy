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
import java.lang.annotation.Retention
import java.lang.annotation.Target
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.TypeVariable
import java.sql.SQLException

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Target([PARAMETER, FIELD, METHOD, ANNOTATION_TYPE, TYPE_USE, LOCAL_VARIABLE])
@Retention(RUNTIME)
@interface JSR308 { }

class JSR308BaseClass<T> {}
interface JSR308Interface1<T> {}
interface JSR308Interface2<T extends @JSR308 CharSequence> {}

class JSR308Class extends @JSR308 JSR308BaseClass<@JSR308 List> implements @JSR308 JSR308Interface1<@JSR308 String>, @JSR308 JSR308Interface2<@JSR308 String> {
    @JSR308 private  String name;

    @JSR308 List<@JSR308 String> test(@JSR308 List<@JSR308 ? extends @JSR308 Object> list) throws @JSR308 IOException, @JSR308 java.sql.SQLException {
        @JSR308 List<@JSR308 String> localVar = new @JSR308 ArrayList<@JSR308 String>();

        try {
            for (e in list) {
                String t = (@JSR308 String) e;
                localVar.add(t);
            }
        } catch (@JSR308 Exception e) {
        }

        String @JSR308 []  strs = new String @JSR308 [] { 'a' }
        String @JSR308 [] @JSR308 [] strs2 = new String @JSR308 [] @JSR308 [] { new String[] {'a', 'b'} }
        String @JSR308 [] @JSR308 [] @JSR308 [] strs3 = new String @JSR308 [1] @JSR308 [2] @JSR308 []
        String @JSR308 [] @JSR308 [] @JSR308 [] @JSR308 [] strs4 = new String @JSR308 [1] @JSR308 [2] @JSR308 [] @JSR308 []

        localVar.add(strs[0])
        localVar.add(strs2[0][1])
        assert null != strs3
        assert null != strs4

        return localVar
    }

    void test2(@JSR308 JSR308Class this) {}
}

def jsr308Class = new JSR308Class();
def list = new ArrayList<@JSR308 String>();
list.addAll(["1", "2"]);
def result = jsr308Class.test(list)
assert ['1', '2', 'a', 'b'] == result

assert 'JSR308BaseClass<java.util.List>' == JSR308Class.class.getAnnotatedSuperclass().type.typeName
assert ['JSR308Interface1<java.lang.String>', 'JSR308Interface2<java.lang.String>'] == JSR308Class.class.getAnnotatedInterfaces().collect(e -> e.type.typeName)

Method testMethod = JSR308Class.class.getDeclaredMethods().find(e -> e.name == 'test')
assert [IOException, SQLException] == testMethod.getAnnotatedExceptionTypes().collect(e -> e.type)
assert 'java.util.List<java.lang.String>' == testMethod.getAnnotatedReturnType().type.typeName

// 1)
assert ['java.util.List<?>', 'java.util.List'].contains(testMethod.getAnnotatedParameterTypes().collect(e -> e.type.typeName).get(0))

Method test2Method = JSR308Class.class.getDeclaredMethods().find(e -> e.name == 'test2')
assert JSR308Class.class == test2Method.getAnnotatedReceiverType().type


// 2)
Parameter listParameter = testMethod.getParameters()[0]
assert ['java.util.List<?>', 'java.util.List'].contains(listParameter.getAnnotatedType().type.typeName)

Field nameField = JSR308Class.class.getDeclaredField('name');
assert String.class == nameField.getAnnotatedType().type


// 3)
TypeVariable tv = JSR308Interface2.class.getTypeParameters()[0]
assert [CharSequence.class, null].contains(tv.getAnnotatedBounds().collect(e -> e.type).get(0))

// the above 3 tests get different result when running in the different CI(travis-ci and teamcity)
// travis-ci succeeds:  https://travis-ci.org/apache/groovy/builds/262506189
// teamcity fails:      http://ci.groovy-lang.org/viewLog.html?buildId=41265&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//                      http://ci.groovy-lang.org/viewLog.html?buildId=41260&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//                      http://ci.groovy-lang.org/viewLog.html?buildId=41257&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//
// I guess the difference is related to the version of JDK8 deployed(Maybe it is a bug of Java8)
