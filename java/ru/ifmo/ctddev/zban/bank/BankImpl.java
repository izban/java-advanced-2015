package ru.ifmo.ctddev.zban.bank;

import java.util.*;
import java.rmi.server.*;
import java.rmi.*;

public class BankImpl implements Bank {
    private final Map<String, Account> accounts = new HashMap<String, Account>();
    private final int port;

    public BankImpl(final int port) {
        this.port = port;
    }

    public Account createAccount(String id) throws RemoteException {
        Account account = new AccountImpl(id);
        accounts.put(id, account);
        UnicastRemoteObject.exportObject(account, port);
        return account;
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }
}