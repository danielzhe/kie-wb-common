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

package org.kie.workbench.common.dmn.client.docks.navigator.drds;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.definition.model.DRGElement;
import org.kie.workbench.common.dmn.api.definition.model.Definitions;
import org.kie.workbench.common.dmn.api.definition.model.TextAnnotation;
import org.kie.workbench.common.dmn.client.docks.navigator.SelectedDMNDiagramElementEvent;
import org.kie.workbench.common.dmn.client.graph.DMNGraphUtils;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresCanvasView;
import org.kie.workbench.common.stunner.client.widgets.canvas.ScrollableLienzoPanel;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasView;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandlerImpl;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeView;
import org.kie.workbench.common.stunner.core.diagram.SelectedDiagramProvider;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;

@ApplicationScoped
public class DMNDiagramElementSwitcher implements SelectedDiagramProvider {

    private final DMNGraphUtils dmnGraphUtils;

    private final Event<SelectedDMNDiagramElementEvent> drdSelectedEvent;

    private DMNDiagramElement currentDMNDiagramElement;

    @Inject
    public DMNDiagramElementSwitcher(final DMNGraphUtils dmnGraphUtils,
                                     final Event<SelectedDMNDiagramElementEvent> drdSelectedEvent) {
        this.dmnGraphUtils = dmnGraphUtils;
        this.drdSelectedEvent = drdSelectedEvent;
    }

    public List<DMNDiagramElement> getDMNDiagramElements() {
        final Definitions definitions = dmnGraphUtils.getDefinitions();
        return definitions.getDmnDiagramElements();
    }

    public Optional<DMNDiagramElement> getCurrentDMNDiagramElement() {
        return Optional.ofNullable(currentDMNDiagramElement);
    }

    public void switchTo(final DMNDiagramElement dmnDiagramElement) {

        final String diagramId = dmnDiagramElement.getId().getValue();
        final AbstractCanvas canvas = getAbstractCanvas();
        final WiresCanvasView view = (WiresCanvasView) canvas.getView();

        setCurrentDMNDiagramElement(dmnDiagramElement);
        getGraphNodes().forEach(node -> {
            getDiagramId(node).ifPresent(nodeDiagramId -> {

                if (Objects.equals(nodeDiagramId, diagramId)) {

                    getShapeView(canvas, node.getUUID()).ifPresent(s -> s.setAlpha(1));
                    getInEdges(node).forEach(edge -> getShapeView(canvas, edge.getUUID()).ifPresent(s -> s.setAlpha(1)));
                    getOutEdges(node).forEach(edge -> getShapeView(canvas, edge.getUUID()).ifPresent(s -> s.setAlpha(1)));
                } else {

                    getShapeView(canvas, node.getUUID()).ifPresent(s -> s.setAlpha(0));
                    getInEdges(node).forEach(edge -> getShapeView(canvas, edge.getUUID()).ifPresent(s -> s.setAlpha(0)));
                    getOutEdges(node).forEach(edge -> getShapeView(canvas, edge.getUUID()).ifPresent(s -> s.setAlpha(0)));
                }
            });
        });

        refreshCanvas(view);
    }

    private void setCurrentDMNDiagramElement(final DMNDiagramElement dmnDiagramElement) {
        currentDMNDiagramElement = dmnDiagramElement;
        drdSelectedEvent.fire(new SelectedDMNDiagramElementEvent(dmnDiagramElement));
    }

    private void refreshCanvas(final AbstractCanvasView abstractCanvasView) {
        final WiresCanvasView view = (WiresCanvasView) abstractCanvasView;
        final ScrollableLienzoPanel scrollableLienzoPanel = (ScrollableLienzoPanel) view.getLienzoPanel();
        scrollableLienzoPanel.refresh();
    }

    private Optional<ShapeView> getShapeView(final AbstractCanvas canvas,
                                             final String uuid) {
        return Optional
                .ofNullable(canvas.getShape(uuid))
                .map(Shape::getShapeView);
    }

    private List<Node> getGraphNodes() {
        return dmnGraphUtils
                .getNodeStream()
                .collect(Collectors.toList());
    }

    private AbstractCanvas getAbstractCanvas() {
        final CanvasHandlerImpl canvasHandler = (CanvasHandlerImpl) dmnGraphUtils.getCanvasHandler();
        return canvasHandler.getCanvas();
    }

    private Optional<String> getDiagramId(final Node node) {
        final Object content = node.getContent();
        if (content instanceof Definition) {
            final Object definition = ((Definition) content).getDefinition();

            if (definition instanceof DRGElement) {
                final DRGElement drgElement = (DRGElement) definition;
                return Optional.of(drgElement.getDiagramId());
            }

            if (definition instanceof TextAnnotation) {
                final TextAnnotation textAnnotation = (TextAnnotation) definition;
                return Optional.of(textAnnotation.getDMNDiagramId());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private List<Edge> getInEdges(final Node node) {
        return (List<Edge>) node.getInEdges();
    }

    @SuppressWarnings("unchecked")
    private List<Edge> getOutEdges(final Node node) {
        return (List<Edge>) node.getOutEdges();
    }

    @Override
    public boolean isGlobalGraph() {
        return getCurrentDMNDiagramElement().isPresent() &&
                Objects.equals(getCurrentDMNDiagramElement().get().getName().getValue(), "DRG");
    }

    @Override
    public String getSelectedDiagramId() {
        if (getCurrentDMNDiagramElement().isPresent()) {
            return getCurrentDMNDiagramElement().get().getId().getValue();
        }
        return null;
    }
}
