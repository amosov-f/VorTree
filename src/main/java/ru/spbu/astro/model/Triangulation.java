package ru.spbu.astro.model;

import com.google.common.primitives.Ints;

import java.util.*;

public class Triangulation extends Graph {
    protected HashSet<Simplex> simplexes = new HashSet();
    //protected HashMap<Simplex, Collection<Simplex> > side2triangles = new HashMap();


    public Collection<Integer> getBorderVertices() {
        HashSet<Integer> borderVertices = new HashSet();
        HashMap<Simplex, Integer> count = new HashMap();

        if (simplexes.size() == 0) {
            return getVertices();
        }

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
        //System.out.println("simplexes: " + t.getSimplexes().size());
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
        private int[] vertices;
        private int level;

        public Simplex(int[] vertices, int level) {
            this.vertices = vertices;
            Arrays.sort(vertices);
            this.level = level;
        }

        public Simplex(int[] vertices) {
            this(vertices, 0);
        }

        public Simplex(Collection<Integer> vertices) {
            this.vertices = Ints.toArray(vertices);
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public List<Integer> getVertices() {
            return Ints.asList(vertices);
        }

        public Graph toGraph() {
            Graph g = new Graph();
            for (int u : vertices) {
                for (int v : vertices) {
                    g.addEdge(u, v);
                }
            }
            return g;
        }

        public ArrayList<Simplex> getSides() {
            ArrayList<Simplex> sideSimplexes = new ArrayList();

            for (int i = 0; i < vertices.length; ++i) {
                List<Integer> sideVertices = new ArrayList(Ints.asList(vertices));
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
            if (!(o instanceof Simplex)) {
                return false;
            }

            Simplex simplex = (Simplex) o;

            return Arrays.equals(vertices, simplex.vertices);
        }

        @Override
        public int hashCode() {
            return vertices != null ? Arrays.hashCode(vertices) : 0;
        }

        @Override
        public String toString() {
            return "Simplex(vertices = " + Arrays.toString(vertices) + ")";
        }
    }


}
