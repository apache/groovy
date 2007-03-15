/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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
package org.codehaus.groovy.control.io;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.groovy.control.HasCleanup;
import org.codehaus.groovy.control.Janitor;

/**
 *  An interface for things that can supply (and potentially resupply) a Reader
 *  on a source stream.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */
public interface ReaderSource extends HasCleanup {
   /**
    *  Returns a new Reader on the underlying source object.  Returns
    *  null if the source can't be reopened.
    * @throws java.io.IOException if there was an error opening for stream
    * @return the reader to the resource
    */
    Reader getReader() throws IOException;
    
   /**
    *  Returns true if the source can be restarted (ie. if getReader()
    *  will return non-null on subsequent calls.
    * @return true if the resource can be reopened for reading
    */
    boolean canReopenSource();
    
   /**
    *  Returns a line from the source, or null, if unavailable.  If
    *  you supply a Janitor, resources will be cached.
    * @param lineNumber the number of the line of interest
    * @param janitor helper to clean up afterwards
    * @return the line of interest
    */
    String getLine( int lineNumber, Janitor janitor );
    
   /**
    *  Cleans up any cached resources used by getLine().
    */
    void cleanup();
}
