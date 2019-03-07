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
package org.codehaus.groovy.control.io;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.util.URLStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *  A ReaderSource for source files hosted at a URL.
 */
public class URLReaderSource extends AbstractReaderSource {
    private final URL url;  // The URL from which we produce Readers.
    
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
       return new InputStreamReader(URLStreams.openUncachedStream(url), configuration.getSourceEncoding() );
    }

    /**
     * Returns a URI for the URL of this source.
     *
     * @return URI for the URL of this source.
     */
    public URI getURI() {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new GroovyRuntimeException("Unable to convert the URL <" + url + "> to a URI!", e);
        }
    }

}
