class Crasher {
    public void m() {
        def fields = [1,2,3]
        def expectedFieldNames = ["patentnumber", "status"].
                for (int i=0; i<fields.size(); i++) {
                    Object f = fields[i]
                    System.out.println(f);
                }
    }
}