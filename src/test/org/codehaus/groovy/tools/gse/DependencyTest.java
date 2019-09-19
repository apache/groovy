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
package org.codehaus.groovy.tools.gse;

import java.io.StringBufferInputStream;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import groovy.test.GroovyTestCase;

@SuppressWarnings("deprecation")
public class DependencyTest extends GroovyTestCase {
    private CompilationUnit cu;
    StringSetMap cache;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cu = new CompilationUnit();
        cache = new StringSetMap();
        cu.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            @Override
            public void call(final SourceUnit source, GeneratorContext context, ClassNode classNode) 
                throws CompilationFailedException 
            {   
                DependencyTracker dt = new DependencyTracker(source,cache);
                dt.visitClass(classNode);
            }
        }, Phases.CLASS_GENERATION);
    }
    
    public void testDep(){
        cu.addSource("testDep.gtest", new StringBufferInputStream(
                "class C1 {}\n" +
                "class C2 {}\n" +
                "class C3 {}\n" +
                "class A1 {C1 x}\n" +
                "class A2 extends C2{}\n" +
                "class A3 {C1 foo(C2 x){new C3()}}\n"
        ));
        cu.compile(Phases.CLASS_GENERATION);
        assertEquals(cache.get("C1").size(),1);
        assertEquals(cache.get("C2").size(),1);
        assertEquals(cache.get("C3").size(),1);
        
        Set<String> dep = cache.get("A1");
        assertEquals(dep.size(),2);
        assertTrue(dep.contains("C1"));
        
        dep = cache.get("A2");
        assertEquals(dep.size(),2);
        assertTrue(dep.contains("C2"));
        
        dep = cache.get("A3");
        assertEquals(dep.size(),4);
        assertTrue(dep.contains("C1"));
        assertTrue(dep.contains("C2"));
        assertTrue(dep.contains("C3"));
    }
    
    public void testTransitiveDep(){
        cu.addSource("testTransitiveDep.gtest", new StringBufferInputStream(
                "class A1 {}\n" +
                "class A2 extends A1{}\n" +
                "class A3 extends A2{}\n"
        ));
        cu.compile(Phases.CLASS_GENERATION);
        cache.makeTransitiveHull();
         
        Set<String> dep = cache.get("A1");
        assertEquals(dep.size(),1);
        
        dep = cache.get("A2");
        assertEquals(dep.size(),2);
        assertTrue(dep.contains("A1"));
        
        dep = cache.get("A3");
        assertEquals(dep.size(),3);
        assertTrue(dep.contains("A1"));
        assertTrue(dep.contains("A2"));
    }
    

}
