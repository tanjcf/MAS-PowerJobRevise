package com.github.kfcfans.powerjob.server.common.utils.user;

/**
 * 用于封住四元素数组
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 */
public class TowTuple<A,B,C,D> {
    public final A first;
    public final B second;
    public final C third;
    public final D four;
    public TowTuple(A a, B b, C c, D d) {
        this.first = a;
        this.second = b;
        this.third = c;
        this.four=d;
    }

    public D getFour() {
        return four;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }
}
