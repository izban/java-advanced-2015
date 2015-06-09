package examples.rmi;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountImpl extends UnicastRemoteObject implements Account {
    private final String id;
    private AtomicInteger amount;

    public AccountImpl(String id) throws RemoteException {
        super();
        this.id = id;
        this.amount = new AtomicInteger(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountImpl account = (AccountImpl) o;

        if (id != null ? !id.equals(account.id) : account.id != null) return false;
        return !(amount != null ? !amount.equals(account.amount) : account.amount != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        return result;
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount.intValue();
    }

    public void setAmount(int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount.set(amount);
    }

    @Override
    public int addAmount(int amount) throws RemoteException {
        System.out.println("Adding amount of money for account " + id);
        return this.amount.addAndGet(amount);
    }
}
