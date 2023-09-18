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

abstract class JSR308Super<T> {}
interface JSR308Interface1<T> {}
interface JSR308Interface2<T  extends @JSR308 CharSequence> {}
    class JSR308Permitted1    extends JSR308Class {}
    class JSR308Permitted2<T> extends JSR308Class {}

sealed class JSR308Class extends @JSR308 JSR308Super<@JSR308 List> implements @JSR308 JSR308Interface1<@JSR308 String>, @JSR308 JSR308Interface2<@JSR308 String>
    permits @JSR308 JSR308Permitted1, @JSR308 JSR308Permitted2
{
    @JSR308 private String name;

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

def jsr308 = new JSR308Class()
def result = jsr308.test(new ArrayList<@JSR308 String>(['1', '2']))
assert result == ['1', '2', 'a', 'b']


assert JSR308Class.annotatedSuperclass.type.typeName == 'JSR308Super<java.util.List>'
assert JSR308Class.permittedSubclasses*.typeName == ['JSR308Permitted1', 'JSR308Permitted2']
assert JSR308Class.annotatedInterfaces*.type*.typeName == ['JSR308Interface1<java.lang.String>', 'JSR308Interface2<java.lang.String>']

Method testMethod = JSR308Class.declaredMethods.find(m -> m.name == 'test')
assert testMethod.annotatedExceptionTypes*.type == [IOException, SQLException]
assert testMethod.annotatedReturnType.type.typeName == 'java.util.List<java.lang.String>'


// 1)
assert testMethod.annotatedParameterTypes.collect(t -> t.type.typeName)[0] in ['java.util.List', 'java.util.List<?>']

Method test2Method = JSR308Class.declaredMethods.find(m -> m.name == 'test2')
assert test2Method.annotatedReceiverType.type == JSR308Class


// 2)
Parameter listParameter = testMethod.parameters[0]
assert listParameter.annotatedType.type.typeName in ['java.util.List', 'java.util.List<?>']

Field nameField = JSR308Class.getDeclaredField('name')
assert nameField.annotatedType.type == String


// 3)
TypeVariable tv = JSR308Interface2.typeParameters[0]
assert tv.getAnnotatedBounds().collect(e -> e.type)[0] in [CharSequence, null]

// the above 3 tests get different result when running in the different CI(travis-ci and teamcity)
// travis-ci succeeds:  https://travis-ci.org/apache/groovy/builds/262506189
// teamcity fails:      http://ci.groovy-lang.org/viewLog.html?buildId=41265&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//                      http://ci.groovy-lang.org/viewLog.html?buildId=41260&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//                      http://ci.groovy-lang.org/viewLog.html?buildId=41257&tab=buildResultsDiv&buildTypeId=Groovy_Jdk8Build_PlusSnapshotDeploy
//
// I guess the difference is related to the version of JDK8 deployed(Maybe it is a bug of Java8)
