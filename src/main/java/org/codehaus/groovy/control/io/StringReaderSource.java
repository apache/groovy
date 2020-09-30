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

import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *  A ReaderSource for source strings.
 */
public class StringReaderSource extends AbstractReaderSource {
    private final String string;  // The String from which we produce Readers.

   /**
    * Creates the ReaderSource from a File descriptor.
    *
    * @param string string containing script source
    * @param configuration configuration for compiling source
    */
   public StringReaderSource( String string, CompilerConfiguration configuration ) {
       super( configuration );
       this.string = string;
   }
    
   /**
    *  Returns a new Reader on the underlying source object.  
    */
   @Override
   public Reader getReader() throws IOException {
       return new StringReader( string );
   }

    /**
     * Returns a Data URI (RFC 2397) containing the literal value of this source string.
     *
     * @return Data URI containing the literal value of this source string.
     */
    @Override
    public URI getURI() {
        try {
            return new URI("data", "," + string, null);
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
