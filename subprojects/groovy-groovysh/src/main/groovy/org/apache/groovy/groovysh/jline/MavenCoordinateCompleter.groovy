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
package org.apache.groovy.groovysh.jline

import org.jline.reader.Completer
import org.jline.reader.Candidate
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class MavenCoordinateCompleter implements Completer {

    private File mavenRepo = new File(System.getProperty('user.home'), '.m2/repository')


    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        def word = line.word()
        def parts = word.split(':', -1)

        switch (parts.size()) {
            case 1:
                suggestGroupIds(parts[0], candidates)
                break
            case 2:
                suggestArtifactIds(parts[0], parts[1], candidates)
                break
            case 3:
                suggestVersions(parts[0], parts[1], parts[2], candidates)
                break
        }
    }

    private void suggestGroupIds(String prefix, List<Candidate> candidates) {
        boolean endsWithDot = prefix.endsWith('.')
        def parts = prefix ? prefix.split(/\./) : []
        def lastPart = endsWithDot ? '' : (parts ? parts[-1] : '')
        def baseParts = endsWithDot ? parts : (parts.size() > 1 ? parts[0..-2] : [])
        def baseDir = baseParts.inject(mavenRepo) { dir, part -> new File(dir, part) }

        if (!baseDir.exists()) return

        baseDir.eachDir { subdir ->
            if (lastPart && !subdir.name.startsWith(lastPart)) return

            def suggestionParts = baseParts + [subdir.name]
            def suggestion = suggestionParts.join('.')
            def candidateFile = new File(baseDir, subdir.name)
            def childDirs = candidateFile.listFiles().grep(File::isDirectory)
            def hasArtifactChildren = childDirs.any { isVersionDirPresent(it) }
            def hasOnlyArtifactChildren = childDirs.every { isVersionDir(it) }
            def hasDeeperGroupChildren = !hasOnlyArtifactChildren && childDirs.any { !isVersionDirPresent(it) }

            if (hasArtifactChildren) {
                candidates << new Candidate(suggestion + ':', suggestion + ':', null, null, '', null, false)
            }
            if (hasDeeperGroupChildren) {
                candidates << new Candidate(suggestion + '.', suggestion + '.', null, null, '', null, false)
            }
        }
    }

    private void suggestArtifactIds(String groupId, String artifactPrefix, List<Candidate> candidates) {
        def groupDir = new File(mavenRepo, groupId.replace('.', File.separator))
        if (!groupDir.exists()) return

        groupDir.eachDir { artifactDir ->
            if (artifactDir.name.startsWith(artifactPrefix) && isVersionDirPresent(artifactDir)) {
                def suggestion = "${groupId}:${artifactDir.name}:"
                candidates << new Candidate(suggestion, suggestion, null, null, '', null, false)
            }
        }
    }

    private void suggestVersions(String groupId, String artifactId, String versionPrefix, List<Candidate> candidates) {
        def artifactDir = new File(mavenRepo, "${groupId.replace('.', File.separator)}/${artifactId}")
        if (!artifactDir.exists()) return

        artifactDir.eachDir { versionDir ->
            if (versionDir.name.startsWith(versionPrefix)) {
                def suggestion = "${groupId}:${artifactId}:${versionDir.name}"
                candidates << new Candidate(suggestion, suggestion, null, null, ' ', null, true)
            }
        }
    }

    private boolean isVersionDirPresent(File dir) {
        dir.isDirectory() && dir.listFiles()?.any { isVersionDir(it) }
    }

    private boolean isVersionDir(File dir) {
        dir.isDirectory() && dir.name ==~ /^\d+(\.\d+)*([-_.].*)?$/
    }

}
