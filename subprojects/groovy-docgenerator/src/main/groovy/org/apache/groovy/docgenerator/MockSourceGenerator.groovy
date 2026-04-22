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
package org.apache.groovy.docgenerator

import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.JavaParameter
import com.thoughtworks.qdox.model.JavaType
import groovy.cli.internal.CliBuilderInternal
import org.codehaus.groovy.runtime.DefaultGroovyMethods

/**
 * Emits Java source files that stand in for the JDK types the Groovy Development
 * Kit enhances, so that {@code groovydoc} can process them as a normal source
 * tree. Each receiver type in the parsed DGM sources becomes one mock class:
 * instance methods for the typical case, static methods for sources whose class
 * name ends in {@code StaticMethods} or {@code StaticExtensions}.
 *
 * Primitive and primitive-array receivers are emitted under legal identifier
 * names in a synthetic package ({@code primitives}); reference-array receivers
 * are emitted in the receiver's own package with a synthetic class name. The
 * true display name (such as {@code int[]}) is carried on the class-level
 * javadoc as a {@code @displayName} tag and read by an override template;
 * a post-process pass renames generated HTML paths back to the historical
 * URLs (e.g. {@code primitives-and-primitive-arrays/int[].html}).
 */
class MockSourceGenerator {

    static final String PRIMITIVES_PKG = 'primitives'
    static final List<String> PRIMITIVE_BASES = [
        'int', 'long', 'double', 'float', 'boolean', 'byte', 'short', 'char'
    ]

    private final List<File> sourceFiles
    private final File outputDir

    MockSourceGenerator(List<File> sourceFiles, File outputDir) {
        this.sourceFiles = sourceFiles
        this.outputDir = outputDir
    }

    /**
     * Lines of {@code fromRelPath\tdisplayPackage\tdisplayName} written alongside
     * the mocks so {@link PrimitiveNameRewriter} can rename HTML output back to
     * historical URLs like {@code primitives-and-primitive-arrays/int[].html}.
     */
    static final String MANIFEST_FILE = 'mocks.manifest'

    void generateAll() {
        def builder = new JavaProjectBuilder()
        sourceFiles.each {
            if (it.exists()) {
                builder.addSource(it.newReader())
            }
        }

        // Bucket methods by receiver FQCN (generics stripped, single-letter type
        // variables coerced to java.lang.Object[] as docgenerator historically did).
        def byReceiver = new LinkedHashMap<String, List<JavaMethod>>()
        def typeOf = new LinkedHashMap<String, JavaType>()
        builder.sources.each { source ->
            source.classes.each { JavaClass aClass ->
                aClass.methods.each { JavaMethod method ->
                    if (!method.public || !method.static) return
                    if (method.parameters.empty) return
                    if (method.annotations.any { it.type.fullyQualifiedName == 'java.lang.Deprecated' }) return
                    def raw = method.parameters[0].type.toString()
                    def fqcn = resolveJdkClassName(raw)
                    byReceiver.computeIfAbsent(fqcn) { [] } << method
                    typeOf.putIfAbsent(fqcn, method.parameters[0].type)
                }
            }
        }

        // Group nested receivers under their outer class so we emit one file per
        // top-level mock with nested classes for enclosed types.
        def topLevelOwners = new LinkedHashMap<String, Owner>()
        byReceiver.each { fqcn, methods ->
            def outer = outerClassFqcn(fqcn)
            if (outer) {
                // Make sure the outer exists as an owner even if it has no direct methods.
                def parent = topLevelOwners.computeIfAbsent(outer) {
                    new Owner(fqcn: outer, type: typeOf[outer])
                }
                parent.nested[fqcn] = new Owner(fqcn: fqcn, type: typeOf[fqcn], methods: methods)
            } else {
                def owner = topLevelOwners.computeIfAbsent(fqcn) {
                    new Owner(fqcn: fqcn, type: typeOf[fqcn])
                }
                owner.methods.addAll(methods)
                if (!owner.type) owner.type = typeOf[fqcn]
            }
        }

        def manifestEntries = []
        topLevelOwners.values().each { owner ->
            def mockPkg = mockPackageFor(owner.fqcn, owner.type)
            def mockName = mockClassNameFor(owner.fqcn, owner.type)
            def displayPkg = displayPackageFor(owner.fqcn, owner.type)
            def displayName = displayNameFor(owner.fqcn)
            if (mockPkg != displayPkg || mockName != displayName) {
                manifestEntries << "${mockPkg}\t${mockName}\t${displayPkg}\t${displayName}"
            }
            emitMock(owner)
        }
        new File(outputDir, MANIFEST_FILE).text = manifestEntries.join('\n') + '\n'
    }

    /** Where the generated HTML page should live after rewriting (historical URL). */
    static String displayPackageFor(String fqcn, JavaType type) {
        def base = stripArraySuffix(fqcn)
        if (base in PRIMITIVE_BASES) return 'primitives-and-primitive-arrays'
        if (type?.isPrimitive()) return 'primitives-and-primitive-arrays'
        def dot = base.lastIndexOf('.')
        dot < 0 ? '' : base[0..<dot]
    }

