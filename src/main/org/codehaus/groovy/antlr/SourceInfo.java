package org.codehaus.groovy.antlr;

public interface SourceInfo {
    /**
     * get start line
     *
     * @return the starting line
     */
    int getLine();

    /**
     * set start line
     *
     * @param l the line
     */
    void setLine(int l);

    /**
     * get starting column
     *
     * @return the starting column
     */
    int getColumn();

    /**
     * set start column
     *
     * @param c the column
     */
    void setColumn(int c);

    /**
     * get ending line
     *
     * @return the ending line
     */
    int getLineLast();

    /**
     * set ending line
     *
     * @param lineLast the line
     */
    void setLineLast(int lineLast);

    /**
     * get ending column
     *
     * @return the ending column
     */
    int getColumnLast();

    /**
     * set ending column
     *
     * @param colLast the column
     */
    void setColumnLast(int colLast);
}
