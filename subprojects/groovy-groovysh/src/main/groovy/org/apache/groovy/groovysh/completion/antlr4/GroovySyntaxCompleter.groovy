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
package org.apache.groovy.groovysh.completion.antlr4

import groovy.transform.TupleConstructor
import jline.console.completer.Completer
import jline.internal.Configuration
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ConsoleErrorListener
import org.antlr.v4.runtime.Token
import org.apache.groovy.groovysh.CommandRegistry
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.completion.BackslashEscapeCompleter
import org.apache.groovy.groovysh.completion.FileNameCompleter
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.codehaus.groovy.tools.shell.util.Logger

import static org.apache.groovy.parser.antlr4.GroovyLexer.AS
import static org.apache.groovy.parser.antlr4.GroovyLexer.BooleanLiteral
import static org.apache.groovy.parser.antlr4.GroovyLexer.BuiltInPrimitiveType
import static org.apache.groovy.parser.antlr4.GroovyLexer.CATCH
import static org.apache.groovy.parser.antlr4.GroovyLexer.CLASS
import static org.apache.groovy.parser.antlr4.GroovyLexer.CapitalizedIdentifier
import static org.apache.groovy.parser.antlr4.GroovyLexer.DEF
import static org.apache.groovy.parser.antlr4.GroovyLexer.DOT
import static org.apache.groovy.parser.antlr4.GroovyLexer.ENUM
import static org.apache.groovy.parser.antlr4.GroovyLexer.EOF
import static org.apache.groovy.parser.antlr4.GroovyLexer.FINALLY
import static org.apache.groovy.parser.antlr4.GroovyLexer.IMPORT
import static org.apache.groovy.parser.antlr4.GroovyLexer.INSTANCEOF
import static org.apache.groovy.parser.antlr4.GroovyLexer.INTERFACE
import static org.apache.groovy.parser.antlr4.GroovyLexer.Identifier
import static org.apache.groovy.parser.antlr4.GroovyLexer.METHOD_POINTER
import static org.apache.groovy.parser.antlr4.GroovyLexer.METHOD_REFERENCE
import static org.apache.groovy.parser.antlr4.GroovyLexer.NOT
import static org.apache.groovy.parser.antlr4.GroovyLexer.NOT_INSTANCEOF
import static org.apache.groovy.parser.antlr4.GroovyLexer.PACKAGE
import static org.apache.groovy.parser.antlr4.GroovyLexer.SAFE_DOT
import static org.apache.groovy.parser.antlr4.GroovyLexer.SPREAD_DOT
import static org.apache.groovy.parser.antlr4.GroovyLexer.THIS
import static org.apache.groovy.parser.antlr4.GroovyLexer.TRY
import static org.apache.groovy.parser.antlr4.GroovyLexer.VOID

/**
 * Implements the Completer interface to provide completions for
 * GroovyShell by tokenizing the buffer and invoking other classes depending on the tokens found.
 */
class GroovySyntaxCompleter implements Completer {

    protected final static Logger LOG = Logger.create(GroovySyntaxCompleter)

    private final Groovysh shell
    private final List<IdentifierCompleter> identifierCompleters
    private final IdentifierCompleter classnameCompleter
    private final ReflectionCompleter reflectionCompleter
    private final InfixKeywordSyntaxCompleter infixCompleter
    private final Completer defaultFilenameCompleter
    private final Completer windowsFilenameCompleter
    private final Completer instringFilenameCompleter
    private final Completer backslashCompleter
    private static final boolean isWin = Configuration.isWindows()
    private final GroovyShell gs = new GroovyShell()

    static enum CompletionCase {
        SECOND_IDENT,
        NO_COMPLETION,
        DOT_LAST,
        SPREAD_DOT_LAST,
        PREFIX_AFTER_DOT,
        PREFIX_AFTER_SPREAD_DOT,
        NO_DOT_PREFIX,
        INSTANCEOF
    }

    GroovySyntaxCompleter(final Groovysh shell,
                          final ReflectionCompleter reflectionCompleter,
                          IdentifierCompleter classnameCompleter,
                          final List<IdentifierCompleter> identifierCompleters,
                          final Completer filenameCompleter) {
        this.shell = shell
        this.classnameCompleter = classnameCompleter
        this.identifierCompleters = identifierCompleters
        infixCompleter = new InfixKeywordSyntaxCompleter()
        backslashCompleter = new BackslashEscapeCompleter()
        this.reflectionCompleter = reflectionCompleter
        defaultFilenameCompleter = filenameCompleter
        windowsFilenameCompleter = new FileNameCompleter(false, true, false)
        instringFilenameCompleter = new FileNameCompleter(false, false, false)
    }

