class EscapedMetaCharacterTest extends GroovyTestCase {

  def newline = "\\"+"n"
  def tab = "\\"+"t"
  def backslash = "\\"
  def doublebackslash = "\\"+"\\"
  def creturn = "\\"+"r"
  def singlequote = "\\"+"'"
  def doublequote = "\\"+'"'
  def dollar= "\\"+"\$"
  
  def all = newline+tab+creturn


  void testNewLine() {
    assert "\\n"==newline
    assert '\\n'==newline
  }

  void testTab() {
    assert "\\t"==tab
    assert '\\t'==tab
  }

 void testBackslash() {
    assert "\\"==backslash 
    assert '\\'==backslash 
/**@todo more than one back slash is evaluated to only one backslash
    assert '\\\\'==doublebackslash 
    assert "\\\\"==doublebackslash
**/ 
 }

 void testReturn(){
   assert "\\r"==creturn 
   assert '\\r'==creturn 
  }

 void testDoubleQuote(){
   assert '\\"'==doublequote 
  }

 void testSingleQuote(){
   assert "\\'"==singlequote 
  }

/** @todo "\\$" is $ and should be \$
  void testDollarSign(){
    assert "\\$"==dollar
    assert '\\$'==dollar
  }
**/

  void testAll() {
/**@todo testAll is not complete because the $-test and the multiple backslash test is failing
    assert "\\n\\t\\r\\\\\\$\\'" == all+doublebackslash+dollar+"\\'"
    assert '\\n\\t\\r\\\\\\$\\"' == all+doublebackslash+dollar+'\\"'
**/
    assert "\\n\\t\\r\\'" == all+"\\'"
    assert '\\n\\t\\r\\"' == all+'\\"'


  }


}