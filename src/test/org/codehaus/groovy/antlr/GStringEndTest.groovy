package org.codehaus.groovy.antlr

import groovy.test.GroovyTestCase
import org.codehaus.groovy.antlr.parser.GroovyLexer

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
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

class GStringEndTest extends GroovyTestCase {
    void testInvalidEndContainsLineNumber(){
        try {
            assertScript '''
                def Target = "releases$"
            '''
        } catch (MultipleCompilationErrorsException mcee) {
            def text = mcee.toString();
            assert text.contains("line 2, column 41") ||  // the old parser
                        text.contains("line 2, column 40") // parrot: column 40 is more accurate than the original 41
        }
    }

    void testErrorReportOnStringEndWithOutParser() {
        // GROOVY-6608: the code did throw a NPE
        def s = '''
def scanFolders()
{ doThis( ~"(?i)^sometext$", 
'''
        def lexer = new GroovyLexer(new StringReader(s))
        try {
            while (lexer.nextToken()!=null) {}
        } catch (antlr.TokenStreamRecognitionException se) {}
    }
}