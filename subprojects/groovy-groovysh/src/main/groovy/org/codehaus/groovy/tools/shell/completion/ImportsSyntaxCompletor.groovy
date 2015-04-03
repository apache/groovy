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
package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Completor completing imported classnames
 */
class ImportsSyntaxCompletor implements IdentifierCompletor {

    final Groovysh shell
    // cache for all preimported classes
    List<String> preimportedClassNames
    // cache for all manually imported classes
    final Map<String, Collection<String>> cachedImports = new HashMap<String, Collection<String>>()

    ImportsSyntaxCompletor(final Groovysh shell) {
        this.shell = shell
    }

    @Override
    boolean complete(final List<GroovySourceToken> tokens, final List<CharSequence> candidates) {
        String prefix = tokens.last().getText()
        boolean foundMatch = findMatchingPreImportedClasses(prefix, candidates)
        for (String importName in shell.imports) {
            foundMatch |= findMatchingImportedClassesCached(prefix, importName, candidates)
        }
        return foundMatch
    }

    boolean findMatchingImportedClassesCached(final String prefix, final String importSpec, final List<String> candidates) {
        Collection<String> cached
        if (! cachedImports.containsKey(importSpec)) {
            cached = new HashSet<String>()
            collectImportedSymbols(importSpec, cached)
            cachedImports.put(importSpec, cached)
        } else {
            cached = cachedImports.get(importSpec)
        }
        Collection<String> matches = cached.findAll({String it -> it.startsWith(prefix)})
        if (matches) {
            candidates.addAll(matches)
            return true
        }
        return false
    }

    boolean findMatchingPreImportedClasses(final String prefix, final Collection<String> matches) {
        boolean foundMatch = false
        if (preimportedClassNames == null) {
            preimportedClassNames = []
            for (packname in ResolveVisitor.DEFAULT_IMPORTS) {
                Set<String> packnames = shell.packageHelper.getContents(packname[0..-2])
                if (packnames) {
                    preimportedClassNames.addAll(packnames.findAll({String it -> it[0] in 'A'..'Z'}))
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

    private static final String STATIC_IMPORT_PATTERN = ~/^import static ([a-z0-9]+\.)+[A-Z][a-zA-Z0-9]*(\.(\*|[^.]+))?$/

    /**
     * finds matching imported classes or static methods
     * @param prefix
     * @param importSpec
     * @param matches
     * @return
     */
    void collectImportedSymbols(final String importSpec, final Collection<String> matches) {
        String asKeyword = ' as '
        int asIndex = importSpec.indexOf(asKeyword)
        if (asIndex > -1) {
            String alias = importSpec.substring(asIndex + asKeyword.length())
            matches << alias
            return
        }
        String staticPrefix = 'import static '
        if (importSpec.startsWith(staticPrefix)) {
            // make sure pattern is safe, though shell should have done anyway
            if (importSpec.matches(STATIC_IMPORT_PATTERN)) {
                String evalImportSpec
                if (importSpec.endsWith('.*')) {
                    evalImportSpec = importSpec[staticPrefix.length()..-3]
                } else {
                    evalImportSpec = importSpec[staticPrefix.length()..(importSpec.lastIndexOf('.') - 1)]
                }
                Class clazz = shell.interp.evaluate([evalImportSpec]) as Class
                if (clazz != null) {
                    Collection<String> members = ReflectionCompletor.getPublicFieldsAndMethods(clazz, '')*.value
                    for (member in members) {
                        matches.add(member)
                    }
                }
            }
        } else if (importSpec.endsWith('*')) {
            Set<String> packnames = shell.packageHelper.getContents(importSpec.substring('import '.length()))
            if (packnames) {
                for (String packName in packnames) {
                    matches << packName
                }
            }
        } else {
            String symbolname = importSpec.substring(importSpec.lastIndexOf('.') + 1)
            matches << symbolname
        }
    }
}
