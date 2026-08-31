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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Scans compiled classes for {@code @org.apache.groovy.lang.annotation.GroovyABI} and
 * maintains the checked-in registry at {@code compatibility/groovy-abi-surface.json}.
 *
 * Modes:
 *  <ul>
 *    <li>{@code generate} — rewrite the registry from the live scan of the compiled classes.</li>
 *    <li>{@code sync} — fail if the live scan and the committed registry disagree
 *        (unmarked removal / signature change / annotation drop). Tombstoned entries are exempt.</li>
 *    <li>{@code change} / {@code release} — compare the committed registry against a baseline
 *        registry (from {@code git show} of the merge-base or the previous release tag),
 *        enforcing the intentional-break (tombstone) policy and the hard {@code deprecated-since} rule.</li>
 *  </ul>
 *
 * The scanner also enforces module scope: any module whose compiled classes carry
 * {@code @GroovyABI} must either be a tracked module in the registry or be listed in
 * {@code ignoredModules}; anything else is an error.
 */
abstract class GroovyABICheckTask extends DefaultTask {

    public static final String ABI_DESC = 'Lorg/apache/groovy/lang/annotation/GroovyABI;'

    @Input
    abstract ListProperty<String> getModuleClassDirs()

    /** Explicit allow-list of modules that may carry @GroovyABI and are tracked in the registry. */
    @Input
    abstract ListProperty<String> getTrackedModules()

    /** Modules deliberately not tracked; their @GroovyABI is ignored. */
    @Input
    abstract ListProperty<String> getIgnoredModules()

    /** generate | sync | change | release */
    @Input
    abstract Property<String> getMode()

    // registryFile/schemaFile are accessed manually (read/write); keep them @Internal so
    // generate can create the registry before it exists (no @InputFile validation).
    @Internal
    abstract RegularFileProperty getRegistryFile()

    @Internal
    abstract RegularFileProperty getSchemaFile()

    /** Baseline registry content (JSON text) for change/release modes. */
    @Internal
    abstract Property<String> getBaselineRegistryJson()

    @Input
    abstract Property<String> getVersion()

    @TaskAction
    void run() {
        def mode = mode.get()
        def entries = new LinkedHashMap<String, Map>()
        def outline = new LinkedHashMap<String, Set<String>>()
        def moduleOf = new LinkedHashMap<String, String>()
        def moduleHasAbi = [] as Set<String>

        // change/release modes compare registries only and must not require compiling modules.
        boolean needsScan = mode == 'generate' || mode == 'sync'
        if (needsScan) {
            def moduleToDirs = [:]  // module -> list of class dir paths (a module may have java+groovy output)
            moduleClassDirs.get().each { s ->
                def idx = s.indexOf('=')
                def module = s.substring(0, idx)
                def dir = s.substring(idx + 1)
                moduleToDirs.computeIfAbsent(module) { [] }.add(dir)
            }
            moduleToDirs.each { module, dirs ->
                dirs.each { dirStr ->
                    def dir = new File(dirStr)
                    if (!dir.exists()) {
                        return
                    }
                    dir.eachFileRecurse { f ->
                        if (!f.name.endsWith('.class') || f.name == 'module-info.class') {
                            return
                        }
                        def result = scanClass(f, module)
                        if (!result.annotated.isEmpty()) {
                            moduleHasAbi.add(module)
                        }
                        result.annotated.each { el ->
                            def k = elementKey(el)
                            entries[k] = el
                            moduleOf[k] = module
                        }
                        result.publicMethodKeys.each { pmk ->
                            outline[pmk.type] = (outline[pmk.type] ?: [] as Set) + pmk.signature
                        }
                    }
                }
            }
        }

        // Module scope enforcement: an @GroovyABI must live in a tracked or ignored module.
        def tracked = [] as Set<String>
        def registryData = null
        if (registryFile.getAsFile().get().exists() && mode != 'generate') {
            registryData = parseJson(registryFile.getAsFile().get().text)
            registryData.modules.each { m -> tracked.add(m.name) }
        }
        tracked.addAll(this.getTrackedModules().get())
        if (needsScan) {
            def abiAllowList = tracked + this.getIgnoredModules().get()
            moduleHasAbi.each { m ->
                if (!abiAllowList.contains(m)) {
                    throw new GradleException(
                        "Module '$m' contains @GroovyABI elements but is neither a tracked module nor " +
                        "listed in ignoredModules. Add it to the allow-list, ignore it, or remove the annotation.")
                }
            }
        }

        switch (mode) {
            case 'generate':
                // Emit only tracked modules; anything not tracked would have failed scope above.
                def filtered = entries.findAll { k, el -> tracked.contains(moduleOf[k]) }
                writeRegistry(filtered, moduleOf)
                break
            case 'sync':
                // Do not silently pass if the compiled classes were missing: if nothing was
                // scanned yet the registry has content, the modules were probably not compiled.
                if (entries.isEmpty() && registryData != null && registryData.modules.any { it.elements }) {
                    throw new GradleException(
                        'No @GroovyABI elements were found in the compiled classes, but the registry has entries. ' +
                        'The tracked modules may not be compiled; compile them (e.g. ./gradlew classes) before this check.')
                }
                checkSync(entries, moduleOf, registryData, outline)
                break
            case 'change':
            case 'release':
                String raws = baselineRegistryJson.isPresent() ? baselineRegistryJson.get() : ''
                def baseline = null
                if (raws) {
                    def f = new File(raws)
                    def text = f.isFile() ? f.text : raws
                    baseline = parseJson(text)
                }
                checkBaseline(registryData, baseline)
                break
            default:
                throw new GradleException("Unknown mode '${mode}'")
        }
    }

