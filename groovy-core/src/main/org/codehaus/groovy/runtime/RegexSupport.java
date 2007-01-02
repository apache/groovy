package org.codehaus.groovy.runtime;

import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: May 17, 2004
 * Time: 9:04:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegexSupport {

    private static ThreadLocal currentMatcher = new ThreadLocal();

    public static Matcher getLastMatcher() {
        return (Matcher) currentMatcher.get();
    }

    public static void setLastMatcher(Matcher matcher) {
        currentMatcher.set(matcher);
    }
}
