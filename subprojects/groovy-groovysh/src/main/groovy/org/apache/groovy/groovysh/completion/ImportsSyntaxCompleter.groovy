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
package org.apache.groovy.groovysh.completion

import org.apache.groovy.groovysh.Groovysh
import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.control.ResolveVisitor

/**
 * Completer completing imported classnames
 */
class ImportsSyntaxCompleter implements IdentifierCompleter {

    final Groovysh shell
    // cache for all preimported classes
    List<String> preimportedClassNames
    // cache for all manually imported classes
    final Map<String, Collection<String>> cachedImports = new HashMap<String, Collection<String>>().withDefault { String key ->
        Collection<String> matchingImports = new TreeSet<String>()
        collectImportedSymbols(key, matchingImports)
        matchingImports
    }

    ImportsSyntaxCompleter(final Groovysh shell) {
        this.shell = shell
    }

    @Override
    boolean complete(final List<GroovySourceToken> tokens, final List<CharSequence> candidates) {
        String prefix = tokens.last().getText()
        boolean foundMatch = findMatchingPreImportedClasses(prefix, candidates)
        for (String importSpec in shell.imports) {
            foundMatch |= findMatchingImportedClassesCached(prefix, importSpec, candidates)
        }
        return foundMatch
    }

    boolean findMatchingImportedClassesCached(final String prefix, final String importSpec, final List<String> candidates) {
        candidates.addAll(cachedImports
                .get(importSpec)
                .findAll({ String it -> it.startsWith(prefix) }))
    }

    boolean findMatchingPreImportedClasses(final String prefix, final Collection<String> matches) {
        boolean foundMatch = false
        if (preimportedClassNames == null) {
            preimportedClassNames = []
            for (packname in ResolveVisitor.DEFAULT_IMPORTS) {
                Set<String> packnames = shell.packageHelper.getContents(packname[0..-2])
                if (packnames) {
                    preimportedClassNames.addAll(packnames.findAll({ String it -> it[0] in 'A'..'Z' }))
                }
            }
            preimportedClassNames.add('BigInteger')
            preimportedClassNames.add('BigDecimal')
        }
        // preimported names
        for (String preImpClassname in preimportedClassNames) {
            if (preImpClassname.startsWith(prefix)) {
                matches.add(preImpClassname)
                foundMatch = true
            }
        }
        return foundMatch
    }

    private static final String STATIC_IMPORT_PATTERN = ~/^static ([a-zA-Z_][a-zA-Z_0-9]*\.)+([a-zA-Z_][a-zA-Z_0-9]*|\*)$/

    /**
     * finds matching imported classes or static methods
     * @param importSpec an import statement without the leading 'import ' or trailing semicolon
     * @param matches all names matching the importSpec will be added to this Collection
     */
    void collectImportedSymbols(final String importSpec, final Collection<String> matches) {
        String asKeyword = ' as '
        int asIndex = importSpec.indexOf(asKeyword)
        if (asIndex > -1) {
            String alias = importSpec.substring(asIndex + asKeyword.length())
            matches << alias
            return
        }
        int lastDotIndex = importSpec.lastIndexOf('.')
        String symbolName = importSpec.substring(lastDotIndex + 1)
        String staticPrefix = 'static '
        if (importSpec.startsWith(staticPrefix)) {
            // make sure pattern is safe, though shell should have done anyway
            if (importSpec.matches(STATIC_IMPORT_PATTERN)) {
                String className = importSpec.substring(staticPrefix.length(), lastDotIndex)
                Class clazz = shell.interp.evaluate([className]) as Class
                if (clazz != null) {
                    Set<String> clazzSymbols = ReflectionCompleter.getPublicFieldsAndMethods(clazz, '')*.value
                    Collection<String> importedSymbols
                    if (symbolName == '*') {
                        importedSymbols = clazzSymbols
                    } else {
                        Set<String> acceptableMatches = [symbolName, symbolName + '(', symbolName + '()']
                        importedSymbols = acceptableMatches.intersect(clazzSymbols)
                    }
                    matches.addAll(importedSymbols)
                }
            }
        } else {
            if (symbolName == '*') {
                matches.addAll(shell.packageHelper.getContents(importSpec))
            } else {
                matches << symbolName
            }
        }
    }
}
