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

import groovy.test.GroovyTestCase

class SafeNumberXmlParserTest extends GroovyTestCase {

    void testSafetyWhenConvertingToNumbers() {
        def xmlText = '''
                <someNumberValues>
                <someBigDecimal>123.4</someBigDecimal>
                <someEmptyBigDecimal></someEmptyBigDecimal>
                <someLong>123</someLong>
                <someEmptyLong></someEmptyLong>
                <someFloat>123.4</someFloat>
                <someEmptyFloat></someEmptyFloat>
                <someDouble>123.4</someDouble>
                <someEmptyDouble></someEmptyDouble>
                <someInteger>123</someInteger>
                <someEmptyInteger></someEmptyInteger>
            </someNumberValues>
                '''
        def xml = new XmlParser().parseText(xmlText)

        assert xml.'**'.find { it.name() == 'someBigDecimal' }.toBigDecimal() == 123.4
        assert xml.'**'.find { it.name() == 'someEmptyBigDecimal' }.toBigDecimal() == null
        assert xml.'**'.find { it.name() == 'someMissingBigDecimal' }?.toBigDecimal() == null
        assert xml.'**'.find { it.name() == 'someLong' }.toLong() == 123
        assert xml.'**'.find { it.name() == 'someEmptyLong' }.toLong() == null
        assert xml.'**'.find { it.name() == 'someMissingLong' }?.toLong() == null
        assert xml.'**'.find { it.name() == 'someFloat' }.toFloat() == 123.4.toFloat()
        assert xml.'**'.find { it.name() == 'someEmptyFloat' }.toFloat() == null
        assert xml.'**'.find { it.name() == 'someMissingFloat' }?.toFloat() == null
        assert xml.'**'.find { it.name() == 'someDouble' }.toDouble() == 123.4.toDouble()
        assert xml.'**'.find { it.name() == 'someEmptyDouble' }.toDouble() == null
        assert xml.'**'.find { it.name() == 'someMissingDouble' }?.toDouble() == null
        assert xml.'**'.find { it.name() == 'someInteger' }.toInteger() == 123
        assert xml.'**'.find { it.name() == 'someEmptyInteger' }.toInteger() == null
        assert xml.'**'.find { it.name() == 'someMissingInteger' }?.toInteger() == null
    }
}
