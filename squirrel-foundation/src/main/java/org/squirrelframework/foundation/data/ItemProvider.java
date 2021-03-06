package org.squirrelframework.foundation.data;

public interface ItemProvider<M extends HierarchyItem<M, N>, N> {
    void createChildren(M parent);
}
