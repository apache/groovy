package org.javanicus.gsql

class DatabaseTest extends GroovyTestCase {
    property table
    property idColumn
    property fred
    property barney
    property wilma
    property db1
    property db2
              
    void setUp() {
        typeMap = new TypeMap()          
        idColumn = new Column(typeMap,"id","id",0,"10",true,true,true,null)     
                  
        fred = new Table("fred")
        fred.addColumn(idColumn)
                  
        barney = new Table("barney")
        barney.addColumn(idColumn)          
                  
        wilma = new Table("wilma")
        wilma.addColumn(idColumn)
                  
        db1 = new Database("db1")
        db1.addTable(fred)
        db1.addTable(barney)
                  
        db2 = new Database("db2")
        db2.addTable(wilma)
        db2.addTable(barney)
    }
    
    void testMergeDatabasesTogether() {
        db1.mergeWith(db2);
        assert 3 == db1.tables.size()
        assert db1.tables.contains(fred)
        assert db1.tables.contains(barney)
        assert null != db1.tables.find(){it.name == "wilma"}
    }
}
