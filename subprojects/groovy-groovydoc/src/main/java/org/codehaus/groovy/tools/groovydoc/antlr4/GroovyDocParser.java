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
package org.codehaus.groovy.tools.groovydoc.antlr4;

import com.github.javaparser.StaticJavaParser;
import org.apache.groovy.antlr.GroovydocVisitor;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.GroovyDocParserI;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GroovyDocParser implements GroovyDocParserI {
    private final List<LinkArgument> links;
    private final Properties properties;
    private final Logger log = Logger.create(GroovyDocParser.class);

    public GroovyDocParser(List<LinkArgument> links, Properties properties) {
        this.links = links;
        this.properties = properties;
    }

    public Map<String, GroovyClassDoc> getClassDocsFromSingleSource(String packagePath, String file, String src)
            throws RuntimeException {
        if (file.indexOf(".java") > 0) { // simple (for now) decision on java or groovy
            // java
            return parseJava(packagePath, file, src);
        }
        if (file.indexOf(".sourcefile") > 0) {
            // java (special name used for testing)
            return parseJava(packagePath, file, src);
        }
        return parseGroovy(packagePath, file, src);
    }

    private Map<String, GroovyClassDoc> parseJava(String packagePath, String file, String src) throws RuntimeException {
        GroovydocJavaVisitor visitor = new GroovydocJavaVisitor(packagePath, links);
        visitor.visit(StaticJavaParser.parse(src), null);
        return visitor.getGroovyClassDocs();
    }

    private Map<String, GroovyClassDoc> parseGroovy(String packagePath, String file, String src) throws RuntimeException {
        CompilerConfiguration config = new CompilerConfiguration();
        config.getOptimizationOptions().put(CompilerConfiguration.GROOVYDOC, true);
        CompilationUnit compUnit = new CompilationUnit(config);
        SourceUnit unit = new SourceUnit(file, src, config, null, new ErrorCollector(config));
        compUnit.addSource(unit);
        compUnit.compile(Phases.SEMANTIC_ANALYSIS);
        ModuleNode root = unit.getAST();
        GroovydocVisitor visitor = new GroovydocVisitor(unit, packagePath, links);
        visitor.visitClass(root.getClasses().get(0));
        return visitor.getGroovyClassDocs();
    }

}
