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
import groovy.transform.CompileStatic

class StreamingJsonBuilderTest extends GroovyTestCase {

    @CompileStatic
    void testJsonBuilderUsageWithCompileStatic() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.call('response') {
                call "status", "ok"
                call "userTier", "free"
                call "total", 2413
                call "startIndex", 1
                call "pageSize", 10
                call "currentPage", 1
                call "pages", 242
                call "orderBy", "newest"
                call("results",
                [
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

            assert w.toString() ==
                    '''{"response":{"status":"ok","userTier":"free","total":2413,"startIndex":1,"pageSize":10,"currentPage":1,"pages":242,"orderBy":"newest","results":[{"id":"world/video/2011/jan/19/tunisia-demonstrators-democracy-video","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:12:46Z","webTitle":"Tunisian demonstrators demand new democracy - video","webUrl":"http://www.guardian.co.uk/world/video/2011/jan/19/tunisia-demonstrators-democracy-video","apiUrl":"http://content.guardianapis.com/world/video/2011/jan/19/tunisia-demonstrators-democracy-video"},{"id":"world/gallery/2011/jan/19/tunisia-protests-pictures","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:01:09Z","webTitle":"Tunisia protests continue in pictures ","webUrl":"http://www.guardian.co.uk/world/gallery/2011/jan/19/tunisia-protests-pictures","apiUrl":"http://content.guardianapis.com/world/gallery/2011/jan/19/tunisia-protests-pictures"}]}}'''
        }
    }

    void testJsonBuilderWithWritableValue() {
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            def writable = new Writable() {
                @Override
                Writer writeTo(Writer writer) throws IOException {
                    def value = "world"
                    new StreamingJsonBuilder(writer).call {
                        sectionId "$value"
                        itemId "foo"
                        assert delegate instanceof StreamingJsonBuilder.StreamingJsonDelegate
                    }
                    return writer
                }
            }
            builder.response {
                status "ok"
                results writable
            }

            assert w.toString() == '{"response":{"status":"ok","results":{"sectionId":"world","itemId":"foo"}}}'
        }
    }

    void testJsonBuilderWithNestedClosures() {
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)

            builder.response {
                status "ok"
                results {
                    sectionId "world"
                    assert delegate instanceof StreamingJsonBuilder.StreamingJsonDelegate
                }
            }

