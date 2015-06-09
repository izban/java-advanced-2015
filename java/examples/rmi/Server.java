package examples.rmi;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public class Server {
    private final static int PORT = 8888;
    public static void main(String[] args) {
        Bank bank;
        try {
            bank = new BankImpl(PORT);
            Naming.rebind("rmi://localhost/bank", bank);
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
