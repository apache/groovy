/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.json

/**
 * @author Tim Yates
 * @author Guillaume Laforge
 */
class StreamingJsonBuilderTest extends GroovyTestCase {

    void testJsonBuilderConstructor() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w, [a: 1, b: true])
            assert w.toString() == '{"a":1,"b":true}'
        }
    }

    void testEmptyArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json([])
            assert w.toString() == '[]'
        }
    }

    void testSimpleArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json 1, 2, "a", "b"

            assert w.toString() == '[1,2,"a","b"]'
        }
    }

    void testComplexArray() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json 1, 2, [k: true], "a", "b", [3, "c"]

            assert w.toString() == '[1,2,{"k":true},"a","b",[3,"c"]]'
        }
    }

    void testMap() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json a: 1, b: 2

            assert w.toString() == '{"a":1,"b":2}'
        }
    }

    void testEmptyObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json {}

            assert w.toString() == '{}'
        }
    }

    void testBasicObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
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
            def json = new StreamingJsonBuilder( w )
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
            def json = new StreamingJsonBuilder( w )
            json.person {
                name "Guillaume"
                age 33
            }

            assert w.toString() == '{"person":{"name":"Guillaume","age":33}}'
        }
    }

    void testMethodCallWithNamedArguments() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json.person name: "Guillaume", age: 33

            assert w.toString() == '{"person":{"name":"Guillaume","age":33}}'
        }
    }

    void testThrowAnExceptionWhenPassingSomethingElseThanAClosure() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )

            shouldFail(JsonException) {
                json.something 1, 2, 3
            }
        }
    }

    void testListWithAnEmptyObject() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json([[:]])

            assert w.toString() == '[{}]'
        }
    }

    void testListOfObjects() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json([name: "Guillaume"], [name: "Jochen"], [name: "Paul"])

            assert w.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
        }
    }

    void testElementHasListOfObjects() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json.response {
                results 1, [a: 2]
            }

            assert w.toString() == '{"response":{"results":[1,{"a":2}]}}'
        }
    }

    void testComplexStructureFromTheGuardian() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
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
            def json = new StreamingJsonBuilder( w )
            json.content {
                list([:],[another:[a:[1,2,3]]])
            }

            assert w.toString() == '''{"content":{"list":[{},{"another":{"a":[1,2,3]}}]}}'''
        }
    }

    void testEmptyList() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
            json()

            assert w.toString() == '''[]'''
        }
    }

    void testTrendsFromTwitter() {
        new StringWriter().with { w ->
            def json = new StreamingJsonBuilder( w )
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
            def builder = new StreamingJsonBuilder( w )
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
            def builder = new StreamingJsonBuilder( w )
            builder { elem 1, 2, 3 }
            
            assert w.toString() == '{"elem":[1,2,3]}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder( w )
            builder.elem()
            
            assert w.toString() == '{"elem":{}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder( w )
            builder.elem(a: 1, b: 2) { c 3 }
            
            assert w.toString() == '{"elem":{"a":1,"b":2,"c":3}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder( w )
            builder.elem( [:] ) { c 3 }
            
            assert w.toString() == '{"elem":{"c":3}}'
        }
        new StringWriter().with { w ->
            def builder = new StreamingJsonBuilder( w )
            shouldFail(JsonException) {
                builder.elem(a: 1, b: 2, "ABCD")
            }
        }
    }
}
