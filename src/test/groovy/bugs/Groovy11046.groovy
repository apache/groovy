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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy11046 {

    @Test
    void testMissingDependency1() {
        // throws NoClassDefFoundError: com.lmax.disruptor.EventTranslatorVararg
        assertScript '''
            @Grab('org.apache.logging.log4j:log4j-core:2.22.0')
            org.apache.logging.log4j.core.async.AsyncLogger log
        '''
    }

    @Test
    void testMissingDependency2() {
        def err = shouldFail '''
            @Grab('org.apache.logging.log4j:log4j-core:2.22.0')
            org.apache.logging.log4j.core.async.AsyncLogger log
            System.setProperty('Log4jContextSelector',
                'org.apache.logging.log4j.core.async.AsyncLoggerContextSelector')
            log = org.apache.logging.log4j.LogManager.getLogger(getClass()) //XXX
        '''
        assert err instanceof NoClassDefFoundError // CompilationFailedException (previously)
        assert err =~ /com.lmax.disruptor.EventTranslatorVararg/
        assert err.asString() =~ /at org.apache.logging.log4j.LogManager.getLogger\(/ : 'script should have failed at runtime'
    }
}
