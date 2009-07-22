package org.codehaus.groovy.tools.gse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class StringSetMap extends HashMap<String,Set<String>> {
    
    public StringSetMap() {
        super();
    }
    
    public StringSetMap(StringSetMap other) {
        for (String key : other.keySet()) {
            get(key).addAll(other.get(key));
        }
    }
    
    public Set<String> get(Object o){
        String name = (String) o;
        Set<String> set = super.get(name);
        if (set==null) {
            set = new HashSet();
            put(name,set);
        }
        return set;
    }

    public void makeTransitiveHull() {
        TreeSet<String> nameSet = new TreeSet(keySet());
        StringSetMap ret = new StringSetMap(this);
        
        for (String k: nameSet) {
            StringSetMap delta = new StringSetMap();
            for (String i: nameSet) {
                for (String j: nameSet) {
                    Set<String> iSet = get(i);
                    if (iSet.contains(k) && get(k).contains(j)) {
                        delta.get(i).add(j);
                    }
                }
            }
            for (String i: nameSet) get(i).addAll(delta.get(i));
        }
    }
}
