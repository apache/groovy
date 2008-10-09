package org.codehaus.groovy.antlr;

public interface SourceInfo {
    /**
     * get start line
     * @return the starting line
     */
    public int getLine();
    /**
     * set start line
     * @param l the line
     */
    public void setLine(int l);
    /**
     * get starting column
     * @return the starting column
     */
    public int getColumn();
    /**
     * set start column
     * @param c the column
     */
    public void setColumn(int c);
    /**
     * get ending line
     * @return the ending line
     */
    public int getLineLast();
    /**
     * set ending line
     * @param lineLast the line
     */
    public void setLineLast(int lineLast);
    /**
     * get ending column
     * @return the ending column
     */
    public int getColumnLast();
    /**
     * set ending column
     * @param colLast the column
     */
    public void setColumnLast(int colLast);
}
