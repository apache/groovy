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
package groovy.util

import groovy.test.GroovyTestCase

class ConfigObjectTest extends GroovyTestCase {

    void test_isSet_Returns_true_for_Boolean_option_with_value_true() {
        def config = new ConfigSlurper().parse('foo { booleanTrue=true }')
        assert config.foo.isSet('booleanTrue')
    }
    
    void test_isSet_Returns_true_for_Boolean_option_with_value_false() {
        def config = new ConfigSlurper().parse('foo { booleanFalse=false }')
        assert config.foo.isSet('booleanFalse')
    }
    
    void test_isSet_Returns_true_for_nonempty_String_option() {
        def config = new ConfigSlurper().parse('foo { string="hello" }')
        assert config.foo.isSet('string')
    }
    
    void test_isSet_Returns_true_for_empty_String_option() {
        def config = new ConfigSlurper().parse("foo { emptyString='' }")
        assert config.foo.isSet('emptyString')
    }
    
    void test_isSet_Returns_true_for_nonempty_List_option() {
        def config = new ConfigSlurper().parse("foo { list=['a', 'b'] }")
        assert config.foo.isSet('list')
    }
    
    void test_isSet_Returns_true_for_empty_List_option() {
        def config = new ConfigSlurper().parse('foo { emptyList=[] }')
        assert config.foo.isSet('emptyList')
    }
    
    void test_isSet_Returns_true_for_nonempty_nested_block() {
        ConfigObject config = new ConfigSlurper().parse('foo { nestedBlock { setting=true } }')
        assert config.foo.isSet('nestedBlock')
    }
    
    void test_isSet_Returns_false_for_nonexisting_option() {
        def config = new ConfigSlurper().parse('foo { }')
        assert config.foo.isSet('nonexisting') == false
    }
    
    void test_isSet_Returns_false_for_unset_option() {
        def config = new ConfigSlurper().parse('foo { unset }')
        assert config.foo.isSet('unset') == false
    }
    
    void test_isSet_Returns_false_for_empty_nested_block() {
        def config = new ConfigSlurper().parse('foo { emptyNestedBlock { } }')
        assert config.foo.isSet('emptyNestedBlock') == false
    }

    void test_prettyPrint() {
        def configString = '''\
development {
    rabbitmq {
        active=true
        hostname='localhost'
    }
}'''

        def config = new ConfigSlurper().parse(configString)
        assert config == new ConfigSlurper().parse(config.prettyPrint())
    }
}
