package examples.rmi;

import java.util.*;
import java.rmi.server.*;
import java.rmi.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BankImpl extends UnicastRemoteObject implements Bank {
    ConcurrentHashMap<Person, ConcurrentHashMap<String, Account>> accounts = new ConcurrentHashMap<>();
    private final int port;

    public BankImpl(final int port) throws RemoteException {
        super();
        this.port = port;
    }

    Person copyPerson(Person person) throws RemoteException {
        if (person == null) {
            return null;
        }
        Person currentPerson;
        if (person.getType().equals(PersonType.LOCAL)) {
            currentPerson = new LocalPerson(person.getFirstName(), person.getSecondName(), person.getPassport());
        } else {
            currentPerson = new RemotePerson(person.getFirstName(), person.getSecondName(), person.getPassport());
        }
        return currentPerson;
    }

    @Override
    public void createAccountIfAbsent(Person person, String account) throws RemoteException {
        Person currentPerson = copyPerson(person);
        accounts.putIfAbsent(currentPerson, new ConcurrentHashMap<>());
        accounts.get(currentPerson).putIfAbsent(account, new AccountImpl(account));
    }

    @Override
    public Person searchPersonByPassport(String passport, PersonType type) throws RemoteException {
        final Person[] result = {null};
        final RemoteException[] exception = {null};
        accounts.forEach((person, stringAccountConcurrentHashMap) -> {
            try {
                if (person.getPassport().equals(passport)) {
                    result[0] = person;
                }
            } catch (RemoteException e) {
                exception[0] = e;
            }
        });
        if (exception[0] != null) {
            throw exception[0];
        }
        return result[0];
    }

    @Override
    public List<Account> getAccounts(Person person) throws RemoteException {
        Person currentPerson = copyPerson(person);
        if (currentPerson == null || !accounts.containsKey(currentPerson)) {
            return null;
        }
        return new ArrayList<>(accounts.get(currentPerson).values());
    }

    @Override
    public Account getAccount(Person person, String accountID) throws RemoteException {
        Person currentPerson = copyPerson(person);
        if (currentPerson == null || !accounts.containsKey(currentPerson)) {
            return null;
        }
        return accounts.get(currentPerson).get(accountID);
    }

    @Override
    public Integer getBalance(Person person, String account) throws RemoteException {
        Person currentPerson = copyPerson(person);
        if (currentPerson == null || account == null || !accounts.containsKey(currentPerson)) {
            return null;
        }
        return accounts.get(currentPerson).get(account).getAmount();
    }
}
