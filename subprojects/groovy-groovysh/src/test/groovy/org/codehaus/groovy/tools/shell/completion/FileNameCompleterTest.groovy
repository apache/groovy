/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completion;

import groovy.util.GroovyTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileNameCompleterTest extends GroovyTestCase {

    public void testRender() throws Exception {
        FileNameCompleter completer = new FileNameCompleter()
        assert completer.render('foo', null) == 'foo'
        assert completer.render('foo bar', null) == '\'foo bar\''
        assert completer.render('foo \'bar', null) == '\'foo \\\'bar\''
        assert completer.render('foo \'bar', '\'') == '\'foo \\\'bar\''
        assert completer.render('foo " \'bar', '"') == '"foo \\" \'bar"'
    }

    public void testMatchFiles() {
        FileNameCompleter completer = new FileNameCompleter()
        List<String> candidates = []
        int resultIndex = completer.matchFiles('foo/bar', '/foo/bar', [new File('/foo/baroo'), new File('/foo/barbee')] as File[], candidates, null)
        assert resultIndex == 'foo/'.length()
        assert candidates == ['baroo', 'barbee']
    }
}
