package groovy.runtime.typehandling;

public class EqualityTestAbstractClass implements EqualityTestInterface {

    private final int id;
    private final String value;

    public EqualityTestAbstractClass(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(EqualityTestInterface other) {
        if (other == null) {
            return 1;
        }
        if (getValue() == null) {
            if (other.getValue() == null) {
                return 0;
            }
            return -1;
        }
        if (other.getValue() == null) {
            return 1;
        }
        return getValue().compareTo(other.getValue());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof EqualityTestInterface) {
            EqualityTestInterface castedOther = (EqualityTestInterface) other;
            return getId() == castedOther.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId();
    }

}
