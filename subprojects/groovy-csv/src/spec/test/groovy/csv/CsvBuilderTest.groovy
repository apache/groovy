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

class CsvBuilderTest {

    @Test
    void testToCsvFromMaps() {
        // tag::to_csv_maps[]
        def data = [
            [name: 'Alice', age: 30],
            [name: 'Bob', age: 25]
        ]
        def csv = CsvBuilder.toCsv(data)
        assert csv.contains('name,age')
        assert csv.contains('Alice,30')
        assert csv.contains('Bob,25')
        // end::to_csv_maps[]
    }

    @Test
    void testToCsvEmpty() {
        assert CsvBuilder.toCsv([]) == ''
        assert CsvBuilder.toCsv(null) == ''
    }

    @Test
    void testToCsvQuotesSpecialChars() {
        def data = [[name: 'Alice, Jr.', note: 'said "hi"']]
        def csv = CsvBuilder.toCsv(data)
        assert csv.contains('"Alice, Jr."')
        assert csv.contains('"said ""hi"""')
    }

    @Test
    void testBuilderInstance() {
        def builder = new CsvBuilder()
        builder.call([[name: 'Alice', age: 30], [name: 'Bob', age: 25]])
        def csv = builder.toString()
        assert csv.contains('name,age')
        assert csv.contains('Alice,30')
    }

    @Test
    void testWritable() {
        def builder = new CsvBuilder()
        builder.call([[x: 1, y: 2]])
        def out = new StringWriter()
        out << builder
        assert out.toString().contains('x,y')
    }

    @Test
    void testRoundTrip() {
        // tag::round_trip[]
        def original = [[name: 'Alice', age: '30'], [name: 'Bob', age: '25']]
        def csv = CsvBuilder.toCsv(original)
        def parsed = new CsvSlurper().parseText(csv)
        assert parsed[0].name == 'Alice'
        assert parsed[1].age == '25'
        // end::round_trip[]
    }

    // tag::typed_writing[]
    static class Product {
        String name
        BigDecimal price
    }
    // end::typed_writing[]

    @Test
    void testToCsvFromTypedObjects() {
        // tag::typed_writing_usage[]
        def products = [new Product(name: 'Widget', price: 9.99),
                        new Product(name: 'Gadget', price: 24.50)]
        def csv = CsvBuilder.toCsv(products, Product)
        assert csv.contains('name,price')
        assert csv.contains('Widget,9.99')
        assert csv.contains('Gadget,24.5')
        // end::typed_writing_usage[]
    }

    @Test
    void testTypedRoundTrip() {
        def products = [new Product(name: 'Widget', price: 9.99)]
        def csv = CsvBuilder.toCsv(products, Product)
        def parsed = new CsvSlurper().parseAs(Product, csv)
        assert parsed[0].name == 'Widget'
        assert parsed[0].price == 9.99
    }

    // tag::temporal_class[]
    static class Event {
        java.time.LocalDate day
        java.time.LocalTime windowStart
        java.time.LocalDateTime updated
        java.time.OffsetDateTime created
        String label
    }
    // end::temporal_class[]

    @Test
    void testTemporalTypedRoundTrip() {
        // tag::temporal_typed[]
        def original = [new Event(
                day: java.time.LocalDate.of(1979, 5, 27),
                windowStart: java.time.LocalTime.of(7, 32, 0),
                updated: java.time.LocalDateTime.of(1979, 5, 27, 7, 32, 0),
                created: java.time.OffsetDateTime.parse('1979-05-27T07:32:00-08:00'),
                label: 'first')]

        def csv = CsvBuilder.toCsv(original, Event)
        def parsed = new CsvSlurper().parseAs(Event, csv)

        assert parsed[0].day == original[0].day                  // LocalDate
        assert parsed[0].windowStart == original[0].windowStart  // LocalTime
        assert parsed[0].updated == original[0].updated          // LocalDateTime
        assert parsed[0].created == original[0].created          // OffsetDateTime, non-UTC offset preserved
        assert parsed[0].label == original[0].label
        // end::temporal_typed[]
        assert parsed[0].day instanceof java.time.LocalDate
        assert parsed[0].windowStart instanceof java.time.LocalTime
        assert parsed[0].updated instanceof java.time.LocalDateTime
        assert parsed[0].created instanceof java.time.OffsetDateTime
        // the round-tripped offset must remain -08:00, not normalised to Z
        assert parsed[0].created.offset == java.time.ZoneOffset.ofHours(-8)
    }

    @Test
    void testTemporalUntypedStringPath() {
        // KNOWN LIMITATION: CSV has no native types — every cell is text. The
        // untyped slurp path therefore always returns String values, including
        // for date/time-looking columns. Use the typed parseAs API with
        // java.time.* fields for temporal fidelity. This test pins the current
        // behaviour so a future change here is deliberate.
        def parsed = new CsvSlurper().parseText('day,windowStart\n1979-05-27,07:32:00')
        assert parsed[0].day == '1979-05-27'
        assert parsed[0].windowStart == '07:32:00'
        assert parsed[0].day instanceof String
        assert parsed[0].windowStart instanceof String
    }
}