    private static class Owner {
        String fqcn
        JavaType type
        List<JavaMethod> methods = []
        Map<String, Owner> nested = [:]
    }

    private void emitMock(Owner owner) {
        def pkg = mockPackageFor(owner.fqcn, owner.type)
        def mockName = mockClassNameFor(owner.fqcn, owner.type)
        def displayName = displayNameFor(owner.fqcn)

        def dir = new File(outputDir, pkg.replace('.', '/'))
        dir.mkdirs()

        def sb = new StringBuilder()
        sb << "package ${pkg};\n\n"
        appendClassJavadoc(sb, displayName, owner.fqcn, '')
        def kw = classKeyword(owner.type)
        sb << "public ${kw} ${mockName} {\n"
        owner.methods.each { sb << serialiseMethod(it, kw == 'interface', '    ') }
        owner.nested.values().each { nested ->
            appendNestedClass(sb, nested, '    ')
        }
        sb << "}\n"

        new File(dir, "${mockName}.java").text = sb.toString()
    }

    private void appendNestedClass(StringBuilder sb, Owner owner, String indent) {
        def displayName = displayNameFor(owner.fqcn)
        def mockName = simpleMockName(owner.fqcn)
        sb << '\n'
        appendClassJavadoc(sb, displayName, owner.fqcn, indent)
        def kw = classKeyword(owner.type)
        sb << "${indent}public static ${kw} ${mockName} {\n"
        owner.methods.each { sb << serialiseMethod(it, kw == 'interface', indent + '    ') }
        sb << "${indent}}\n"
    }

    private static void appendClassJavadoc(StringBuilder sb, String displayName, String fqcn, String indent) {
        sb << "${indent}/**\n"
        sb << "${indent} * GDK enhancements for ${displayName}.\n"
        sb << "${indent} *\n"
        sb << "${indent} * @displayName ${displayName}\n"
        sb << "${indent} * @receiverFqcn ${fqcn}\n"
        sb << "${indent} */\n"
    }

    private static String classKeyword(JavaType type) {
        (type instanceof JavaClass && ((JavaClass) type).isInterface()) ? 'interface' : 'class'
    }

    private String serialiseMethod(JavaMethod m, boolean ownerIsInterface, String indent) {
        def sb = new StringBuilder()
        def javadoc = copyJavadoc(m)
        if (javadoc) {
            sb << "${indent}/**\n"
            javadoc.readLines().each { line -> sb << "${indent} * ${line}\n".replace("${indent} * \n", "${indent} *\n") }
            sb << "${indent} */\n"
        }
        sb << indent << 'public '
        boolean isStatic = m.declaringClass.name.endsWith('StaticMethods') || m.declaringClass.name.endsWith('StaticExtensions')
        if (isStatic) sb << 'static '
        if (m.typeParameters) {
            sb << '<' << m.typeParameters.collect { it.toString() }.join(', ') << '> '
        }
        sb << (m.returns ? m.returns.genericCanonicalName : 'void') << ' '
        sb << m.name << '('
        def params = isStatic ? m.parameters.toList() : (m.parameters.size() > 1 ? m.parameters.toList()[1..-1] : [])
        sb << params.collect { JavaParameter p -> "${p.type.genericCanonicalName} ${p.name}" }.join(', ')
        sb << ')'
        if (m.exceptions) {
            sb << ' throws ' << m.exceptions.collect { it.genericCanonicalName }.join(', ')
        }
        if (ownerIsInterface && !isStatic) {
            sb << ';\n\n'
        } else {
            def body = bodyFor(m)
            sb << ' { ' << body << ' }\n\n'
        }
        sb.toString()
    }

    private static String bodyFor(JavaMethod m) {
        def rt = m.returns?.toString()
        if (!rt || rt == 'void') return ''
        'throw new UnsupportedOperationException();'
    }

    /**
     * Copies the method's javadoc, stripping the first {@code @param} (the receiver)
     * and rewriting internal {@code {@link #m(Recv,X,Y)}} references to
     * {@code {@link Recv#m(X,Y)}} so groovydoc's link resolver can resolve them
     * against the mock source tree.
     */
    private static String copyJavadoc(JavaMethod m) {
        def text = m.comment ?: ''
        def allTags = m.tags
        def paramTags = allTags.findAll { it.name == 'param' }
        def otherTags = allTags.findAll { it.name != 'param' }
        boolean isStatic = m.declaringClass.name.endsWith('StaticMethods') || m.declaringClass.name.endsWith('StaticExtensions')
        def remainingParams = isStatic ? paramTags : (paramTags.size() > 1 ? paramTags.drop(1) : [])
        def sb = new StringBuilder(rewriteLinks(text))
        if (sb.length() > 0) sb << '\n'
        remainingParams.each { sb << "@param ${rewriteLinks(it.value)}\n" }
        otherTags.each { sb << "@${it.name} ${rewriteLinks(it.value)}\n" }
        sb.toString().trim()
    }

