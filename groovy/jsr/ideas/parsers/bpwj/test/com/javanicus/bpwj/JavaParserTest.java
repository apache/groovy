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

import junit.framework.*;
import sjm.parse.*;
import sjm.parse.tokens.*;

public class JavaParserTest extends TestCase {
    private Parser parser;

    public void setUp() {
        parser = new JavaParser().start();
    }

    public void testEmptyExpression() {
//        assertNull(parse(""));
    }

    public void testSimpleJavaProgram() {
        assertNotNull(parse("package foo.wibble; public class Foo {}"));
    }

    /* utility wrapper for tokenizer and parser */
    private Object parse(String userInput) {
        Assembly in = new TokenAssembly(userInput);
        Assembly out = parser.completeMatch(in);
        if (out != null) {
            return out.getTarget();
        } else {
            return null;
        }
    }
}
