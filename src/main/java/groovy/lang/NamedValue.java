package groovy.lang;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.FormatHelper;

import java.util.Map;
import java.util.Objects;

@Incubating
public class NamedValue<T> {
    private final String name;
    private final T val;

    public String getName() {
        return name;
    }

    public T getVal() {
        return val;
    }

    public NamedValue(String name, T val) {
        this.name = name;
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValue<?> that = (NamedValue<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(val, that.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, val);
    }

    @Override
    public String toString() {
        return name + "=" + FormatHelper.format(val, true, false, -1, true);
    }

    public String toString(Map<String, Object> options) {
        return name + "=" + FormatHelper.toString(options, val);
    }
}
