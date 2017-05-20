/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.nsc.interval.thermocompensation.optim;

import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
class Basis {

    private final BitSet basisV;
    private final Set<Integer> basisC;
    final int Vcard;
    final int[] index;

    Basis(BitSet basisV, Set<Integer> basisC) {
        this.basisV = new BitSet();
        this.basisV.or(basisV);
        this.basisC = new TreeSet<>(basisC);
        Vcard = basisV.cardinality();
        index = new int[basisV.cardinality() + basisC.size()];
        int k = 0;
        for (int v = basisV.length(); (v = basisV.previousSetBit(v - 1)) >= 0;) {
            index[k++] = -v - 1;
        }
        for (int c : basisC) {
            index[k++] = c;
        }
        assert k == index.length;
        check();
    }

    Basis(Basis old, int rem, int add) {
        basisV = new BitSet();
        basisV.or(old.basisV);
        basisC = new TreeSet<>(old.basisC);
        int card = old.Vcard;
        if (rem >= 0) {
            boolean ok = basisC.remove(rem);
            assert ok;
        } else {
            int v = -rem - 1;
            assert basisV.get(v);
            basisV.clear(v);
            card--;
        }
        if (add >= 0) {
            boolean ok = basisC.add(add);
            assert ok;
        } else {
            int v = -add - 1;
            assert !basisV.get(v);
            basisV.set(v);
            card++;
        }
        Vcard = card;
        index = old.index.clone();
        if (rem > add) {
            int k = index.length - 1;
            while (index[k] > rem) {
                k--;
            }
            while (k > 0 && index[k - 1] > add) {
                index[k] = index[k - 1];
                k--;
            }
            index[k] = add;
        } else if (rem < add) {
            int k = 0;
            while (index[k] < rem) {
                k++;
            }
            while (k < index.length - 1 && index[k + 1] < add) {
                index[k] = index[k + 1];
                k++;
            }
            index[k] = add;
        }
        check();
    }

    private void check() {
        for (int k = 1; k < index.length; k++) {
            assert index[k - 1] < index[k];
        }
        BitSet bv = new BitSet();
        TreeSet<Integer> bc = new TreeSet<>();
        for (int v : index) {
            if (v >= 0) {
                bc.add(v);
            } else {
                bv.set(-v - 1);
            }
        }
        assert bv.equals(basisV);
        assert bc.equals(basisC);
        assert Vcard == basisV.cardinality();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Basis) {
            Basis that = (Basis) o;
            return this.basisV.equals(that.basisV) && this.basisC.equals(that.basisC);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + basisV.hashCode();
        hash = 97 * hash + basisC.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return basisV + "  " + basisC;
    }

}
