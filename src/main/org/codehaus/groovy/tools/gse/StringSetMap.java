package org.codehaus.groovy.tools.gse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StringSetMap extends HashMap<String,Set<String>> {
    public Set<String> get(Object o){
        String name = (String) o;
        Set<String> set = super.get(name);
        if (set==null) {
            set = new HashSet();
            put(name,set);
        }
        return set;
    }
}
