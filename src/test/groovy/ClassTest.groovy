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
package groovy

class ClassTest extends GroovyTestCase {

    void testClassExpression() {
        def c = String.class
        println c
        assert c instanceof Class
        assert c.name == "java.lang.String" , c.name
        
        c = GroovyTestCase.class
        println c
        assert c instanceof Class
        assert c.name.endsWith("GroovyTestCase") , c.name
        
        c = ClassTest.class
        println c
        assert c instanceof Class
        assert c.name.endsWith("ClassTest") , c.name
    }

    def testClassesHaveSuperModiferSet() {
        assert java.lang.reflect.Modifier.isSynchronized(this.class.modifiers)
    }

}