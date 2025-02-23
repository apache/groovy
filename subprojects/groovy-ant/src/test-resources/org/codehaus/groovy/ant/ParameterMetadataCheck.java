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
package org.codehaus.groovy.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ParameterMetadataCheck {
    public static void main(String[] args) throws IOException {
        File file = new File("target/classes/groovy/test/org/codehaus/groovy/ant/ParameterMetadataCheck_Result.txt");
        file.createNewFile();
        try (FileOutputStream fout = new FileOutputStream(file)) {
            fout.write("OK.".getBytes());
        }
    }
}
