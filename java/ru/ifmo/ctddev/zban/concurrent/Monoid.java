package ru.ifmo.ctddev.zban.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

/**
 * Created by izban on 07.04.15.
 */
public class Monoid<T> {
    private final Optional<T> id;
    private final BinaryOperator<Optional<T>> op;

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

    Monoid(BinaryOperator<T> op) {
        id = Optional.empty();
        this.op = getOptionalOperator(op);
    }

    Monoid(BinaryOperator<T> op, T id) {
        this.id = Optional.of(id);
        this.op = getOptionalOperator(op);
    }

    Optional<T> getId() {
        return id;
    }

    Optional<T> op(Optional<T> t1, Optional<T> t2) {
        return op.apply(t1, t2);
    }

    static <T> Monoid<List<T>> monoidConcatList() {
        return new Monoid<>((l1, l2) -> {
            List<T> result = new ArrayList<>();
            result.addAll(l1);
            result.addAll(l2);
            return result;
        }, new ArrayList<>());
    }
}
