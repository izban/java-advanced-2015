package ru.ifmo.ctddev.zban.bank;

import java.rmi.*;

public interface Bank extends Remote {
    public Account createAccount(String id)
            throws RemoteException;
    public Account getAccount(String id)
            throws RemoteException;
}