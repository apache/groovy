/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * ???
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Parser
{
    static final String SCRIPT_FILENAME = 'groovysh_parse'

    static final String NEWLINE = System.getProperty('line.separator')
    
    private final Logger log = Logger.create(this.class)

    /**
     * Attempt to parse the given buffer.
     */
    ParseStatus parse(final List buffer) {
        assert buffer

        String source = buffer.join(NEWLINE)

        log.debug("Parsing: $source")

        SourceUnit parser
        Throwable error

        try {
            parser = SourceUnit.create(SCRIPT_FILENAME, source, /*tolerance*/ 1)
            parser.parse()

            log.debug('Parse complete')

            return new ParseStatus(ParseCode.COMPLETE)
        }
        catch (CompilationFailedException e) {
            //
            // FIXME: Seems like failedWithUnexpectedEOF() is not always set as expected, as in:
            //
            // class a {               <--- is true here
            //    def b() {            <--- is false here :-(
            //

            if (parser.errorCollector.errorCount > 1 || !parser.failedWithUnexpectedEOF()) {
                //
                // HACK: Super insane hack... if we detect a syntax error, but the last line of the
                //       buffer ends with a {, [, ''', """ or \ then ignore...
                //       and pretend its okay, cause it might be...
                //
                //       This seems to get around the problem with things like:
                //
                //       class a { def b() {
                //

                if (buffer[-1].trim().endsWith('{')) {
                    // ignore, this blows
                }
                else if (buffer[-1].trim().endsWith('[')) {
                    // ignore, this blows
                }
                else if (buffer[-1].trim().endsWith("'''")) {
                    // ignore, this blows
                }
                else if (buffer[-1].trim().endsWith('"""')) {
                    // ignore, this blows
                }
                else if (buffer[-1].trim().endsWith('\\')) {
                    // ignore, this blows
                }
                else {
                    error = e
                }
            }
        }
        catch (Throwable e) {
            error = e
        }

        if (error) {
            log.debug("Parse error: $error")

            return new ParseStatus(error)
        }
        else {
            log.debug('Parse incomplete')

            return new ParseStatus(ParseCode.INCOMPLETE)
        }
    }
}

/**
 * Container for the parse code.
 */
final class ParseCode
{
    static final ParseCode COMPLETE = new ParseCode(code: 0)

    static final ParseCode INCOMPLETE = new ParseCode(code: 1)

    static final ParseCode ERROR = new ParseCode(code: 2)

    int code

    String toString() {
        return code
    }
}

/**
 * Container for parse status details.
 */
final class ParseStatus
{
    final ParseCode code

    final Throwable cause

    ParseStatus(final ParseCode code, final Throwable cause) {
        this.code = code
        this.cause = cause
    }

    ParseStatus(final ParseCode code) {
        this(code, null)
    }

    ParseStatus(final Throwable cause) {
        this(ParseCode.ERROR, cause)
    }
}
