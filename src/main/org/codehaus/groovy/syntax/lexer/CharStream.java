package org.codehaus.groovy.syntax.lexer;

/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import java.io.IOException;

/** Conduit of characters  to a lexer.
 *
 *  @see Lexer
 *  @see AbstractCharStream
 *
 *  @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 *
 *  @version $Id$
 */
public interface CharStream
{
    // ----------------------------------------------------------------------
    //     Constants
    // ----------------------------------------------------------------------

    /** End-of-stream value. */
    final char EOS = (char) -1;

    // ----------------------------------------------------------------------
    //     Interface
    // ----------------------------------------------------------------------

    String getDescription();

    /** Look-ahead to the next character.
     *
     *  <p>
     *  This method is equivalent to <code>la(1)</code>.
     *  </p>
     *
     *  @see #la(int)
     *
     *  @return The next character or -1 if no more characters
     *          available.
     *
     *  @throws IOException If an error occurs attempting to lookahead
     *          a character.
     */
    char la()
        throws IOException;

    /** Look-ahead to the <code>k</code><i>th</i> character.
     *
     *  @param k Number of characters to look ahead.
     *
     *  @return the <code>k</code><i>th</i> character or -1 if no
     *          more characters available.
     *
     *  @throws IOException If an error occurs attempting to lookahead
     *          a character.
     */
    char la(int k)
        throws IOException;

    /** Consume the next character.
     *
     *  @return The consumed character or -1 if no more characters
     *          available.
     *
     *  @throws IOException If an error occurs attempting to consume
     *          a character.
     */
    char consume()
        throws IOException;
}
