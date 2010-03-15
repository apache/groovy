package groovy.sql;

import java.util.List;

public class SqlWithParams {
    private String sql;
    private List<Object> params;

    public SqlWithParams(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }
}