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
package groovy.io;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * A buffered writer only for OutputStreamWriter that is aware of
 * the encoding of the OutputStreamWriter.
 */
public class EncodingAwareBufferedWriter extends BufferedWriter {
    private final OutputStreamWriter out;
    public EncodingAwareBufferedWriter(OutputStreamWriter out) {
        super(out);
        this.out = out;
    }

    /**
     * The encoding as returned by the underlying OutputStreamWriter. Can be the historical name.
     *
     * @return the encoding
     * @see java.io.OutputStreamWriter#getEncoding()
     */
    public String getEncoding() {
        return out.getEncoding();
    }

    /**
     * The encoding as returned by the underlying OutputStreamWriter. Will be the preferred name.
     *
     * @return the encoding
     * @see java.io.OutputStreamWriter#getEncoding()
     */
    public String getNormalizedEncoding() {
        return Charset.forName(getEncoding()).name();
    }
}
