package me.ramazanenescik04.diken.tools;

/**
 * Defines the `ListAdapter` type within the DikenEngine `tools` package.
 */
public interface ListAdapter<E> {
    void onAdd(E item);      // Eleman eklendiğinde
    void onRemove(E item);   // Eleman silindiğinde
    void onUpdate();         // Sıralama veya toplu değişim olduğunda
    void onClear();          // Liste tamamen boşaltıldığında
}
