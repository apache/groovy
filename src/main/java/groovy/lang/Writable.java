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
package groovy.lang;

import java.io.IOException;
import java.io.Writer;


/**
 * Represents an object which is capable of writing itself to a text stream
 * in a more efficient format than just creating a toString() representation
 * of itself. This mechanism is particularly useful for templates and such like.
 * <p>
 * It is worth noting that writable implementations often override their
 * toString() implementation as well to allow rendering the same result 
 * directly to a String; however this is not required.
 */
public interface Writable {

    /**
     * Writes this object to the given writer.
     * <p>
     * This is used to defer content creation until the point when it is
     * streamed to the output destination.  Oftentimes, content will be defined 
     * but not necessarily created (as is may be the case with a Closure 
     * definition.)  In that case, the output is then 'deferred' to the point 
     * when it is serialized to the writer. This class may be used whenever an 
     * object should be responsible for creating its own textual representation, 
     * but creating the entire output as a single String would be inefficient
     * (such as outputting a multi-gigabyte XML document.)
     * 
     * @param out the Writer to which this Writable should output its data.
     * @return the Writer that was passed
     * @throws IOException if an error occurred while outputting data to the writer
     */
    Writer writeTo(Writer out) throws IOException;
        
}
