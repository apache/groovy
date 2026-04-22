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

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Post-process pass over groovydoc's HTML output that renames mock-class pages
 * back to the historical Groovy-JDK URLs. Reads a manifest written by
 * {@link MockSourceGenerator} — each line carries
 * {@code mockPkg\tmockName\tdisplayPkg\tdisplayName}. For each entry:
 * <ul>
 *   <li>moves {@code mockPkg/mockName.html} to {@code displayPkg/displayName.html}</li>
 *   <li>rewrites references to both the file and the containing directory in
 *       every {@code .html} file in the output tree</li>
 * </ul>
 * Also rewrites the header text ({@code Class PrimitiveInt} → {@code Class int},
 * {@code Class ObjectArray} → {@code Class Object[]}) and {@code <title>} strings.
 */
class PrimitiveNameRewriter {

    static void rewrite(File outputDir, File manifestFile) {
        if (!manifestFile.exists()) return
        def renames = []
        manifestFile.eachLine { line ->
            def parts = line.split('\t')
            if (parts.length == 4) {
                // Normalize to forward-slash form — groovydoc emits hrefs that way.
                renames << [mockPkg: parts[0].replace('.', '/'), mockName: parts[1],
                            displayPkg: parts[2].replace('.', '/'), displayName: parts[3]]
            }
        }
        if (renames.empty) return

        // Also rewrite the package directory name itself (primitives → primitives-and-primitive-arrays).
        def pkgRenames = new LinkedHashMap<String, String>()
        renames.each { r ->
            if (r.mockPkg != r.displayPkg) {
                pkgRenames[r.mockPkg] = r.displayPkg
            }
        }

        // 1) Rename files on disk.
        renames.each { r ->
            def srcDir = new File(outputDir, r.mockPkg)
            def src = new File(srcDir, "${r.mockName}.html")
            if (!src.exists()) return
            def targetDir = new File(outputDir, r.displayPkg)
            targetDir.mkdirs()
            def dst = new File(targetDir, "${r.displayName}.html")
            src.renameTo(dst)
        }

        // 2) Rename the package directory if needed (primitives → primitives-and-primitive-arrays).
        //    renameTo above already created the new directory when the first file moved;
        //    any remaining non-mock files in the old dir (e.g. package-summary.html,
        //    package-frame.html, package-tree.html) get moved below.
        pkgRenames.each { oldPkg, newPkg ->
            def oldDir = new File(outputDir, oldPkg)
            if (!oldDir.directory) return
            def newDir = new File(outputDir, newPkg)
            newDir.mkdirs()
            oldDir.listFiles()?.each { f ->
                if (f.file) f.renameTo(new File(newDir, f.name))
            }
            if (oldDir.list()?.length == 0) oldDir.delete()
        }

        // 3) Rewrite all HTML files for href/text references.
        outputDir.eachFileRecurse { f ->
            if (!f.file || !f.name.endsWith('.html')) return
            def orig = f.text
            def rewritten = orig
            // Sort longer mockNames first so e.g. "PrimitiveIntArray" is rewritten
            // before "PrimitiveInt" (otherwise we'd leave a stray "intArray").
            def sortedRenames = renames.toSorted { -it.mockName.length() }
            sortedRenames.each { r ->
                // Word-boundary replacement: matches the mock class name anywhere
                // it appears as a standalone identifier (header text, href basenames,
                // title attribute values, package-list entries, nav links). The
                // regex \b prevents collisions like PrimitiveInt vs PrimitiveIntArray.
                def pat = Pattern.compile('\\b' + Pattern.quote(r.mockName) + '\\b')
                rewritten = pat.matcher(rewritten).replaceAll(Matcher.quoteReplacement(r.displayName))
            }
            pkgRenames.each { oldPkg, newPkg ->
                rewritten = rewritten.replace("${oldPkg}/", "${newPkg}/")
            }
            if (rewritten != orig) f.text = rewritten
        }

        // 4) The package-list file at the top of the output also enumerates packages.
        def packageListFile = new File(outputDir, 'package-list')
        if (packageListFile.exists()) {
            def text = packageListFile.text
            pkgRenames.each { oldPkg, newPkg ->
                text = text.replace(oldPkg, newPkg)
            }
            packageListFile.text = text
        }
    }
}
