package org.javanicus.gsql

public class Reference implements Cloneable {
    property local
    property foreign
    
    public Object clone() { // @todo throws CloneNotSupportedException {
        result = new Reference()
        
        result.local   = local
        result.foreign = foreign
        return result
    }
}
