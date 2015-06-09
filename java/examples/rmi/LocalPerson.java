package examples.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Created by izban on 28.05.15.
 */
public class LocalPerson implements Person, Serializable {
    private static final long serialVersionUID = 5454435435353L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalPerson)) return false;
        LocalPerson that = (LocalPerson) o;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (secondName != null ? !secondName.equals(that.secondName) : that.secondName != null) return false;
        return !(passport != null ? !passport.equals(that.passport) : that.passport != null);
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (secondName != null ? secondName.hashCode() : 0);
        result = 31 * result + (passport != null ? passport.hashCode() : 0);
        return result;
    }

    private final String firstName;
    private final String secondName;
    private final String passport;

    LocalPerson(String firstName, String secondName, String passport) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.passport = passport;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getSecondName() throws RemoteException {
        return secondName;
    }

    @Override
    public String getPassport() throws RemoteException {
        return passport;
    }

    @Override
    public PersonType getType() throws RemoteException {
        return PersonType.LOCAL;
    }
}
