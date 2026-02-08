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
package groovy.json

import groovy.json.JsonGenerator.Converter
import groovy.json.JsonGenerator.Options
import groovy.test.GroovyTestCase
import org.apache.groovy.json.internal.CharBuf

/**
 * Tests extensibility of JsonGenerator and associated classes
 */
class CustomJsonGeneratorTest extends GroovyTestCase {

    void testCustomGenerator() {
        def generator = new CustomJsonOptions()
                            .excludeNulls()
                            .lowerCaseFieldNames()
                            .addCustomConverter(new CustomJsonConverter())
                            .build()

        assert generator.toJson(['one', null, 'two', null]) == '["one","two"]'
        assert generator.toJson(['Foo':'test1', 'BAR':'test2']) == '{"foo":"test1","bar":"test2"}'
        assert generator.toJson(['foo': new CustomFoo(c: { -> "CustomFoo from CustomJsonConverter" })]) == '{"foo":"CustomFoo from CustomJsonConverter"}'
        assert generator.toJson(['foo': new CustomFoo(c: { -> JsonOutput.unescaped("{}") })]) == '{"foo":{}}'
    }

    static class CustomJsonOptions extends Options {
        boolean lowerCaseFieldNames
        CustomJsonOptions lowerCaseFieldNames() {
            lowerCaseFieldNames = true
            return this
        }
        CustomJsonOptions addCustomConverter(Converter converter) {
            converters.add(converter)
            return this
        }
        @Override
        CustomJsonGenerator build() {
            return new CustomJsonGenerator(this)
        }
    }

    static class CustomJsonGenerator extends DefaultJsonGenerator {
        boolean lowerCaseFieldNames
        CustomJsonGenerator(CustomJsonOptions opts) {
            super(opts)
            lowerCaseFieldNames = opts.lowerCaseFieldNames
        }
        @Override
        protected void writeMapEntry(String key, Object value, CharBuf buffer) {
            String newKey = (lowerCaseFieldNames) ? key.toLowerCase() : key
            super.writeMapEntry(newKey, value, buffer)
        }
    }

    static class CustomJsonConverter implements Converter {
        @Override
        boolean handles(Class<?> type) {
            return CustomFoo.isAssignableFrom(type)
        }

        @Override
        Object convert(Object value, String key) {
            return ((CustomFoo)value).c.call()
        }
    }

    static class CustomFoo {
        Closure c
    }
}
