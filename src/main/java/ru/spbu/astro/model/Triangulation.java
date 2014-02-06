package ru.spbu.astro.model;

import com.google.common.primitives.Ints;

import java.util.*;

public class Triangulation extends Graph {
    protected HashSet<Simplex> simplexes = new HashSet();

    public Collection<Integer> getBorderVertices() {
        if (simplexes.size() == 0) {
            return getVertices();
        }

        HashSet<Integer> borderVertices = new HashSet();
        HashMap<Simplex, Integer> count = new HashMap();

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

    public HashSet<Simplex> getSimplexes() {
        return simplexes;
    }

    public void addTriangulation(Triangulation t) {
        addGraph(t);
        addSimplexes(t.getSimplexes());
    }

    public void addSimplex(Simplex s) {
        simplexes.add(s);
    }

    public final void addSimplexes(Iterable<Simplex> simplexes) {
        for (Simplex s : simplexes) {
            addSimplex(s);
        }
    }

    @Override
    public Object clone() {
        Triangulation graph = (Triangulation) super.clone();
        graph.simplexes = (HashSet) simplexes.clone();
        return graph;
    }

    public static class Simplex {
        private ArrayList<Integer> vertices;
        private int level;

        public Simplex(Collection<Integer> vertices) {
            this(vertices, 0);
        }

        public Simplex(Collection<Integer> vertices, int level) {
            this.vertices = new ArrayList(vertices);
            Collections.sort(this.vertices);
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public ArrayList<Integer> getVertices() {
            return (ArrayList) vertices.clone();
        }

        public Graph toGraph() {
            Graph g = new Graph();
            for (int i = 0; i < vertices.size(); ++i) {
                for (int j = i + 1; j < vertices.size(); ++j) {
                    g.addEdge(vertices.get(i), vertices.get(j));
                }
            }
            return g;
        }

        public ArrayList<Simplex> getSides() {
            ArrayList<Simplex> sideSimplexes = new ArrayList();

            for (int i = 0; i < vertices.size(); ++i) {
                ArrayList<Integer> sideVertices = (ArrayList) vertices.clone();
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

            Simplex simplex = (Simplex) o;

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
    }


}
