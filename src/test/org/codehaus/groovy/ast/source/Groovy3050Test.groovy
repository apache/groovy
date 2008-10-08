package org.codehaus.groovy.ast.source


class Groovy3050Test extends SourceBaseTestCase {
    def script = '''
        (5..8).a
        [1,2].a
    '''        
    

    void testLine2() {
        def statements = statements()
        
        // (5..8).a
        def propExpression = statements[0].expression 
        assert sourceInfo(propExpression) == [2,9, 2,17]
        
        // a
        assert sourceInfo(propExpression.property) == [2,16, 2,17]
        
        // (5..8)
        def range = propExpression.objectExpression
        assert sourceInfo(range) == [2,9, 2,15]
        
        // 5
        //assert sourceInfo(range.from) == [2,10, 2,11]
        
        // 8
        //assert sourceInfo(range.to) == [2,13, 2,13]
   }
   
    void testLine3() {
        def statements = statements()
        // [1,2].a
        def propExpression = statements[1].expression
        assert sourceInfo(propExpression) == [3,9, 3,16]
        
        // a
        assert sourceInfo(propExpression.property) == [3,15, 3,16]
        
        // [1,2]
        def list = propExpression.objectExpression
        assert sourceInfo(list) == [3,9, 3,14]
        
        // 1
        assert sourceInfo(list.expressions[0]) == [3,10, 3,11]
        
        // 2
        assert sourceInfo(list.expressions[1]) == [3,12, 3,13]
   }
}