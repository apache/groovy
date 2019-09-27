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
package org.apache.groovy.groovysh

import antlr.RecognitionException
import antlr.TokenStreamException
import antlr.collections.AST
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyRecognizer
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * A relaxed parser, which tends to allow more, but won't really catch valid syntax errors.
 */
final class RelaxedParser implements Parsing {
    private final Logger log = Logger.create(this.class)

    private SourceBuffer sourceBuffer

    private String[] tokenNames

    @Override
    ParseStatus parse(final Collection<String> buffer) {
        assert buffer

        sourceBuffer = new SourceBuffer()

        def source = buffer.join(Parser.NEWLINE)

        log.debug("Parsing: $source")

        try {
            doParse(new UnicodeEscapingReader(new StringReader(source), sourceBuffer))

            log.debug('Parse complete')

            return new ParseStatus(ParseCode.COMPLETE)
        }
        catch (e) {
            switch (e.getClass()) {
                case TokenStreamException:
                case RecognitionException:
                    log.debug("Parse incomplete: $e (${e.getClass().name})")

                    return new ParseStatus(ParseCode.INCOMPLETE)

                default:
                    log.debug("Parse error: $e (${e.getClass().name})")

                    return new ParseStatus(e)
            }
        }
    }

    protected AST doParse(final UnicodeEscapingReader reader) throws Exception {
        GroovyLangLexer lexer = new GroovyLangLexer(reader)
        reader.setLexer(lexer)

        def parser = GroovyRecognizer.make(lexer)
        parser.setSourceBuffer(sourceBuffer)
        tokenNames = parser.tokenNames

        parser.compilationUnit()
        return parser.AST
    }
}