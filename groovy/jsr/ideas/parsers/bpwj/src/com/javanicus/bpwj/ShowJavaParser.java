package com.javanicus.bpwj;

/*
  $Id$

   Copyright (c) 2004 Jeremy Rayner. All Rights Reserved.

   Jeremy Rayner makes no representations or warranties about
   the fitness of this software for any particular purpose,
   including the implied warranty of merchantability.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import sjm.parse.*;
import sjm.parse.tokens.*;
import sjm.examples.track.TrackException;

public class ShowJavaParser {

    public static void main(String[] args) {
        // build a parser
        Parser qualifiedIdentifierParser = new JavaParser().qualifiedIdentifier();
        Parser expressionParser = new JavaParser().expression();
        System.out.println("   parser: " + qualifiedIdentifierParser);


        ShowJavaParser javaParser = new ShowJavaParser();

        String qualifiedIdentifierTest[] = new String[] {
            "",
            "foo",
            "foo.bar",
            "foo.bar.mooky"};

        for (int i=0; i < qualifiedIdentifierTest.length;i++) {
            try {
                javaParser.parse(qualifiedIdentifierParser,qualifiedIdentifierTest[i]);
            } catch (TrackException e) {
                reportError(e);
            }
        }

        String expressionTest[] = new String[] {
            "",
            "foo",
            "foo=bar"};
        for (int i=0; i < expressionTest.length; i++) {
            try {
                javaParser.parse(expressionParser, expressionTest[i]);
            } catch (TrackException e) {
                reportError(e);
            }
        }

    }

    private static void reportError(TrackException e) {
        System.out.println("**** parse error ****");
        System.out.println("    after: " + e.getAfter());
        System.out.println(" expected: " + e.getExpected());
        System.out.println("    found: " + e.getFound());
    }


    public ShowJavaParser() {
        System.out.println("----");
        System.out.println("JavaParser");
        System.out.println("----");

    }

    /** tokenize user input */
    public Assembly tokenize(String userInput) {
        Assembly in = new TokenAssembly(userInput);
        System.out.println(" ");
        System.out.println("userInput: " + userInput);
        System.out.println("   tokens: " + in.remainder(in.defaultDelimiter()));

        return in;
    }

    /** perform match */
    public void parse(Parser parser, String userInput) {
        Assembly out = parser.completeMatch(tokenize(userInput));
        if (out != null) {
            System.out.println("      out: " + out.getStack());
            System.out.println("   target: " + out.getTarget());
        } else {
            System.out.println("      out: null");
        }
    }
}