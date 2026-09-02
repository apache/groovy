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
package org.apache.groovy.groovysh.jline

import org.junit.jupiter.api.Test

/**
 * Format selection and the remote-evaluation notice for {@code /slurp}. Lives in the same package so
 * the helpers can be exercised without standing up a terminal or an HTTP server.
 */
class GroovyCommandsSlurpTest {

    // GROOVY-12334
    @Test
    void extensionSelectsADataFormat() {
        assert GroovyCommands.formatForExtension('json', false) == 'JSON'
        assert GroovyCommands.formatForExtension('YML', false) == 'YAML'
        assert GroovyCommands.formatForExtension('markdown', false) == 'MARKDOWN'
        assert GroovyCommands.formatForExtension('csv', true) == 'CSV'
    }

    // GROOVY-12334
    @Test
    void groovyIsSelectableForALocalFileButNeverForAUrl() {
        assert GroovyCommands.formatForExtension('groovy', true) == 'GROOVY'
        assert GroovyCommands.formatForExtension('groovy', false) == null
    }

    // GROOVY-12334
    @Test
    void unknownOrAbsentExtensionSelectsNothing() {
        // null means "fall back to AUTO", which parses but never evaluates
        assert GroovyCommands.formatForExtension('dat', true) == null
        assert GroovyCommands.formatForExtension('', true) == null
        assert GroovyCommands.formatForExtension(null, true) == null
    }

    // GROOVY-12334
    @Test
    void urlPathExtensionIsTakenFromTheLastSegmentOnly() {
        assert GroovyCommands.extensionOf('/data/feed.json') == 'json'
        assert GroovyCommands.extensionOf('/a.b.c/feed.yaml') == 'yaml'
        assert GroovyCommands.extensionOf('/no/extension/here') == null
        assert GroovyCommands.extensionOf('/trailing/slash/') == null
        assert GroovyCommands.extensionOf('') == null
        assert GroovyCommands.extensionOf(null) == null
        // a dotfile has no extension to speak of
        assert GroovyCommands.extensionOf('/dir/.hidden') == null
    }

    // GROOVY-12334
    @Test
    void noticeNamesTheUrlEvaluatedFrom() {
        def url = new URL('http://example.test/x.groovy')
        assert GroovyCommands.remoteEvaluationNotice(url, url) ==
                'evaluating Groovy fetched from http://example.test/x.groovy'
        assert GroovyCommands.remoteEvaluationNotice(url, null) ==
                'evaluating Groovy fetched from http://example.test/x.groovy'
    }

    // GROOVY-12334
    @Test
    void noticeReportsBothUrlsWhenARedirectMovedTheContent() {
        // the address typed is not necessarily where the code came from
        def asked = new URL('http://example.test/x.groovy')
        def served = new URL('http://elsewhere.test/payload.groovy')
        assert GroovyCommands.remoteEvaluationNotice(asked, served) ==
                'evaluating Groovy fetched from http://elsewhere.test/payload.groovy' +
                ' (redirected from http://example.test/x.groovy)'
    }
}
