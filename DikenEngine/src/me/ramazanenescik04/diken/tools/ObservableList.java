package me.ramazanenescik04.diken.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.UnaryOperator;

/**
 * Represents the `ObservableList` type within the DikenEngine `tools` package.
 */
public class ObservableList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 8387720031395531503L;
    
    // ListAdapter'ın bir List interface'i olduğunu varsayarak:
    private transient ListAdapter<E> onActionCallback;

    public void setListAdapter(ListAdapter<E> callback) {
        this.onActionCallback = callback;
    }

    // --- EKLEME İŞLEMLERİ ---

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result && onActionCallback != null) onActionCallback.onAdd(e);
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        if (onActionCallback != null) onActionCallback.onAdd(element);
    }

    @Override
    public void addFirst(E element) {
        super.addFirst(element);
        if (onActionCallback != null) onActionCallback.onAdd(element);
    }

    @Override
    public void addLast(E element) {
        super.addLast(element);
        if (onActionCallback != null) onActionCallback.onAdd(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = super.addAll(c);
        if (result && onActionCallback != null) {
            for (E item : c) onActionCallback.onAdd(item);
        }
        return result;
    }

    // --- SİLME İŞLEMLERİ ---

    @Override
    public E remove(int index) {
        E removed = super.remove(index);
        if (onActionCallback != null) onActionCallback.onRemove(removed);
        return removed;
    }

    @SuppressWarnings("unchecked")
	@Override
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed && onActionCallback != null) onActionCallback.onRemove((E) o);
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        if (onActionCallback != null) onActionCallback.onClear();
    }

    // --- GÜNCELLEME VE SIRALAMA ---

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        if (onActionCallback != null) onActionCallback.onUpdate();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        super.replaceAll(operator);
        if (onActionCallback != null) onActionCallback.onUpdate();
    }
}
