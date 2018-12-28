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

class JsonBuilderTest extends GroovyTestCase {

    void testJsonBuilderConstructor() {
        def json = new JsonBuilder([a: 1, b: true])

        assert json.toString() == '{"a":1,"b":true}'
    }

    void testEmptyArray() {
        def json = new JsonBuilder()
        json([])

        assert json.toString() == '[]'
    }

    void testSimpleArray() {
        def json = new JsonBuilder()
        json 1, 2, "a", "b"

        assert json.toString() == '[1,2,"a","b"]'
    }

    void testComplexArray() {
        def json = new JsonBuilder()
        json 1, 2, [k: true], "a", "b", [3, "c"]

        assert json.toString() == '[1,2,{"k":true},"a","b",[3,"c"]]'
    }

    void testMap() {
        def json = new JsonBuilder()
        json a: 1, b: 2

        assert json.toString() == '{"a":1,"b":2}'
    }

    void testEmptyObject() {
        def json = new JsonBuilder()
        json {}

        assert json.toString() == '{}'
    }

    void testBasicObject() {
        def json = new JsonBuilder()
        json {
            a 1
            b true
            c null
        }

        assert json.toString() == '{"a":1,"b":true,"c":null}'
    }
    
    void testNestedObjects() {
        def json = new JsonBuilder()
        json {
            a {
                b {
                    c 1
                }
            }
        }

        assert json.toString() == '{"a":{"b":{"c":1}}}'
    }

    void testStandardBuilderStyle() {
        def json = new JsonBuilder()
        json.person {
            name "Guillaume"
            age 33
        }

        assert json.toString() == '{"person":{"name":"Guillaume","age":33}}'
    }

    void testMethodCallWithNamedArguments() {
        def json = new JsonBuilder()
        json.person name: "Guillaume", age: 33

        assert json.toString() == '{"person":{"name":"Guillaume","age":33}}'
    }

    void testThrowAnExceptionWhenPassingSomethingElseThanAClosure() {
        def json = new JsonBuilder()

        shouldFail(JsonException) {
            json.something 1, 2, 3
        }
    }

    void testListWithAnEmptyObject() {
        def json = new JsonBuilder()
        json([[:]])

        assert json.toString() == '[{}]'
    }

    void testListOfObjects() {
        def json = new JsonBuilder()
        json([name: "Guillaume"], [name: "Jochen"], [name: "Paul"])

        assert json.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
    }

    void testElementHasListOfObjects() {
        def json = new JsonBuilder()
        json.response {
            results 1, [a: 2]
        }

        assert json.toString() == '{"response":{"results":[1,{"a":2}]}}'
    }

    private class Author {
        String name
    }

    void testCollectionAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        def json = new JsonBuilder()
        json authors, { Author author ->
            name author.name
        }

