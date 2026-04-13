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

import groovy.json.JsonOutput
import groovy.transform.Internal
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import org.junit.jupiter.api.Test

class MetaClassInternalPropertyTest {

    static class WithInternalField {
        String name
        @Internal String secret
    }

    static class WithInternalGetter {
        String name
        private String hidden = 'secret'

        @Internal
        String getHidden() { hidden }
    }

    @Test
    void internalFieldExcludedFromProperties() {
        def props = new WithInternalField(name: 'test', secret: 'x').metaClass.properties*.name
        assert 'name' in props
        assert !('secret' in props)
    }

    @Test
    void internalGetterExcludedFromProperties() {
        def props = new WithInternalGetter(name: 'test').metaClass.properties*.name
        assert 'name' in props
        assert !('hidden' in props)
    }

    @Test
    void internalFieldExcludedFromJsonOutput() {
        def json = JsonOutput.toJson(new WithInternalField(name: 'test', secret: 'x'))
        assert json.contains('"name"')
        assert !json.contains('"secret"')
    }

    @ToString
    static class WithInternalForToString {
        String name
        @Internal String secret
    }

    @Test
    void internalFieldExcludedFromToString() {
        def obj = new WithInternalForToString(name: 'test', secret: 'x')
        def str = obj.toString()
        assert str.contains('test')
        assert !str.contains('x')
    }

    @EqualsAndHashCode
    static class WithInternalForEquals {
        String name
        @Internal String tag
    }

    @Test
    void internalFieldExcludedFromEqualsAndHashCode() {
        def a = new WithInternalForEquals(name: 'test', tag: 'one')
        def b = new WithInternalForEquals(name: 'test', tag: 'two')
        // tag is internal so should not affect equality
        assert a == b
        assert a.hashCode() == b.hashCode()
    }

    @Test
    void regularPropertiesStillIncluded() {
        def obj = new WithInternalField(name: 'test', secret: 'x')
        // direct access still works
        assert obj.name == 'test'
        assert obj.secret == 'x'
        // only metaClass.properties filters it
        def propNames = obj.metaClass.properties*.name
        assert 'name' in propNames
        assert 'class' in propNames
    }
}
