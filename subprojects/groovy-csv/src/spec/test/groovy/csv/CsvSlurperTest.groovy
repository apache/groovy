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
package groovy.csv

import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class CsvSlurperTest {

    @Test
    void testParseText() {
        // tag::parse_text[]
        def csv = new CsvSlurper().parseText('name,age\nAlice,30\nBob,25')
        assert csv.size() == 2
        assert csv[0].name == 'Alice'
        assert csv[0].age == '30'
        assert csv[1].name == 'Bob'
        // end::parse_text[]
    }

    @Test
    void testPropertyAccess() {
        // tag::property_access[]
        def csv = new CsvSlurper().parseText('''\
            name,city,country
            Alice,London,UK
            Bob,Paris,France'''.stripIndent())
        assert csv[0].city == 'London'
        assert csv[1].country == 'France'
        // end::property_access[]
    }

    @Test
    void testCustomSeparator() {
        // tag::custom_separator[]
        def csv = new CsvSlurper().setSeparator((char) '\t').parseText('name\tage\nAlice\t30')
        assert csv[0].name == 'Alice'
        assert csv[0].age == '30'
        // end::custom_separator[]
    }

    @Test
    void testQuotedFields() {
        // tag::quoted_fields[]
        def csv = new CsvSlurper().parseText('name,note\nAlice,"hello, world"\nBob,"say ""hi"""')
        assert csv[0].note == 'hello, world'
        assert csv[1].note == 'say "hi"'
        // end::quoted_fields[]
    }

    @Test
    void testEmptyInput() {
        def csv = new CsvSlurper().parseText('')
        assert csv.isEmpty()
    }

    @Test
    void testSingleRow() {
        def csv = new CsvSlurper().parseText('name,age\nAlice,30')
        assert csv.size() == 1
        assert csv[0].name == 'Alice'
    }

    @Test
    void testSemicolonSeparator() {
        def csv = new CsvSlurper().setSeparator((char) ';').parseText('name;age\nAlice;30')
        assert csv[0].name == 'Alice'
    }

    @Test
    void testParseFromReader() {
        def reader = new StringReader('name,age\nAlice,30')
        def csv = new CsvSlurper().parse(reader)
        assert csv[0].name == 'Alice'
    }

    @Test
    void testParseStreamDefaultsToUtf8() {
        def bytes = 'name,city\nAlice,café'.getBytes(StandardCharsets.UTF_8)
        def csv = new CsvSlurper().parse(new ByteArrayInputStream(bytes))
        assert csv[0].city == 'café'
    }

    @Test
    void testParseStreamWithCharset() {
        // bytes encoded as ISO-8859-1 (é is a single 0xE9 byte there, not valid UTF-8)
        def bytes = 'name,city\nAlice,café'.getBytes(StandardCharsets.ISO_8859_1)
        def csv = new CsvSlurper().parse(new ByteArrayInputStream(bytes), StandardCharsets.ISO_8859_1)
        assert csv[0].city == 'café'
    }

    @Test
    void testParsePathWithCharset() {
        def tmp = Files.createTempFile('csvslurper', '.csv')
        try {
            Files.write(tmp, 'name,city\nAlice,café'.getBytes(StandardCharsets.ISO_8859_1))
            def csv = new CsvSlurper().parse(tmp, StandardCharsets.ISO_8859_1)
            assert csv[0].city == 'café'
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    @Test
    void testParseAsPathWithCharset() {
        def tmp = Files.createTempFile('csvslurper', '.csv')
        try {
            Files.write(tmp, 'customer,amount\nCafé,9.99'.getBytes(StandardCharsets.ISO_8859_1))
            def sales = new CsvSlurper().parseAs(Sale, tmp.toFile(), StandardCharsets.ISO_8859_1)
            assert sales[0].customer == 'Café'
            assert sales[0].amount == 9.99
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    // tag::typed_parsing[]
    static class Sale {
        String customer
        BigDecimal amount
    }
    // end::typed_parsing[]

    @Test
    void testTypedParsing() {
        // tag::typed_parsing_usage[]
        def sales = new CsvSlurper().parseAs(Sale, 'customer,amount\nAcme,1500.00\nGlobex,250.50')
        assert sales.size() == 2
        assert sales[0].customer == 'Acme'
        assert sales[0].amount == 1500.00
        assert sales[1].customer == 'Globex'
        // end::typed_parsing_usage[]
    }

    @Test
    void testTypedParsingMultipleFields() {
        def items = new CsvSlurper().parseAs(Sale, 'customer,amount\nAlice,99.99')
        assert items[0] instanceof Sale
        assert items[0].amount instanceof BigDecimal
    }
}
