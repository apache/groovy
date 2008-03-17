package org.codehaus.groovy.runtime.dgmimpl.arrays;

/**
 * Created by IntelliJ IDEA.
 * User: applerestore
 * Date: Mar 10, 2008
 * Time: 9:07:18 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ArrayPutAtMetaMethod extends ArrayMetaMethod {
    public String getName() {
        return "putAt";
    }

    public Class getReturnType() {
        return Void.class;
    }
}
