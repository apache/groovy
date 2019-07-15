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
package groovy

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ParserPluginFactory

/**
 * Test for the multi-catch exception from JDK 7 (Project Coin)
 */
class MultiCatchTest extends GroovyTestCase {

    void testDynamicCatch() {
        def catched = false
        try {
            throw new RuntimeException("Error")
        } catch (e) {
            catched = true
        }
        assert catched
    }

    void testRegularCatch() {
        def catched = false
        try {
            throw new RuntimeException("Error")
        } catch (RuntimeException ex) {
            catched = true
        }
        assert catched
    }

    void testRegularCatchWithFinalModifier() {
        def catched = false
        try {
            throw new RuntimeException("Error")
        } catch (final RuntimeException ex) {
            catched = true
        }
        assert catched
    }

    void testRegularCatchWithFinalModifierAndDynamicType() {
        def catched = false
        try {
            throw new RuntimeException("Error")
        } catch (final def ex) {
            catched = true
        }
        assert catched
    }

    void testRegularCatchWithFinalModifierWithoutType() {
        def catched = false
        try {
            throw new RuntimeException("Error")
        } catch (final ex) {
            catched = true
        }
        assert catched
    }

    void testMultipleCatchJavaStyle() {
        def catched = false
        try {
            throw new IOException("Error")
        } catch (NullPointerException e) {
            catched = false
        } catch (IOException e) {
            catched = true
        }
        assert catched
    }

    void testMultipleCatchGroovyStyle1() {
        def catched = false
        try {
            throw new IOException("Error")
        } catch (NullPointerException | IOException e) {
            catched = true
        }
        assert catched
    }

    void testMultipleCatchGroovyStyle2() {
        def catched = false
        try {
            throw new NullPointerException()
        } catch (NullPointerException | IOException e) {
            catched = true
        }
        assert catched
    }

    void testMultipleCatchGroovyStyle3() {
        def catched = false
        try {
            throw new RuntimeException()
        } catch (NullPointerException | IOException e) {
            catched = false
        } catch (RuntimeException e) {
            catched = true
        }
        assert catched
    }

    // GROOVY-8238
    void testMultipleCatchGroovyAndJavaExceptions() {
        def cc = new CompilerConfiguration(pluginFactory: ParserPluginFactory.antlr2())
        new GroovyShell(cc).evaluate '''
            import groovy.cli.CliBuilderException
            try {
                throw new RuntimeException('boom')
            } catch ( RuntimeException | CliBuilderException e ) {
                assert e.message == 'boom'
            }
        '''
    }
}
