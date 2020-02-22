package org.apache.groovy.util


import org.junit.Test

class SystemUtilTest {
    @Test
    void getBooleanSafe() {
        def propName = "org.apache.groovy.util.SystemUtilTest.foo.bar"
        assert SystemUtil.getBooleanSafe(propName, true)
        assert !SystemUtil.getBooleanSafe(propName, false)

        System.setProperty(propName, "true")
        assert SystemUtil.getBooleanSafe(propName, true)
        assert SystemUtil.getBooleanSafe(propName, false)

        System.setProperty(propName, "false")
        assert !SystemUtil.getBooleanSafe(propName, true)
        assert !SystemUtil.getBooleanSafe(propName, false)
    }
}
