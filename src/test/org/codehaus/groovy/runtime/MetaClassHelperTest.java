package org.codehaus.groovy.runtime;

import junit.framework.TestCase;

public class MetaClassHelperTest extends TestCase {
    public void testGetClassName() {
        // GROOVY-1262
        MetaClassHelper.getClassName(null);
    }
}