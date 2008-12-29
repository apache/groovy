package groovy

class SliceTest extends GroovyTestCase {

    void testListSlice() {
        def list = [1, 2, 3, 4]
        list[1,2] = 5

        assert list == [1, 5, 5, 4]

        list[0,1] = [5, 6]

        assert list == [5, 6, 5, 4]

    }
}
