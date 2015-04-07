package ru.ifmo.ctddev.zban.implementor;


import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class is wrapper on {@link java.lang.reflect.Method} class.
 * <p>
 * It overrides equals and hashCode methods to be able to be put in {@link java.util.HashSet}.
 *
 * @author izban
 * @see java.lang.reflect.Method
 */
public class MyMethod {
    /**
     * Method to restore.
     */
    private final Method method;

    /**
     * Parameters of method.
     */
    private final Class[] parameters;

    /**
     * Name of method.
     */
    private final String name;

    /**
     * Constructor from {@link java.lang.reflect.Method}.
     *
     * @param method Method to wrap.
     */
    public MyMethod(Method method) {
        this.method = method;
        this.parameters = method.getParameterTypes();
        this.name = method.getName();
    }

    /**
     * Return {@link java.lang.reflect.Method} value,
     *
     * @return unwrapped Method,
     */
    public Method toMethod() {
        return method;
    }

    /**
     * Checks for equality. Return true if arguments and name of methods are equal.
     *
     * @param o Object to compare.
     * @return true or false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        if (name == null) {
            return ((MyMethod)o).name == null;
        }
        if (!name.equals(((MyMethod)o).name)) {
            return false;
        }
        return Arrays.equals(parameters, ((MyMethod)o).parameters);
    }

    /**
     * HashCode of this object.
     *
     * @return hashCode.
     */
    @Override
    public int hashCode() {
        int result = 0;
        if (parameters != null) {
            result = Arrays.hashCode(parameters);
        }
        result *= 239;
        if (name != null) {
            result += name.hashCode();
        }
        return result;
    }

    /**
     * String representation.
     *
     * @return String representation of MyMethod.
     */
    @Override
    public String toString() {
        return "{MyMethod: " + method + "}";
    }
}