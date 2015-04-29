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
package groovy.security;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.junit.Ignore;

import java.io.File;

/**
 * Test case for running a single groovy script parsed from a .groovy file.
 */
@Ignore("does not work at the moment, please fix me")
public class RunOneGroovyScript extends SecurityTestSupport {

    protected static String file;

    public void testScript() {
        String fileName = System.getProperty("script", file);
        if (fileName == null) {
            throw new RuntimeException("No filename given in the 'script' system property so cannot run a Groovy script");
        }
        assertExecute(new File(fileName), null);
    }
}
