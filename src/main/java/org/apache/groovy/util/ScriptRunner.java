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
package org.apache.groovy.util;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.tools.GroovyStarter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The tool to simulate running script files via groovy command
 *
 * @since 3.0.0
 */
public class ScriptRunner {
    /**
     * Run the script file specified by the file path
     *
     * @param path the file
     * @since 3.0.0
     */
    public static void runScript(File path) {
        try {
            GroovyStarter.main(new String[] {"--main", "groovy.ui.GroovyMain", path.getCanonicalPath()});
        } catch (IOException e) {
            throw new GroovyRuntimeException("Failed to run script: " + path, e);
        }
    }

    /**
     * Run the script file specified by the classpath
     *
     * @param cp the classpath
     * @since 3.0.0
     */
    public static void runScript(String cp) {
        try {
            runScript(new File(ScriptRunner.class.getResource(cp).toURI()));
        } catch (URISyntaxException e) {
            throw new GroovyRuntimeException("Failed to run script: " + cp, e);
        }
    }

    private ScriptRunner() {}
}