    /**
     * Rewrites {@code {@link #method(Recv, A, B)}} to {@code {@link Recv#method(A, B)}}
     * — the first argument becomes the explicit class prefix, matching how
     * DocUtil.linkify operated in the original DocGenerator.
     */
    static String rewriteLinks(String text) {
        text.replaceAll(/\{@link\s+#([^(}\s]+)\(([^)]*)\)([^}]*)\}/) { all, name, argsText, rest ->
            def args = argsText.split(/,\s*/).findAll { it }.toList()
            if (args.empty) return all
            def first = args.remove(0)
            "{@link ${first}#${name}(${args.join(', ')})${rest}}"
        }
    }

    static String resolveJdkClassName(String className) {
        if (className in 'A'..'Z') return 'java.lang.Object'
        if (className in ('A'..'Z').collect { it + '[]' }) return 'java.lang.Object[]'
        className
    }

    /** Package the mock file lives in, derived from the receiver's FQCN. */
    static String mockPackageFor(String fqcn, JavaType type) {
        def base = stripArraySuffix(fqcn)
        if (base in PRIMITIVE_BASES) return PRIMITIVES_PKG
        if (type?.isPrimitive()) return PRIMITIVES_PKG
        def dot = base.lastIndexOf('.')
        dot < 0 ? '' : base[0..<dot]
    }

    /** Legal Java identifier used as the mock class name for a top-level receiver. */
    static String mockClassNameFor(String fqcn, JavaType type) {
        def base = stripArraySuffix(fqcn)
        def dims = arrayDimensions(fqcn)
        def simple = (base.lastIndexOf('.') < 0) ? base : base[(base.lastIndexOf('.') + 1)..-1]
        def name
        if (base in PRIMITIVE_BASES || (type?.isPrimitive())) {
            name = 'Primitive' + simple.capitalize()
        } else {
            name = simple
        }
        dims.times { name += 'Array' }
        name
    }

    /** Simple class name to use inside a nested declaration (no array suffix handling — nested mocks aren't arrays). */
    private static String simpleMockName(String fqcn) {
        def parts = fqcn.split(/\./)
        parts[-1]
    }

    /** Human-readable label used in @displayName tag: preserves brackets and the simple name only. */
    static String displayNameFor(String fqcn) {
        def base = stripArraySuffix(fqcn)
        def dims = arrayDimensions(fqcn)
        def simple = (base.lastIndexOf('.') < 0) ? base : base[(base.lastIndexOf('.') + 1)..-1]
        simple + ('[]' * dims)
    }

    /**
     * If {@code fqcn} names a nested type (has an uppercase-starting segment that
     * isn't the last), returns the outer type's FQCN. Otherwise returns null.
     */
    static String outerClassFqcn(String fqcn) {
        def base = stripArraySuffix(fqcn)
        def parts = base.split(/\./)
        if (parts.length < 2) return null
        def firstUpperIdx = -1
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] && Character.isUpperCase(parts[i].charAt(0))) { firstUpperIdx = i; break }
        }
        if (firstUpperIdx < 0 || firstUpperIdx == parts.length - 1) return null
        parts[0..firstUpperIdx].join('.')
    }

    private static String stripArraySuffix(String fqcn) {
        fqcn.replaceAll(/(\[\])+$/, '')
    }

    private static int arrayDimensions(String fqcn) {
        def base = stripArraySuffix(fqcn)
        (fqcn.length() - base.length()) / 2 as int
    }

    static void main(String... args) {
        def cli = new CliBuilderInternal(usage: 'MockSourceGenerator [options] [sourcefiles]', posix: false)
        cli.help(longOpt: 'help', 'Print this help')
        cli.o(longOpt: 'outputDir', args: 1, argName: 'path', 'Output directory for generated mock sources')
        def options = cli.parse(args)
        if (options.help) { cli.usage(); return }

        def outputDir = new File(options.outputDir ?: 'build/tmp/groovy-jdk-mocks')
        outputDir.mkdirs()

        def srcFiles = options.arguments().collect { sourceFileOf(it) }
        DefaultGroovyMethods.ADDITIONAL_CLASSES.each { aClass ->
            def className = aClass.name.replaceAll(/\$.*/, '')
            def f = sourceFileOf(className)
            if (srcFiles.every { it.canonicalPath != f.canonicalPath }) {
                srcFiles << f
            }
        }

        new MockSourceGenerator(srcFiles, outputDir).generateAll()
    }

    private static File sourceFileOf(String pathOrClassName) {
        if (pathOrClassName.contains(File.separator) || pathOrClassName.contains('/')) {
            return new File(pathOrClassName)
        }
        new File('../../src/main/java/' + pathOrClassName.replace('.', '/') + '.java')
    }
}
