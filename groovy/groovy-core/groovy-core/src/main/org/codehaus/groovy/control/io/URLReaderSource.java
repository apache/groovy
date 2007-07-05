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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.codehaus.groovy.control.CompilerConfiguration;

/**
 *  A ReaderSource for source files hosted at a URL.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */
public class URLReaderSource extends AbstractReaderSource {
    private URL url;  // The URL from which we produce Readers.
    
   /**
    *  Creates the ReaderSource from a File descriptor.
    * @param url url pointing to script source
    * @param configuration configuration for compiling source
    */
    public URLReaderSource( URL url, CompilerConfiguration configuration ) {
       super( configuration );
        this.url = url;
    }

   /**
    *  Returns a new Reader on the underlying source object.  
    */
    public Reader getReader() throws IOException {
       return new InputStreamReader( url.openStream(), configuration.getSourceEncoding() );
    }

}
