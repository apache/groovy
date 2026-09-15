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
package org.codehaus.groovy.control.messages

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Janitor
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

final class LocatedMessageTest {

    @Test
    void toDiagnosticAllowsMissingContext() {
        def source = new SourceUnit('A.groovy', 'x', new CompilerConfiguration(), null, null)
        def message = new WarningMessage(WarningMessage.LIKELY_ERRORS, 'unused import', null, source)
        def diagnostic = message.toDiagnostic()
        assert diagnostic.text() == 'unused import'
        assert diagnostic.line() == -1
        def writer = new StringWriter()
        message.write(new PrintWriter(writer), new Janitor())
        assert writer.toString().contains('unused import')
    }
}
