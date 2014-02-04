package ru.spbu.astro.model;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;

import java.util.*;

public class Graph implements Iterable<Graph.Edge> {
    protected Map<Integer, Set<Integer>> neighbors = new HashMap();

    @Override
    public Iterator<Edge> iterator() {
        return getEdges().iterator();
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
    }

    public void addVertex(int u) {
        if (!containsVertex(u)) {
            neighbors.put(u, new HashSet());
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

    public void addGraph(final Graph g) {
        if (g == null) {
            return;
        }

        for (Edge edge : g) {
            if (!neighbors.containsKey(edge.getFirst())) {
                neighbors.put(edge.getFirst(), new HashSet());
            }
            neighbors.get(edge.getFirst()).add(edge.getSecond());
        }
    }

    public void removeEdge(int u, int v) {
        if (u != v) {
            if (neighbors.containsKey(u)) {
                neighbors.get(u).remove(v);
            }
            if (neighbors.containsKey(v)) {
                neighbors.get(v).remove(u);
            }
        }
    }

    public void removeGraph(final Graph g) {
        for (Edge edge : g) {
            if (neighbors.containsKey(edge.getFirst())) {
                neighbors.get(edge.getFirst()).remove(edge.getSecond());
            }
        }
    }

    public boolean containsEdge(int u, int v) {
        if (!neighbors.containsKey(u) || !neighbors.containsKey(v)) {
            return false;
        }
        if (!neighbors.get(u).contains(v) || !neighbors.get(v).contains(u)) {
            return false;
        }
        return true;
    }

    public boolean containsEdge(Edge edge) {
        return containsEdge(edge.getFirst(), edge.getSecond());
    }

    public boolean containsVertex(int u) {
        return neighbors.containsKey(u);
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

    public Collection<Integer> getVertices() {
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

}
