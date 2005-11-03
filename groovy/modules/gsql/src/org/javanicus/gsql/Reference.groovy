package org.javanicus.gsql

public class Reference implements Cloneable {
    @Property local
    @Property foreign
    
    public Object clone() { // @todo throws CloneNotSupportedException {
        def result = new Reference()
        
        result.local   = local
        result.foreign = foreign
        return result
    }
}
