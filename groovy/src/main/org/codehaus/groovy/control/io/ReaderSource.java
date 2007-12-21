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
