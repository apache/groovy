package org.codehaus.groovy.antlr;

import groovy.util.GroovyTestCase;

public class GroovySourceASTTest extends GroovyTestCase {
    GroovySourceAST a;
    GroovySourceAST b;

    protected void setUp() throws Exception {
        a = new GroovySourceAST();
        a.setLine(3);
        a.setColumn(3);

        b = new GroovySourceAST();
        b.setLine(4);
        b.setColumn(2);
    }

    public void testLessThan() throws Exception {
        assertTrue(a.compareTo(b) < 0);
    }

    public void testEquality() throws Exception {
        assertTrue(a.equals(a));
        assertTrue(a.compareTo(a) == 0);
    }

    public void testGreaterThan() throws Exception {
        assertTrue(b.compareTo(a) > 0);
    }
}
