package org.javanicus.gsql

class DatabaseTest extends GroovyTestCase {
    @Property table
    @Property idColumn
    @Property fred
    @Property barney
    @Property wilma
    @Property db1
    @Property db2
              
    void setUp() {
        typeMap = new TypeMap()          
        idColumn = new Column(typeMap,"id","id",0,"10",true,true,true,null)     
                  
        fred = new Table("fred")
        fred.columns << idColumn
                  
        barney = new Table("barney")
        barney.columns << idColumn          
                  
        wilma = new Table("wilma")
        wilma.columns << idColumn
                  
        db1 = new Database("db1")
        db1.tables << fred
        db1.tables << barney
                  
        db2 = new Database("db2")
        db2.tables << wilma
        db2.tables << barney
    }
    
    void testMergeDatabasesTogether() {
        db1.mergeWith(db2);
        assert 3 == db1.tables.size()
        assert db1.tables.contains(fred)
        assert db1.tables.contains(barney)
        assert null != db1.tables.find(){it.name == "wilma"}
    }
}
