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

import groovy.util.GroovyTestCase;

import java.util.ArrayList;

public class GroovyRootDocBuilderTest extends GroovyTestCase {
    private String src = "/**\n" +
            " * <h1>Description</h1>\n" +
            " * This class is used by an application to process the command line\n" +
            " * that invokes it. The processing of the command line proceeds in\n" +
            " * the following steps.\n" +
            " * <ol>\n" +
            " * <li>Create an <code>CmdLine</code> object specifying the properties\n" +
            " * that control how the command line is to be processed and what\n" +
            " * options the program supports.</li>\n" +
            " * <li>Call \n" +
            " * {@link org.dummy.cmdline.CmdLine#processCmdLine()}\n" +
            " * to process the command line.</li>\n" +
            " */\n" +
            "package org.dummy.cmdline;";

    public void testCommentExtraction() throws Exception {
        GroovyRootDocBuilder builder = new GroovyRootDocBuilder(null, null, new ArrayList<LinkArgument>(), null);
        SimpleGroovyPackageDoc doc = new SimpleGroovyPackageDoc("org.dummy.cmdline");
        builder.processPackageInfo(src, "package-info.groovy", doc);
        assertEquals("<h1>Description</h1>\n" +
                " This class is used by an application to process the command line\n" +
                " that invokes it. The processing of the command line proceeds in\n" +
                " the following steps.\n" +
                " <ol>\n" +
                " <li>Create an <code>CmdLine</code> object specifying the properties\n" +
                " that control how the command line is to be processed and what\n" +
                " options the program supports.</li>\n" +
                " <li>Call \n" +
                " org.dummy.cmdline.CmdLine#processCmdLine()\n" +
                " to process the command line.</li>", doc.description().trim());
    }
}