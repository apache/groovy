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
package org.apache.groovy.groovysh.commands

import groovy.transform.CompileStatic
import jline.console.completer.AggregateCompleter
import jline.console.completer.Completer
import jline.console.completer.NullCompleter
import jline.console.completer.StringsCompleter
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Evaluator
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.Interpreter
import org.apache.groovy.groovysh.completion.ReflectionCompleter
import org.apache.groovy.groovysh.completion.ReflectionCompletionCandidate
import org.apache.groovy.groovysh.completion.StricterArgumentCompleter
import org.apache.groovy.groovysh.util.PackageHelper
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.tools.shell.util.Logger

import java.util.regex.Pattern

/**
 * The 'import' command.
 */
class ImportCommand extends CommandSupport {

    /**
     * pattern used to validate the arguments to the import command,
     * which proxies the Groovy import statement
     * chars, digits, underscore, dot, star
     */
    private static final Pattern IMPORTED_ITEM_PATTERN = ~'[a-zA-Z0-9_. *]+;?$'

    ImportCommand(final Groovysh shell) {
        super(shell, 'import', ':i')
    }

    @Override
    Completer getCompleter() {
        // need a different completer setup due to static import
        Completer impCompleter = new StringsCompleter(name + ' ', shortcut + ' ')
        Completer asCompleter = new StringsCompleter('as ')
        Completer nullCompleter = new NullCompleter()
        PackageHelper packageHelper = shell.packageHelper
        Interpreter interp = shell.interp
        Completer nonStaticCompleter = new StricterArgumentCompleter([
                impCompleter,
                new ImportCompleter(packageHelper, interp, false),
                asCompleter,
                nullCompleter])
        Completer staticCompleter = new StricterArgumentCompleter([
                impCompleter,
                new StringsCompleter('static '),
                new ImportCompleter(packageHelper, interp, true),
                asCompleter,
                nullCompleter])
        Collection<Completer> argCompleters = [
                nonStaticCompleter,
                staticCompleter]
        return new AggregateCompleter(argCompleters)

    }

    Object execute(final List<String> args) {
        assert args != null

        if (args.isEmpty()) {
            fail('Command \'import\' requires one or more arguments') // TODO: i18n
        }

        def importSpec = args.join(' ')

        // technically java conventions don't allow numerics at the start of package/class names so the regex below
        // is a bit lacking.  this approach works reasonably well ->
        // "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+((\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart})|\\*)+;?$"
        // but there's something preventing it from working when class names end in a "d" or "D" like
        // "java.awt.TextField" so it is not implemented as such here.  Perhaps this could be made to be more
        // intelligent if someone could figure out why that is happening or could write a nicer batch of regex to
        // solve the problem
        if (!(importSpec.matches(IMPORTED_ITEM_PATTERN))) {
            def msg = "Invalid import definition: '${importSpec}'" // TODO: i18n
            log.debug(msg)
            fail(msg)
        }
        // remove last semicolon
        importSpec = importSpec.replace(';', '')

        def buff = ['import ' + args.join(' ')]
        buff << 'def dummp = false'

        def type
        try {
            type = classLoader.parseClass(buff.join(NEWLINE))

            // No need to keep duplicates, but order may be important so remove the previous def, since
            // the last defined import will win anyways

            if (imports.remove(importSpec)) {
                log.debug('Removed duplicate import from list')
            }

            log.debug("Adding import: $importSpec")

            imports.add(importSpec)
            return imports.join(', ')
        }
        catch (CompilationFailedException e) {
            def msg = "Invalid import definition: '${importSpec}'; reason: $e.message" // TODO: i18n
            log.debug(msg, e)
            fail(msg)
        }
        finally {
            // Remove the class generated while testing the import syntax
            classLoader.removeClassCacheEntry(type?.name)
        }
    }
}

class ImportCompleter implements Completer {

    protected final Logger log = Logger.create(ImportCompleter)

    PackageHelper packageHelper
    Groovysh shell

