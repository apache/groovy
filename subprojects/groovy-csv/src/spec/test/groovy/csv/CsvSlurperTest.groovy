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

import groovy.test.GroovyTestCase

class CsvSlurperTest extends GroovyTestCase {

    void testParseText() {
        // tag::parse_text[]
        def csv = new CsvSlurper().parseText('name,age\nAlice,30\nBob,25')
        assert csv.size() == 2
        assert csv[0].name == 'Alice'
        assert csv[0].age == '30'
        assert csv[1].name == 'Bob'
        // end::parse_text[]
    }

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

    void testCustomSeparator() {
        // tag::custom_separator[]
        def csv = new CsvSlurper().setSeparator((char) '\t').parseText('name\tage\nAlice\t30')
        assert csv[0].name == 'Alice'
        assert csv[0].age == '30'
        // end::custom_separator[]
    }

    void testQuotedFields() {
        // tag::quoted_fields[]
        def csv = new CsvSlurper().parseText('name,note\nAlice,"hello, world"\nBob,"say ""hi"""')
        assert csv[0].note == 'hello, world'
        assert csv[1].note == 'say "hi"'
        // end::quoted_fields[]
    }

    void testEmptyInput() {
        def csv = new CsvSlurper().parseText('')
        assert csv.isEmpty()
    }

    void testSingleRow() {
        def csv = new CsvSlurper().parseText('name,age\nAlice,30')
        assert csv.size() == 1
        assert csv[0].name == 'Alice'
    }

    void testSemicolonSeparator() {
        def csv = new CsvSlurper().setSeparator((char) ';').parseText('name;age\nAlice;30')
        assert csv[0].name == 'Alice'
    }

    void testParseFromReader() {
        def reader = new StringReader('name,age\nAlice,30')
        def csv = new CsvSlurper().parse(reader)
        assert csv[0].name == 'Alice'
    }

    // tag::typed_parsing[]
    static class Sale {
        String customer
        BigDecimal amount
    }
    // end::typed_parsing[]

    void testTypedParsing() {
        // tag::typed_parsing_usage[]
        def sales = new CsvSlurper().parseAs(Sale, 'customer,amount\nAcme,1500.00\nGlobex,250.50')
        assert sales.size() == 2
        assert sales[0].customer == 'Acme'
        assert sales[0].amount == 1500.00
        assert sales[1].customer == 'Globex'
        // end::typed_parsing_usage[]
    }

    void testTypedParsingMultipleFields() {
        def items = new CsvSlurper().parseAs(Sale, 'customer,amount\nAlice,99.99')
        assert items[0] instanceof Sale
        assert items[0].amount instanceof BigDecimal
    }
}
