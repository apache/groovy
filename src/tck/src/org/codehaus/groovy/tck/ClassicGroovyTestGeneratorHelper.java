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
package org.codehaus.groovy.tck;

import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
// Jsr parser
// @todo - refactor pulling generic parser interface up
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

// codehaus reference implementation usage
// @todo - remove classic references from the TCK
import org.codehaus.groovy.control.CompilerConfiguration;
import groovy.lang.GroovyShell;
import antlr.RecognitionException;

/** Helper methods for generated TCK test case using new JSR parser and classic groovy AST and evaluation */
public class ClassicGroovyTestGeneratorHelper implements TestGeneratorHelper {

    /** evaluate the source text against the classic AST with the JSR parser implementation*/
    public Object evaluate(String theSrcText, String testName) throws Exception {
        parse(theSrcText, testName); // fail early with a direct message if possible')
        GroovyShell groovy = new GroovyShell(new CompilerConfiguration());
        return groovy.run(theSrcText, "main", new ArrayList());
    }

    /** run the JSR parser implementation over the supplied source text*/
    public void parse(String theSrcText, String testName) throws Exception {
        System.out.println("-------------------------------");
        System.out.println("  " + testName);
        System.out.println("-------------------------------");
        try {
            Reader reader = new BufferedReader(new StringReader(theSrcText));
            GroovyRecognizer recognizer = GroovyRecognizer.make(reader);
            recognizer.compilationUnit();
            System.out.println(decorateWithLineNumbers(theSrcText));

        } catch (RecognitionException parseException) {
            System.out.println(decorateWithLineNumbersAndErrorMessage(theSrcText,parseException));
            throw parseException;
        }
        System.out.println("-------------------------------");

    }

    private String decorateWithLineNumbersAndErrorMessage(String theSrcText, RecognitionException parseException) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(theSrcText));
            String line = null;
            StringBuilder numberedSrcTextBuffer = new StringBuilder();
            int lineNum = 1;
            while ((line = reader.readLine() ) != null) {
                numberedSrcTextBuffer.append(lineNum);
                numberedSrcTextBuffer.append("\t");
                numberedSrcTextBuffer.append(line);
                numberedSrcTextBuffer.append(lineSep);

                if (parseException != null) {
                    if (lineNum == parseException.getLine()) {
                        StringBuilder padding = new StringBuilder("\t");
                        for (int col=1; col<parseException.getColumn();col++) {
                            padding.append(" ");
                        }
                        numberedSrcTextBuffer.append(padding);
                        numberedSrcTextBuffer.append("^");
                        numberedSrcTextBuffer.append(lineSep);
                        numberedSrcTextBuffer.append("ERROR:");
                        numberedSrcTextBuffer.append(lineSep);
                        numberedSrcTextBuffer.append(parseException.getMessage());
                        numberedSrcTextBuffer.append(lineSep);
                        numberedSrcTextBuffer.append(lineSep);
                    }
                }

                lineNum++;

            }
            theSrcText = numberedSrcTextBuffer.toString();
        } catch (IOException e) {
            //ignore
        }
        return theSrcText;
    }

    private String decorateWithLineNumbers(String theSrcText) {
        return decorateWithLineNumbersAndErrorMessage(theSrcText,null);
    }

    protected String lineSep = System.lineSeparator();
}