    /*
     * The following rules do not need to work for all thinkable situations,just for all reasonable situations.
     * In particular the underscore and dollar signs in Class or method names usually indicate something internal,
     * which we intentionally want to hide in tab completion
     */
    // matches fully qualified Classnames with dot at the end
    private static final Pattern QUALIFIED_CLASS_DOT_PATTERN = ~/^[a-z_]{1}[a-z0-9_]*(\.[a-z0-9_]*)*\.[A-Z][^.]*\.$/
    // matches empty, packagenames or fully qualified classNames
    private static final Pattern PACK_OR_CLASSNAME_PATTERN = ~/^([a-z_]{1}[a-z0-9_]*(\.[a-z0-9_]*)*(\.[A-Z][^.]*)?)?$/
    // matches empty, packagenames or fully qualified classNames without special symbols
    private static final Pattern PACK_OR_SIMPLE_CLASSNAME_PATTERN = ~'^([a-z_]{1}[a-z0-9_]*(\\.[a-z0-9_]*)*(\\.[A-Z][^.\$_]*)?)?\$'
    // matches empty, packagenames or fully qualified classNames or fully qualified method names
    private static final Pattern PACK_OR_CLASS_OR_METHODNAME_PATTERN = ~'^([a-z_]{1}[a-z0-9.]*(\\.[a-z0-9_]*)*(\\.[A-Z][^.\$_]*(\\.[a-zA-Z0-9_]*)?)?)?\$'
    private static final Pattern LOWERCASE_IMPORT_ITEM_PATTERN = ~/^[a-z0-9.]+$/

    final boolean staticImport
    final Evaluator interpreter


    ImportCompleter(final PackageHelper packageHelper, final Evaluator interp, final boolean staticImport) {
        this.packageHelper = packageHelper
        this.staticImport = staticImport
        this.interpreter = interp
        this.shell = shell
    }

    @Override
    @CompileStatic
    int complete(final String buffer, final int cursor, final List<CharSequence> result) {
        String currentImportExpression = buffer ? buffer.substring(0, cursor) : ''
        if (staticImport) {
            if (!(currentImportExpression.matches(PACK_OR_CLASS_OR_METHODNAME_PATTERN))) {
                return -1
            }
        } else {
            if (!(currentImportExpression.matches(PACK_OR_SIMPLE_CLASSNAME_PATTERN))) {
                return -1
            }
        }
        if (currentImportExpression.contains('..')) {
            return -1
        }

        if (currentImportExpression.endsWith('.')) {
            // no upper case?
            if (currentImportExpression.matches(LOWERCASE_IMPORT_ITEM_PATTERN)) {
                Set<String> classnames = packageHelper.getContents(currentImportExpression[0..-2])
                if (classnames) {
                    if (staticImport) {
                        result.addAll(classnames.collect({ String it -> it + '.' }))
                    } else {
                        result.addAll(classnames.collect({ String it -> addDotOrBlank(it) }))
                    }
                }
                if (!staticImport) {
                    result.add('* ')
                }
                return currentImportExpression.length()
            } else if (staticImport && currentImportExpression.matches(QUALIFIED_CLASS_DOT_PATTERN)) {
                Class clazz = interpreter.evaluate([currentImportExpression[0..-2]]) as Class
                if (clazz != null) {
                    Collection<ReflectionCompletionCandidate> members = ReflectionCompleter.getPublicFieldsAndMethods(clazz, '')
                    result.addAll(members.collect({ ReflectionCompletionCandidate it -> it.value.replace('(', '').replace(')', '') + ' ' }))
                }
                result.add('* ')
                return currentImportExpression.length()
            }
            return -1
        } // endif startswith '.', we have a prefix

        String prefix
        int lastDot = currentImportExpression.lastIndexOf('.')
        if (lastDot == -1) {
            prefix = currentImportExpression
        } else {
            prefix = currentImportExpression.substring(lastDot + 1)
        }
        String baseString = currentImportExpression.substring(0, Math.max(lastDot, 0))

        // expression could be for Classname, or for static methodname
        if (currentImportExpression.matches(PACK_OR_CLASSNAME_PATTERN)) {
            Set<String> candidates = packageHelper.getContents(baseString)
            if (candidates == null || candidates.size() == 0) {
                // At least give standard package completion, else static keyword is highly annoying
                Collection<String> standards = ResolveVisitor.DEFAULT_IMPORTS.findAll({ String it -> it.startsWith(currentImportExpression) })
                if (standards) {
                    result.addAll(standards)
                    return 0
                }
                return -1
            }

            log.debug(prefix)
            Collection<String> matches = candidates.findAll({ String it -> it.startsWith(prefix) })
            if (matches) {
                result.addAll(matches.collect({ String it -> addDotOrBlank(it) }))
                return lastDot <= 0 ? 0 : lastDot + 1
            }
        } else if (staticImport) {
            Class clazz = interpreter.evaluate([baseString]) as Class
            if (clazz != null) {
                Collection<ReflectionCompletionCandidate> members = ReflectionCompleter.getPublicFieldsAndMethods(clazz, prefix)
                if (members) {
                    result.addAll(members.collect({ ReflectionCompletionCandidate it -> it.value.replace('(', '').replace(')', '') + ' ' }))
                    return lastDot <= 0 ? 0 : lastDot + 1
                }
            }
        }
        return -1
    }

    private static String addDotOrBlank(final String it) {
        if (it[0] in 'A'..'Z') {
            return it + ' '
        }
        return it + '.'
    }
}
