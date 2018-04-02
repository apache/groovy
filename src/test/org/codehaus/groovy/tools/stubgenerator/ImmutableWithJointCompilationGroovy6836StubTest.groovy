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
package org.codehaus.groovy.tools.stubgenerator

/**
 * Checks that {@code @Immutable} classes work correctly with stubs.
 */
class ImmutableWithJointCompilationGroovy6836StubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'foo/JavaFoo.java': '''package foo;
                    public final class JavaFoo {}
                ''',
                'foo/ImmutableBean.groovy': '''package foo
                    @groovy.transform.Immutable(knownImmutables=['id'])
                    @groovy.transform.CompileStatic // do NOT remove this
                    class ImmutableBean { JavaFoo id }
                ''',
                'foo/ImmutableTest.groovy': '''package foo
                    def map = [id:new JavaFoo()]
                    def mc = map.getMetaClass()
                    mc.blah = { 123 }
                    assert map.blah() == 123
                    def bean = new ImmutableBean(map)
                    def beanMc = bean.getMetaClass()
                    // now this is where it gets funny
                    int hash = bean.hashCode() // will trigger call to getProperty, which will use the wrong metaclass
                    Set set = []
                    set.add(bean) // same, internally calls .hashCode() and .equals()
                '''
        ]
    }

    void verifyStubs() {
        def test = loader.loadClass('foo.ImmutableTest').newInstance()
        test.run()
    }
}
