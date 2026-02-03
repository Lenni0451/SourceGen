package net.lenni0451.sourcegen.utils.remapping;

public record TinyNamespace(String from, String to) {

    public static TinyNamespace[] merge(final TinyNamespace initial, final TinyNamespace... others) {
        TinyNamespace[] namespaces = new TinyNamespace[others.length + 1];
        namespaces[0] = initial;
        System.arraycopy(others, 0, namespaces, 1, others.length);
        return namespaces;
    }

}
