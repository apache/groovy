class StringIncrementAndDecrementTest extends GroovyTestCase {

  void testIncrement() {
    str="9"
    str++
    assert str == "10"
    str="9-"
    str++
    assert str == "10-"
    str=".9"
    str++
    assert str == ".10"
    str=".09"
    str++
    assert str == ".10"
    str="0"
    str++
    assert str == "1"
    str="99"
    str++
    assert str == "100"
    str="a"
    str++
    assert str == "b"
    str="z"
    str++
    assert str == "aa"
    str=" "
    str++
    assert str == "!"
    str="--9--"
    str++
    assert str == "--10--"
    str="--a9--"
    str++
    assert str == "--b0--"
  }
 
  void testDecrement() {
    str="10"
    str--
    assert str == "9"
    str="10-"
    str--
    assert str == "9-"
    str=".10"
    str--
    assert str == ".9"
    str=".020"
    str--
    assert str == ".019"
    str="9"
    str--
    assert str == "8"
    str="100"
    str--
    assert str == "99"
    str="z"
    str--
    assert str == "y"
    str="aa"
    str--
    assert str == "z"
    str="!"
    str--
    assert str == " "
    str="--10--"
    str--
    assert str == "--9--"
    str="b0"
    str--
    assert str == "a9"
    str="#0"
    str--
    assert str == "#/"
  }

  void testIncrementDecrementCombination() {
    sb1 = new StringBuffer(" ")
    for (i in 0..Character.MAX_VALUE) {
      sb1.setCharAt(0,(char)i)
      str = sb1.toString();
      str++
      str--
      sb2 = new StringBuffer(str)
      int j = sb2.charAt(0)
      assert i==j
    }
  }

  void testDecrementIncrementCombination() {
    sb1 = new StringBuffer(" ")
    for (i in 0..Character.MAX_VALUE) {
      if (i==58) continue
      if (i==91) continue
      if (i==123) continue
      sb1.setCharAt(0,(char)i)
      str = sb1.toString();
      str--
      str++
      sb2 = new StringBuffer(str)
      int j = sb2.charAt(0)
      assert i==j
    }
 }
}
