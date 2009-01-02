package groovy.jmx.builder.vm5;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.HashMap;

/**
 * Helper class to help with RMI
 */
public class JmxConnectorHelper {
    public static Map createRmiRegistry(int initPort){
        Map <String,Object> result = new HashMap<String,Object>(2);
        int counter = 0;
        int port    = initPort;
        Registry reg = null;
        while(reg == null && counter <= 4){
            try{
                reg = LocateRegistry.createRegistry(port);
                result.put("registry", reg);
                result.put("port", new Integer(port));
                break;
            }catch(RemoteException ex){
                counter ++;
                port = port + 1;
                System.out.println ("JmxBuilder - *** FAILED *** to create RMI Registry - Will Retry on port [" + port + "].");
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {throw new RuntimeException(e);}
            }
        }
        return result;
    }

    public static void destroyRmiRegistry(Registry reg){
        try {
            if(reg != null){
                java.rmi.server.UnicastRemoteObject.unexportObject(reg, true);
            }
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }
}
