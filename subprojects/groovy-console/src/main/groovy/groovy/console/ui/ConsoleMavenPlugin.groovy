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
package groovy.console.ui

import groovy.grape.Grape
import groovy.grape.GrapeEngine

/**
 * Adds Groovy Grape feedback for the Maven Resolver based engine.
 *
 * @since 6.0.0
 */
class ConsoleMavenPlugin {
    /** Console receiving dependency progress messages. */
    Console savedConsole
    /** Dependency coordinates already reported as resolving. */
    Set<String> resolvedDependencies = []
    /** Artifacts already reported as downloading. */
    Set<String> downloadedArtifacts = []

    /**
     * Registers a Maven Resolver listener that reports Grape progress to the console.
     *
     * @param console console to receive progress messages
     */
    def addListener(Console console) {
        savedConsole = console
        GrapeEngine engine = Grape.instance
        if (engine?.class?.name == 'groovy.grape.maven.GrapeMaven' && engine.metaClass.respondsTo(engine, 'addProgressListener', Closure)) {
            engine.addProgressListener { Map event ->
                switch (event.type) {
                    case 'resolving':
                        if (resolvedDependencies.add(event.name)) {
                            savedConsole.showMessage "Resolving ${event.name} ..."
                        }
                        break
                    case 'downloading':
                        if (downloadedArtifacts.add(event.name)) {
                            savedConsole.showMessage "Downloading artifact ${event.name} ..."
                        }
                        break
                }
            }
        }
    }
}
