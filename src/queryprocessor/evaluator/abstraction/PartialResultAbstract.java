package queryprocessor.evaluator.abstraction;

import pkb.ast.abstraction.ASTNode;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class PartialResultAbstract<KT, VT, V extends Set<?>, T extends Pair<? super KT, ? super KT>> {
    private final KT key;
    private final T keyPair;
    private final V value;

    public PartialResultAbstract(KT key, Set<? super VT> value) {
        this.key = key;
        this.value = (V) value;
        this.keyPair = null;
    }

    public PartialResultAbstract(T keyPair, Set<? extends Pair<? super VT, ? super VT>> value) {
        this.key = null;
        this.keyPair = keyPair;
        this.value = (V) value;
    }

    public boolean containsKey(KT key) {
        if(this.key != null)
            return this.key == key;

        if(keyPair != null)
            return keyPair.getFirst() == key || keyPair.getSecond() == key;

        return false;
    }

    public boolean containsKey(T pair) {
        return this.keyPair == pair;
    }

    public boolean hasSingleKey() {
        return key != null;
    }

    public boolean hasDoubleKey() {
        return keyPair != null;
    }

    public T getKeyPair() {
        return keyPair;
    }

    public KT getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public List<VT> getValue(KT key) {
        if(this.key == key)
            return new ArrayList<VT>((Collection<? extends VT>) value);

        if(this.keyPair == null)
            return null;

        int keyNo = 0;
        if(keyPair.getFirst() == key)
            keyNo = 1;
        else if (keyPair.getSecond() == key)
            keyNo = 2;

        if(keyNo == 0)
            return null;

        final int finalKeyNo = keyNo;
        try {
            Set<Pair<VT, VT>> valRef = (Set<Pair<VT, VT>>) value;
            return valRef.stream().map(p -> finalKeyNo == 1 ? p.getFirst() : p.getSecond()).collect(Collectors.toList());
        }
        catch (ClassCastException e) {
            return null;
        }
    }
}
