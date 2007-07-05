package org.codehaus.groovy.runtime;

import junit.framework.TestCase;
import org.codehaus.groovy.runtime.MetaClassHelper;

public class MetaClassHelperTest extends TestCase {
    public void testGetClassName() {
        // GROOVY-1262
        MetaClassHelper.getClassName(null);
    }
}