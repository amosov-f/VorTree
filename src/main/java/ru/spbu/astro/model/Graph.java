package ru.spbu.astro.model;

import java.io.Serializable;
import java.util.*;

public class Graph implements Iterable<Graph.Edge>, Cloneable, Serializable {
    protected HashMap<Integer, Set<Integer>> neighbors = new HashMap();

    public Graph() {
    }

    public Graph(Graph g) {
        this.neighbors = new HashMap(g.neighbors);
    }

    public void addVertex(int u) {
        if (!containsVertex(u)) {
            neighbors.put(u, new HashSet());
        }
    }

    public void addVertices(Iterable<Integer> vertices) {
        for (int u : vertices) {
            addVertex(u);
        }
    }

    public void addEdge(int u, int v) {
        addVertex(u);
        addVertex(v);
        if (u != v) {
            neighbors.get(u).add(v);
            neighbors.get(v).add(u);
        }
    }

    public void addEdge(Edge e) {
        addEdge(e.first, e.second);
    }

    public void addGraph(Graph g) {
        for (Edge edge : g) {
            addEdge(edge);
        }
    }

    public void removeVertex(int u) {
        if (containsVertex(u)) {
            neighbors.remove(u);
        }
    }

    public void removeEdge(int u, int v) {
        if (u != v) {
            if (containsVertex(u)) {
                neighbors.get(u).remove(v);
            }
            if (containsVertex(v)) {
                neighbors.get(v).remove(u);
            }
        }
    }

    public void removeEdge(Edge e) {
        removeEdge(e.first, e.second);
    }

    public void removeGraph(Graph g) {
        for (Edge e : g) {
            removeEdge(e);
        }
    }

    public boolean containsVertex(int u) {
        return neighbors.containsKey(u);
    }

    public boolean containsEdge(int u, int v) {
        return containsVertex(u) && containsVertex(v) && neighbors.get(u).contains(v) && neighbors.get(v).contains(u);
    }

    public boolean containsEdge(Edge edge) {
        return containsEdge(edge.getFirst(), edge.getSecond());
    }

    public boolean containsGraph(final Graph g) {
        for (Edge edge : g) {
            if (!neighbors.containsKey(edge.getFirst())) {
                return false;
            }
            if (!neighbors.get(edge.getFirst()).contains(edge.getSecond())) {
                return false;
            }
        }
        return true;
    }

    public Set<Integer> getVertices() {
        return neighbors.keySet();
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList();
        for (Map.Entry<Integer, Set<Integer>> entry : neighbors.entrySet()) {
            int u = entry.getKey();
            for (int v : entry.getValue()) {
                edges.add(new Edge(u, v));
            }
        }

        return edges;
    }

    public int size() {
        return neighbors.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    protected Object clone() {
        Graph g;
        try {
            g = (Graph) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }

        g.neighbors = (HashMap) neighbors.clone();

        return g;
    }

    @Override
    public Iterator<Edge> iterator() {
        return getEdges().iterator();
    }

    @Override
    public String toString() {
        String str = String.valueOf(size()) + "\n";
        for (Edge e : this) {
            str += e.toString() + "\n";
        }
        return str + "\n";
    }

    public static class Edge {
        private final int first;
        private final int second;

        public Edge(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return first + " " + second;
        }
    }
}