    @Override
    int complete(final String bufferLine, final int cursor, final List<CharSequence> candidates) {
        if (!bufferLine) {
            return -1
        }
        if (isCommand(bufferLine, shell.registry)) {
            return -1
        }
        // complete given the context of the whole buffer, not just last line
        // Build a single string for the lexer
        List<Token> tokens = []
        try {
            if (!tokenizeBuffer(bufferLine.substring(0, cursor), shell.buffers.current(), tokens)) {
                return -1
            }
        } catch (InStringException ise) {
            int completionStart = ise.column + ise.openDelim.size()
            def remainder = bufferLine.substring(completionStart)
            def completer = instringFilenameCompleter
            if (['"', "'", '"""', "'''"].contains(ise.openDelim)) {
                if (isWin) {
                    completer = windowsFilenameCompleter
                }
                // perhaps a backslash
                if (remainder.contains("\\")) {
                    try {
                        gs.evaluate("'$remainder'")
                    } catch (Exception ex1) {
                        try {
                            gs.evaluate("'${remainder.substring(0, remainder.size() - 1)}'")
                            // only get here if there is an unescaped backslash at the end of the buffer
                            // ignore the result since it is only informational
                            return backslashCompleter.complete(remainder, cursor, candidates)
                        } catch (Exception ex2) {
                        }
                    }
                }
            }
            int completionResult = completer.complete(remainder, cursor - completionStart, candidates)
            if (completionResult >= 0) {
                return completionStart + completionResult
            }
            return completionResult
        }

        CompletionCase completionCase = getCompletionCase(tokens)
        if (completionCase == CompletionCase.NO_COMPLETION) {
            return -1
        }
        if (completionCase == CompletionCase.SECOND_IDENT) {
            if (infixCompleter.complete(tokens, candidates)) {
                return tokens.last().startIndex
            }
            return -1
        }
        if (completionCase == CompletionCase.INSTANCEOF) {
            if (classnameCompleter.complete(tokens, candidates)) {
                return tokens.last().startIndex
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
                result = reflectionCompleter.complete(tokens, candidates)
                break
            default:
                // bug
                throw new RuntimeException("Unknown Completion case: $completionCase")

        }
        return result
    }

    static CompletionCase getCompletionCase(final List<Token> tokens) {
        Token currentToken = tokens[-1]

        // now look at last 2 tokens to decide whether we are in a completion situation at all
        if (currentToken.type == Identifier || currentToken.type == CapitalizedIdentifier) {
            // cursor is on identifier, use it as prefix and check whether it follows a dot

            if (tokens.size() == 1) {
                return CompletionCase.NO_DOT_PREFIX
            }
            Token previousToken = tokens[-2]
            if (previousToken.type == DOT || previousToken.type == SAFE_DOT) {
                // we have a dot, so need to evaluate the statement up to the dot for completion
                if (tokens.size() < 3) {
                    return CompletionCase.NO_COMPLETION
                }
                return CompletionCase.PREFIX_AFTER_DOT
            } else if (previousToken.type == SPREAD_DOT || previousToken.type == METHOD_POINTER || previousToken.type == METHOD_REFERENCE) {
                // we have a dot, so need to evaluate the statement up to the dot for completion
                if (tokens.size() < 3) {
                    return CompletionCase.NO_COMPLETION
                }
                return CompletionCase.PREFIX_AFTER_SPREAD_DOT
            } else {
                // no dot, so we complete a varname, classname, or similar
                switch (previousToken.type) {
                // if any of these is before, no useful completion possible in this completer
                    case IMPORT:
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case DEF:
                    case VOID:
                    case BuiltInPrimitiveType:
//                    case LITERAL_byte:
//                    case LITERAL_char:
//                    case LITERAL_short:
//                    case LITERAL_int:
//                    case LITERAL_float:
//                    case LITERAL_long:
//                    case LITERAL_double:
                    case PACKAGE:
                    case BooleanLiteral:
//                    case LITERAL_true:
//                    case LITERAL_false:
                    case AS:
                    case THIS:
                    case TRY:
                    case FINALLY:
                    case CATCH:
                        return CompletionCase.NO_COMPLETION
                    case NOT: // just for !in and !instanceof; maybe needs special case
                    case CapitalizedIdentifier:
                    case Identifier:
                        // identifiers following each other could mean Declaration (no completion) or closure invocation
                        // closure invocation too complex for now to complete
                        return CompletionCase.SECOND_IDENT
                    default:
                        return CompletionCase.NO_DOT_PREFIX
                }
            }

        } else if (currentToken.type == DOT || currentToken.type == SAFE_DOT) {
            // cursor is on dot, so need to evaluate the statement up to the dot for completion
            if (tokens.size() == 1) {
                return CompletionCase.NO_COMPLETION
            }
            return CompletionCase.DOT_LAST
        } else if (currentToken.type == SPREAD_DOT || currentToken.type == METHOD_REFERENCE || currentToken.type == METHOD_POINTER) {
            // cursor is on spread-dot, so need to evaluate the statement up to the dot for completion
            if (tokens.size() == 1) {
                return CompletionCase.NO_COMPLETION
            }
            return CompletionCase.SPREAD_DOT_LAST
        } else if (currentToken.type == INSTANCEOF || currentToken.type == NOT_INSTANCEOF) {
            return CompletionCase.INSTANCEOF
        } else {
            LOG.debug('Unhandled token type: ' + currentToken.type)
        }
        return CompletionCase.NO_COMPLETION
    }

    int completeIdentifier(final List<Token> tokens, final List<CharSequence> candidates) {
        boolean foundMatches = false
        for (IdentifierCompleter completer : identifierCompleters) {
            foundMatches |= completer.complete(tokens, candidates)
        }
        if (foundMatches) {
            return tokens.last().startIndex
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

    static createTokenStream(String text) {
        CharStream charStream = CharStreams.fromReader(new StringReader(text))
        GroovyLangLexer lexer = new GroovyLangLexer(charStream)
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE)
        def tokenStream = new CommonTokenStream(lexer)
        tokenStream.fill()
        return tokenStream
    }

    @TupleConstructor
    static class InStringException extends Exception {
        int column
        String openDelim

        @Override
        String toString() {
            super.toString() + "[column=$column, openDelim=$openDelim]"
        }
    }

    private static final STRING_STARTERS = [/"""/, /'''/, /"/, /'/, '$/', '/']

    /**
     * Adds to result the identified tokens for the bufferLines
     * @param bufferLine
     * @param previousLines
     * @param result
     * @return true if lexing was successful
     */
    static boolean tokenizeBuffer(final String bufferLine,
                                  final List<String> previousLines,
                                  final List<Token> result) {
        def tokenStream
        if (previousLines.size() > 0) {
            StringBuilder src = new StringBuilder()
            for (String line : previousLines) {
                src.append(line).append('\n')
            }
            src.append(bufferLine)
            tokenStream = createTokenStream(src.toString()).getTokens().iterator()
        } else {
            tokenStream = createTokenStream(bufferLine).getTokens().iterator()
        }
        // Build a list of tokens
        Token nextToken
        Token lastToken
        while (true) {
            try {
                nextToken = tokenStream.next() as Token
                if (nextToken.type == EOF) {
                    if (!result.isEmpty() && nextToken.line > result.last().line) {
                        // no completion if EOF line has no tokens
                        return false
                    }
                    break
                }
                result << nextToken
                lastToken = nextToken
            } catch (Exception e) {
                // TODO this whole section needs a rework for antlr4
                // getting the next token failed, possibly due to unclosed quotes; investigate rest of the line to confirm
                if (lastToken != null) {
                    String restline = bufferLine.substring(lastToken.columnLast - 1)
                    int leadingBlanks = restline.find('^[ ]*').length()
                    if (restline) {
                        String remainder = restline.substring(leadingBlanks)
                        // Exception with following quote either means we're in String or at end of GString.
                        String openDelim = STRING_STARTERS.find { remainder.startsWith(it) }
                        if (openDelim && previousLines.size() + 1 == lastToken.line) {
                            throw new InStringException(lastToken.columnLast + leadingBlanks - 1, openDelim)
                        }
                    }
                }
                return false
//            } catch (NullPointerException e) {
//                // this can happen when e.g. a string as not closed
//                new File('/tmp/groovysh_log.txt') << e.message << '\n'
//                return false
            }
        }
        return !result.empty
    }
}
