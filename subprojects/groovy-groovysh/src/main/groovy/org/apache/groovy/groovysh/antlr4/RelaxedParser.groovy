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
package org.apache.groovy.groovysh.antlr4

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ConsoleErrorListener
import org.apache.groovy.groovysh.ParseCode
import org.apache.groovy.groovysh.ParseStatus
import org.apache.groovy.groovysh.Parser
import org.apache.groovy.groovysh.Parsing
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * A relaxed parser, which tends to allow more, but won't really catch valid syntax errors.
 */
final class RelaxedParser implements Parsing {
    private final Logger log = Logger.create(this.class)

    @Override
    ParseStatus parse(final Collection<String> buffer) {
        assert buffer
        def source = buffer.join(Parser.NEWLINE)

        log.debug("Parsing: $source")

        try {
            CharStream charStream = CharStreams.fromReader(new StringReader(source))
            GroovyLangLexer lexer = new GroovyLangLexer(charStream)
            lexer.removeErrorListener(ConsoleErrorListener.INSTANCE)
            def tokenStream = new CommonTokenStream(lexer)
            tokenStream.fill()

            log.debug('Parse complete')

            return new ParseStatus(ParseCode.COMPLETE)
        }
        catch (e) {
            log.debug(e)
            switch (e.getClass()) {
                // TODO determine appropriate antlr4 exceptions or detect EOF earlier at end of stream
//                case TokenStreamException:
//                case RecognitionException:
//                    log.debug("Parse incomplete: $e (${e.getClass().name})")
//
//                    return new ParseStatus(ParseCode.INCOMPLETE)

                default:
                    log.debug("Parse error: $e (${e.getClass().name})")

                    return new ParseStatus(e)
            }
        }
    }
}