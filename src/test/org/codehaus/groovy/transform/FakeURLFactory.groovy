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
package org.codehaus.groovy.transform

/**
 * A factory for creating fake URLs for testing purposes.
 */

class FakeURLFactory {
    /*
     * Creates a URL whose InputStream will return the given content.
     */
    URL createURL(String content) {
        return new URL(null, "fake://abc", new FakeURLStreamHandler(content))
    }
}

class FakeURLStreamHandler extends URLStreamHandler {
    private String content

    FakeURLStreamHandler(String content) {
        this.content = content
    }

    @Override
    URLConnection openConnection(URL url) {
        return new FakeURLConnection(url, content)
    }
}

class FakeURLConnection extends URLConnection {
    private String content

    FakeURLConnection(URL url, String content) {
        super(url)
        this.content = content
    }

    @Override
    void connect() {}

    @Override
    InputStream getInputStream() {
        return new ByteArrayInputStream(content.bytes)
    }
}
