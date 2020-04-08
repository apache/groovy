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
package org.codehaus.groovy.tools.groovydoc;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.java.Groovifier;
import org.codehaus.groovy.antlr.java.Java2GroovyConverter;
import org.codehaus.groovy.antlr.java.JavaLexer;
import org.codehaus.groovy.antlr.java.JavaRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.antlr.treewalker.SourceCodeTraversal;
import org.codehaus.groovy.antlr.treewalker.Visitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 *  todo: order methods alphabetically (implement compareTo enough?)
 */
@Deprecated
public class GroovyDocParser implements GroovyDocParserI {
    private final List<LinkArgument> links;
    private final Properties properties;
    private final Logger log = Logger.create(GroovyDocParser.class);

    public GroovyDocParser(List<LinkArgument> links, Properties properties) {
        this.links = links;
        this.properties = properties;
    }

    public Map<String, GroovyClassDoc> getClassDocsFromSingleSource(String packagePath, String file, String src)
            throws RuntimeException {
        if (file.indexOf(".java") > 0) { // simple (for now) decision on java or groovy
            // java
            return parseJava(packagePath, file, src);
        }
        if (file.indexOf(".sourcefile") > 0) {
            // java (special name used for testing)
            return parseJava(packagePath, file, src);
        }
        // not java, try groovy instead :-)
        return parseGroovy(packagePath, file, src);
    }

    private Map<String, GroovyClassDoc> parseJava(String packagePath, String file, String src)
            throws RuntimeException {
        SourceBuffer sourceBuffer = new SourceBuffer();
        JavaRecognizer parser = getJavaParser(src, sourceBuffer);
        String[] tokenNames = parser.getTokenNames();
        try {
            parser.compilationUnit();
        } catch (OutOfMemoryError e) {
            log.error("Out of memory while processing: " + packagePath + "/" + file);
            throw e;
        } catch (RecognitionException | TokenStreamException e) {
            throw new RuntimeException(e);
        }
        AST ast = parser.getAST();

        // modify the Java AST into a Groovy AST (just token types)
        Visitor java2groovyConverter = new Java2GroovyConverter(tokenNames);
        AntlrASTProcessor java2groovyTraverser = new PreOrderTraversal(java2groovyConverter);
        java2groovyTraverser.process(ast);

        // now mutate (groovify) the ast into groovy
        Visitor groovifier = new Groovifier(tokenNames, false);
        AntlrASTProcessor groovifierTraverser = new PreOrderTraversal(groovifier);
        groovifierTraverser.process(ast);

        // now do the business     
        SimpleGroovyClassDocAssembler visitor = new SimpleGroovyClassDocAssembler(packagePath, file, sourceBuffer, links, properties, false);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return visitor.getGroovyClassDocs();
    }

    private Map<String, GroovyClassDoc> parseGroovy(String packagePath, String file, String src)
            throws RuntimeException {
        SourceBuffer sourceBuffer = new SourceBuffer();
        GroovyRecognizer parser = getGroovyParser(src, sourceBuffer);
        try {
            parser.compilationUnit();
        } catch (OutOfMemoryError e) {
            log.error("Out of memory while processing: " + packagePath + "/" + file);
            throw e;
        } catch (RecognitionException | TokenStreamException e) {
            throw new RuntimeException(e);
        }
        AST ast = parser.getAST();

        // now do the business
        SimpleGroovyClassDocAssembler visitor = new SimpleGroovyClassDocAssembler(packagePath, file, sourceBuffer, links, properties, true);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        return visitor.getGroovyClassDocs();
    }

    private static JavaRecognizer getJavaParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        JavaLexer lexer = new JavaLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        JavaRecognizer parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

    private static GroovyRecognizer getGroovyParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

}
