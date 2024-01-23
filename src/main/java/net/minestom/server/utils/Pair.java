package net.minestom.server.utils;

public final class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    public static <T, K> Pair<T, K> of(T left, K right) {
        return new Pair<>(left, right);
    }
}
