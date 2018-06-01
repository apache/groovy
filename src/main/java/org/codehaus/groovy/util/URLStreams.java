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
package org.codehaus.groovy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLStreams {
    private URLStreams() {

    }

    /**
     * Opens an {@link InputStream} reading from the given URL without
     * caching the stream. This prevents file descriptor leaks when reading
     * from file system URLs.
     *
     * @param url the URL to connect to
     * @return an input stream reading from the URL connection
     */
    public static InputStream openUncachedStream(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setUseCaches(false);
        return urlConnection.getInputStream();
    }
}
