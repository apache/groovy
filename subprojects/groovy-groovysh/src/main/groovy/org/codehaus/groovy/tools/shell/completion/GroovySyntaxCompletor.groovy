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
package org.codehaus.groovy.tools.shell.completion

import antlr.TokenStreamException
import jline.console.completer.Completer
import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.Logger

import static org.codehaus.groovy.antlr.parser.GroovyTokenTypes.*

/**
 * Implements the Completor interface to provide competions for
 * GroovyShell by tokenizing the buffer and invoking other classes depending on the tokens found.
 *
 * @author <a href="mailto:probabilitytrees@gmail.com">Marty Saxton</a>
 */
class GroovySyntaxCompletor implements Completer {

    protected final static Logger LOG = Logger.create(GroovySyntaxCompletor)

    private final Groovysh shell
    private final List<IdentifierCompletor> identifierCompletors
    private final IdentifierCompletor classnameCompletor
    private final ReflectionCompletor reflectionCompletor
    private final InfixKeywordSyntaxCompletor infixCompletor
    private final Completer filenameCompletor

    static final enum CompletionCase {
        SECOND_IDENT,
        NO_COMPLETION,
        DOT_LAST,
        SPREAD_DOT_LAST,
        PREFIX_AFTER_DOT,
        PREFIX_AFTER_SPREAD_DOT,
        NO_DOT_PREFIX,
        INSTANCEOF
    }

    GroovySyntaxCompletor(final Groovysh shell,
                          final ReflectionCompletor reflectionCompletor,
                          IdentifierCompletor classnameCompletor,
                          final List<IdentifierCompletor> identifierCompletors,
                          final Completer filenameCompletor) {
        this.shell = shell
        this.classnameCompletor = classnameCompletor
        this.identifierCompletors = identifierCompletors
        infixCompletor = new InfixKeywordSyntaxCompletor()
        this.reflectionCompletor = reflectionCompletor
        this.filenameCompletor = filenameCompletor
    }

    @Override
    int complete(final String bufferLine, final int cursor, final List<CharSequence> candidates) {
        if (! bufferLine) {
            return -1
        }
        if (isCommand(bufferLine, shell.registry)) {
            return -1
        }
        // complete given the context of the whole buffer, not just last line
        // Build a single string for the lexer
        List<GroovySourceToken> tokens = []
        try {
            if (! tokenizeBuffer(bufferLine.substring(0, cursor), shell.buffers.current(), tokens)) {
                return -1
            }
        } catch (InStringException ise) {
            int completionStart = ise.column + 1
            int fileResult = + filenameCompletor.complete(bufferLine.substring(completionStart),
                    cursor - completionStart, candidates)
            if (fileResult >= 0) {
                return completionStart + fileResult
            }
            return -1
        }

        CompletionCase completionCase = getCompletionCase(tokens)
        if (completionCase == CompletionCase.NO_COMPLETION) {
            return -1
        }
        if (completionCase == CompletionCase.SECOND_IDENT) {
            if (infixCompletor.complete(tokens, candidates)) {
                return tokens.last().column - 1
            }
            return -1
        }
        if (completionCase == CompletionCase.INSTANCEOF) {
            if (classnameCompletor.complete(tokens, candidates)) {
                return tokens.last().column - 1
            }
            return -1
        }


        int result
        switch (completionCase) {
            case CompletionCase.NO_DOT_PREFIX:
                result = completeIdentifier(tokens, candidates)
                break
            case CompletionCase.DOT_LAST:
            case CompletionCase.PREFIX_AFTER_DOT:
            case CompletionCase.SPREAD_DOT_LAST:
            case CompletionCase.PREFIX_AFTER_SPREAD_DOT:
                result = reflectionCompletor.complete(tokens, candidates)
                break
            default:
                // bug
                throw new RuntimeException("Unknown Completion case: $completionCase")

        }
        return result
    }

