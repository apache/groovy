/**
 * Test to verify valid construction of default DDL
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
package org.javanicus.gsql

class SqlGeneratorTest extends GroovyTestCase {
    property database
              
    void setUp() {
        build = new RelationalBuilder(new TypeMap())
                  
        database = build.database(name:'genealogy') {
          table(name:'event') {
              column(name:'event_id', type:'integer', size:10, primaryKey:true, required:true)
              column(name:'description', type:'varchar', size:30)          
          }
          table(name:'individual') {
            column(name:'individual_id', type:'integer', size:10, required:true, primaryKey:true, autoIncrement:true)
            column(name:'surname', type:'varchar', size:15, required:true)
            column(name:'event_id', type:'integer', size:10)
            foreignKey(foreignTable:'event') {
                reference(local:'event_id',foreign:'event_id')
            }
            index(name:'surname_index') {
                indexColumn(name:'surname')
            }
          }
        }
    }
    
    void testGenerateDDL() {
        sqlGenerator = new SqlGenerator(new TypeMap(),System.getProperty( "line.separator", "\n" ))
        sqlGenerator.writer = System.out
        sqlGenerator.createDatabase(database,true)
   }

}
