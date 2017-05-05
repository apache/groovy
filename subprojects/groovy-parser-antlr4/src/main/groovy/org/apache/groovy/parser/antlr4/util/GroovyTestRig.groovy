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
package org.apache.groovy.parser.antlr4.util

import groovy.util.logging.Log
import org.antlr.v4.gui.TestRig
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.apache.groovy.parser.antlr4.GroovyLangParser

/**
 * A basic debug tool for investigating the parse trees and tokens of Groovy source code
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/08/14
 */
@Log
public class GroovyTestRig extends TestRig {
    public GroovyTestRig(String[] args) throws Exception {
        super(['Groovy', args.contains('-lexer') ? 'tokens' : 'compilationUnit', *args] as String[]);
    }

    public void inspectParseTree() {
        def inputFile = new File(this.inputFiles[0]);

        if (!(inputFile.exists() && inputFile.isFile())) {
            log.info "Input file[${inputFile.absolutePath}] does not exist."
            return;
        }

        byte[] content = inputFile.bytes;
        String text = new String(content, this.encoding ?: 'UTF-8');

        GroovyLangLexer lexer = new GroovyLangLexer(new ANTLRInputStream(text));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GroovyLangParser parser = new GroovyLangParser(tokens);

        this.process(lexer, GroovyLangParser.class, parser, new ByteArrayInputStream(content), new StringReader(text));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            log.info "Usage: [-tokens] [-lexer] [-tree] [-gui] [-ps file.ps] [-encoding encodingname] [-trace] [-diagnostics] [-SLL] input-filename";
            return;
        }

        if (args.every { it.startsWith('-') }) {
            log.info "input-filename is required!"
            return;
        }

        GroovyTestRig groovyTestRig = new GroovyTestRig(args);

        groovyTestRig.inspectParseTree();
    }
}

