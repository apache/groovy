package org.javanicus.gsql

public class ForeignKey implements Cloneable {
    property foreignTable
    property List references
    
    public ForeignKey() {
        references = []
    }
    
    public Object clone() { // @todo throws CloneNotSupportedException {
        result = new ForeignKey()
        
        result.foreignTable = foreignTable
        result.references   = references.clone()
        return result
    }
}

