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
package org.codehaus.groovy.runtime

/**
 * Tests for {@link DefaultGroovyMethods} URL.getText() methods.
 */
class URLGetTextTest extends GroovyTestCase {
    void testGetTextFromURLWithParameters() {
        def url = new URL('http','groovy.codehaus.org',80, '/', new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                new DummyURLConnection(new URL('http://groovy.codehaus.org'))
            }

        })

        assert url.text == 'Groovy'

        shouldFail(SocketTimeoutException) {
            url.getText(readTimeout:5)
        }

        shouldFail(SocketTimeoutException) {
            url.getText(connectTimeout:5)
        }

        shouldFail(SocketTimeoutException) {
            url.getText('utf-8',connectTimeout:5)
        }

        shouldFail(RuntimeException) {
            url.getText(allowUserInteraction:true)
        }

        assert url.getText(useCaches:true) == 'Groovy cached'

        assert url.getText(requestProperties:[a:'b']) == 'Groovy a:b'

        assert url.getText(useCaches:true, requestProperties:[a:'b']) == 'Groovy cached a:b'

        assert url.getText() == url.getText()

        assert url.getText() == url.getText((Map)null)

        def val = 'b'
        assert url.getText(requestProperties: [a:"$val"]) == "Groovy a:b"
    }

    private static class DummyURLConnection extends URLConnection {

        boolean throwConnectTimeout = false
        boolean throwReadTimeout = false
        boolean useCache = false
        boolean allowUserInteraction = false
        def properties = [:]


        DummyURLConnection(final URL url) {
            super(url)
        }

        @Override
        InputStream getInputStream() {
            if (throwConnectTimeout) throw new SocketTimeoutException()
            if (throwReadTimeout) throw new SocketTimeoutException()
            if (allowUserInteraction) throw new RuntimeException('User interaction')
            def string = useCache?'Groovy cached':'Groovy'
            properties.each { k,v -> string = string + " $k:$v"}
            new ByteArrayInputStream(string.bytes)
        }

        @Override
        public void setAllowUserInteraction(boolean allowuserinteraction) {
            allowUserInteraction = allowuserinteraction
        }

        @Override
        void setConnectTimeout(int timeout) {
            super.setConnectTimeout(timeout)
            throwConnectTimeout = true
        }

        @Override
        void setReadTimeout(int timeout) {
            super.setReadTimeout(timeout)
            throwReadTimeout = true
        }

        @Override
        void setUseCaches(boolean usecaches) {
            super.setUseCaches(usecaches)
            useCache = true
        }

        @Override
        void setRequestProperty(String key, String value) {
            properties[key] = value
        }


        @Override
        void connect() {
        }

    }
}
