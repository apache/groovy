/**
 * Test to verify that builder constructs valid Relational Schema
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

package org.javanicus.gsql

class RelationalBuilderTest extends GroovyTestCase {
    property database
    property table
              
    void setUp() {
        build = new RelationalBuilder(new TypeMap())
                  
        database = build.database(name:'fred') {
            table(name:'wilma') {
                column(name:'pebbles',size:'10,2',required:true)
                column(name:'bambam',size:'20')
            }
        }
        
        table = build.table(name:'individual') {
            column(name:'individual_id', type:'integer', required:true, primaryKey:true, autoIncrement:true)
            column(name:'surname', type:'varchar', size:15, required:true)
            column(name:'event_id', type:'integer')
            foreignKey(foreignTable:'event') {
                reference(local:'event_id',foreign:'event_id')
            }
            index(name:'surname_index') {
                indexColumn(name:'surname')
            }
        }
                  
    }
    
    void testFinders() {
        assert null != database
        assert "pebbles" == database.wilma.pebbles.name
        assert 10 == database.wilma.pebbles.size
        assert 2 == database.wilma.pebbles.scale
    }
    
    void testForeignKeys() {
        assert "event" == table.foreignKeys[0].foreignTable
        assert "event_id" == table.foreignKeys[0].references[0].local
        assert "event_id" == table.foreignKeys[0].references[0].foreign
    }          
}
