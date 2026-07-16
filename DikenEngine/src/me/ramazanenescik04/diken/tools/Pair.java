package me.ramazanenescik04.diken.tools;

import java.util.Objects;

public class Pair<A, B> {
	public A first;
	public B second;
	
	public Pair(A a, B b) {
		this.first = a;
		this.second = b;
	}
	
	public String toString() {
        return "Pair[" + first + "," + second + "]";
    }

    public boolean equals(Object other) {
        return other instanceof Pair<?,?> pair &&
            Objects.equals(first, pair.first) &&
            Objects.equals(second, pair.second);
    }

    public int hashCode() {
        if (first == null) return (second == null) ? 0 : second.hashCode() + 1;
        else if (first == null) return second.hashCode() + 2;
        else return first.hashCode() * 17 + second.hashCode();
    }

    public static <A,B> Pair<A,B> of(A a, B b) {
        return new Pair<>(a,b);
    }
}