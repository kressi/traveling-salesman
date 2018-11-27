package ch.kressibucher.travelingSalesman;

import java.util.Arrays;

/**
 * Traveling Salesman Problem (kind of?) branch and bound
 *
 * https://www.geeksforgeeks.org/traveling-salesman-problem-using-branch-and-bound-2/
 * -> wrong calculation of curr_bound, FirstMin, SecondMin
 *
 * Further reading:
 * - https://github.com/vadimkantorov/tsp-bb/blob/master/tsp.cpp
 * - https://github.com/fkmclane/TSP/blob/master/src/Solver.java
 * - https://github.com/karepker/little-tsp/blob/master/src/tsp_solver/little/tree_node.cpp
 * - https://www.techiedelight.com/travelling-salesman-problem-using-branch-and-bound/
 * - [SOLVING THE TRAVELLING SALESMAN PROBLEM USING THE BRANCH AND BOUND METHOD](https://hrcak.srce.hr/file/236378)
 *
 */
public class BruetForceBound {

    final static int INF = Integer.MAX_VALUE;
    final static int FIRST_VERTEX = 0;
    final static int NO_VERTEX = -1;

    // final shortest route
    public int[] best_path;
    // cost of the shortest route and an upper
    // bound for finding new routes
    public int best_cost;

    // adjacancy matrix of a complete graph
    private int[][] adj;
    // length of adjacancy matrix
    private int adj_len;
    // path which currently is being calculated
    private int[] curr_path;
    // vertices already visited by curr_path
    private boolean[] visited;

    public BruetForceBound(int[][] adj) {
        this.adj = adj;
        adj_len = adj.length;
        visited = new boolean[adj_len]; 
        curr_path = new int[adj_len+1];
        Arrays.parallelSetAll(curr_path, ix -> {return NO_VERTEX;});
        best_cost = INF;
        best_path = new int[adj_len + 1];
        Arrays.parallelSetAll(best_path, ix -> {return NO_VERTEX;});

        visited[FIRST_VERTEX] = true;
        curr_path[0] = FIRST_VERTEX;
    }

    public void solve() {
        // bonud includes all edges twice. before being
        // compared, it has to be divided by two.
        int bound = 0;
        for (int i=0; i < adj_len; i++) {
            bound += sum2MinEdges(i, -1);
        }
        final int weight = 0;
        final int level = 1;
        solveRec(bound, weight, level);
    }

    /**
      * @param root_bound -> lower bound of the root node
      * @param root_cost -> stores the weight of the path so far
      * @param level -> current level while moving in the search space tree
      */
    private void solveRec(int root_bound, int root_cost, int level) {

        final int root_vertex = curr_path[level-1];

        if (level == adj_len) {
            final int first_last_edge_cost = adj[root_vertex][FIRST_VERTEX];
            final int curr_cost = root_cost + first_last_edge_cost;
            if (first_last_edge_cost != INF && curr_cost < best_cost) {
                copyCurrToBest();
                best_cost = curr_cost;
            }
        } else {

            for (int curr_vertex=0; curr_vertex < adj_len; curr_vertex++) {
                if (adj[root_vertex][curr_vertex] != INF && visited[curr_vertex] == false) {
                    final int curr_weight = root_cost + adj[root_vertex][curr_vertex];

                    int curr_bound;
                    if (level == 1) {
                        curr_bound = root_bound - sum2MinEdges(curr_vertex, -1)
                                                + costMinEdge(curr_vertex, root_vertex)
                                                - sum2MinEdges(root_vertex, -1)
                                                + costMinEdge(root_vertex, curr_vertex);
                    } else {
                        curr_bound = root_bound - sum2MinEdges(curr_vertex, -1)
                                                + costMinEdge(curr_vertex, root_vertex)
                                                - costMinEdge(root_vertex, curr_path[level-2]);
                    }

                    if (curr_bound/2 + curr_weight < best_cost) {
                        curr_path[level] = curr_vertex;
                        visited[curr_vertex] = true;
                        solveRec(curr_bound, curr_weight, level+1);
                    }
                    resetVisited(level);
                }
            }
        }
    }

    private void copyCurrToBest() {
        for (int i=0; i < adj_len; i++) {
            best_path[i] = curr_path[i];
        }
        best_path[adj_len] = FIRST_VERTEX;
    }

    /**
     * Find the minimum edge cost having an end at the vertex {@code vert}
     *
     * @param vert Vertex of which edges are considered.
     * @param visited_vert Just visited vertex.
     */
    private int costMinEdge(int vert, int visited_vert) {
        int min = INF;
        for (int j = 0; j < adj_len; j++) {
            if (adj[vert][j] < min && j != vert && j != visited_vert) {
                min = adj[vert][j];
            }
        }
        return min;
    }

    private int sum2MinEdges(int vert, int visited_vert) {
        int first = INF;
        int second = INF;
        for (int j = 0; j < adj_len; j++) {
            if (j != vert && j != visited_vert) {
                if (adj[vert][j] < first) {
                    second = first;
                    first = adj[vert][j];
                } else if (adj[vert][j] < second) {
                    second = adj[vert][j];
                }
            }
        }
        return first + second;
    }

    private void resetVisited(int level) {
        for (int j=0; j < adj_len; j++) {
            visited[j] = false;
        }
        for (int j=0; j < level; j++) {
            visited[curr_path[j]] = true;
        }
    }
}
