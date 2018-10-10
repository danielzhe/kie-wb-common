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
package org.kie.workbench.common.stunner.core.graph.processing.layout;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step01.ReverseEdgesCycleBreaker;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step02.LongestPathVertexLayerer;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step03.DefaultVertexOrdering;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step03.LayerCrossingCount;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step03.MedianVertexLayerPositioning;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step03.VerticesTransposer;
import org.kie.workbench.common.stunner.core.graph.processing.layout.step04.DefaultVertexPositioning;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationTests {

    @Test
    public void testRealCase1() {
        ReorderedGraph graph = new ReorderedGraph(Graphs.RealCase1);

        ReverseEdgesCycleBreaker s01 = new ReverseEdgesCycleBreaker();
        s01.breakCycle(graph);
        LongestPathVertexLayerer s02 = new LongestPathVertexLayerer();
        s02.createLayers(graph);

        final MedianVertexLayerPositioning vertexPositioning = new MedianVertexLayerPositioning();
        final LayerCrossingCount crossingCount = new LayerCrossingCount();
        final VerticesTransposer verticesTransposer = new VerticesTransposer(crossingCount);

        DefaultVertexOrdering s03 = new DefaultVertexOrdering(vertexPositioning,
                                                              crossingCount,
                                                              verticesTransposer);
        s03.orderVertices(graph);

        Assert.assertEquals(6, graph.getLayers().size());

        DefaultVertexPositioning defaultVertexPositioning = new DefaultVertexPositioning();
        defaultVertexPositioning.calculateVerticesPositions(graph,
                                                            DefaultVertexPositioning.LayerArrangement.TopDown);
    }
}