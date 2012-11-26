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
package org.codehaus.groovy.classgen.asm.indy;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.*;

/**
 * Dummy class used by the indy implementation.
 * This class mostly contains empty stubs for calls to the call site writer,
 * since this class is normally used to prepare call site caching and in indy
 * call site caching is done by the jvm.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class IndyCallSiteWriter extends CallSiteWriter {
    private WriterController controller;
    public IndyCallSiteWriter(WriterController controller) {
        super(controller);
        this.controller = controller;
    }
    
    @Override
    public void generateCallSiteArray() {}
    @Override
    public void makeCallSite(Expression receiver, String message,
            Expression arguments, boolean safe, boolean implicitThis,
            boolean callCurrent, boolean callStatic) {}
    @Override
    public void makeSingleArgumentCall(Expression receiver, String message, Expression arguments) {}
    @Override
    public void prepareCallSite(String message) {}    
    @Override
    public void makeSiteEntry() {}
    @Override
    public void makeCallSiteArrayInitializer() {}
    
    @Override
    public void makeGetPropertySite(Expression receiver, String name, boolean safe, boolean implicitThis) {
        InvokeDynamicWriter idw = (InvokeDynamicWriter)controller.getInvocationWriter();
        idw.writeGetProperty(receiver, name, safe, implicitThis, false);
    }
    @Override
    public void makeGroovyObjectGetPropertySite(Expression receiver, String name, boolean safe, boolean implicitThis) {
        InvokeDynamicWriter idw = (InvokeDynamicWriter)controller.getInvocationWriter();
        idw.writeGetProperty(receiver, name, safe, implicitThis, true);
    }
    
}
