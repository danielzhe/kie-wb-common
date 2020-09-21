/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.drd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.event.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagram;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.definition.model.Definitions;
import org.kie.workbench.common.dmn.api.definition.model.InputData;
import org.kie.workbench.common.dmn.api.graph.DMNDiagramUtils;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.client.commands.clone.DMNDeepCloneProcess;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramSelected;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramTuple;
import org.kie.workbench.common.dmn.client.docks.navigator.drds.DMNDiagramsSession;
import org.kie.workbench.common.dmn.client.marshaller.unmarshall.DMNUnmarshaller;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bound;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.HasContentDefinitionId;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DRDContextMenuServiceTest {

    private DRDContextMenuService drdContextMenuService;

    @Mock
    private DMNDiagramsSession dmnDiagramsSession;

    @Mock
    private FactoryManager factoryManager;

    @Mock
    private Event<DMNDiagramSelected> selectedEvent;

    @Mock
    private DMNDiagramUtils dmnDiagramUtils;

    @Mock
    private DMNDeepCloneProcess dmnDeepCloneProcess;

    @Mock
    private Graph graph;

    @Mock
    private DMNUnmarshaller dmnUnmarshaller;

    @Before
    public void setUp() {
        drdContextMenuService = spy(new DRDContextMenuService(dmnDiagramsSession,
                                                              factoryManager,
                                                              selectedEvent,
                                                              dmnDiagramUtils,
                                                              dmnDeepCloneProcess,
                                                              dmnUnmarshaller));
    }

    @Test
    public void testGetDiagrams() {
        final DMNDiagramTuple dmnDiagramTuple = mock(DMNDiagramTuple.class);
        when(dmnDiagramsSession.getDMNDiagrams()).thenReturn(Collections.singletonList(dmnDiagramTuple));

        final List<DMNDiagramTuple> diagrams = drdContextMenuService.getDiagrams();
        assertThat(diagrams).isNotEmpty();
        assertThat(diagrams).hasSize(1);
        assertThat(diagrams).contains(dmnDiagramTuple);
    }

    @Test
    public void testAddToNewDRD() {
        final Collection<Node<? extends Definition<?>, Edge>> nodes = mock(Collection.class);
        final Definitions definitions = mock(Definitions.class);
        final Diagram drgDiagram = mock(Diagram.class);
        final List<DMNDiagramElement> diagramElements = mock(List.class);
        when(dmnDiagramsSession.getDRGDiagram()).thenReturn(drgDiagram);
        when(dmnDiagramUtils.getDefinitions(drgDiagram)).thenReturn(definitions);
        when(definitions.getDiagramElements()).thenReturn(diagramElements);

        drdContextMenuService.addToNewDRD(nodes);

        verify(diagramElements, times(1)).add(any(DMNDiagramElement.class));
        verify(dmnDiagramsSession, times(1)).add(any(DMNDiagramElement.class), any(Diagram.class));
        verify(selectedEvent, times(1)).fire(any(DMNDiagramSelected.class));
    }

    @Test
    public void testAddToExistingDRD() {

        final DMNDiagramTuple diagramTuple = mockDmnDiagramTuple();
        final Diagram diagram = mock(Diagram.class);
        final Node graphNode = mock(Node.class);
        final View graphNodeDefinition = mock(View.class);
        final DMNDiagram dmnDiagram = spy(new DMNDiagram());
        final Collection<Node<? extends Definition<?>, Edge>> selectedNodes = mockNodes();
        selectedNodes.add(graphNode);

        when(diagramTuple.getStunnerDiagram()).thenReturn(diagram);
        when(diagram.getGraph()).thenReturn(graph);
        when(graph.nodes()).thenReturn(selectedNodes);
        when(graphNode.getContent()).thenReturn(graphNodeDefinition);
        when(graphNodeDefinition.getDefinition()).thenReturn(dmnDiagram);

        drdContextMenuService.addToExistingDRD(diagramTuple, selectedNodes);

        verify(graph).addNode(any(Node.class));
        verify(selectedEvent, times(1)).fire(any(DMNDiagramSelected.class));
    }

    @Test
    public void testRemoveFromCurrentDRD() {
        final String nodeUUID = "UUID";
        final Node node = mock(Node.class);
        final Diagram diagram = mock(Diagram.class);
        final DMNDiagramElement dmnDiagram = mock(DMNDiagramElement.class);
        when(node.getUUID()).thenReturn(nodeUUID);
        when(dmnDiagramsSession.getCurrentDiagram()).thenReturn(Optional.of(diagram));
        when(dmnDiagramsSession.getCurrentDMNDiagramElement()).thenReturn(Optional.of(dmnDiagram));
        when(diagram.getGraph()).thenReturn(graph);

        drdContextMenuService.removeFromCurrentDRD(singleton(node));

        verify(graph, times(1)).removeNode(nodeUUID);
        verify(selectedEvent, times(1)).fire(any(DMNDiagramSelected.class));
    }

    @Test
    public void testCloneNode() {
        final Node nodeToClone = mock(Node.class);
        final DMNDiagramElement dmnElement = mock(DMNDiagramElement.class);
        final Bounds bounds = mock(Bounds.class);
        final Optional<Bounds> optionalBounds = Optional.of(bounds);
        final View content = mock(View.class);
        final Object definition = mock(Object.class);
        final Node clonedNode = mock(Node.class);
        final View clonedContent = mock(View.class);
        final HasContentDefinitionId clonedDefinition = mock(HasContentDefinitionId.class);
        final Bounds clonedBounds = mock(Bounds.class);
        final Element clonedElement = mock(Element.class);

        when(content.getDefinition()).thenReturn(definition);
        when(nodeToClone.getContent()).thenReturn(content);
        when(clonedNode.getContent()).thenReturn(clonedContent);
        when(clonedElement.asNode()).thenReturn(clonedNode);
        when(factoryManager.newElement(any(), any())).thenReturn(clonedElement);

        doReturn(clonedDefinition).when(drdContextMenuService).cloneDefinition(dmnElement, definition);
        doReturn(clonedBounds).when(drdContextMenuService).getBounds(optionalBounds, content);

        final Node result = drdContextMenuService.cloneNode(nodeToClone, dmnElement, optionalBounds);

        verify(clonedContent).setDefinition(clonedDefinition);
        verify(clonedContent).setBounds(clonedBounds);
        assertEquals(clonedNode, result);
    }

    @Test
    public void testGetBoundsWhenBoundsIsNotPresent() {

        final View content = mock(View.class);
        final Bounds bounds = mock(Bounds.class);
        final Bounds clonedBounds = mock(Bounds.class);

        when(content.getBounds()).thenReturn(bounds);
        doReturn(clonedBounds).when(drdContextMenuService).cloneBounds(bounds);

        final Bounds result = drdContextMenuService.getBounds(Optional.empty(), content);

        verify(drdContextMenuService).cloneBounds(bounds);
        assertEquals(clonedBounds, result);
    }

    @Test
    public void testGetBoundsWhenBoundsIsPresent() {

        final View content = mock(View.class);
        final Bounds bounds = mock(Bounds.class);
        final Bounds existingBounds = mock(Bounds.class);

        when(content.getBounds()).thenReturn(bounds);

        final Bounds result = drdContextMenuService.getBounds(Optional.of(existingBounds), content);

        verify(drdContextMenuService, never()).cloneBounds(bounds);
        assertEquals(existingBounds, result);
    }

    @Test
    public void testCloneBounds() {

        final double x1 = 100;
        final double y1 = 150;
        final double x2 = 377;
        final double y2 = 455;

        final Bounds bounds = Bounds.create(x1, y1, x2, y2);
        final Bounds cloned = drdContextMenuService.cloneBounds(bounds);

        assertEquals(x1, cloned.getUpperLeft().getX(), 0.001d);
        assertEquals(y1, cloned.getUpperLeft().getY(), 0.001d);
        assertEquals(x2, cloned.getLowerRight().getX(), 0.001d);
        assertEquals(y2, cloned.getLowerRight().getY(), 0.001d);
    }

    private Collection<Node<? extends Definition<?>, Edge>> mockNodes() {

        final Node node = mock(Node.class);
        final Node clonedNode = mock(Node.class);
        final View content = mock(View.class);
        final View clonedContent = mock(View.class);
        final Bounds bounds = mock(Bounds.class);
        final Bound upperLeft = mock(Bound.class);
        final Bound lowerRight = mock(Bound.class);
        final InputData inputData = spy(new InputData());
        final InputData clonedInputData = spy(new InputData());
        final Collection<Node<? extends Definition<?>, Edge>> nodes = new ArrayList<>();

        when(node.getContent()).thenReturn(content);
        when(content.getDefinition()).thenReturn(inputData);
        when(content.getBounds()).thenReturn(bounds);
        when(bounds.getUpperLeft()).thenReturn(upperLeft);
        when(bounds.getLowerRight()).thenReturn(lowerRight);
        when(factoryManager.newElement(anyString(), anyString())).thenReturn(clonedNode);
        when(clonedNode.asNode()).thenReturn(clonedNode);
        when(clonedNode.getContent()).thenReturn(clonedContent);
        when(clonedContent.getDefinition()).thenReturn(clonedInputData);
        when(dmnDeepCloneProcess.clone(eq(inputData))).thenReturn(clonedInputData);

        nodes.add(node);
        return nodes;
    }

    private DMNDiagramTuple mockDmnDiagramTuple() {
        final DMNDiagramTuple dmnDiagramTuple = mock(DMNDiagramTuple.class);
        final Diagram diagram = mock(Diagram.class);
        final DMNDiagramElement dmnDiagram = mock(DMNDiagramElement.class);
        final Id diagramId = new Id("DIAGRAM_ID");
        when(dmnDiagramTuple.getStunnerDiagram()).thenReturn(diagram);
        when(dmnDiagramTuple.getDMNDiagram()).thenReturn(dmnDiagram);
        when(diagram.getGraph()).thenReturn(graph);
        when(dmnDiagram.getId()).thenReturn(diagramId);
        return dmnDiagramTuple;
    }
}
