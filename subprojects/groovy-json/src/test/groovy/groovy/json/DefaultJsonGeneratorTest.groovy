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

import groovy.test.GroovyTestCase

class DefaultJsonGeneratorTest extends GroovyTestCase {

    void testExcludesNullValues() {
        def generator = new JsonGenerator.Options()
                .excludeNulls()
                .build()

        def json = generator.toJson(new JsonObject(name: 'test', properties: null))
        assert json == '{"name":"test"}'

        json = generator.toJson([field1: null, field2: "test"])
        assert json == '{"field2":"test"}'

        assert generator.toJson([null]) == '[]'
        assert generator.toJson(['a','b','c','d', null]) == '["a","b","c","d"]'
        assert generator.toJson(['a', null, null, null, null]) == '["a"]'
        assert generator.toJson(['a', null, null, null, 'e']) == '["a","e"]'

        def jsonArray = ["foo", null, "bar"]
        def jsonExpected = '["foo","bar"]'
        assert generator.toJson(jsonArray) == jsonExpected
        assert generator.toJson(jsonArray as Object[]) == jsonExpected
        assert generator.toJson(jsonArray.iterator()) == jsonExpected
        assert generator.toJson((Iterable)jsonArray) == jsonExpected

        assert generator.toJson((Boolean)null) == ''
        assert generator.toJson((Number)null) == ''
        assert generator.toJson((Character)null) == ''
        assert generator.toJson((String)null) == ''
        assert generator.toJson((Date)null) == ''
        assert generator.toJson((Calendar)null) == ''
        assert generator.toJson((UUID)null) == ''
        assert generator.toJson((Closure)null) == ''
        assert generator.toJson((Expando)null) == ''
        assert generator.toJson((Object)null) == ''
        assert generator.toJson((Map)null) == ''
    }

    void testCustomDateFormat() {
        def generator = new JsonGenerator.Options()
                .dateFormat('yyyy-MM')
                .build()

        Date aDate = Date.parse('yyyy-MM-dd', '2016-07-04')
        assert generator.toJson(aDate) == '"2016-07"'

        def jsonObject = new JsonObject(name: 'test', properties: [startDate: aDate])
        def json = generator.toJson(jsonObject)
        assert json.contains('{"startDate":"2016-07"}')

        def jsonArray = ["foo", aDate, "bar"]
        def jsonExpected = '["foo","2016-07","bar"]'
        assert generator.toJson(jsonArray) == jsonExpected
        assert generator.toJson(jsonArray as Object[]) == jsonExpected
        assert generator.toJson(jsonArray.iterator()) == jsonExpected
        assert generator.toJson((Iterable)jsonArray) == jsonExpected
    }

