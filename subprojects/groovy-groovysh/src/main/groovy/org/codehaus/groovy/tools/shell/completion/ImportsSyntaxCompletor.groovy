/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * Completor completing imported classnames
 */
public class ImportsSyntaxCompletor implements IdentifierCompletor {

    Groovysh shell
    // cache for all preimported classes
    List<String> preimportedClassNames
    // cache for all manually imported classes
    Map<String, Collection<String>> cachedImports = new HashMap<String, Collection<String>>()

    ImportsSyntaxCompletor(Groovysh shell) {
        this.shell = shell
    }

    @Override
    public boolean complete(final List<GroovySourceToken> tokens, List candidates) {
        String prefix = tokens.last().getText()
        boolean foundMatch = findMatchingPreImportedClasses(prefix, candidates)
        for (String importName in shell.imports) {
            foundMatch |= findMatchingImportedClassesCached(prefix, importName, candidates)
        }
        return foundMatch
    }

    boolean findMatchingImportedClassesCached(final String prefix, final String importSpec, List candidates) {
        Collection<String> cached
        if (! cachedImports.containsKey(importSpec)) {
            cached = new HashSet<String>()
            collectImportedSymbols(importSpec, cached)
            cachedImports.put(importSpec, cached);
        } else {
            cached = cachedImports.get(importSpec)
        }
        Collection<String> matches = cached.findAll {String it -> it.startsWith(prefix)}
        if (matches) {
            candidates.addAll(matches)
            return true
        }
        return false
    }

    boolean findMatchingPreImportedClasses(final String prefix, Collection matches) {
        boolean foundMatch = false
        if (preimportedClassNames == null) {
            preimportedClassNames = []
            for (packname in org.codehaus.groovy.control.ResolveVisitor.DEFAULT_IMPORTS) {
                Set<String> packnames = shell.packageHelper.getContents(packname[0..-2])
                if (packnames) {
                    preimportedClassNames.addAll(packnames.findAll{String it -> it[0] in "A".."Z"})
                }
            }
            preimportedClassNames.add("BigInteger")
            preimportedClassNames.add("BigDecimal")
        }
        // preimported names
        for (String preImpClassname in preimportedClassNames) {
            if (preImpClassname.startsWith(prefix)) {
                matches << preImpClassname
                foundMatch = true
            }
        }
        return foundMatch
    }

    static final String STATIC_IMPORT_PATTERN = /^import static ([a-z0-9]+\.)+[A-Z][a-zA-Z0-9]*(\.(\*|[^.]+))?$/

    /**
     * finds matching imported classes or static methods
     * @param prefix
     * @param importSpec
     * @param matches
     * @return
     */
    void collectImportedSymbols(String importSpec, Collection matches) {
        String AS_KEYWORD = " as "
        int asIndex = importSpec.indexOf(AS_KEYWORD)
        if (asIndex > -1) {
            String alias = importSpec.substring(asIndex + AS_KEYWORD.length())
            matches << alias
            return
        }
        String static_prefix = 'import static '
        if (importSpec.startsWith(static_prefix)) {
            // make sure pattern is safe, though shell should have done anyway
            if (importSpec  ==~ STATIC_IMPORT_PATTERN) {
                if (importSpec.endsWith(".*")) {
                    importSpec = importSpec[static_prefix.length()..-3]
                } else {
                    importSpec = importSpec[static_prefix.length()..(importSpec.lastIndexOf('.') - 1)]
                }
                Class clazz = shell.interp.evaluate([importSpec]) as Class
                if (clazz != null) {
                    Collection<String> members = ReflectionCompletor.getPublicFieldsAndMethods(clazz, '')
                    for (member in members) {
                        matches << member
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
