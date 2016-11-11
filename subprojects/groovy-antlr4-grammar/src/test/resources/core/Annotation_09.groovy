@Test1(12)
@Test1(@Test1)
@Test2(v=6)
@Test3(v1=6, v2=8, v3=10)
@Test4([1, 2, 3])
@Test5(v=[1, 2, 3])
@Test6(v1=[1, 2, 3], v2=[1], v3=6)
package core

@Grapes([
        @Grab('xx:yy:1.0'), // xx
        @Grab('zz:yy:1.0') /* zz */
])
import xx.yy.ZZ;