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
package org.codehaus.groovy.antlr;

import antlr.ASTFactory;
import antlr.CommonAST;
import antlr.Token;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;

class Main {

    static boolean whitespaceIncluded = false;

    static boolean showTree = false;
    //static boolean xml = false;
    static boolean verbose = false;
    public static void main(String[] args) {
        // Use a try/catch block for parser exceptions
        try {
            // if we have at least one command-line argument
            if (args.length > 0 ) {
                System.err.println("Parsing...");

                // for each directory/file specified on the command line
                for (String arg : args) {
                    if (arg.equals("-showtree")) {
                        showTree = true;
                    }
                    //else if ( args[i].equals("-xml") ) {
                    //    xml = true;
                    //}
                    else if (arg.equals("-verbose")) {
                        verbose = true;
                    } else if (arg.equals("-trace")) {
                        GroovyRecognizer.tracing = true;
                        GroovyLexer.tracing = true;
                    } else if (arg.equals("-traceParser")) {
                        GroovyRecognizer.tracing = true;
                    } else if (arg.equals("-traceLexer")) {
                        GroovyLexer.tracing = true;
                    } else if (arg.equals("-whitespaceIncluded")) {
                        whitespaceIncluded = true;
                    } else {
                        doFile(new File(arg)); // parse it
                    }
                }
            }
            else
                System.err.println("Usage: java -jar groovyc.jar [-showtree] [-verbose] [-trace{,Lexer,Parser}]"+
                                   "<directory or file name>");
        }
        catch(Exception e) {
            System.err.println("exception: "+e);
            e.printStackTrace(System.err);   // so we can get stack trace
        }
    }


    // This method decides what action to take based on the type of
    //   file we are looking at
    public static void doFile(File f)
                              throws Exception {
        // If this is a directory, walk each file/dir in that directory
        if (f.isDirectory()) {
            String files[] = f.list();
            for (String file : files) doFile(new File(f, file));
        }

        // otherwise, if this is a groovy file, parse it!
        else if (f.getName().endsWith(".groovy")) {
            System.err.println(" --- "+f.getAbsolutePath());
            // parseFile(f.getName(), new FileInputStream(f));
            SourceBuffer sourceBuffer = new SourceBuffer();
            UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new FileReader(f),sourceBuffer);
            GroovyLexer lexer = new GroovyLexer(unicodeReader);
            unicodeReader.setLexer(lexer);
            parseFile(f.getName(),lexer,sourceBuffer);
        }
    }

    // Here's where we do the real work...
    public static void parseFile(String f, GroovyLexer l, SourceBuffer sourceBuffer)
                                 throws Exception {
        try {
            // Create a parser that reads from the scanner
            GroovyRecognizer parser = GroovyRecognizer.make(l);
            parser.setSourceBuffer(sourceBuffer);
            parser.setFilename(f);
                        
                        if (whitespaceIncluded) {
                            GroovyLexer lexer = parser.getLexer();
                            lexer.setWhitespaceIncluded(true);
                            while (true) {
                                Token t = lexer.nextToken();
                                System.out.println(t);
                                if (t == null || t.getType() == Token.EOF_TYPE)  break;
                            }
                            return;
                        }

            // start parsing at the compilationUnit rule
            parser.compilationUnit();
            
            System.out.println("parseFile "+f+" => "+parser.getAST());

            // do something with the tree
            doTreeAction(f, parser.getAST(), parser.getTokenNames());
        }
        catch (Exception e) {
            System.err.println("parser exception: "+e);
            e.printStackTrace();   // so we can get stack trace        
        }
    }
    
    public static void doTreeAction(String f, AST t, String[] tokenNames) {
        if ( t==null ) return;
        if ( showTree ) {
            CommonAST.setVerboseStringConversion(true, tokenNames);
            ASTFactory factory = new ASTFactory();
            AST r = factory.create(0,"AST ROOT");
            r.setFirstChild(t);
            final ASTFrame frame = new ASTFrame("Groovy AST", r);
            frame.setVisible(true);
            frame.addWindowListener(
                new WindowAdapter() {
                   public void windowClosing (WindowEvent e) {
                       frame.setVisible(false); // hide the Frame
                       frame.dispose();
                       System.exit(0);
                   }
                }
            );
            if (verbose)  System.out.println(t.toStringList());
        }
        /*if ( xml ) {
            ((CommonAST)t).setVerboseStringConversion(true, tokenNames);
            ASTFactory factory = new ASTFactory();
            AST r = factory.create(0,"AST ROOT");
            r.setFirstChild(t);
            XStream xstream = new XStream();
            xstream.alias("ast", CommonAST.class);
            try {
                xstream.toXML(r,new FileWriter(f + ".xml"));
                System.out.println("Written AST to " + f + ".xml");
            } catch (Exception e) {
                System.out.println("couldn't write to " + f + ".xml");
                e.printStackTrace();
            }
            //if (verbose)  System.out.println(t.toStringList());
        }*/
    /*@todo
        GroovyTreeParser tparse = new GroovyTreeParser();
        try {
            tparse.compilationUnit(t);
            if (verbose)  System.out.println("successful walk of result AST for "+f);
        }
        catch (RecognitionException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    @todo*/

    }
}

