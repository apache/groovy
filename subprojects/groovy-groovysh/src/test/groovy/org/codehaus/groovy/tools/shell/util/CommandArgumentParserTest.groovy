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

package org.codehaus.groovy.tools.shell.util


public class CommandArgumentParserTest extends GroovyTestCase {

    void testParseLine() {
        // empty
        assertEquals([], CommandArgumentParser.parseLine(""))

        // blanks
        assertEquals(['foo', 'bar'], CommandArgumentParser.parseLine("  foo bar  "))

        // empties
        assertEquals(['', '', '', ''], CommandArgumentParser.parseLine('\'\'\'\' \'\'  \'\''))
        assertEquals(['', '', '', ''], CommandArgumentParser.parseLine('"""" ""  ""'))

        // hyphen groups
        assertEquals(['foo bar', 'baz bam'], CommandArgumentParser.parseLine(' \'foo bar\' \'baz bam\''))
        assertEquals(['foo bar', 'baz bam'], CommandArgumentParser.parseLine(' "foo bar" "baz bam"'))

        // escaped hyphens and escape signs
        assertEquals(['foo \\ "\' bar'], CommandArgumentParser.parseLine('\'foo \\\\ "\\\' bar\''))
        // no space between hyphentokens
        assertEquals(['bar', 'foo', 'bam', 'baz'], CommandArgumentParser.parseLine('bar"foo"\'bam\'\'baz\''))

        // limited number of tokens
        assertEquals(['foo'], CommandArgumentParser.parseLine("  foo bar  ", 1))
        assertEquals(['bar', 'foo'], CommandArgumentParser.parseLine('bar"foo"\'bam\'\'baz\'', 2))

        assertEquals(['map.put('], CommandArgumentParser.parseLine("map.put('a': 2)", 1))
        assertEquals(['map.put('], CommandArgumentParser.parseLine("map.put('a", 1))

    }

}