    // ------------------------------------------------------------------ scanning

    Map scanClass(File f, String module) {
        def cr = new ClassReader(new FileInputStream(f))
        def holder = [annotated: [], publicMethods: [], classSince: null, type: cr.className.replace('/', '.')]
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc == ABI_DESC) {
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        void visit(String nm, Object val) {
                            if (nm == 'since') {
                                holder.classSince = String.valueOf(val)
                            }
                        }
                    }
                }
                return null
            }

            @Override
            MethodVisitor visitMethod(int access, String mname, String mdesc, String sig, String[] exns) {
                boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_PRIVATE) == 0
                boolean isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0
                if (isPublic && !isSynthetic && mname != '<clinit>') {
                    holder.publicMethods.add([name: mname, descriptor: mdesc])
                }
                def method = [kind: 'method', name: mname, descriptor: mdesc, type: holder.type]
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    AnnotationVisitor visitAnnotation(String adesc, boolean visible) {
                        if (adesc == ABI_DESC) {
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                void visit(String nm, Object val) {
                                    if (nm == 'since') {
                                        method.since = String.valueOf(val)
                                    }
                                }
                            }
                        }
                        return null
                    }

                    @Override
                    void visitEnd() {
                        if (method.since != null && isPublic && !isSynthetic) {
                            holder.annotated.add(method)
                        }
                    }
                }
            }

            @Override
            FieldVisitor visitField(int access, String fname, String fdesc, String sig, Object fval) {
                boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_PRIVATE) == 0
                def field = [kind: 'field', name: fname, descriptor: fdesc, type: holder.type]
                return new FieldVisitor(Opcodes.ASM9) {
                    @Override
                    AnnotationVisitor visitAnnotation(String adesc, boolean visible) {
                        if (adesc == ABI_DESC) {
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                void visit(String nm, Object val) {
                                    if (nm == 'since') {
                                        field.since = String.valueOf(val)
                                    }
                                }
                            }
                        }
                        return null
                    }

                    @Override
                    void visitEnd() {
                        if (field.since != null && isPublic) {
                            holder.annotated.add(field)
                        }
                    }
                }
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)

        // Class-level annotation expands to the type's public methods unless a method
        // carries its own since. Duplicate removal and since assignment happen here.
        def result = []
        def seen = [] as Set
        holder.annotated.each { el ->
            if (el.since == null && holder.classSince != null) {
                el.since = holder.classSince
            }
            if (el.since != null && seen.add("${el.kind}:${el.name}${el.descriptor}")) {
                result.add(el)
            }
        }
        // Class-level annotated types: every public method without its own annotation
        // inherits the class since (e.g. ScriptBytecodeAdapter's class-level 1.0.0 alongside
        // createRange@4.0.0 / compoundAssign@6.0.0).
        if (holder.classSince != null) {
            holder.publicMethods.each { pm ->
                if (!seen.contains("method:${pm.name}${pm.descriptor}")) {
                    result.add([kind: 'method', name: pm.name, descriptor: pm.descriptor, since: holder.classSince, type: holder.type])
                }
            }
        }
        [annotated: result, publicMethodKeys: holder.publicMethods.collect { [type: holder.type, name: it.name, signature: it.descriptor] }]
    }


    // ------------------------------------------------------------------ helpers

    String elementKey(Map el) {
        "${el.type}#${el.kind}:${el.name}${el.descriptor}"
    }

    Map parseJson(String text) {
        new JsonSlurper().parseText(text)
    }

    void writeRegistry(Map entries, Map moduleOf) {
        def modules = [:]
        entries.values().each { el ->
            def m = moduleOf[elementKey(el)] ?: 'unknown'
            modules.computeIfAbsent(m) { [] }.add(sanitize(el))
        }
        def modArr = modules.keySet().sort().collect { name ->
            [name: name, elements: modules[name].sort { "${it.type}#${it.kind}:${it.name}" }]
        }
        def doc = [abiSchema: 1, version: version.get(), modules: modArr]
        def f = registryFile.getAsFile().get()
        f.parentFile.mkdirs()
        f.setText(JsonOutput.prettyPrint(JsonOutput.toJson(doc)) + '\n', 'UTF-8')
        logger.lifecycle("GroovyABI registry written to $f")
    }

    Map sanitize(Map el) {
        def out = [kind: el.kind, name: el.name, descriptor: el.descriptor, since: el.since,
                   'deprecated-since': null, tombstone: null]
        if (el.type) {
            out.type = el.type
        }
        out
    }

    // ------------------------------------------------------------------ sync

    void checkSync(Map liveEntries, Map moduleOf, Map registry, Map outline) {
        if (registry == null) {
            throw new GradleException('No registry file present; run checkGroovyABI with mode=generate first.')
        }
        validateRegistry(registry)
        def problems = []
        def registryByKey = [:]
        registry.modules.each { m ->
            m.elements.each { el ->
                registryByKey[registryKey(el, m.name)] = [element: el, module: m.name]
            }
        }
        registryByKey.each { k, rec ->
            def el = rec.element
            def liveEl = liveEntries.values().find {
                it.kind == el.kind && it.name == el.name && it.descriptor == el.descriptor && it.type == el.type
            }
            if (liveEl != null) {
                def m = moduleOf[elementKey(liveEl)]
                if (m != rec.module) {
                    problems << "Element ${rec.module} ${el.type}#${el.name}${el.descriptor} moved to module ${m}. Module ownership must not change; keep it in ${rec.module} or break it as a tombstone (removed) and add the new element separately."
                    return
                }
            }
            if (liveEl == null) {
                if (el.tombstone != null) {
                    return // expected absent
                }
                def type = el.type
                def stillPresent = el.kind == 'method' && type != null &&
                        outline[type]?.contains(el.descriptor)
                if (stillPresent) {
                    problems << "Annotation removed from ${rec.module} ${type}#${el.name}${el.descriptor} (the method still exists). Mark it as a tombstone type=annotation-removed or restore the annotation."
                } else {
                    problems << "Element ${rec.module} ${el.type ? el.type + '#' : ''}${el.name}${el.descriptor} is in the registry but missing from the compiled classes. Removing an @GroovyABI element breaks binary compatibility; mark it as a tombstone (removed) or restore it."
                }
                return
            }
            if (el.since != liveEl.since) {
                if (el.tombstone?.type == 'version-changed' && el.tombstone.'previous-since' == el.since) {
                    return // authorized
                }
                problems << "since changed for ${rec.module} ${el.type}#${el.name}: ${el.since} -> ${liveEl.since}. Only valid via a tombstone type=version-changed."
            }
        }
        if (problems) {
            throw new GradleException(
                '@GroovyABI sync check failed:\n  ' + problems.join('\n  ') +
                '\nUpdate compatibility/groovy-abi-surface.json (mode=generate) or mark intentional breaks as tombstones.')
        }
        logger.lifecycle('GroovyABI sync check passed.')
    }


    // ------------------------------------------------------------------ baseline

    void checkBaseline(Map current, Map baseline) {
        if (current == null) {
            throw new GradleException('Current registry is missing at the commit being checked.')
        }
        validateRegistry(current)
        if (baseline == null) {
            logger.lifecycle('GroovyABI: no baseline registry found; treating prior as empty. Only additions expected.')
            return
        }
        validateRegistry(baseline)
        def curByKey = [:]
        current.modules.each { m ->
            m.elements.each { el ->
                curByKey[registryKey(el, m.name)] = [element: el, module: m.name]
            }
        }
        def problems = []

        // Hard deprecated-since rule for tombstones newly introduced in `current`:
        // i.e. the element was a live (non-tombstone) entry in the baseline and is being
        // removed (or its annotation dropped) now.
        current.modules.each { m ->
            m.elements.each { el ->
                if (el.tombstone == null) {
                    return
                }
                def k = registryKey(el, m.name)
                def baseEl = baseline.modules.find { it.name == m.name }?.elements?.find { be -> registryKey(be, m.name) == k }
                def newlyTombstoned = baseEl != null && baseEl.tombstone == null
                if (!newlyTombstoned) {
                    return // already a tombstone in the baseline, or absent there; not a new break
                }
                if (el.tombstone.type in ['removed', 'annotation-removed']) {
                    if (el.'deprecated-since' == null) {
                        problems << "Intentional break on ${m.name} ${el.name}${el.descriptor} (tombstone type=${el.tombstone.type}) has no 'deprecated-since'. The element must be marked @Deprecated in the previous major version line before removal."
                    } else if (!sameOrEarlierMajor(el.'deprecated-since', String.valueOf(el.tombstone.version))) {
                        problems << "Intentional break on ${m.name} ${el.name}${el.descriptor}: tombstone version ${el.tombstone.version} is not > deprecated-since ${el.'deprecated-since'}."
                    }
                }
                if (el.tombstone.type == 'version-changed' && el.tombstone.'previous-since' == null) {
                    problems << "version-changed tombstone on ${m.name} ${el.name}${el.descriptor} is missing 'previous-since'."
                }
            }
        }

        // Compare baseline -> current
        baseline.modules.each { bm ->
            bm.elements.each { be ->
                def k = registryKey(be, bm.name)
                def cur = curByKey[k]
                if (cur == null) {
                    def tomb = current.modules.find { it.name == bm.name }?.elements?.find {
                        it.tombstone != null && registryKey(it, bm.name) == k
                    }
                    if (tomb == null) {
                        problems << "Element ${bm.name} ${be.name}${be.descriptor} was removed from the ABI without a tombstone. Mark it as a tombstone or keep it."
                    }
                    return
                }
                if (cur.element.since != be.since) {
                    def authorized = be.tombstone?.type == 'version-changed' || cur.element.tombstone?.type == 'version-changed'
                    if (!authorized) {
                        problems << "since changed for ${bm.name} ${be.name}: ${be.since} -> ${cur.element.since}."
                    }
                }
            }
        }
        if (problems) {
            throw new GradleException('@GroovyABI compatibility check failed:\n  ' + problems.join('\n  '))
        }
        logger.lifecycle('GroovyABI baseline comparison passed.')
    }

    String registryKey(Map el, String module) {
        "${module}#${el.type}#${el.kind}:${el.name}${el.descriptor}"
    }

    boolean sameOrEarlierMajor(String deprecatedSince, String tombVersion) {
        def a = (deprecatedSince =~ /^(\d+)\./)[0][1] as int
        def b = (tombVersion =~ /^(\d+)\./)[0][1] as int
        a <= b
    }


    // ------------------------------------------------------------------ validation

    void validateRegistry(Map reg) {
        if (!(reg.abiSchema == 1 || reg.abiSchema == '1')) {
            throw new GradleException("Unsupported abiSchema '${reg.abiSchema}'; this tool requires abiSchema 1.")
        }
        if (!validVersion(String.valueOf(reg.version))) {
            throw new GradleException("Invalid registry version '${reg.version}'; expected X.Y.Z or SNAPSHOT.")
        }
        reg.modules.each { m ->
            if (m.name == null) {
                throw new GradleException('A module entry is missing its name.')
            }
            m.elements.each { el ->
                if (el.kind !in ['method', 'field', 'constructor']) {
                    throw new GradleException("Invalid kind '${el.kind}' for ${m.name} ${el.name}.")
                }
                if (!validVersion(String.valueOf(el.since))) {
                    throw new GradleException("Invalid since '${el.since}' for ${m.name} ${el.name}; expected X.Y.Z or SNAPSHOT.")
                }
                if (el.tombstone != null) {
                    if (el.tombstone.type !in ['removed', 'annotation-removed', 'version-changed']) {
                        throw new GradleException("Invalid tombstone type '${el.tombstone.type}' for ${m.name} ${el.name}.")
                    }
                    if (el.tombstone.type == 'version-changed' && el.tombstone.'previous-since' == null) {
                        throw new GradleException("version-changed tombstone for ${m.name} ${el.name} is missing previous-since.")
                    }
                }
            }
        }
    }

    boolean validVersion(String v) {
        v == 'SNAPSHOT' || (v ==~ /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-SNAPSHOT)?$/)
    }
}

