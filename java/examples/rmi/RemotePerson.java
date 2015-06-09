package examples.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by izban on 28.05.15.
 */
public class RemotePerson extends UnicastRemoteObject implements Person {
    private final LocalPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemotePerson)) return false;
        RemotePerson that = (RemotePerson) o;
        return !(person != null ? !person.equals(that.person) : that.person != null);
    }

    @Override
    public int hashCode() {
        return (person != null ? person.hashCode() : 0);
    }

    RemotePerson(String firstName, String secondName, String passport) throws RemoteException {
        super();
        this.person = new LocalPerson(firstName, secondName, passport);
    }

    @Override
    public String getFirstName() throws RemoteException {
        return person.getFirstName();
    }

    @Override
    public String getSecondName() throws RemoteException {
        return person.getSecondName();
    }

    @Override
    public String getPassport() throws RemoteException {
        return person.getPassport();
    }

    @Override
    public PersonType getType() throws RemoteException {
        return PersonType.REMOTE;
    }
}
