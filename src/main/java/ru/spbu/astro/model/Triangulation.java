package ru.spbu.astro.model;

import ru.spbu.astro.Message;

import java.util.*;

public class Triangulation extends Graph {

    private final Set<Simplex> simplexes;

    protected Triangulation() {
        simplexes = new HashSet<>();
    }

    public Triangulation(final Triangulation t) {
        super(t);
        simplexes = new HashSet<>(t.simplexes);
    }

    protected Triangulation(final Message.Triangulation message) {
        super(message.getGraph());

        simplexes = new HashSet<>();
        for (Message.Simplex simplex : message.getSimplexesList()) {
            simplexes.add(new Simplex(simplex));
        }
    }

    public Set<Integer> getBorderVertices() {
        if (simplexes.isEmpty()) {
            return getVertices();
        }

        final Set<Integer> borderVertices = new HashSet<>();
        final Map<Simplex, Integer> count = new HashMap<>();

        for (Simplex s : simplexes) {
            for (Simplex side : s.getSides()) {
                if (!count.containsKey(side)) {
                    count.put(side, 0);
                }
                count.put(side, count.get(side) + 1);
            }
        }

        for (Map.Entry<Simplex, Integer> entry : count.entrySet()) {
            if (entry.getValue() == 1) {
                borderVertices.addAll(entry.getKey().getVertices());
            }
        }

        return borderVertices;
    }

    public final Set<Simplex> getSimplexes() {
        return simplexes;
    }

    public final void addTriangulation(final Triangulation t) {
        addGraph(t);
        addSimplexes(t.simplexes);
    }

    public void addSimplex(final Simplex s) {
        addGraph(s.toGraph());
        simplexes.add(s);
    }

    public Graph removeSimplex(final Simplex s) {
        removeGraph(s.toGraph());
        simplexes.remove(s);
        return s.toGraph();
    }

    public final void addSimplexes(final Iterable<Simplex> simplexes) {
        for (Simplex s : simplexes) {
            addSimplex(s);
        }
    }

    public Message.Triangulation toTriangulationMessage() {
        final Message.Triangulation.Builder builder = Message.Triangulation.newBuilder();
        builder.setGraph(super.toMessage());
        for (final Simplex s : getSimplexes()) {
            builder.addSimplexes(s.toMessage());
        }
        return builder.build();
    }

    public static final class Simplex implements Iterable<Integer> {

        private final List<Integer> vertices;

        public Simplex(final Collection<Integer> vertices) {
            this.vertices = new ArrayList<>(vertices);
            Collections.sort(this.vertices);
        }

        public Simplex(final Message.Simplex message) {
            this(message.getVerticesList());
        }

        public List<Integer> getVertices() {
            return vertices;
        }

        public Graph toGraph() {
            final Graph g = new Graph();
            for (int i = 0; i < vertices.size(); ++i) {
                for (int j = i + 1; j < vertices.size(); ++j) {
                    g.addEdge(vertices.get(i), vertices.get(j));
                }
            }
            return g;
        }

        public List<Simplex> getSides() {
            final List<Simplex> sideSimplexes = new ArrayList<>();

            for (int i = 0; i < vertices.size(); ++i) {
                final List<Integer> sideVertices = new ArrayList<>(vertices);
                sideVertices.remove(i);
                sideSimplexes.add(new Simplex(sideVertices));
            }

            return sideSimplexes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Simplex simplex = (Simplex) o;

            return vertices.equals(simplex.vertices);
        }

        @Override
        public int hashCode() {
            return vertices.hashCode();
        }

        @Override
        public String toString() {
            return "Simplex(" + vertices + ")";
        }

        @Override
        public Iterator<Integer> iterator() {
            return vertices.iterator();
        }

        public Message.Simplex toMessage() {
            final Message.Simplex.Builder builder = Message.Simplex.newBuilder();
            builder.addAllVertices(vertices);
            return builder.build();
        }

    }

}