        assert json.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
    }

    void testIterableAndClosure() {
        Iterable authorIterable = [iterator:{->
            [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")].iterator()
        }] as Iterable
        def json = new JsonBuilder()
        json authorIterable, { Author author ->
            name author.name
        }

        assert json.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
    }

    void testMethodWithIterableAndClosure() {
        Iterable authorIterable = [iterator:{->
            [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")].iterator()
        }] as Iterable

        def json = new JsonBuilder()
        json.authors authorIterable, { Author author ->
            name author.name
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testNestedMethodWithIterableAndClosure() {
        Iterable authorIterable = [iterator:{->
            [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")].iterator()
        }] as Iterable

        def json = new JsonBuilder()
        json {
            authors authorIterable, { Author author ->
                name author.name
            }
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testMethodWithArrayAndClosure() {
        def authorArray = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")] as Author[]
        def json = new JsonBuilder()
        json.authors authorArray, { Author author ->
            name author.name
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testNestedMethodWithArrayAndClosure() {
        Author[] authorArray = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")] as Author[]
        def json = new JsonBuilder()
        json {
            authors authorArray, { Author author ->
                name author.name
            }
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testMethodWithCollectionAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        def json = new JsonBuilder()
        json.authors authors, { Author author ->
            name author.name
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testNestedMethodWithCollectionAndClosure() {
        def theAuthors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        def json = new JsonBuilder()
        json {
            authors theAuthors, { Author author ->
                name author.name
            }
        }

        assert json.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
    }

    void testComplexStructureFromTheGuardian() {
        def json = new JsonBuilder()
        json.response {
            status "ok"
            userTier "free"
            total 2413
            startIndex 1
            pageSize 10
            currentPage 1
            pages 242
            orderBy "newest"
            results([
                id: "world/video/2011/jan/19/tunisia-demonstrators-democracy-video",
                sectionId: "world",
                sectionName: "World news",
                webPublicationDate: "2011-01-19T15:12:46Z",
                webTitle: "Tunisian demonstrators demand new democracy - video",
                webUrl: "http://www.guardian.co.uk/world/video/2011/jan/19/tunisia-demonstrators-democracy-video",
                apiUrl: "http://content.guardianapis.com/world/video/2011/jan/19/tunisia-demonstrators-democracy-video"
            ],
            [
                id: "world/gallery/2011/jan/19/tunisia-protests-pictures",
                sectionId: "world",
                sectionName: "World news",
                webPublicationDate: "2011-01-19T15:01:09Z",
                webTitle: "Tunisia protests continue in pictures ",
                webUrl: "http://www.guardian.co.uk/world/gallery/2011/jan/19/tunisia-protests-pictures",
                apiUrl: "http://content.guardianapis.com/world/gallery/2011/jan/19/tunisia-protests-pictures"
            ])
        }

        assert json.toString() ==
                '''{"response":{"status":"ok","userTier":"free","total":2413,"startIndex":1,"pageSize":10,"currentPage":1,"pages":242,"orderBy":"newest","results":[{"id":"world/video/2011/jan/19/tunisia-demonstrators-democracy-video","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:12:46Z","webTitle":"Tunisian demonstrators demand new democracy - video","webUrl":"http://www.guardian.co.uk/world/video/2011/jan/19/tunisia-demonstrators-democracy-video","apiUrl":"http://content.guardianapis.com/world/video/2011/jan/19/tunisia-demonstrators-democracy-video"},{"id":"world/gallery/2011/jan/19/tunisia-protests-pictures","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:01:09Z","webTitle":"Tunisia protests continue in pictures ","webUrl":"http://www.guardian.co.uk/world/gallery/2011/jan/19/tunisia-protests-pictures","apiUrl":"http://content.guardianapis.com/world/gallery/2011/jan/19/tunisia-protests-pictures"}]}}'''
    }

    void testNestedListMap() {
        def json = new JsonBuilder()
        json.content {
            list([:], [another: [a: [1, 2, 3]]])
        }

        assert json.toString() == '''{"content":{"list":[{},{"another":{"a":[1,2,3]}}]}}'''
    }

    void testEmptyList() {
        def json = new JsonBuilder()
        json()
        assert json.toString() == '''[]'''
    }

    void testTrendsFromTwitter() {
        def json = new JsonBuilder()
        json.trends {
            "2010-06-22 17:20" ([
                    name: "Groovy rules",
                    query: "Groovy rules"
            ], {
                    name "#worldcup"
                    query "#worldcup"
            }, [
                    name: "Uruguai",
                    query: "Uruguai"
            ])
            "2010-06-22 06:20" ({
                name "#groovy"
                query "#groovy"
            }, [
                name: "#java",
                query: "#java"
            ])
        }
        assert json.toString() == '''{"trends":{"2010-06-22 17:20":[{"name":"Groovy rules","query":"Groovy rules"},{"name":"#worldcup","query":"#worldcup"},{"name":"Uruguai","query":"Uruguai"}],"2010-06-22 06:20":[{"name":"#groovy","query":"#groovy"},{"name":"#java","query":"#java"}]}}'''
    }

    void testBuilderAsWritable() {
        def json = new JsonBuilder()
        json.person {
            name "Guillaume"
            age 33
        }

        def output = new StringWriter()
        output << json
        assert output.toString() == '{"person":{"name":"Guillaume","age":33}}'
    }

    void testExampleFromTheGep7Page() {
        def builder = new groovy.json.JsonBuilder()
        def root = builder.people {
            person {
                firstName 'Guillame'
                lastName 'Laforge'
                // Maps are valid values for objects too
                address(
                        city: 'Paris',
                        country: 'France',
                        zip: 12345,
                )
                married true
                conferences 'JavaOne', 'Gr8conf'
            }
        }

        // creates a data structure made of maps (Json object) and lists (Json array)
        assert root instanceof Map

        assert builder.toString() == '{"people":{"person":{"firstName":"Guillame","lastName":"Laforge","address":{"city":"Paris","country":"France","zip":12345},"married":true,"conferences":["JavaOne","Gr8conf"]}}}'
    }

    void testEdgeCases() {
        def builder = new JsonBuilder()

        assert builder { elem 1, 2, 3 } == [elem: [1, 2, 3]]

        assert builder.elem() == [elem: [:]]
        assert builder.elem(a: 1, b: 2) { c 3 } == [elem: [a: 1, b: 2, c: 3]]

        shouldFail(JsonException) {
            builder.elem(a: 1, b: 2, "ABCD")
        }
    }

    void testSupportForUUID() {
        def id = UUID.randomUUID()
        def json = new groovy.json.JsonBuilder()
        json { uuid id }
        assert json.toString() == "{\"uuid\":\"${id.toString()}\"}"
    }

    // GROOVY-4988
    void testStringEscape() {
        def original, serialized, deserialized

        original = [elem: "\\n"]
        serialized = (new JsonBuilder(original)).toString()
        deserialized = (new JsonSlurper()).parseText(serialized)
        assert original.elem == deserialized.elem

        original = [elem: "\\t"]
        serialized = (new JsonBuilder(original)).toString()
        deserialized = (new JsonSlurper()).parseText(serialized)
        assert original.elem == deserialized.elem

        original = [elem: "\\\\n"]
        serialized = (new JsonBuilder(original)).toString()
        deserialized = (new JsonSlurper()).parseText(serialized)
        assert original.elem == deserialized.elem

        original = [elem: "\\u000A"]
        serialized = (new JsonBuilder(original)).toString()
        deserialized = (new JsonSlurper()).parseText(serialized)
        assert original.elem == deserialized.elem
    }

    void testSpecialCharEscape() {
        assert new JsonBuilder({'"' 0}).toString() == '{"\\"":0}'
        assert new JsonBuilder({'\b' 0}).toString() == '{"\\b":0}'
        assert new JsonBuilder({'\f' 0}).toString() == '{"\\f":0}'
        assert new JsonBuilder({'\n' 0}).toString() == '{"\\n":0}'
        assert new JsonBuilder({'\r' 0}).toString() == '{"\\r":0}'
        assert new JsonBuilder({'\t' 0}).toString() == '{"\\t":0}'
        assert new JsonBuilder({'\\' 0}).toString() == '{"\\\\":0}'
        assert new JsonBuilder({'\1' 0}).toString() == '{"\\u0001":0}'
        assert new JsonBuilder({'\u0002' 0}).toString() == '{"\\u0002":0}'
    }

    void testWithGenerator() {
        def generator = new JsonGenerator.Options()
                .excludeNulls()
                .dateFormat('yyyyMM')
                .excludeFieldsByName('secretKey', 'creditCardNumber')
                .excludeFieldsByType(URL)
                .addConverter(java.util.concurrent.atomic.AtomicBoolean) { ab -> ab.get() }
                .build()

        def json = new JsonBuilder(generator)

        json.payload {
            id 'YT-1234'
            location null
            secretKey 'J79-A25'
            creditCardNumber '123-444-789-2233'
            site new URL('http://groovy-lang.org')
            isActive new java.util.concurrent.atomic.AtomicBoolean(true)
        }

        assert json.toString() == '{"payload":{"id":"YT-1234","isActive":true}}'

        json = new JsonBuilder(['foo', null, 'bar', new URL('http://groovy-lang.org')], generator)
        assert json.toString() == '["foo","bar"]'
    }

}
