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

import java.lang.reflect.Modifier;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;

/**
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class PropertyTest extends TestSupport {

    public void testFields() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC, "java.lang.Object");
        classNode.addField("x", ACC_PUBLIC, "java.lang.Object", null);
        classNode.addField("y", ACC_PUBLIC, "java.lang.Integer", null);
        classNode.addField("z", ACC_PRIVATE, "java.lang.String", null);

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        assertField(fooClass, "x", Modifier.PUBLIC, "java.lang.Object");
        assertField(fooClass, "y", Modifier.PUBLIC, "java.lang.Integer");
        assertField(fooClass, "z", Modifier.PRIVATE, "java.lang.String");
    }

    public void testProperties() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC + ACC_SUPER, "java.lang.Object");
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, "java.lang.String", "Foo", null, null, null));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        assertField(fooClass, "bar", 0, "java.lang.String");

        assertGetProperty(bean, "bar", null);
        assertSetProperty(bean, "bar", "newValue");
    }

    public void testInheritedProperties() throws Exception {
        ClassNode classNode = new ClassNode("Foo", ACC_PUBLIC + ACC_SUPER, "org.codehaus.groovy.runtime.DummyBean");
        classNode.addProperty(new PropertyNode("bar", ACC_PUBLIC, "java.lang.String", "Foo", null, null, null));

        Class fooClass = loadClass(classNode);
        assertTrue("Loaded a new class", fooClass != null);

        Object bean = fooClass.newInstance();
        assertTrue("Managed to create bean", bean != null);

        assertField(fooClass, "bar", 0, "java.lang.String");

        assertGetProperty(bean, "name", "James");
        assertSetProperty(bean, "name", "Bob");

        assertGetProperty(bean, "bar", null);
        assertSetProperty(bean, "bar", "newValue");
    }
}
