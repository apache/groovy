/**
 * @author Zohar Melamed
 * @version $Revision$
 */
class ZoharsBug extends GroovyTestCase {
    
    void testBug() {
        def values = [1,2,3,4]
        def result = bloo(values, {it > 1})
        result.each{println(it)}
    }
    
    def bloo(a,b){
        return a.findAll{b.call(it)}
    }    
}