    void testDateFormatBadInput() {
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().dateFormat(null)
        }
        shouldFail(IllegalArgumentException) {
            new JsonGenerator.Options().dateFormat('abcde')
        }
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().timezone(null)
        }
    }

    void testClosureConverters() {
        def generator = new JsonGenerator.Options()
                .addConverter(JsonCyclicReference) { object, key ->
            return "JsonCyclicReference causes a stackoverflow"
        }
        .addConverter(Date) { object ->
            return "4 score and 7 years ago"
        }
        .addConverter(Calendar) { object ->
            return "22 days ago"
        }
        .build()

        assert generator.toJson(new Date()) == '"4 score and 7 years ago"'

        def ref = new JsonBar('bar', new Date())
        def json = generator.toJson(ref)
        assert json.contains('"lastVisit":"4 score and 7 years ago"')
        assert json.contains('"cycle":"JsonCyclicReference causes a stackoverflow"')

        def jsonArray = ["foo", new JsonCyclicReference(), "bar", new Date()]
        def jsonExpected = '["foo","JsonCyclicReference causes a stackoverflow","bar","4 score and 7 years ago"]'
        assert generator.toJson(jsonArray) == jsonExpected
        assert generator.toJson(jsonArray as Object[]) == jsonExpected
        assert generator.toJson(jsonArray.iterator()) == jsonExpected
        assert generator.toJson((Iterable)jsonArray) == jsonExpected

        assert generator.toJson([timeline: Calendar.getInstance()]) == '{"timeline":"22 days ago"}'
    }

    void testCustomConverters() {
        def converter = new JsonGenerator.Converter() {
            @Override
            boolean handles(Class<?> type) { Date.class == type }
            @Override
            Object convert(Object value, String key) { '42' }
        }

        def generator = new JsonGenerator.Options()
                            .addConverter(converter)
                            .build()

        assert generator.toJson([new Date()]) == '["42"]'

        def mapConverter = [handles: { Date.class == it }, convert: { obj, key -> 7 }]
        generator = new JsonGenerator.Options()
                       .addConverter(mapConverter as JsonGenerator.Converter)
                        .build()

        assert generator.toJson([new Date()]) == '[7]'
    }

    void testConverterAddedLastTakesPrecedence() {
        def options = new JsonGenerator.Options()
        def c1 = { 'c1' }
        def c2 = { 'c2' }
        options.addConverter(URL, {})
        options.addConverter(Date, c1)
        options.addConverter(Calendar, {})
        options.addConverter(Date, c2)
        options.addConverter(java.sql.Date, {})

        assert options.@converters.size() == 4
        assert options.@converters[2].convert(null, null) == 'c2'
        assert !options.@converters.find { it.convert(null, null) == 'c1' }
    }

    void testConvertersBadInput() {
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().addConverter(null, null)
        }
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().addConverter(Date, null)
        }
        shouldFail(IllegalArgumentException) {
            new JsonGenerator.Options().addConverter(Date, {-> 'no args closure'})
        }
        shouldFail(IllegalArgumentException) {
            new JsonGenerator.Options().addConverter(Date, { UUID obj -> 'mis-matched types'})
        }
        shouldFail(IllegalArgumentException) {
            new JsonGenerator.Options().addConverter(Date, { Date obj, UUID cs -> 'mis-matched types'})
        }
    }

    void testExcludesFieldsByName() {
        def generator = new JsonGenerator.Options()
                .excludeFieldsByName('name')
                .build()

        def ref = new JsonObject(name: 'Jason', properties: ['foo': 'bar'])
        def json = generator.toJson(ref)
        assert json == '{"properties":{"foo":"bar"}}'

        def jsonArray = ["foo", ["bar":"test","name":"Jane"], "baz"]
        def jsonExpected = '["foo",{"bar":"test"},"baz"]'
        assert generator.toJson(jsonArray) == jsonExpected
        assert generator.toJson(jsonArray as Object[]) == jsonExpected
        assert generator.toJson(jsonArray.iterator()) == jsonExpected
        assert generator.toJson((Iterable)jsonArray) == jsonExpected

        def excludeList = ['foo', 'bar', "${'zoo'}"]
        generator = new JsonGenerator.Options()
                .excludeFieldsByName(excludeList)
                .build()

        json = generator.toJson([foo: 'one', bar: 'two', baz: 'three', zoo: 'four'])
        assert json == '{"baz":"three"}'
    }

    void testExcludeFieldsByNameBadInput() {
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().excludeFieldsByName(null)
        }
    }

    void testExcludeFieldsByNameShouldIgnoreNulls() {
        def opts = new JsonGenerator.Options()
                .excludeFieldsByName('foo', null, "${'bar'}")
                .excludeFieldsByName([new StringBuilder('one'), null, 'two'])

        assert opts.@excludedFieldNames.size() == 4
        assert !opts.@excludedFieldNames.contains(null)
    }

    void testExcludesFieldsByType() {
        def generator = new JsonGenerator.Options()
                .excludeFieldsByType(Date)
                .build()

        def ref = [name: 'Jason', dob: new Date(), location: 'Los Angeles']
        assert generator.toJson(ref) == '{"name":"Jason","location":"Los Angeles"}'

        def jsonArray = ["foo", "bar", new Date()]
        def jsonExpected = '["foo","bar"]'
        assert generator.toJson(jsonArray) == jsonExpected
        assert generator.toJson(jsonArray as Object[]) == jsonExpected
        assert generator.toJson(jsonArray.iterator()) == jsonExpected
        assert generator.toJson((Iterable)jsonArray) == jsonExpected

        generator = new JsonGenerator.Options()
                .excludeFieldsByType(Integer)
                .excludeFieldsByType(Boolean)
                .excludeFieldsByType(Character)
                .excludeFieldsByType(Calendar)
                .excludeFieldsByType(UUID)
                .excludeFieldsByType(URL)
                .excludeFieldsByType(Closure)
                .excludeFieldsByType(Expando)
                .excludeFieldsByType(TreeMap)
                .excludeFieldsByType(Date)
                .build()

        assert generator.toJson(Integer.valueOf(7)) == ''
        assert generator.toJson(Boolean.TRUE) == ''
        assert generator.toJson((Character)'c') == ''
        assert generator.toJson(Calendar.getInstance()) == ''
        assert generator.toJson(UUID.randomUUID()) == ''
        assert generator.toJson(new URL('http://groovy-lang.org')) == ''
        assert generator.toJson({ url new URL('http://groovy-lang.org') }) == ''
        assert generator.toJson(new Expando()) == ''
        assert generator.toJson(new TreeMap()) == ''
        assert generator.toJson(new java.sql.Date(new Date().getTime())) == ''

        def excludeList = [URL, Date]
        generator = new JsonGenerator.Options()
                .excludeFieldsByType(excludeList)
                .build()

        def json = generator.toJson([foo: new Date(), bar: 'two', baz: new URL('http://groovy-lang.org')])
        assert json == '{"bar":"two"}'
    }

    void testExcludeFieldsByTypeBadInput() {
        shouldFail(NullPointerException) {
            new JsonGenerator.Options().excludeFieldsByType(null)
        }
    }

    void testExcludeFieldsByTypeShouldIgnoreNulls() {
        def opts = new JsonGenerator.Options()
                .excludeFieldsByType(Date, null, URL)
                .excludeFieldsByType([Calendar, null, TreeMap])

        assert opts.@excludedFieldTypes.size() == 4
        assert !opts.@excludedFieldTypes.contains(null)
    }

    void testDisableUnicodeEscaping() {
        def json = new JsonGenerator.Options()
                .disableUnicodeEscaping()
                .build()

        String unicodeString = 'ΚΡΕΩΠΟΛΕΙΟ'
        assert json.toJson([unicodeString]) == """["${unicodeString}"]"""

        assert json.toJson(['KÉY':'VALUE']) == '{"KÉY":"VALUE"}'
    }

}

class JsonBar {
    String favoriteDrink
    Date lastVisit
    JsonCyclicReference cycle = new JsonCyclicReference()
    JsonBar(String favoriteDrink, Date lastVisit) {
        this.favoriteDrink = favoriteDrink
        this.lastVisit = lastVisit
    }
}

class JsonCyclicReference {
    static final DEFAULT = new JsonCyclicReference()
    JsonCyclicReference() { }
}
