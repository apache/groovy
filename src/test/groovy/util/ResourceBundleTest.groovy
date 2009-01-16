package groovy.util;

import java.util.ResourceBundle;

public class ResourceBundleTest extends GroovyTestCase {
    public void testNoClassLoaderNoLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n")
            results << rb
            assert rb.getString('upvote') == '+1'
        }
        assert results.size() == 2
    }

    public void testWithLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH)
            results << rb
            println "en"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'yes'
            assert rb.getString('upvote') == '+1'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH)
            results << rb
            println "fr"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'oui'
            assert rb.getString('upvote') == '+1'
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
            println "en"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'yes'
            assert rb.getString('upvote') == '+1'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH, cl)
            results << rb
            println "fr"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'oui'
            assert rb.getString('upvote') == '+1'
        }
        assert results.size() == 4
    }
}
