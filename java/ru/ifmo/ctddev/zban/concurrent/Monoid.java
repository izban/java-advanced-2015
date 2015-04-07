package ru.ifmo.ctddev.zban.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

/**
 * Monoid abstraction used in {@link ru.ifmo.ctddev.zban.concurrent.IterativeParallelism}
 * <p>
 * Monoid has neytral element and binary operator.
 */
public class Monoid<T> {
    /**
     * Neytral element.
     */
    private final Optional<T> id;

    /**
     * Binary operation.
     */
    private final BinaryOperator<Optional<T>> op;

    /**
     * Make given operator able to work with Optional class.
     *
     * @param op operator to modify
     * @param <T> type of elements in monoid
     * @return new operator
     */
    private static <T> BinaryOperator<Optional<T>> getOptionalOperator(BinaryOperator<T> op) {
        return (t1, t2) -> {
            if (!t1.isPresent()) {
                return t2;
            }
            if (!t2.isPresent()) {
                return t1;
            }
            return Optional.of(op.apply(t1.get(), t2.get()));
        };
    }

    /**
     * Constructor from given binary operator. Neytral element is set as Optional.empty().
     *
     * @param op operator in monoid
     */
    Monoid(BinaryOperator<T> op) {
        id = Optional.empty();
        this.op = getOptionalOperator(op);
    }

    /**
     * Constructor from given binary operator and neytral element.
     *
     * @param op operator in monoid
     * @param id neytral element in monoid
     */
    Monoid(BinaryOperator<T> op, T id) {
        this.id = Optional.of(id);
        this.op = getOptionalOperator(op);
    }

    /**
     * Returns neytral element of given monoid.
     *
     * @return neytral element
     */
    Optional<T> getId() {
        return id;
    }

    /**
     * Apply monoid operation to arguments.
     *
     * @param t1 first argument
     * @param t2 second argument
     * @return result of operation
     */
    Optional<T> op(Optional<T> t1, Optional<T> t2) {
        return op.apply(t1, t2);
    }

    /**
     * Static method, which returns monoid with list concatination operation.
     *
     * @param <T> type of elements in list
     * @return monoid
     */
    static <T> Monoid<List<T>> monoidConcatList() {
        return new Monoid<>((l1, l2) -> {
            List<T> result = new ArrayList<>();
            result.addAll(l1);
            result.addAll(l2);
            return result;
        }, new ArrayList<>());
    }
}
