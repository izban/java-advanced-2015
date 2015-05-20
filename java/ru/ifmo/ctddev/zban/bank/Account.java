package ru.ifmo.ctddev.zban.bank;

import java.rmi.*;

public interface Account extends Remote {
    public String getId()
            throws RemoteException;

    public int getAmount()
            throws RemoteException;

    public void setAmount(int amount)
            throws RemoteException;
}