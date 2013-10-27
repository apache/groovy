/*
 * Copyright 2003-2013 the original author or authors.
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
package groovy.util

class ConfigObjectTest extends GroovyTestCase {

    void test_isSet_set_BooleanTrueValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { setting=true }')
        assert config.foo.isSet('setting')
    }
    
    void test_isSet_set_BooleanFalseValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { setting=false }')
        assert config.foo.isSet('setting')
    }
    
    void test_isSet_set_StringValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { password="hello" }')
        assert config.foo.isSet('password')
    }
    
    void test_isSet_set_EmptyStringValue() {
        ConfigObject config = new ConfigSlurper().parse("foo { string='' }")
        assert config.foo.isSet('string')
    }
    
    void test_isSet_set_ListValue() {
        ConfigObject config = new ConfigSlurper().parse("foo { list=['a', 'b'] }")
        assert config.foo.isSet('list')
    }
    
    void test_isSet_set_EmptyListValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { list=[] }')
        assert config.foo.isSet('list')
    }
    
    void test_isSet_set_NestedConfigObject() {
        ConfigObject config = new ConfigSlurper().parse('foo { bar { setting=true } }')
        assert config.foo.isSet('bar')
    }
    
    void test_isSet_unset_MissingValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { }')
        assert config.foo.isSet('setting') == false
    }
    
    void test_isSet_unset_EmptyValue() {
        ConfigObject config = new ConfigSlurper().parse('foo { setting }')
        assert config.foo.isSet('setting') == false
    }
    
    void test_isSet_unset_EmptyNestedConfigObject() {
        ConfigObject config = new ConfigSlurper().parse('foo { bar { } }')
        assert config.foo.isSet('bar') == false
    }
}
