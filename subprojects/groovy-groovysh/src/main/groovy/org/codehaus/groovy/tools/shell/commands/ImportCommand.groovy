/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.tools.shell.commands

import jline.ArgumentCompletor
import jline.Completor
import jline.MultiCompletor
import org.codehaus.groovy.control.CompilationFailedException

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.Interpreter
import org.codehaus.groovy.tools.shell.completion.ReflectionCompletor
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.PackageHelper
import org.codehaus.groovy.tools.shell.util.SimpleCompletor


/**
 * The 'import' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ImportCommand
    extends CommandSupport
{
    ImportCommand(final Groovysh shell) {
        super(shell, 'import', '\\i')
    }

    @Override
    Completor getCompletor() {
        // need a different completor setup due to static import
        Completor impCompletor = new SimpleCompletor(name, shortcut)
        Completor asCompletor = new SimpleCompletor('as')
        PackageHelper packageHelper = shell.packageHelper
        Interpreter interp = shell.interp
        return new MultiCompletor([
                new ArgumentCompletor([
                        impCompletor,
                        new ImportCompletor(packageHelper, interp, false),
                        asCompletor,
                        null]),
                new ArgumentCompletor([
                        impCompletor,
                        new SimpleCompletor('static'),
                        new ImportCompletor(packageHelper, interp, true),
                        asCompletor,
                        null])])

    }

    Object execute(final List args) {
        assert args != null

        if (args.isEmpty()) {
            fail("Command 'import' requires one or more arguments") // TODO: i18n
        }

        def buff = [ 'import ' + args.join(' ') ]
        buff << 'def dummp = false'
        
        def type
        try {
            type = classLoader.parseClass(buff.join(NEWLINE))
            
            // No need to keep duplicates, but order may be important so remove the previous def, since
            // the last defined import will win anyways
            
            if (imports.remove(buff[0])) {
                log.debug("Removed duplicate import from list")
            }
            
            log.debug("Adding import: ${buff[0]}")
            
            imports << buff[0]
        }
        catch (CompilationFailedException e) {
            def msg = "Invalid import definition: '${buff[0]}'; reason: $e.message" // TODO: i18n
            log.debug(msg, e)
            fail(msg)
        }
        finally {
            // Remove the class generated while testing the import syntax
            classLoader.classCache.remove(type?.name)
        }
    }
}

class ImportCompletor implements jline.Completor {

    PackageHelper packageHelper
    Groovysh shell
    protected final Logger log = Logger.create(ImportCompletor.class)
    public final static String PACKNAME_PATTERN = "^([a-z0-9]+(\\.[a-z0-9]*)*(\\.[A-Z][^.\$_]*)?)?\$"
    public final static String PACKNAMECLASS_PATTERN = "^([a-z0-9]+(\\.[a-z0-9]*)*(\\.[A-Z][^.\$_]*(\\.[^.]*)?)?)?\$"
    boolean staticImport
    def interpreter


    public ImportCompletor(PackageHelper packageHelper, interp, boolean staticImport) {
        this.packageHelper = packageHelper
        this.staticImport = staticImport
        this.interpreter = interp
        this.shell = shell
    }

    @Override
    int complete(String buffer, int cursor, List result) {
        String current = buffer ? buffer.substring(0, cursor) : ""
        if (staticImport) {
            if (! (current ==~ PACKNAMECLASS_PATTERN)) {
                return -1
            }
        } else {
            if (! (current ==~ PACKNAME_PATTERN)) {
                return -1
            }
        }
        if (current.contains("..")) {
            return -1
        }

        if (current.endsWith('.')) {
            // no upper case?
            if (current ==~ /^[a-z0-9.]+$/) {
                Set<String> classnames = packageHelper.getContents(current[0..-2])
                if (classnames) {
                    if (staticImport) {
                        result.addAll(classnames.collect { String it -> it + "."})
                    } else {
                        result.addAll(classnames.collect { String it -> filterMatches(it) })
                    }
                }
                if (! staticImport) {
                    result.add('* ')
                }
                return current.length()
            } else if (staticImport && current ==~ /^[a-z0-9.]+\.[A-Z][^.]*\.$/) {
                Class clazz = interpreter.evaluate([current[0..-2]]) as Class
                if (clazz != null) {
                    Collection<String> members = ReflectionCompletor.getPublicFieldsAndMethods(clazz, "")
                    result.addAll(members.collect({ String it -> it.replace('(', '').replace(')', '') + " " }))
                }
                result.add('* ')
                return current.length()
            }
            return -1
        }
        String prefix
        int lastDot = current.lastIndexOf('.')
        if (lastDot == -1) {
            prefix = current
        } else {
            prefix = current.substring(lastDot + 1)
        }
        String baseString = current.substring(0, Math.max(lastDot, 0))

        // method completion?
        if (current ==~ /^[a-z0-9.]*(\.[A-Z][^.]*)?$/) {
            Set<String> candidates = packageHelper.getContents(baseString)
            if (candidates == null || candidates.size() == 0) {
                // At least give standard package completion, else static keyword is highly annoying
                Collection<String> standards = org.codehaus.groovy.control.ResolveVisitor.DEFAULT_IMPORTS.findAll {String it -> it.startsWith(current)}
                if (standards) {
                    result.addAll(standards)
                    return 0
                }
                return -1
            }

            log.debug(prefix)
            Collection<String> matches = candidates.findAll { String it -> it.startsWith(prefix) }
            if (matches) {
                result.addAll(matches.collect { String it -> filterMatches(it) })
                return lastDot <= 0 ? 0 : lastDot + 1
            }
        } else if (staticImport) {
            Class clazz = interpreter.evaluate([baseString]) as Class
            if (clazz != null) {
                Collection<String> members = ReflectionCompletor.getPublicFieldsAndMethods(clazz, prefix)
                if (members) {
                    result.addAll(members.collect({ String it -> it.replace('(', '').replace(')', '') + " " }))
                    return lastDot <= 0 ? 0 : lastDot + 1
                }
            }
        }
        return -1
    }

    def filterMatches(String it) {
        if (it[0] in 'A' .. 'Z') {
           return it + ' '
        }
        return it + '.'
    }
}