/**
 * Test to verify that builder constructs valid Relational Schema
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

package org.javanicus.gsql

class RelationalBuilderTest extends GroovyTestCase {
    property database
              
    void setUp() {
        build = new RelationalBuilder(new TypeMap())
                  
        database = build.database(name:'fred') {
            table(name:'wilma') {
                column(name:'pebbles',size:'10,2',required:true)
                column(name:'bambam',size:'20')
            }
        }
    }
    
    void testFinders() {
        assert null != database
        assert "pebbles" == database.wilma.pebbles.name
        assert 10 == database.wilma.pebbles.size
        assert 2 == database.wilma.pebbles.scale
    }
}
