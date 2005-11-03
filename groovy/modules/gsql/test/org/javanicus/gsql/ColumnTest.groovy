package org.javanicus.gsql

class ColumnTest extends GroovyTestCase {
    @Property column
    @Property idColumn
    @Property nameColumn
    @Property yearOfBirthColumn          
              
    void setUp() {
        TypeMap typeMap = new TypeMap()          
        column = new Column(typeMap)
        idColumn = new Column(typeMap,"id","id",0,"10",true,true,true,null)     
        nameColumn = new Column(typeMap,"name","name",0,"250",true,false,false,"<no name>")
        yearOfBirthColumn = new Column(typeMap,"year of birth","yearOfBirth",0,"4",false,false,false,null,0)
    }
    
    void testPrimaryKey() { //todo: throws Exception {
        assert false == column.isPrimaryKey()
    }
    
    void testSizeAndScale() {
        column.size = "10,2"
        assert 2 == column.scale
        //bug? can't initialise properly (null not zero) - see constructor in Column.groovy
        //assert 0 == nameColumn.scale          
        assert 0 == yearOfBirthColumn.scale          
    }
    
    void testTypeNameHasBeenInitialisedWhenOnlyColumnTypeHasBeenSet() {
        assert "null" == idColumn.type
    }
    
    
}
