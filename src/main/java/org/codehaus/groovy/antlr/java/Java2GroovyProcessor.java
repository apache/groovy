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
package org.codehaus.groovy.antlr.java;

import antlr.collections.AST;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.treewalker.MindMapPrinter;
import org.codehaus.groovy.antlr.treewalker.NodePrinter;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.antlr.treewalker.SourceCodeTraversal;
import org.codehaus.groovy.antlr.treewalker.SourcePrinter;
import org.codehaus.groovy.antlr.treewalker.Visitor;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;

@Deprecated
public class Java2GroovyProcessor {

    public static void processFiles(List<String> fileNames) throws Exception {
        for (String filename : fileNames) {
            File f = new File(filename);
            String text = ResourceGroovyMethods.getText(f);
            System.out.println(convert(filename, text, true, true));
        }
    }

    public static String convert(String filename, String input) throws Exception {
        return convert(filename, input, false, false);
    }

    public static String convert(String filename, String input, boolean withHeader, boolean withNewLines) throws Exception {
        JavaRecognizer parser = getJavaParser(input);
        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();

        // output AST in format suitable for opening in http://freemind.sourceforge.net
        // which is a really nice way of seeing the AST, folding nodes etc
        if ("mindmap".equals(System.getProperty("ANTLR.AST".toLowerCase()))) { // uppercase to hide from jarjar
            try {
                PrintStream out = new PrintStream(new FileOutputStream(filename + ".mm"));
                Visitor visitor = new MindMapPrinter(out, tokenNames);
                AntlrASTProcessor treewalker = new PreOrderTraversal(visitor);
                treewalker.process(ast);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create " + filename + ".mm");
            }
        }

        // modify the Java AST into a Groovy AST
        modifyJavaASTintoGroovyAST(tokenNames, ast);
        String[] groovyTokenNames = getGroovyTokenNames(input);
        // groovify the fat Java-Like Groovy AST
        groovifyFatJavaLikeGroovyAST(ast, groovyTokenNames);

        // now output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Visitor visitor = new SourcePrinter(new PrintStream(baos), groovyTokenNames, withNewLines);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        String header = "";
        if (withHeader) {
            header = "/*\n" +
                    "  Automatically Converted from Java Source \n" +
                    "  \n" +
                    "  by java2groovy v0.0.1   Copyright Jeremy Rayner 2007\n" +
                    "  \n" +
                    "  !! NOT FIT FOR ANY PURPOSE !! \n" +
                    "  'java2groovy' cannot be used to convert one working program into another" +
                    "  */\n\n";
        }
        return header + new String(baos.toByteArray());
    }

    private static void groovifyFatJavaLikeGroovyAST(AST ast, String[] groovyTokenNames) {
        Visitor groovifier = new Groovifier(groovyTokenNames);
        AntlrASTProcessor groovifierTraverser = new PreOrderTraversal(groovifier);
        groovifierTraverser.process(ast);
    }

    private static void modifyJavaASTintoGroovyAST(String[] tokenNames, AST ast) {
        // mutate the tree when in Javaland
        Visitor preJava2groovyConverter = new PreJava2GroovyConverter(tokenNames);
        AntlrASTProcessor preJava2groovyTraverser = new PreOrderTraversal(preJava2groovyConverter);
        preJava2groovyTraverser.process(ast);

        // map the nodes to Groovy types
        Visitor java2groovyConverter = new Java2GroovyConverter(tokenNames);
        AntlrASTProcessor java2groovyTraverser = new PreOrderTraversal(java2groovyConverter);
        java2groovyTraverser.process(ast);
    }

    private static JavaRecognizer getJavaParser(String input) {
        JavaRecognizer parser = null;
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        JavaLexer lexer = new JavaLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

    public static String mindmap(String input) throws Exception {
        JavaRecognizer parser = getJavaParser(input);
        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();
        // modify the Java AST into a Groovy AST
        modifyJavaASTintoGroovyAST(tokenNames, ast);
        String[] groovyTokenNames = getGroovyTokenNames(input);
        // groovify the fat Java-Like Groovy AST
        groovifyFatJavaLikeGroovyAST(ast, groovyTokenNames);

        // now output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Visitor visitor = new MindMapPrinter(new PrintStream(baos), groovyTokenNames);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return new String(baos.toByteArray());
    }

    public static String nodePrinter(String input) throws Exception {
        JavaRecognizer parser = getJavaParser(input);
        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();
        // modify the Java AST into a Groovy AST
        modifyJavaASTintoGroovyAST(tokenNames, ast);
        String[] groovyTokenNames = getGroovyTokenNames(input);
        // groovify the fat Java-Like Groovy AST
        groovifyFatJavaLikeGroovyAST(ast, groovyTokenNames);

        // now output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Visitor visitor = new NodePrinter(new PrintStream(baos), groovyTokenNames);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return new String(baos.toByteArray());
    }

    private static String[] getGroovyTokenNames(String input) {
        GroovyRecognizer groovyParser = null;
        SourceBuffer groovySourceBuffer = new SourceBuffer();
        UnicodeEscapingReader groovyUnicodeReader = new UnicodeEscapingReader(new StringReader(input), groovySourceBuffer);
        GroovyLexer groovyLexer = new GroovyLexer(groovyUnicodeReader);
        groovyUnicodeReader.setLexer(groovyLexer);
        groovyParser = GroovyRecognizer.make(groovyLexer);
        return groovyParser.getTokenNames();
    }

}
