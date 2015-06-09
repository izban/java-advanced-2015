package examples.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by izban on 28.05.15.
 */
public interface Person extends Remote {
    String getFirstName() throws RemoteException;
    String getSecondName() throws RemoteException;
    String getPassport() throws RemoteException;
    PersonType getType() throws RemoteException;
}
