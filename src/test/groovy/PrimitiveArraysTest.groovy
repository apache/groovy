class PrimitiveArraysTest extends GroovyTestCase {

    c1Field = new char[]{}
    char[] c2Field = new char[]{}
    
    i1Field = new int[]{}
    int[] i2Field = new int[]{}
    

    d1Field = new double[]{}
    double[] d2Field = new double[]{}

    f1Field = new float[]{}
    float[] f2Field = new float[]{}
    
    l1Field = new long[]{}
    long[] l2Field = new long[]{}    


    b1Field = new byte[]{}
    byte[] b2Field = new byte[]{}
    
    
    s1Field = new short[]{}
    short[] s2Field = new short[]{}
    
    void testChar() {
        assert c1Field.class == c2Field.class
        ca = new char[]{'l','l'}
        char[] cb = new char[]{'l','l'}
        assert ca.class == cb.class
        assert c1Field.class == ca.class
        assert ca.class.name == "[C"
        
        ca.each{ assert it=='l' }
        cb.each{ assert it=='l' }
    }


    void testInt() {
        assert i1Field.class == i2Field.class
        ia = new int[]{1,1}
        int[] ib = new int[]{1,1}
        assert ia.class == ib.class
        assert i1Field.class == ia.class
        assert ia.class.name == "[I"
        
        ia.each{ assert it==1 }
        ib.each{ assert it==1 }
    }


    void testLong() {
        assert l1Field.class == l2Field.class
        la = new long[]{1,1}
        long[] lb = new long[]{1,1}
        assert la.class == lb.class
        assert l1Field.class == la.class
        assert la.class.name == "[J"
        
        la.each{ assert it==1l }
        lb.each{ assert it==1l }
    }


    void testShort() {
        assert s1Field.class == s2Field.class
        sa = new short[]{1,1} 
        short[] sb = new short[]{1,1}
        assert sa.class == sb.class
        assert s1Field.class == sa.class
        assert sa.class.name == "[S"
        
        sa.each{ assert it==1 }
        sb.each{ assert it==1 }
    }

    void testByte() {
        assert b1Field.class == b2Field.class
        ba = new byte[]{1,1}
        byte[] bb = new byte[]{1,1}
        assert ba.class == bb.class
        assert b1Field.class == ba.class
        assert ba.class.name == "[B"
        
        ba.each{ assert it==1 }
        bb.each{ assert it==1 }
    }
    
    
    void testDouble() {
        assert d1Field.class == d2Field.class
        da = new double[]{1,1}
        double[] db = new double[]{1,1}
        assert da.class == db.class
        assert d1Field.class == da.class
        assert da.class.name == "[D"
        
        da.each{ assert it==1.0d }
        db.each{ assert it==1.0d }
    }

    void testFloat() {
        assert f1Field.class == f2Field.class
        fa = new float[]{1,1}
        float[] fb = new float[]{1,1}
        assert fa.class == fb.class
        assert f1Field.class == fa.class
        assert fa.class.name == "[F"
        
        fa.each{ assert it==1.0f }
        fb.each{ assert it==1.0f }
    }

}