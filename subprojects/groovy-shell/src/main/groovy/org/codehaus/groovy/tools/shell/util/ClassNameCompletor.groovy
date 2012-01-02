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

package org.codehaus.groovy.tools.shell.util

/**
 * Provides completion for class names.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ClassNameCompletor
    extends SimpleCompletor
{
    private final GroovyClassLoader classLoader

    ClassNameCompletor(final GroovyClassLoader classLoader) {
        assert classLoader

        this.classLoader = classLoader
        
        delimiter = '.'
    }

    SortedSet getCandidates() {
        def set = new TreeSet()

        //
        // TODO: Figure out what class names to include, for now just hack in some to test with
        //

        set << 'java.lang.System'
        set << 'groovy.lang.GroovyObject'

        return set
    }
}