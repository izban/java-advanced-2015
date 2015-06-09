package examples.rmi;

import java.rmi.*;

public interface Account extends Remote {
    String getId()
        throws RemoteException;

    int getAmount()
        throws RemoteException;

    void setAmount(int amount)
        throws RemoteException;

    int addAmount(int amount)
        throws RemoteException;
}
