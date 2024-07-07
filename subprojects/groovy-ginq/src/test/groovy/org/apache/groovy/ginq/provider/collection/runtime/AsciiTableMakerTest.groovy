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
package org.apache.groovy.ginq.provider.collection.runtime

import org.junit.Test

class AsciiTableMakerTest {
    @Test
    void makeAsciiTable() {
        // GROOVY-11436
        def result = GQ {
            from r in [
                [name: 'Daniel', age: 39, location: 'Shanghai'],
                [name: '山风小子', age: 40, location: '上海'],
                [name: 'Candy', age: 36, location: 'Shanghai'],
                [name: '中文，English, 123, ａｂｃ，ひらがな，カタカナ', age: 1, location: '未知']
            ]
            select  r.name, r.age, r.location
        }
        def tableStr = AsciiTableMaker.makeAsciiTable(result)
        assert tableStr == '\n' +
            '+------------------------------------------------+-----+----------+\n' +
            '| name                                           | age | location |\n' +
            '+------------------------------------------------+-----+----------+\n' +
            '| Daniel                                         | 39  | Shanghai |\n' +
            '| 山风小子                                       | 40  | 上海     |\n' +
            '| Candy                                          | 36  | Shanghai |\n' +
            '| 中文，English, 123, ａｂｃ，ひらがな，カタカナ | 1   | 未知     |\n' +
            '+------------------------------------------------+-----+----------+\n'
    }
}