    static CompletionCase getCompletionCase(final List<GroovySourceToken> tokens) {
        GroovySourceToken currentToken = tokens[-1]

        // now look at last 2 tokens to decide whether we are in a completion situation at all
        if (currentToken.type == IDENT) {
            // cursor is on identifier, use it as prefix and check whether it follows a dot

            if (tokens.size() == 1) {
                return CompletionCase.NO_DOT_PREFIX
            }
            GroovySourceToken previousToken = tokens[-2]
            if (previousToken.type == DOT || previousToken.type == OPTIONAL_DOT) {
                // we have a dot, so need to evaluate the statement up to the dot for completion
                if (tokens.size() < 3) {
                    return CompletionCase.NO_COMPLETION
                }
                return CompletionCase.PREFIX_AFTER_DOT
            } else if (previousToken.type == SPREAD_DOT) {
                    // we have a dot, so need to evaluate the statement up to the dot for completion
                    if (tokens.size() < 3) {
                        return CompletionCase.NO_COMPLETION
                    }
                    return CompletionCase.PREFIX_AFTER_SPREAD_DOT
            } else {
                // no dot, so we complete a varname, classname, or similar
                switch (previousToken.type) {
                // if any of these is before, no useful completion possible in this completor
                    case LITERAL_import:
                    case LITERAL_class:
                    case LITERAL_interface:
                    case LITERAL_enum:
                    case LITERAL_def:
                    case LITERAL_void:
                    case LITERAL_boolean:
                    case LITERAL_byte:
                    case LITERAL_char:
                    case LITERAL_short:
                    case LITERAL_int:
                    case LITERAL_float:
                    case LITERAL_long:
                    case LITERAL_double:
                    case LITERAL_package:
                    case LITERAL_true:
                    case LITERAL_false:
                    case LITERAL_as:
                    case LITERAL_this:
                    case LITERAL_try:
                    case LITERAL_finally:
                    case LITERAL_catch:
                        return CompletionCase.NO_COMPLETION
                    case IDENT:
                        // identifiers following each other could mean Declaration (no completion) or closure invocation
                        // closure invocation too complex for now to complete
                        return CompletionCase.SECOND_IDENT
                    default:
                        return CompletionCase.NO_DOT_PREFIX
                }
            }

        } else if (currentToken.type == DOT || currentToken.type == OPTIONAL_DOT) {
            // cursor is on dot, so need to evaluate the statement up to the dot for completion
            if (tokens.size() == 1) {
                return CompletionCase.NO_COMPLETION
            }
            return CompletionCase.DOT_LAST
        } else if (currentToken.type == SPREAD_DOT) {
            // cursor is on spread-dot, so need to evaluate the statement up to the dot for completion
            if (tokens.size() == 1) {
                return CompletionCase.NO_COMPLETION
            }
            return CompletionCase.SPREAD_DOT_LAST
        } else if (currentToken.type == LITERAL_instanceof) {
            return CompletionCase.INSTANCEOF
        } else {
            LOG.debug('Untreated toke type: ' + currentToken.type)
        }
        return CompletionCase.NO_COMPLETION
    }

    int completeIdentifier(final  List<GroovySourceToken> tokens, final List<CharSequence> candidates) {
        boolean foundMatches = false
        for (IdentifierCompletor completor: identifierCompletors) {
            foundMatches |= completor.complete(tokens, candidates)
        }
        if (foundMatches) {
            return tokens.last().column - 1
        }
        return -1
    }

    static boolean isCommand(final String bufferLine, final CommandRegistry registry) {
        // for shell commands, don't complete
        int commandEnd = bufferLine.indexOf(' ')
        if (commandEnd != -1) {
            String commandTokenText = bufferLine.substring(0, commandEnd)
            for (command in registry.commands()) {
                if (commandTokenText == command.name || commandTokenText in command.aliases) {
                    return true
                }
            }
        }
        return false
    }

    static GroovyLexer createGroovyLexer(final String src) {
        Reader unicodeReader = new UnicodeEscapingReader(new StringReader(src), new SourceBuffer())
        GroovyLexer lexer = new GroovyLexer(unicodeReader)
        unicodeReader.setLexer(lexer)
        return lexer
    }

    static class InStringException extends Exception {
        int column
        InStringException(int column) {
            this.column  = column
        }
    }

    /**
     * Adds to result the identified tokens for the bufferLines
     * @param bufferLine
     * @param previousLines
     * @param result
     * @return true if lexing was successfull
     */
    static boolean tokenizeBuffer(final String bufferLine,
                                  final List<String> previousLines,
                                  final List<GroovySourceToken> result) {
        GroovyLexer groovyLexer
        if (previousLines.size() > 0) {
            StringBuilder src = new StringBuilder()
            for (String line: previousLines) {
                src.append(line + '\n')
            }
            src.append(bufferLine)
            groovyLexer = createGroovyLexer(src.toString())
        } else {
            groovyLexer = createGroovyLexer(bufferLine)
        }
        // Build a list of tokens using a GroovyLexer
        GroovySourceToken nextToken
        GroovySourceToken lastToken
        boolean isGString = false
        while (true) {
            try {
                nextToken = groovyLexer.nextToken() as GroovySourceToken
                if (nextToken.type == EOF) {
                    if (! result.isEmpty() && nextToken.line > result.last().line) {
                        // no completion if EOF line has no tokens
                        return false
                    }
                    break
                }
                if (nextToken.type == STRING_CTOR_START) {
                    isGString = true
                }
                result << nextToken
                lastToken = nextToken
            } catch (TokenStreamException e) {
                // getting the next token failed, possibly due to unclosed hyphens, need to investigate rest of the line to confirm
                if (! isGString && lastToken != null) {
                    String restline = bufferLine.substring(lastToken.columnLast - 1)
                    int leadingBlanks = restline.find('^[ ]*').length()
                    if (restline) {
                        char firstChar = restline.charAt(leadingBlanks)
                        // Exception with following hyphen either means we're in String or at end of GString.
                        if (firstChar.toString() in ['"', "'"]
                                && previousLines.size() + 1 == lastToken.line) {
                            throw new InStringException(lastToken.columnLast + leadingBlanks - 1)
                        }
                    }
                }
                return false
            } catch (NullPointerException e) {
                // this can happen when e.g. a string as not closed
                return false
            }
        }
        return !result.empty
    }
}
