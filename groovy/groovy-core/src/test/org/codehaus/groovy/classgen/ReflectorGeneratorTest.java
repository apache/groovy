/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.classgen;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ReflectorGeneratorTest extends GroovyTestCase {

    public void testGenerator() throws Exception {
        List methods = new ArrayList();
        methods.add(new MetaMethod("toCharArray", String.class, new Class[0], char[].class, 0));
        //methods.add(new MetaMethod("toString", String.class, new Class[0], String.class, 0));
        testMethods(methods);
    }

    public void testObjectGenerator() throws Exception {
        List methods = InvokerHelper.getMetaClass(new Object()).getMethods();
        testMethods(methods);
    }

    public void testDummyReflector() throws Exception {
        DummyReflector dummy = new DummyReflector();
        assertTrue(dummy != null);
    }

    protected void testMethods(List methods) throws Exception {
        ReflectorGenerator generator = new ReflectorGenerator(methods);
        String name = getClass().getName() + "." + getMethodName();
        ClassWriter cw = new ClassWriter(true);

        //ASMifierClassVisitor dumper = new ASMifierClassVisitor(new PrintWriter(new OutputStreamWriter(System.out)));
        //generator.generate(dumper, name);

        generator.generate(new CheckClassAdapter(cw), name);

        byte[] bytecode = cw.toByteArray();

        // lets write it to disk
        String fileName = "target/" + name + ".class";
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(bytecode);
        out.close();

        // now lets try dump it
        ASMifierClassVisitor.main(new String[]{fileName});

        // now lets try class load it
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        Object reflector = ((MetaClassRegistryImpl) registry).loadReflector(getClass(), methods);

        System.out.println("Created new reflector: " + reflector);
    }
}
