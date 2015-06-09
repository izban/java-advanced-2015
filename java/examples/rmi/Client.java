package examples.rmi;

import java.rmi.*;
import java.net.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.function.Consumer;

public class Client {
    private static final String USAGE = "Usage: FirstName SecondName Passport Account MoneyAdded";

    public static void main(String[] args) throws RemoteException {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(s -> s == null)) {
            System.err.println(USAGE);
            return;
        }
        try {
            Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
            return;
        }

        String firstName = args[0];
        String secondName = args[1];
        String passport = args[2];
        String accountID = args[3];
        int added = Integer.parseInt(args[4]);

        // can be changed
        PersonType type = PersonType.REMOTE;

        Bank bank;
        try {
            bank = (Bank) Naming.lookup("rmi://localhost/bank");
        } catch (NotBoundException e) {
            System.out.println("Bank URL is invalid");
            return;
        } catch (MalformedURLException e) {
            System.out.println("Bank is not bound");
            return;
        }

        Person person;
        if (type.equals(PersonType.LOCAL)) {
            person = new LocalPerson(firstName, secondName, passport);
        } else {
            person = new RemotePerson(firstName, secondName, passport);
        }

        bank.createAccountIfAbsent(person, accountID);
        Account account = bank.getAccount(person, accountID);
        System.out.println("Balance before update: " + account.getAmount());
        account.addAmount(added);
        System.out.println("Balance before update: " + account.getAmount());

        if (type.equals(PersonType.REMOTE) && !UnicastRemoteObject.unexportObject(person, true)) {
            System.err.println("failed to unexport");
        }
    }
}
