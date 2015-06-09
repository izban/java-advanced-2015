package examples.rmi;

import java.rmi.*;
import java.util.List;

public interface Bank extends Remote {
    void createAccountIfAbsent(Person person, String account)
        throws RemoteException;
    Person searchPersonByPassport(String passport, PersonType type)
        throws RemoteException;
    List<Account> getAccounts(Person person)
        throws RemoteException;
    Account getAccount(Person person, String accountID)
        throws RemoteException;
    Integer getBalance(Person person, String account)
        throws RemoteException;
}
