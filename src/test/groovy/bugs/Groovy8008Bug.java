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
package groovy.bugs;

import junit.framework.TestCase;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

/**
 * Synthetic parameters such as those added for inner class constructors may not be
 * included in the parameter annotations array.  This is the case when at least one
 * parameter of an inner class constructor is annotated with an annotation with
 * a RUNTIME retention policy.
 */
public class Groovy8008Bug extends TestCase {

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnno1 {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnno2 {}

    public void testConstructorParamAnnotationsWithSyntheticParam() throws Exception {
        Class<Inner> innerClass = Inner.class;
        Constructor<Inner> ctor = innerClass.getDeclaredConstructor(Groovy8008Bug.class, String.class, Date.class, String.class);

        assertEquals(4, ctor.getParameterTypes().length);
        // JDK 9 and above correctly report 4
        // assertEquals(3, ctor.getParameterAnnotations().length); //[],[@Anno1,@Anno2],[@Anno2]

        ClassNode cn = new ClassNode(innerClass);

        // this will trigger the call to VMPlugin#configureClassNode(CompileUnit,ClassNode)
        List<ConstructorNode> ctors = cn.getDeclaredConstructors();
        assertEquals(1, ctors.size());

        Parameter[] params = ctors.get(0).getParameters();
        checkOuterRef(params[0], Groovy8008Bug.class);
        checkStringWithNoAnnos(params[1]);
        checkDateWithTwoAnnos(params[2]);
        checkStringWithOneAnno(params[3]);

        // do the same again for an inner inner class
        Class<Inner.InnerInner> innerInnerClass = Inner.InnerInner.class;
        Constructor<Inner.InnerInner> innerCtor = innerInnerClass.getDeclaredConstructor(Groovy8008Bug.Inner.class, String.class, Date.class, String.class);
        assertEquals(4, innerCtor.getParameterTypes().length);

        cn = new ClassNode(innerInnerClass);

        // trigger the call to VMPlugin#configureClassNode(CompileUnit,ClassNode)
        ctors = cn.getDeclaredConstructors();
        assertEquals(1, ctors.size());

        params = ctors.get(0).getParameters();
        checkOuterRef(params[0], Groovy8008Bug.Inner.class);
        checkStringWithNoAnnos(params[1]);
        checkDateWithTwoAnnos(params[2]);
        checkStringWithOneAnno(params[3]);
    }

    private void checkOuterRef(Parameter param, Class outerClass) {
        assertEquals(outerClass.getName(), param.getType().getName());
        assertEquals(0, param.getAnnotations().size());
    }

    private void checkStringWithOneAnno(Parameter param) {
        assertEquals(String.class.getName(), param.getType().getName());
        assertEquals(1, param.getAnnotations().size());
        assertEquals(TestAnno2.class.getName(), param.getAnnotations().get(0).getClassNode().getName());
    }

    private void checkStringWithNoAnnos(Parameter param) {
        assertEquals(String.class.getName(), param.getType().getName());
        assertEquals(0, param.getAnnotations().size());
    }

    private void checkDateWithTwoAnnos(Parameter param2) {
        assertEquals(Date.class.getName(), param2.getType().getName());
        assertEquals(2, param2.getAnnotations().size());
        assertEquals(TestAnno1.class.getName(), param2.getAnnotations().get(0).getClassNode().getName());
        assertEquals(TestAnno2.class.getName(), param2.getAnnotations().get(1).getClassNode().getName());
    }

    private class Inner {
        private Inner(String arg1, @TestAnno1 @TestAnno2 Date arg2, @TestAnno2 String arg3) { }

        private class InnerInner {
            private InnerInner(String arg1, @TestAnno1 @TestAnno2 Date arg2, @TestAnno2 String arg3) { }
        }
    }
}
