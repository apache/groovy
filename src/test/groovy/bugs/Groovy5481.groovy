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
package bugs

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyShell
import org.codehaus.groovy.tools.LoaderConfiguration
import org.codehaus.groovy.tools.RootLoader
import org.junit.jupiter.api.Test

import javax.tools.ToolProvider

/**
 * GROOVY-5481: a command-line classpath with more than one {@code *}
 * wildcard used to drop later entries ({@code dep/*:resources/*} did
 * not include the second directory). {@link LoaderConfiguration#addClassPath}
 * is the expander used by the starter; it must keep every wildcard segment.
 */
final class Groovy5481 {

    @Test
    void testTwoClasspathWildcardsBothExpand() {
        def root = File.createTempDir()
        try {
            def depDir = new File(root, 'target/dependency')
            def resDir = new File(root, 'src/main/resources')
            depDir.mkdirs()
            resDir.mkdirs()
            compileJar(new File(root, 'FromDep.java'), 'FromDep', 'DEP', new File(depDir, 'dep.jar'))
            compileJar(new File(root, 'FromRes.java'), 'FromRes', 'RES', new File(resDir, 'res.jar'))

            def cfg = new LoaderConfiguration()
            cfg.addClassPath(depDir.absolutePath + '/*' + File.pathSeparator + resDir.absolutePath + '/*')
            def urls = cfg.classPathUrls
            assert urls.any { it.toString().contains('dep.jar') }, "dep.jar missing from ${urls}"
            assert urls.any { it.toString().contains('res.jar') }, "res.jar missing from ${urls}"

            def loader = new GroovyClassLoader(new RootLoader(urls, Groovy5481.classLoader))
            def result = new GroovyShell(loader).evaluate('FromDep.id() + "," + FromRes.id()')
            assert result == 'DEP,RES'
        } finally {
            root.deleteDir()
        }
    }

    private static void compileJar(File src, String className, String id, File jar) {
        src.write "public class ${className} { public static String id() { return \"${id}\"; } }\n"
        def out = File.createTempDir()
        try {
            def compiler = ToolProvider.systemJavaCompiler
            assert compiler != null
            int rc = compiler.run(null, null, null, '-d', out.absolutePath, src.absolutePath)
            assert rc == 0
            def process = ['jar', 'cf', jar.absolutePath, '-C', out.absolutePath, "${className}.class"].execute()
            assert process.waitFor() == 0
        } finally {
            out.deleteDir()
        }
    }
}
