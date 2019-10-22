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
package org.apache.groovy.groovydoc.tools;

import java.io.File;

public class GroovyDocUtil {
    public static String getPath(String filename) {
        String path = new File(filename).getParent();
        // path length of 1 indicates that probably is 'default package' i.e. "/"
        if (path == null || (path.length() == 1 && !Character.isJavaIdentifierStart(path.charAt(0)))) {
            path = "DefaultPackage"; // "DefaultPackage" for 'default package' path, rather than null...
        }
        return path;
    }

    public static String getFile(String filename) {
        return new File(filename).getName();
    }
}
