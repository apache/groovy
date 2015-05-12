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
package asciidoctor

import groovy.transform.CompileStatic

@CompileStatic
class Utils {
    public static String stripAsciidocMarkup(String string, String newline='\n') {
        boolean inside = false
        StringBuilder sb = new StringBuilder()
        new StringReader(string).eachLine { line ->
            if (line =~ 'tag::') {
                inside = true
            } else if (line =~ 'end::') {
                inside = false
            } else if (inside) {
                if (sb.length() > 0) {
                    sb.append(newline)
                }
                sb.append(line)
            }
        }
        sb
    }
}
