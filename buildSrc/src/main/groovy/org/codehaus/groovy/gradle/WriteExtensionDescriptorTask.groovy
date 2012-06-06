/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A Gradle task for modules which generate the module descritor file.
 *
 * @author Cedric Champeau
 */
class WriteExtensionDescriptorTask extends DefaultTask {
    String description = 'Generates the org.codehaus.groovy.runtime.ExtensionModule descriptor file of a module'
    String extensionClasses = ''
    String staticExtensionClasses = ''

    @TaskAction
    def writeDescriptor() {
        def metaInfDir = new File("${project.buildDir}/classes/main/META-INF/services")
        metaInfDir.mkdirs()
        def descriptor = new File(metaInfDir, "org.codehaus.groovy.runtime.ExtensionModule")
        descriptor.withWriter {
            it << """moduleName=${project.name}
moduleVersion=${project.version}
extensionClasses=${extensionClasses}
staticExtensionClasses=${staticExtensionClasses}"""
        }
    }

}

