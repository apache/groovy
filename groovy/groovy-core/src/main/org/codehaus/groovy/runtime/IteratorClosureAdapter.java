package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A closure which stores calls in a List so that method calls 
 * can be iterated over in a 'yield' style way
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IteratorClosureAdapter extends Closure {

    private List list = new ArrayList();
    private MetaClass metaClass = InvokerHelper.getMetaClass(this);
    
    public IteratorClosureAdapter(Object delegate) {
        super(delegate);
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }
    
    public List asList() {
        return list;
    }

    protected Object doCall(Object argument) {
        list.add(argument);
        return null;
    }
}
