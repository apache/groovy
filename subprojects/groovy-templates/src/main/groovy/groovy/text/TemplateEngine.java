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
package groovy.text;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

/**
 * Represents an API to any template engine which is basically a factory of Template instances from a given text input.
 */
public abstract class TemplateEngine {
    public abstract Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException;
    
    public Template createTemplate(String templateText) throws CompilationFailedException, ClassNotFoundException, IOException {
        return createTemplate(new StringReader(templateText));
    }
    
    public Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        Reader reader = new FileReader(file);
        try {
            return createTemplate(reader);
        } finally {
            DefaultGroovyMethodsSupport.closeWithWarning(reader);
        }
    }

    public Template createTemplate(URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        Reader reader = new InputStreamReader(url.openStream());
        try {
            return createTemplate(reader);
        } finally {
            DefaultGroovyMethodsSupport.closeWithWarning(reader);
        }
    }
}
