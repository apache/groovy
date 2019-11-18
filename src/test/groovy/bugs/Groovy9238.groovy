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
package groovy.bugs

import static groovy.test.GroovyAssert.assertScript
import groovy.transform.*
import org.junit.Test

@CompileStatic
final class Groovy9238 {

    @Test
    void testAnnotationCollectorOfCollectedAnnotations() {
        assertScript '''
            import groovy.bugs.Collector

            @Collector
            class Pogo9238 {
              String string
            }

            def pogo = new Pogo9238('old')
            pogo = pogo.copyWith(string: 'new')

            assert pogo.string == 'new'
        '''
    }
}

@CompileStatic
@Immutable(copyWith=true) // uses AnnotationCollector
@AnnotationCollector
@interface Collector {
}
