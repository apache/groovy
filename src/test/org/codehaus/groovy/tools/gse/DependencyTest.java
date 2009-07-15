package org.codehaus.groovy.tools.gse;

import java.io.StringBufferInputStream;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import groovy.util.GroovyTestCase;

public class DependencyTest extends GroovyTestCase {
    public void testDep(){
        CompilationUnit cu = new CompilationUnit();
        final StringSetMap cache = new StringSetMap();
        cu.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            @Override
            public void call(final SourceUnit source, GeneratorContext context, ClassNode classNode) 
                throws CompilationFailedException 
            {   
                DependencyTracker dt = new DependencyTracker(source,cache);
                dt.visitClass(classNode);
            }
        }, Phases.CLASS_GENERATION);
        cu.addSource("depTest.gtest", new StringBufferInputStream(
                "class C1 {}\n" +
                "class C2 {}\n" +
                "class C3 {}\n" +
                "class A1 {C1 x}\n" +
                "class A2 extends C2{}\n" +
                "class A3 {C1 foo(C2 x){new C3()}}\n"
        ));
        cu.compile(Phases.CLASS_GENERATION);
        assertEquals(cache.get("C1").size(),0);
        assertEquals(cache.get("C2").size(),0);
        assertEquals(cache.get("C3").size(),0);
        
        Set<String> dep = cache.get("A1");
        assertEquals(dep.size(),1);
        assertTrue(dep.contains("C1"));
        
        dep = cache.get("A2");
        assertEquals(dep.size(),1);
        assertTrue(dep.contains("C2"));
        System.out.println(dep);
        
        dep = cache.get("A3");
        assertEquals(dep.size(),3);
        assertTrue(dep.contains("C1"));
        assertTrue(dep.contains("C2"));
        assertTrue(dep.contains("C3"));
    }
}
