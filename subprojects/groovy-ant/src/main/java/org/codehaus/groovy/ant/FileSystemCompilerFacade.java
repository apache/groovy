/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ant;

import org.codehaus.groovy.tools.FileSystemCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a helper class, to provide a controlled entry point for the groovyc
 * ant task forked mode.
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class FileSystemCompilerFacade {
    public static void main(String[] args) {
        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        boolean forceLookupUnnamedFiles = argList.contains("--forceLookupUnnamedFiles");
        if (forceLookupUnnamedFiles) {
            argList.remove("--forceLookupUnnamedFiles");
        }
        String[] newArgs = forceLookupUnnamedFiles ? argList.toArray(new String[argList.size()]) : args;
        FileSystemCompiler.commandLineCompileWithErrorHandling(newArgs, forceLookupUnnamedFiles);
    }
}
