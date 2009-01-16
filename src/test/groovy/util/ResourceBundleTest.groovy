package groovy.util;

import java.util.ResourceBundle;

public class ResourceBundleTest extends GroovyTestCase {
    public void testNoClassLoaderNoLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH)
            results << rb
            // we could be defaulted to french or english, just assert something
            assert rb.getString('yes') != null
        }
        assert results.size() == 2
    }

    public void testWithLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH)
            results << rb
            assert rb.getString('yes') == 'yes'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH)
            results << rb
            assert rb.getString('yes') == 'oui'
        }
        assert results.size() == 4
    }

    public void testWithClassLoader() {
        def results = []
        ClassLoader cl = this.class.classLoader
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH, cl)
            results << rb
            assert rb.getString('yes') == 'yes'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH, cl)
            results << rb
            assert rb.getString('yes') == 'oui'
        }
        assert results.size() == 4
    }
}
