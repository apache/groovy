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
package groovy.grape.ivy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

/**
 * {@code uninstallArtifact} deletes the jars named by a cached ivy descriptor. An artifact name in
 * that descriptor must not delete a file outside the module's own jars directory, whatever a
 * crafted or corrupt descriptor puts in it.
 */
final class GrapeIvyUninstallContainmentTest {

    @TempDir
    Path grapeRoot

    private static String descriptor(String artifactName) {
        """<?xml version="1.0"?>
           <ivy-module version="2.0">
             <info organisation="mygroup" module="mymodule" revision="1.0"/>
             <publications>
               <artifact name="${artifactName}" type="jar" ext="jar"/>
             </publications>
           </ivy-module>""".stripIndent()
    }

    private File setUpModule(String artifactName) {
        File moduleDir = new File(grapeRoot.toFile(), 'grapes/mygroup/mymodule')
        File jars = new File(moduleDir, 'jars')
        jars.mkdirs()
        new File(moduleDir, 'ivy-1.0.xml').text = descriptor(artifactName)
        moduleDir
    }

    @Test
    void aTraversingArtifactNameDoesNotDeleteOutsideTheJarsDirectory() {
        String previous = System.getProperty('grape.root')
        System.setProperty('grape.root', grapeRoot.toFile().absolutePath)
        try {
            File moduleDir = setUpModule('../victim')
            // the file the traversal would reach, a level above the jars directory
            File victim = new File(moduleDir, 'victim-1.0.jar')
            victim.text = 'precious'

            new GrapeIvy().uninstallArtifact('mygroup', 'mymodule', '1.0')

            assert victim.exists() : 'a ".." in the artifact name deleted a file outside the jars directory'
        } finally {
            if (previous == null) System.clearProperty('grape.root')
            else System.setProperty('grape.root', previous)
        }
    }

    @Test
    void anOrdinaryArtifactIsStillDeleted() {
        String previous = System.getProperty('grape.root')
        System.setProperty('grape.root', grapeRoot.toFile().absolutePath)
        try {
            File moduleDir = setUpModule('goodlib')
            File jar = new File(moduleDir, 'jars/goodlib-1.0.jar')
            jar.text = 'contents'

            new GrapeIvy().uninstallArtifact('mygroup', 'mymodule', '1.0')

            assert !jar.exists() : 'a normal artifact in the jars directory should be deleted'
        } finally {
            if (previous == null) System.clearProperty('grape.root')
            else System.setProperty('grape.root', previous)
        }
    }
}
