/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.CallSiteWriter;

/**
 * A call site writer which is able to switch between two modes :
 * <ul>
 *     <li>dynamic</li> mode which makes use of call site caching
 *     <li>static</li> mode which produces optimized bytecode with direct method calls
 * </ul>
 * The static mode is used when a method is annotated with {@link groovy.transform.CompileStatic}.
 *
 * @author Cedric Champeau
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter {

    public StaticTypesCallSiteWriter(final StaticTypesWriterController controller) {
        super(controller);
    }

    @Override
    public void generateCallSiteArray() {
    }

    @Override
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments, final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
    }

    @Override
    public void makeGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
    }

    @Override
    public void makeGroovyObjectGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
    }


    @Override
    public void makeSiteEntry() {
    }

    @Override
    public void prepareCallSite(final String message) {
    }
}