            assert w.toString() == '{"response":{"status":"ok","results":{"sectionId":"world"}}}'
        }
    }

    void testJsonBuilderConstructor() {
        new StringWriter().with { w ->
            new StreamingJsonBuilder(w, [a: 1, b: true])
            assert w.toString() == '{"a":1,"b":true}'
        }
    }

    void testUnescapedJson() {
        new StringWriter().with { w ->
            new StreamingJsonBuilder(w).call {
                a 1
                b JsonOutput.unescaped('{"name":"Fred"}')
                c 3
            }
            assert w.toString() == '{"a":1,"b":{"name":"Fred"},"c":3}'
        }
    }


    @CompileStatic
    void testUnescapedJsonCompileStatic() {
        new StringWriter().with { w ->
            new StreamingJsonBuilder(w).call {
                call 'a', 1
                call 'b', JsonOutput.unescaped('{"name":"Fred"}')
                call 'c', 3
            }
            assert w.toString() == '{"a":1,"b":{"name":"Fred"},"c":3}'
        }
    }

    void testEmptyArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json([])
            assert w.toString() == '[]'
        }
    }

    void testSimpleArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json 1, 2, "a", "b"

            assert w.toString() == '[1,2,"a","b"]'
        }
    }

    void testComplexArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json 1, 2, [k: true], "a", "b", [3, "c"]

            assert w.toString() == '[1,2,{"k":true},"a","b",[3,"c"]]'
        }
    }

    void testMap() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json a: 1, b: 2

            assert w.toString() == '{"a":1,"b":2}'
        }
    }

    void testEmptyObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json {}

            assert w.toString() == '{}'
        }
    }

    void testBasicObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json {
                a 1
                b true
                c null
            }

            assert w.toString() == '{"a":1,"b":true,"c":null}'
        }
    }
    
    void testNestedObjects() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json {
                a {
                    b {
                        c 1
                    }
                }
            }

            assert w.toString() == '{"a":{"b":{"c":1}}}'
        }
    }

    void testStandardBuilderStyle() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.person {
                name "Guillaume"
                age 33
            }

            assert w.toString() == '{"person":{"name":"Guillaume","age":33}}'
        }
    }

    void testMethodCallWithNamedArguments() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.person name: "Guillaume", age: 33

            assert w.toString() == '{"person":{"name":"Guillaume","age":33}}'
        }
    }

    void testThrowAnExceptionWhenPassingSomethingElseThanAClosure() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)

            shouldFail(JsonException) {
                json.something 1, 2, 3
            }
        }
    }

    void testListWithAnEmptyObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json([[:]])

            assert w.toString() == '[{}]'
        }
    }

    void testListOfObjects() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json([name: "Guillaume"], [name: "Jochen"], [name: "Paul"])

            assert w.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
        }
    }

    void testElementHasListOfObjects() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.response {
                results 1, [a: 2]
            }

            assert w.toString() == '{"response":{"results":[1,{"a":2}]}}'
        }
    }

    private class Author {
        String name
    }

    void testCollectionAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json authors, { Author author ->
                name author.name
            }

            assert w.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
        }
    }

    void testIterableAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]
        Iterable it = [iterator:{->
            authors.iterator()
        }] as Iterable
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json it, { Author author ->
                name author.name
            }

            assert w.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
        }
    }

    void testMethodWithIterableAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]
        Iterable it = [iterator:{->
            authors.iterator()
        }] as Iterable

        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.authors it, { Author author ->
                name author.name
            }

            assert w.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
        }
    }

    void testMethodWithArrayAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]


        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.authors authors as Author[], { Author author ->
                name author.name
            }

            assert w.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
        }
    }

    void testMethodWithCollectionAndClosure() {
        def authors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.authors authors, { Author author ->
                name author.name
            }

            assert w.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
        }
    }

    void testNestedMethodWithCollectionAndClosure() {
        def theAuthors = [new Author(name: "Guillaume"), new Author(name: "Jochen"), new Author(name: "Paul")]

        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json {
                authors theAuthors, { Author author ->
                    name author.name
                }
            }

            assert w.toString() == '{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}'
        }
    }

    void testComplexStructureFromTheGuardian() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
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

            assert w.toString() ==
                '''{"response":{"status":"ok","userTier":"free","total":2413,"startIndex":1,"pageSize":10,"currentPage":1,"pages":242,"orderBy":"newest","results":[{"id":"world/video/2011/jan/19/tunisia-demonstrators-democracy-video","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:12:46Z","webTitle":"Tunisian demonstrators demand new democracy - video","webUrl":"http://www.guardian.co.uk/world/video/2011/jan/19/tunisia-demonstrators-democracy-video","apiUrl":"http://content.guardianapis.com/world/video/2011/jan/19/tunisia-demonstrators-democracy-video"},{"id":"world/gallery/2011/jan/19/tunisia-protests-pictures","sectionId":"world","sectionName":"World news","webPublicationDate":"2011-01-19T15:01:09Z","webTitle":"Tunisia protests continue in pictures ","webUrl":"http://www.guardian.co.uk/world/gallery/2011/jan/19/tunisia-protests-pictures","apiUrl":"http://content.guardianapis.com/world/gallery/2011/jan/19/tunisia-protests-pictures"}]}}'''
        }
    }

    void testNestedListMap() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json.content {
                list([:], [another: [a: [1, 2, 3]]])
            }

            assert w.toString() == '''{"content":{"list":[{},{"another":{"a":[1,2,3]}}]}}'''
        }
    }

    void testEmptyList() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
            json()

            assert w.toString() == '''[]'''
        }
    }

    void testTrendsFromTwitter() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder(w)
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
            assert w.toString() == '''{"trends":{"2010-06-22 17:20":[{"name":"Groovy rules","query":"Groovy rules"},{"name":"#worldcup","query":"#worldcup"},{"name":"Uruguai","query":"Uruguai"}],"2010-06-22 06:20":[{"name":"#groovy","query":"#groovy"},{"name":"#java","query":"#java"}]}}'''
        }
    }

    void testExampleFromTheGep7Page() {
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            builder.people {
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

            assert w.toString() == '{"people":{"person":{"firstName":"Guillame","lastName":"Laforge","address":{"city":"Paris","country":"France","zip":12345},"married":true,"conferences":["JavaOne","Gr8conf"]}}}'
        }
    }

    void testEdgeCases() {
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            builder { elem 1, 2, 3 }
            
            assert w.toString() == '{"elem":[1,2,3]}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            builder.elem()

            assert w.toString() == '{"elem":{}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            builder.elem(a: 1, b: 2) { c 3 }
            
            assert w.toString() == '{"elem":{"a":1,"b":2,"c":3}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)
            builder.elem([:]) { c 3 }
            
            assert w.toString() == '{"elem":{"c":3}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w)

            shouldFail(JsonException) {
                builder.elem(a: 1, b: 2, "ABCD")
            }
        }
    }

    void testWithGenerator() {
        def generator = new JsonGenerator.Options()
                .excludeNulls()
                .dateFormat('yyyyMM')
                .excludeFieldsByName('secretKey', 'creditCardNumber')
                .excludeFieldsByType(URL)
                .addConverter(java.util.concurrent.atomic.AtomicBoolean) { ab -> ab.get() }
                .build()

        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w, generator)

            builder.payload {
                id 'YT-1234'
                location null
                secretKey 'J79-A25'
                creditCardNumber '123-444-789-2233'
                site new URL('http://groovy-lang.org')
                isActive new java.util.concurrent.atomic.AtomicBoolean(true)
            }

            assert w.toString() == '{"payload":{"id":"YT-1234","isActive":true}}'
        }
    }

    @CompileStatic
    void testWithGeneratorCompileStatic() {
        def generator = new JsonGenerator.Options()
                .excludeNulls()
                .dateFormat('yyyyMM')
                .excludeFieldsByName('secretKey', 'creditCardNumber')
                .excludeFieldsByType(URL)
                .addConverter(java.util.concurrent.atomic.AtomicBoolean) { ab -> ab.get() }
                .build()

        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder(w, generator)
            builder.call('payload') {
                call 'id', 'YT-1234'
                call 'location', (String)null
                call 'secretKey', 'J79-A25'
                call 'creditCardNumber', '123-444-789-2233'
                call 'site', new URL('http://groovy-lang.org')
                call 'isActive', new java.util.concurrent.atomic.AtomicBoolean(true)
            }

            assert w.toString() == '{"payload":{"id":"YT-1234","isActive":true}}'
        }
    }
}
