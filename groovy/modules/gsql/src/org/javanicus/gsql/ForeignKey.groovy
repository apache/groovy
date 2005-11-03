package org.javanicus.gsql

public class ForeignKey implements Cloneable {
    @Property foreignTable
    @Property List references
    
    public ForeignKey() {
        references = []
    }
    
    public Object clone() { // @todo throws CloneNotSupportedException {
        def result = new ForeignKey()
        
        result.foreignTable = foreignTable
        result.references   = references.clone()
        return result
    }
}

