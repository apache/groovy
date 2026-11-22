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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.junit.jupiter.api.Test

/**
 * GROOVY-10760: printing an error collector that holds both a warning
 * with a null / CST context and another error must not throw
 * {@code NullPointerException} from {@code LocatedMessage.write}
 * (historically that NPE swallowed the collected messages).
 */
final class Groovy10760 {

    @Test
    void testWarningWithNullContextPlusErrorDoesNotNpeWhenPrinted() {
        def message = messageForWarningAndError(null)
        assert message.contains('class Foo already exists')
        assert message.contains('boom')
        assert !message.contains('NullPointerException')
    }

    @Test
    void testWarningWithSourceCstPlusErrorDoesNotNpeWhenPrinted() {
        def config = new CompilerConfiguration()
        def collector = new ErrorCollector(config)
        def source = new SourceUnit('t.groovy', 'class T {}', config, new GroovyClassLoader(), collector)
        def unit = new CompilationUnit(config, null, source.classLoader)
        unit.addSource(source)
        unit.compile()
        def message = messageForWarningAndError(source.CST, collector, source)
        assert message.contains('class Foo already exists')
        assert message.contains('boom')
        assert !message.contains('NullPointerException')
    }

    private static String messageForWarningAndError(context) {
        def config = new CompilerConfiguration()
        def collector = new ErrorCollector(config)
        def source = new SourceUnit('t.groovy', 'class T {}', config, new GroovyClassLoader(), collector)
        def unit = new CompilationUnit(config, null, source.classLoader)
        unit.addSource(source)
        unit.compile()
        return messageForWarningAndError(context, collector, source)
    }

    private static String messageForWarningAndError(context, collector, source) {
        collector.addWarning(WarningMessage.LIKELY_ERRORS, 'class Foo already exists', context, source)
        collector.addErrorAndContinue(new SyntaxException('boom', 1, 1), source)
        new MultipleCompilationErrorsException(collector).message
    }
}
