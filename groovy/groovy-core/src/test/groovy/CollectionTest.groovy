package groovy

class CollectionTest extends GroovyTestCase {

    void testUniqueOnListNoDupls() {
    	assert [].unique() == []
    	assert [1].unique() == [1]
    	assert [1,2].unique() == [1,2]
    	def a = [1,2]
    	assert a.is(a.unique())
    }

    void testUniqueOnListOneDupl() {
    	assert [1,1].unique() == [1]
    	def a = [1,1]
    	assert a.is(a.unique())
    	assert [1,2,1].unique() == [1,2]
    	assert [1,2,1,1].unique() == [1,2]
    	assert [1,1,2].unique() == [1,2]
    	assert [1,1,2,1].unique() == [1,2]
    	assert [1,1,2,1,1].unique() == [1,2]
    }

    void testUniqueOnListTwoDupls() {
    	assert [1,1,2,2].unique() == [1,2]
    	def a = [1,1,2,2]
    	assert a.is(a.unique())
    	assert [1,2,1,2].unique() == [1,2]
    	assert [1,2,1,1,2].unique() == [1,2]
    	assert [1,1,2,2].unique() == [1,2]
    	assert [1,1,2,1,2].unique() == [1,2]
    	assert [1,1,2,2,1,1,2,2].unique() == [1,2]
    }

    void testUniqueOnOtherCollections() {
    	def a = new HashSet([1,1])
    	assert a.is(a.unique())
    	assert 1 == a.size()
    	a = new TreeSet([1,1])
    	assert a.is(a.unique())
    	assert 1 == a.size()
    	a = new Vector([1,1])
    	assert a.is(a.unique())
    	assert 1 == a.size()
    	a = new LinkedList([1,1])
    	assert a.is(a.unique())
    	assert 1 == a.size()
    }

    // todo: GROOVY-1006
    void testUniqueOnDifferentTypes() {
    	def a = [1, 2, (short)1, 2L, 2.0]
    	def b = a.unique()
    	assert (b == a && a == [1, 2])
    	a = [Math.PI, "foo", 1.0, 2L, (short)2, 2.0F]
    	b = a.unique()
    	assert (b == a && a == [Math.PI, "foo", 1.0, 2L])
    }
}
