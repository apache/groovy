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
package org.apache.groovy.lsp.internal.util

import org.junit.jupiter.api.Test

import java.nio.file.Path

final class UrisTest {

    @Test
    void fileNameFromUnixAndWindowsFileUris() {
        assert Uris.fileName(URI.create('file:///tmp/Hello.groovy')) == 'Hello.groovy'
        assert Uris.fileName(URI.create('file:///d:/a/groovy/Hello.groovy')) == 'Hello.groovy'
        assert Uris.fileName(URI.create('file:///c:/Users/me/src/main/groovy/Pack.groovy')) == 'Pack.groovy'
        assert Uris.fileName(null) == ''
        assert Uris.fileName(URI.create('untitled:untitled-1')) == 'untitled-1'
    }

    @Test
    void toUnixRewritesBackslashes() {
        def relative = Path.of('src').resolve('main').resolve('groovy')
        assert Uris.toUnix(relative) == 'src/main/groovy'
        assert Uris.toUnix(null) == ''
        assert !Uris.toUnix(relative).contains('\\')
    }

    @Test
    void parseAndNormalizeRoundTrip() {
        def uri = Uris.parse('file:///tmp/A.groovy')
        assert uri.scheme == 'file'
        assert Uris.toPath(uri).fileName.toString() == 'A.groovy'
        assert Uris.isGroovyDocument(uri, 'groovy')
        assert Uris.isGroovyDocument(URI.create('file:///tmp/x.gvy'), null)
        assert !Uris.isGroovyDocument(URI.create('file:///tmp/x.txt'), null)
        assert Uris.isJavaDocument(URI.create('file:///tmp/A.java'), null)
        assert Uris.isJavaDocument(URI.create('file:///tmp/A.txt'), 'java')
        assert !Uris.isJavaDocument(URI.create('file:///tmp/A.groovy'), null)
        assert !Uris.isJavaFileName(null)
    }

    @Test
    void normalizeLowercasesWindowsDriveLetters() {
        assert Uris.normalize(URI.create('file:///C:/Users/me/Hello.groovy')) ==
                URI.create('file:///c:/Users/me/Hello.groovy')
        assert Uris.normalize(URI.create('file:///c:/Users/me/Hello.groovy')) ==
                URI.create('file:///c:/Users/me/Hello.groovy')
        assert Uris.parse('file:///C:/Users/me/Hello.groovy') ==
                Uris.normalize(URI.create('file:///C:/Users/me/Hello.groovy'))
        assert Uris.fileName(URI.create('foo:bar')) == 'bar'
    }
}
