package groovy.bugs

class SingleEvalTest extends GroovyTestCase {
    int getArrayCount
    int getIndexCount
    int getValueCount

    def array
    def list

    void setUp() {
        getArrayCount = 0
        getIndexCount = 0
        getValueCount = 0
        array = [*100..104] as int[]
        list = [*100..104]
    }

    int[] getArray() {
        getArrayCount += 1
        return array
    }

    List getList() {
        getArrayCount += 1
        return list
    }

    int getIndex() {
        getIndexCount += 1
        return 3
    }

    int getValue() {
        getValueCount += 1
        return 42
    }

    int doArrayAssignment() {
        return getArray () [ getIndex() ] = getValue()
    }

    void testSingleEvalForArrayAssignment() {
        int foo = doArrayAssignment()
        assert 1 == getArrayCount  // Fails 1.0, 1.5.0, 1.5.1
        assert 1 == getIndexCount
        assert 1 == getValueCount
        assert getValue() == getArray () [ getIndex() ]
        assert 42 == foo
        assert 2 == getArrayCount
        assert 2 == getIndexCount
        assert 2 == getValueCount
        assert 3 == getIndex()
        assert 42 == getValue()
        assert [100, 101, 102, 42, 104] == getArray()
    }

    void testSingleEvalForArrayAssignment2() {
        int foo = (getArray () [ getIndex() ] = 42)
        assert 1 == getArrayCount
        assert 1 == getIndexCount
        assert 42 == getArray () [ getIndex() ]
        assert 42 == foo  // Fails 1.0
        assert 2 == getArrayCount
        assert 2 == getIndexCount
        assert 3 == getIndex()
        assert 42 == getValue()
        assert [100, 101, 102, 42, 104] == getArray()  // Fails 1.0
        assert 42 == (getArray () [ getIndex() ] = 42)
    }

    void testSingleEvalForListAssignment() {
        assert 42 == (getList () [ getIndex() ] = getValue() )  // Fails 1.0, 1.5.0, 1.5.1, 1.5.2
        assert 1 == getArrayCount  // Fails 1.0, 1.5.0, 1.5.1
        assert 1 == getIndexCount
        assert 1 == getValueCount
        assert getValue() == getList () [ getIndex() ]
        assert 2 == getArrayCount
        assert 2 == getIndexCount
        assert 2 == getValueCount
        assert 3 == getIndex()
        assert 42 == getValue()
        assert [100, 101, 102, 42, 104] == getList()
    }

    void testSingleEvalForListAssignmentInClosure() {
        assert 42 == { getList () [ getIndex() ] = getValue() } .call()   // Fails 1.5.2
        assert 1 == getArrayCount  // Fails 1.0, 1.5.0, 1.5.1
        assert 1 == getIndexCount
        assert 1 == getValueCount
        assert getValue() == getList () [ getIndex() ]
        assert 2 == getArrayCount
        assert 2 == getIndexCount
        assert 2 == getValueCount
        assert 3 == getIndex()
        assert 42 == getValue()
        assert [100, 101, 102, 42, 104] == getList()
    }

}
