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

package org.kie.workbench.common.stunner.core.graph.processing.layout.sugiyama.step02;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.graph.processing.layout.Vertex;
import org.kie.workbench.common.stunner.core.graph.processing.layout.sugiyama.GraphLayer;
import org.kie.workbench.common.stunner.core.graph.processing.layout.sugyiama.LayeredGraph;
import org.kie.workbench.common.stunner.core.graph.processing.layout.sugyiama.step02.LongestPathVertexLayerer;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LongestPathVertexLayererTest {

    @Test
    public void simple2LayersTest() {

        final LayeredGraph graph = new LayeredGraph();
        graph.addEdge("A", "E");
        graph.addEdge("A", "G");
        graph.addEdge("A", "H");
        graph.addEdge("B", "F");
        graph.addEdge("C", "H");
        graph.addEdge("D", "H");

        final LongestPathVertexLayerer layerer = new LongestPathVertexLayerer();
        layerer.createLayers(graph);
        final List<GraphLayer> result = graph.getLayers();

        assertEquals(2, result.size());

        final GraphLayer layer01 = result.get(0);
        match(new String[]{"A", "B", "C", "D"}, layer01);

        final GraphLayer layer02 = result.get(1);
        match(new String[]{"E", "F", "G", "H"}, layer02);
    }

    @Test
    public void simple3Layers() {

        final LayeredGraph graph = new LayeredGraph();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "F");
        graph.addEdge("C", "E");
        graph.addEdge("C", "G");
        graph.addEdge("C", "H");
        graph.addEdge("D", "F");

        final LongestPathVertexLayerer layerer = new LongestPathVertexLayerer();
        layerer.createLayers(graph);
        final List<GraphLayer> result = graph.getLayers();

        assertEquals(3, result.size());

        final GraphLayer layer01 = result.get(0);
        match(new String[]{"A"}, layer01);

        final GraphLayer layer02 = result.get(1);
        match(new String[]{"D", "B", "C"}, layer02);

        final GraphLayer layer03 = result.get(2);
        match(new String[]{"E", "F", "G", "H"}, layer03);
    }

    @Test
    public void simple4Layers() {

        final LayeredGraph graph = new LayeredGraph();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "F");
        graph.addEdge("C", "E");
        graph.addEdge("C", "G");
        graph.addEdge("C", "H");
        graph.addEdge("B", "I");
        graph.addEdge("H", "I");
        graph.addEdge("G", "I");

        final LongestPathVertexLayerer layerer = new LongestPathVertexLayerer();
        layerer.createLayers(graph);
        final List<GraphLayer> result = graph.getLayers();

        assertEquals(4, result.size());

        /* We're ensuring that the default algorithm behaviour is "good enough" and is not break by some change,
         * but If we changed it to a better one we'll have to modify this test to the new better expected result.
         */
        final GraphLayer layer01 = result.get(0);
        match(new String[]{"A"}, layer01);

        final GraphLayer layer02 = result.get(1);
        match(new String[]{"C"}, layer02);

        final GraphLayer layer03 = result.get(2);
        match(new String[]{"B", "G", "H"}, layer03);

        final GraphLayer layer04 = result.get(3);
        match(new String[]{"F", "I", "E"}, layer04);
    }

    @Test
    public void singleLineLayered() {
        final LayeredGraph graph = new LayeredGraph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "D");

        final LongestPathVertexLayerer layerer = new LongestPathVertexLayerer();
        layerer.createLayers(graph);
        final List<GraphLayer> result = graph.getLayers();

        assertEquals(4, result.size()); // 4 = layered = vertical line
    }

    private static void match(final String[] expected,
                              final GraphLayer layer) {
        assertEquals("kie.wb.common.graph.layout.Layer " + layer.getLevel() + " contains " + expected.length + " vertices",
                     expected.length,
                     layer.getVertices().size());

        final String[] fromLayer = layer.getVertices().stream()
                .map(Vertex::getId)
                .distinct()
                .toArray(String[]::new);

        final boolean containsAllElements = Arrays.asList(expected)
                .containsAll(Arrays.asList(fromLayer));

        assertTrue("kie.wb.common.graph.layout.Layer " + layer.getLevel() + " contains all expected vertices",
                   containsAllElements);
    }
}