package org.codehaus.groovy.ast.source


class Groovy3049Test extends SourceBaseTestCase {
    def script = '''
        println new URL("http://google.com").getT
            
        name = "xxx"
        "User ${name}"
    '''        
    

    void testLine2() {
        def statements = statements()
        
        // println((new URL("http://google.com")).getT)
        def printlnCall = statements[0].expression 
        assert sourceInfo(printlnCall) == [2,9, 2,50]
        
        // println
        assert sourceInfo(printlnCall.method) == [2,9, 2,16]
        
        // (new URL("http://google.com")).getT
        def propertyExpression = printlnCall.arguments.expressions[0]
        assert sourceInfo(propertyExpression) == [2,17, 2,50]
        
        // .getT
        assert sourceInfo(propertyExpression.property) == [2,46, 2,50]
        
        // new URL("http://google.com")
        def newExpression = propertyExpression.objectExpression
        assert sourceInfo(newExpression) == [2,17, 2,45]
        
        // "http://google.com"
        assert sourceInfo(newExpression.arguments.expressions[0]) ==[2,25, 2,44] 
   }
   
   void testLine4() {
        def statements = statements()
        
        // name = "xxx"
        def assignment = statements[1].expression
        assert sourceInfo(assignment) == [4,9, 4,21]
        
        // name
        assert sourceInfo(assignment.leftExpression) == [4,9, 4,13]
        
        // "xxx"
        assert sourceInfo(assignment.rightExpression) == [4,16, 4,21]
   }
   
   void testLine5() {
        def statements = statements()
        
        // "User ${name}"
        def gstring = statements[2].expression
        assert sourceInfo(gstring) == [5,9, 5,23]
   }
}