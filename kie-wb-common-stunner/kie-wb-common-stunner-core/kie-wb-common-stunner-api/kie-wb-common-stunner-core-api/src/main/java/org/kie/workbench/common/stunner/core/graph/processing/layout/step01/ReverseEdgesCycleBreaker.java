/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.graph.processing.layout.step01;

import java.util.HashSet;

import javax.enterprise.inject.Default;

import org.kie.workbench.common.stunner.core.graph.processing.layout.Edge;
import org.kie.workbench.common.stunner.core.graph.processing.layout.ReorderedGraph;

/**
 * Break cycles in a graph reversing some edges.
 */
@Default
public final class ReverseEdgesCycleBreaker implements CycleBreaker {

    private ReorderedGraph graph;
    private final HashSet<String> visitedVertices;
    private final HashSet<Edge> reversed;

    public ReverseEdgesCycleBreaker() {
        this.reversed = new HashSet<>();
        this.visitedVertices = new HashSet<>();
    }

    /**
     * Breaks all cycles found in a cyclic graph to make it acyclic.
     * @param graph The graph.
     */
    @Override
    public void breakCycle(final ReorderedGraph graph) {
        this.graph = graph;

        for (String vertex : this.graph.getVertices()) {
            visit(vertex);
        }
    }

    /**
     * Visit a vertex searching for acyclic paths.
     * @param vertex The vertex to visit.
     * @return true if the path is acylic, false if is cyclic.
     */
    private boolean visit(final String vertex) {
        if (visitedVertices.contains(vertex)) {
            // Found a cycle.
            return false;
        }
        visitedVertices.add(vertex);

        String[] verticesFromThis = getVerticesFrom(vertex);
        for (String nextVertex : verticesFromThis) {
            if (!visit(nextVertex)) {
                Edge toReverse = this.graph.getEdges().stream().filter(edge -> edge.getFrom().equals(vertex)
                        && edge.getTo().equals(nextVertex))
                        .findFirst()
                        .orElse(null);

                if (toReverse != null) {
                    reversed.add(toReverse);
                    this.graph.getEdges().remove(toReverse);
                    Edge reversed = new Edge(toReverse.getTo(), toReverse.getFrom());
                    this.graph.getEdges().add(reversed);
                }
            }
        }

        visitedVertices.remove(vertex);
        return true;
    }

    private String[] getVerticesFrom(final String vertex) {
        final HashSet<String> verticesFrom = new HashSet<>();
        for (Edge edge : this.graph.getEdges()) {
            if (edge.getFrom().equals(vertex)) {
                verticesFrom.add(edge.getTo());
            }
        }
        return verticesFrom.toArray(new String[0]);
    }
}