package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.runtime.metaclass.MetaClassHelper;

import junit.framework.TestCase;

public class MetaClassHelperTest extends TestCase {
    public void testGetClassName() {
        // GROOVY-1262
        MetaClassHelper.getClassName(null); 
    }
}