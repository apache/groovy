/**
 * @author Zohar Melamed
 * @version $Revision$
 */
class ZoharsBug extends GroovyTestCase {
    
    void testBug() {
        values = [1,2,3,4]
        result = bloo(values, {it > 1})
        result.each{println(it)}
    }
    
    def bloo(a,b){
        return a.findAll{b.call(it)}
    }    
}