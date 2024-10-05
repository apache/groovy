package groovy.transform.stc;

import java.util.UUID;

public class Pojo11451 {
    public void setId(String s) { System.out.print(s); }
    public void setId(UUID uid) { System.err.print(uid); }
}
