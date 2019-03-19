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
import me.champeau.gradle.japicmp.JapicmpTask

plugins {
    id("me.champeau.gradle.japicmp") version "0.2.6"
}

val checkBinaryCompatibility = tasks.register("checkBinaryCompatibility") {
    description = "Generates binary compatibility reports"
}

tasks.check {
    dependsOn(checkBinaryCompatibility)
}

// for comparing between versions with different modules, set excludeModules to differing modules, e.g.
val excludeModules = setOf(
        "groovy-cli-picocli", "groovy-cli-commons", "groovy-dateutil", "groovy-datetime", "groovy-jaxb",
        "groovy-macro", "groovy-json-direct", "groovy-test-junit5", "groovy-yaml", "performance", "tests-vm8",
        "binary-compatibility"
)

val compatibilityBaselineVersion = "2.4.15"

val binaryCompatProject = project

rootProject.allprojects {
    if (name !in excludeModules) {
        val baseline = binaryCompatProject.configurations.create("${getJapiTaskName()}Baseline") {
            dependencies.add(binaryCompatProject.dependencies.create("org.codehaus.groovy:${project.name}:${compatibilityBaselineVersion}@jar"))
        }
        val singleProjectCheck = binaryCompatProject.tasks.register<JapicmpTask>(getJapiTaskName()) {
            oldArchives = baseline
            newArchives = files(tasks.named("jarjar"))
            oldClasspath = files()
            newClasspath = files()
            accessModifier = "protected"
            onlyModified = true
            failOnModification = false
            ignoreMissingClasses = true
            classExcludes = listOf("**_closure**", "org.codehaus.groovy.runtime.dgm$**")
            packageExcludes = listOf("**internal**", "groovyjarjar**")
            htmlOutputFile = file("${binaryCompatProject.buildDir}/reports/${getJapiTaskName()}.html")
        }
        checkBinaryCompatibility {
            dependsOn(singleProjectCheck)
        }
    }
}

fun Project.getJapiTaskName() =
        "japicmp${name.split('-').joinToString(separator = "", transform = String::capitalize)}"