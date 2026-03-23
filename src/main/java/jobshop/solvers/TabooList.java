package jobshop.solvers;

import jobshop.solvers.neighborhood.Nowicki;

import java.util.Arrays;

public class TabooList {

    final Nowicki.Swap[] taboo;
    // where to insert the next element
    int next = 0;

    public TabooList(int size) {
        taboo = new Nowicki.Swap[size];
    }

    public boolean contains(Nowicki.Swap elem) {
        return Arrays.stream(taboo).anyMatch(elem::equals);
    }

    public void insert(Nowicki.Swap elem) {
        taboo[next] = elem;
        next = (next + 1) % taboo.length;
    }
}
