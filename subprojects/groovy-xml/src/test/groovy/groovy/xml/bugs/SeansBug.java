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
package groovy.xml.bugs;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.classgen.TestSupport;

public class SeansBug extends TestSupport {

    public void testBug() throws Exception {
        String code = "for (i in 1..10) \n{\n  println(i)\n}";
        GroovyShell shell = new GroovyShell();
        shell.evaluate(code);
    }

    public void testMarkupBug() throws Exception {
        String[] lines =
                {
                        "package groovy.xml",
                        "",
                        "b = new MarkupBuilder()",
                        "",
                        "b.root1(a:5, b:7) { ",
                        "    elem1('hello1') ",
                        "    elem2('hello2') ",
                        "    elem3(x:7) ",
                        "}"};
        String code = asCode(lines);
        GroovyShell shell = new GroovyShell();
        shell.evaluate(code);
    }

    /**
     * Converts the array of lines of text into one string with newlines
     */
    protected String asCode(String[] lines) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
