package org.javanicus.gsql

class TableTest extends GroovyTestCase {
    @Property table
    @Property idColumn
    @Property nameColumn
              
    void setUp() {
        typeMap = new TypeMap()          
        idColumn = new Column(typeMap,"id","id",0,"10",true,true,true,null)     
        nameColumn = new Column(typeMap,"name","name",0,"250",true,false,false,"<no name>")
        table = new Table("wheelbarrow")
        table.columns << idColumn          
        table.columns << nameColumn     
    }
    
    void testPrimaryKey() { //todo: throws Exception {
        
        // @todo wouldn't it be groovy to have the inverse of
        // the contains() method, on Object, such that you
        // could express "if idColumn.isIn(table.primaryKeyColumns) {"
        // i.e. add to DefaultGroovyMethods something along the
        // lines of public static boolean isIn(Object obj,Collection cltn) ...
        
        assert table.primaryKeyColumns.contains(idColumn)
    }
    
    void testFindColumnUsingCaseInsensitiveName() {
        assert nameColumn == table.findColumn("NaMe")
    }
}
