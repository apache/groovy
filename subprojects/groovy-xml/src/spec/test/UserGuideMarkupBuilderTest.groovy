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
package groovy.xml

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.tools.xml.DomToGroovy

/**
* Tests for the Groovy Xml user guide related to MarkupBuilderTest.
*/
class UserGuideMarkupBuilderTest  extends GroovyTestCase {

    void createCarsTest() {
        // tag::createCarsTest[]
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer) // <1>

        xml.records() { // <2>
            car(name:'HSV Maloo', make:'Holden', year:2006) {
                country('Australia')
                record(type:'speed', 'Production Pickup Truck with speed of 271kph')
            }
            car(name:'Royale', make:'Bugatti', year:1931) {
                country('France')
                record(type:'price', 'Most Valuable Car at $15 million')
            }
        }

        def records = new XmlSlurper().parseText(writer.toString()) // <3>

        assert records.car.first().name.text() == 'HSV Maloo'
        assert records.car.last().name.text() == 'Royale'
        // end::createCarsTest[]
    }

    void testCreateSimpleXml1() {
        // tag::testCreateSimpleXml1[]
        def xmlString = "<movie>the godfather</movie>" // <1>

        def xmlWriter = new StringWriter() // <2>
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        xmlMarkup.movie("the godfather") // <3>

        assert xmlString == xmlWriter.toString() // <4>
        // end::testCreateSimpleXml1[]
    }


    void testCreateSimpleXml2() {
        // tag::testCreateSimpleXml2[]
        def xmlString = "<movie id='2'>the godfather</movie>"

        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        xmlMarkup.movie(id: "2", "the godfather") // <1>

        assert xmlString == xmlWriter.toString()
        // end::testCreateSimpleXml2[]
    }

    void testCreateSimpleXml3() {
        // tag::testCreateSimpleXml3[]
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        xmlMarkup.movie(id: 2) { // <1>
            name("the godfather")
        }

        def movie = new XmlSlurper().parseText(xmlWriter.toString())

        assert movie.@id == 2
        assert movie.name.text() == 'the godfather'
        // end::testCreateSimpleXml3[]
    }

    void testNamespaceAware() {
        // tag::testNamespaceAware[]
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        xmlMarkup
            .'x:movies'('xmlns:x':'http://www.groovy-lang.org') { // <1>
                'x:movie'(id: 1, 'the godfather')
                'x:movie'(id: 2, 'ronin')
            }

        def movies =
            new XmlSlurper() // <2>
                .parseText(xmlWriter.toString())
                .declareNamespace(x:'http://www.groovy-lang.org')

        assert movies.'x:movie'.last().@id == 2
        assert movies.'x:movie'.last().text() == 'ronin'
        // end::testNamespaceAware[]
    }

    void testComplexUse1() {
        // tag::testComplexUse1[]
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        xmlMarkup
            .'x:movies'('xmlns:x':'http://www.groovy-lang.org') {
                (1..3).each { n -> // <1>
                    'x:movie'(id: n, "the godfather $n")
                    if (n % 2 == 0) { // <2>
                        'x:movie'(id: n, "the godfather $n (Extended)")
                    }
                }
            }

        def movies =
            new XmlSlurper()
                .parseText(xmlWriter.toString())
                .declareNamespace(x:'http://www.groovy-lang.org')

        assert movies.'x:movie'.size() == 4
        assert movies.'x:movie'*.text().every { name -> name.startsWith('the')}
        // end::testComplexUse1[]
    }

    void testComplexUse2() {
        // tag::testComplexUse2[]
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)

        // <1>
        Closure<MarkupBuilder> buildMovieList = { MarkupBuilder builder ->
            (1..3).each { n ->
                builder.'x:movie'(id: n, "the godfather $n")
                if (n % 2 == 0) {
                    builder.'x:movie'(id: n, "the godfather $n (Extended)")
                }
            }

            return builder
        }

        xmlMarkup.'x:movies'('xmlns:x':'http://www.groovy-lang.org') {
            buildMovieList(xmlMarkup) // <2>
        }

        def movies =
            new XmlSlurper()
                .parseText(xmlWriter.toString())
                .declareNamespace(x:'http://www.groovy-lang.org')

        assert movies.'x:movie'.size() == 4
        assert movies.'x:movie'*.text().every { name -> name.startsWith('the')}
        // end::testComplexUse2[]
    }

    void testDOMToGroovy() {
        // tag::testDOMToGroovy[]
        def songs  = """
            <songs>
              <song>
                <title>Here I go</title>
                <band>Whitesnake</band>
              </song>
            </songs>
        """

        def builder     =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()

            def inputStream = new ByteArrayInputStream(songs.bytes)
            def document    = builder.parse(inputStream)
            def output      = new StringWriter()
            def converter   = new DomToGroovy(new PrintWriter(output)) // <1>

            converter.print(document) // <2>

            String xmlRecovered  =
                new GroovyShell()
                .evaluate("""
                   def writer = new StringWriter()
                   def builder = new groovy.xml.MarkupBuilder(writer)
                   builder.${output}

                   return writer.toString()
                """) // <3>

            assert new XmlSlurper().parseText(xmlRecovered).song.title.text() == 'Here I go' // <4>
        // end::testDOMToGroovy[]
    }

    void testMkp1() {
        // tag::testMkp1[]
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter).rules {
            mkp.comment('THIS IS THE MAIN RULE') // <1>
            rule(sentence: mkp.yield('3 > n')) // <2>
        }

        // <3>
        assert xmlWriter.toString().contains('3 &gt; n')
        assert xmlWriter.toString().contains('<!-- THIS IS THE MAIN RULE -->')
        // end::testMkp1[]
    }

}
