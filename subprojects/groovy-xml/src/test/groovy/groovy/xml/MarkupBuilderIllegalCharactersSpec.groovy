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

import spock.lang.Specification

class MarkupBuilderIllegalCharactersSpec extends Specification {
    static final char[] nullCharacter  = [0x0]
    static final char[] c0Controls     = ((0x0..0x8) + [0xB] + (0xE..0x1F))
    static final char[] extControl1    = (0x7F..0x84)
    static final char[] nextLine       = [0x85]
    static final char[] extControl2    = (0x86..0x9F)
    static final char[] surrogates     = (0xD800..0xDFFF)
    static final char[] nonCharacters1 = (0xFDD0..0xFDEF)
    static final char[] nonCharacters2 = [0xFFFE, 0xFFFF]

    def "Default MarkupBuilder character filter for #characterRange"(String characterRange, boolean shouldFilter,  char[] characters) {
        given:
        def writer
        def xml
        def characterFilter = MarkupBuilder.CharFilter.XML_STRICT

        expect:
        characters.each {
            writer = new StringWriter()
            xml = new MarkupBuilder(writer)
            xml.characterFilter = characterFilter
            def encoded = shouldFilter ? '\uFFFD' : it

            xml.tag(attr: it, it)
            def actual = writer.toString()

            assert actual == "<tag attr='$encoded'>$encoded</tag>",
                    "Character (${it as int}) is encoded correctly"
        }

        where:
        characterRange              | shouldFilter | characters
        'Null'                      | true         | nullCharacter         // Not neccessarily XML, not allowed in HTML
        'C0 control w/o whitespace' | true         | c0Controls            // Not neccessarily XML, not in HTML char references
        'ext control I'             | true         | extControl1           // Discouraged XML, not in HTML char references
        'Next line NEL'             | true         | nextLine              // Not in HTML char references
        'ext control II'            | true         | extControl2           // Discouraged XML, not in HTML char references
        'Surrogates'                | true         | surrogates            // Not neccessarily XML, not in HTML char references
        'Non-characters I'          | true         | nonCharacters1        // Discouraged XML, not in HTML char references
        'Non-characters II'         | true         | nonCharacters2        // Discouraged XML, not in HTML char references
    }

    def "MarkupBuilder ALL_XML character filter for #characterRange"(String characterRange, boolean shouldFilter,  char[] characters) {
        given:
        def writer
        def xml
        def characterFilter = MarkupBuilder.CharFilter.XML_ALL

        expect:
        characters.each {
            writer = new StringWriter()
            xml = new MarkupBuilder(writer)
            xml.characterFilter = characterFilter
            def encoded = shouldFilter ? '\uFFFD' : it

            xml.tag(attr: it, it)
            def actual = writer.toString()

            assert actual == "<tag attr='$encoded'>$encoded</tag>",
                    "Character (${it as int}) is encoded correctly"
        }

        where:
        characterRange              | shouldFilter | characters
        'Null'                      | true         | nullCharacter         // Not neccessarily XML, not allowed in HTML
        'C0 control w/o whitespace' | true         | c0Controls            // Not neccessarily XML, not in HTML char references
        'ext control I'             | false        | extControl1           // Discouraged XML, not in HTML char references
        'Next line NEL'             | false        | nextLine              // Not in HTML char references
        'ext control II'            | false        | extControl2           // Discouraged XML, not in HTML char references
        'Surrogates'                | true         | surrogates            // Not neccessarily XML, not in HTML char references
        'Non-characters I'          | false        | nonCharacters1        // Discouraged XML, not in HTML char references
        'Non-characters II'         | false        | nonCharacters2        // Discouraged XML, not in HTML char references
    }

    def "MarkupBuilder NONE character filter for #characterRange"(String characterRange, boolean shouldFilter, char[] characters) {
        given:
        def writer
        def xml
        def characterFilter = MarkupBuilder.CharFilter.NONE

        expect:
        characters.each {
            writer = new StringWriter()
            xml = new MarkupBuilder(writer)
            xml.characterFilter = characterFilter
            def encoded = shouldFilter ? '\uFFFD' : it

            xml.tag(attr: it, it)
            def actual = writer.toString()

            assert actual == "<tag attr='$encoded'>$encoded</tag>",
                    "Character (${it as int}) is encoded correctly"
        }

        where:
        characterRange              | shouldFilter | characters
        'Null'                      | false        | nullCharacter         // Not neccessarily XML, not allowed in HTML
        'C0 control w/o whitespace' | false        | c0Controls            // Not neccessarily XML, not in HTML char references
        'ext control I'             | false        | extControl1           // Discouraged XML, not in HTML char references
        'Next line NEL'             | false        | nextLine              // Not in HTML char references
        'ext control II'            | false        | extControl2           // Discouraged XML, not in HTML char references
        'Surrogates'                | false        | surrogates            // Not neccessarily XML, not in HTML char references
        'Non-characters I'          | false        | nonCharacters1        // Discouraged XML, not in HTML char references
        'Non-characters II'         | false        | nonCharacters2        // Discouraged XML, not in HTML char references
    }
}
