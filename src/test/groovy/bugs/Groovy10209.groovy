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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-10209: {@code JsonBuilder} used to throw {@code StackOverflowError}
 * when rendering an {@code InetAddress}, because {@code DefaultJsonGenerator}
 * converted an unrecognized object to a map and then recursed through fields
 * of the same type. The original snippet must complete with a finite string.
 */
final class Groovy10209 {

    @Test
    void testJsonBuilderDoesNotOverflowOnInetAddress() {
        assertScript '''
            import groovy.json.JsonBuilder

            def object = [ip: InetAddress.getByName('192.168.0.1')]
            def text = new JsonBuilder(object).toPrettyString()
            assert text.contains('192.168.0.1')
        '''
    }
}
