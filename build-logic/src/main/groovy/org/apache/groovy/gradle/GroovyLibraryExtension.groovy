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
package org.apache.groovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.file.ProjectLayout
import org.gradle.api.java.archives.Manifest
import org.gradle.api.java.archives.ManifestMergeDetails
import org.gradle.api.java.archives.ManifestMergeSpec
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar

import javax.inject.Inject

/**
 * Provides information about Groovy libraries
 */
@CompileStatic
class GroovyLibraryExtension {
    final SharedConfiguration sharedConfiguration
    final Property<Boolean> includeInGroovyAll
    final ListProperty<String> repackagedDependencies
    private final Property<Boolean> grooid
    final Property<Boolean> checkBinaryCompatibility

    final ObjectFactory objects
    final JavaPluginExtension javaPluginExtension
    final SoftwareComponentContainer components
    final ConfigurationContainer configurations
    private final String projectName
    final ProjectLayout layout
    final TaskContainer tasks

    @Inject
    GroovyLibraryExtension(ObjectFactory factory,
                           SharedConfiguration sharedConfiguration,
                           JavaPluginExtension javaPluginExtension,
                           SoftwareComponentContainer components,
                           ConfigurationContainer configurations,
                           Project project, ProjectLayout layout,
                           TaskContainer tasks
    ) {
        this.objects = factory
        this.sharedConfiguration = sharedConfiguration
        this.includeInGroovyAll = factory.property(Boolean).convention(true)
        this.grooid = factory.property(Boolean).convention(false)
        this.checkBinaryCompatibility = factory.property(Boolean).convention(true)
        this.repackagedDependencies = factory.listProperty(String).convention([])
        this.javaPluginExtension = javaPluginExtension
        this.components = components
        this.configurations = configurations
        this.projectName = project.name
        this.layout = layout
        this.tasks = tasks
    }

    void optionalModule() {
        includeInGroovyAll.set(false)
    }

    void withoutBinaryCompatibilityChecks() {
        checkBinaryCompatibility.set(false)
    }

    boolean hasGrooid() {
        grooid.get()
    }

    void withGrooid(String capability,
                    Set<String> librariesToRepackage,
                    Map<String, String> mappingPatterns,
                    Map<String, List<String>> libraryExcludes,
                    List<String> allExcludes,
                    Map<String, String> resources
    ) {
        grooid.set(true)
        def grooidJar = tasks.register("grooidJar", JarJarTask) {
            def jarjar = tasks.named("jarjar", JarJarTask)
            it.dependsOn(jarjar)
            it.from.set(jarjar.flatMap { it.outputFile })
            if (librariesToRepackage) {
                it.repackagedLibraries.from configurations.getByName('runtimeClasspath').incoming.artifactView { view ->
                    view.componentFilter { ComponentIdentifier component ->
                        if (component instanceof ModuleComponentIdentifier) {
                            return component.module in librariesToRepackage
                        }
                        return false
                    }
                }.files
            }
            it.patterns = mappingPatterns
            it.excludesPerLibrary = libraryExcludes
            it.excludes = allExcludes
            it.createManifest = false
            it.includedResources = resources
            it.outputFile.set(layout.buildDirectory.file(
                    tasks.named('jar', Jar).map { jar ->
                        "libs/${jar.archiveBaseName.get()}-${jar.archiveVersion.get()}-grooid.jar"
                    }
            ))
        }
        def androidRuntime = configurations.create("androidRuntimeElements") { Configuration it ->
            it.canBeConsumed = true
            it.canBeResolved = false
            it.attributes {
                it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                it.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "jar"))
                it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
                it.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            }
            it.outgoing {
                it.artifact(grooidJar)
                it.capability(capability)
            }
        }
        AdhocComponentWithVariants component = findComponent()
        component.addVariantsFromConfiguration(androidRuntime) {
            it.mapToOptional()
        }
    }

    void registerOptionalFeature(String name) {
        def sourceSet = javaPluginExtension.sourceSets.create(name)
        def main = javaPluginExtension.sourceSets.getByName('main')
        main.compileClasspath += sourceSet.compileClasspath
        javaPluginExtension.registerFeature(name) {
            it.usingSourceSet(sourceSet)
        }
        AdhocComponentWithVariants component = findComponent()
        def apiElements = configurations.getByName("${name}ApiElements")
        apiElements.artifacts.clear()
        component.addVariantsFromConfiguration(apiElements) {
            if (it.configurationVariant.name != "${name}ApiElements") {
                it.skip()
            }
            it.mapToMavenScope("compile")
            it.mapToOptional()
        }
        def runtimeElements = configurations.getByName("${name}RuntimeElements")
        runtimeElements.artifacts.clear()
        component.addVariantsFromConfiguration(runtimeElements) {
            if (it.configurationVariant.name != "${name}RuntimeElements") {
                it.skip()
            }
            it.mapToMavenScope("runtime")
            it.mapToOptional()
        }
    }

    void moduleDescriptor(Action<? super ModuleDescriptorSpec> spec) {
        def md = new ModuleDescriptorSpec()
        spec.execute(md)
        md.build()
    }

    private AdhocComponentWithVariants findComponent() {
        (AdhocComponentWithVariants) components.getByName("groovyLibrary")
    }

    void configureManifest(Manifest manifest, List<String> exclusions) {
        manifest.from(createBaseManifest()) { ManifestMergeSpec spec ->
            spec.eachEntry { ManifestMergeDetails details ->
                if (exclusions.contains(details.getKey())) {
                    details.exclude()
                }
            }
        }
    }

    private Manifest createBaseManifest() {
        def groovyBundleVersion = sharedConfiguration.groovyBundleVersion.get()
        javaPluginExtension.manifest {
            attributes(
                'Bundle-ManifestVersion': '2',
                'Bundle-Description'    : 'Groovy Runtime',
                'Bundle-Vendor'         : 'The Apache Software Foundation',
                'Bundle-Version'        : groovyBundleVersion,
                'Bundle-License'        : 'Apache-2.0',
                'Specification-Title'   : 'Groovy: a powerful, multi-faceted language for the JVM',
                'Specification-Vendor'  : 'The Apache Software Foundation',
                'Specification-Version' : groovyBundleVersion,
                'Implementation-Title'  : 'Groovy: a powerful, multi-faceted language for the JVM',
                'Implementation-Vendor' : 'The Apache Software Foundation',
                'Implementation-Version': groovyBundleVersion
            )
            if (projectName == 'groovy') {
                attributes(
                    'DynamicImport-Package': '*', // GROOVY-3192
                    'Eclipse-BuddyPolicy'  : 'dependent', // GROOVY-5571
                    'Main-Class'           : 'groovy.ui.GroovyMain'
                )
            }
        }
    }

    class ModuleDescriptorSpec {
        String extensionClasses = ''
        String staticExtensionClasses = ''

        private void build() {
            def moduleDescriptor = tasks.register('moduleDescriptor', WriteExtensionDescriptorTask) { t ->
                t.extensionClasses = extensionClasses
                t.staticExtensionClasses = staticExtensionClasses
            }
            tasks.named('processResources') { Task t ->
                t.dependsOn(moduleDescriptor)
            }
        }
    }
}
