import java.awt.BasicStroke

class A<T, K> {
    {
        new String()
        new ArrayList<T>()
        new HashMap<>()
        new HashMap<Integer, List<?>>()
        new HashMap<Integer, List<? extends Serializable>>()

        new ArrayList<?>()
        new ArrayList<? super Integer>()
        new A<Integer, List<? super Integer>>()
        new A<Integer, List<? extends Boolean>>(1, 2)
        new A<Integer, Map<?, Boolean>>(1, 2, [])

        new int[1]
        new int[1][2]
        new int[1][2][3]
    }
}
