/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.client.docks.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagramElement;
import org.kie.workbench.common.dmn.api.definition.model.DRGElement;
import org.kie.workbench.common.dmn.api.definition.model.Definitions;
import org.kie.workbench.common.dmn.client.graph.DMNGraphUtils;
import org.kie.workbench.common.dmn.client.session.MultiDRDControl;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasView;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandlerImpl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.actions.TextPropertyProvider;
import org.kie.workbench.common.stunner.core.client.canvas.controls.actions.TextPropertyProviderFactory;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.svg.client.shape.view.impl.SVGShapeViewImpl;

public class GraphDRDSwitchPOC {

    @Inject
    private DMNGraphUtils dmnGraphUtils;

    @Inject
    private TextPropertyProviderFactory textPropertyProviderFactory;

    private List<DRDNode> repositoryOfNodes = new ArrayList<>();

    MultiDRDControl multiDRDControl;

    public void setMultiDRDControl(final MultiDRDControl multiDRDControl) {
        this.multiDRDControl = multiDRDControl;
    }

    public void show(final Integer i) {
        final List<DRDNode> globalRepositoryOfNodes = getGlobalRepositoryOfNodes();
        DomGlobal.console.log("[MULTIPLE DRDS - WORK IN PROGRESS] " + globalRepositoryOfNodes.size() + " = Showing the DRD " + (i == 0 ? "global" : i));

        globalRepositoryOfNodes.forEach((node) -> {

            try {
                if (node.name.equals("ROOT") || i == 0 || node.name.endsWith(i.toString())) {
                    DomGlobal.console.log("[MULTIPLE DRDS - WORK IN PROGRESS] ==> KEEPING: ", node.name);
                    add(node);
                } else {
                    DomGlobal.console.log("[MULTIPLE DRDS - WORK IN PROGRESS] ==> REMOVING: ", node.name);
                    remove(node);
                }
            } catch (Exception e) {
                DomGlobal.console.log("[MULTIPLE DRDS - WORK IN PROGRESS] ==> ERROR: ", e);
            }
        });
    }

    public List<DMNDiagramElement> getDRDs() {
        final Definitions definitions = dmnGraphUtils.getDefinitions();
        return definitions.getDmnDiagramElements();
    }

    public void clear() {
        dmnGraphUtils.getCanvasHandler().clear();
    }

    private void remove(final DRDNode node) {
        final CanvasHandlerImpl canvasHandler = (CanvasHandlerImpl) dmnGraphUtils.getCanvasHandler();
        final AbstractCanvas canvas = canvasHandler.getCanvas();
        final AbstractCanvasView view = (AbstractCanvasView) canvas.getView();

        final SVGShapeViewImpl shapeView = (SVGShapeViewImpl) node.shape.getShapeView();
        shapeView.setAlpha(0);
        view.onResize();
    }

    private void add(final DRDNode node) {
        final CanvasHandlerImpl canvasHandler = (CanvasHandlerImpl) dmnGraphUtils.getCanvasHandler();
        final AbstractCanvas canvas = canvasHandler.getCanvas();
        final AbstractCanvasView view = (AbstractCanvasView) canvas.getView();

        node.shape.getShapeView().setAlpha(1);
        view.onResize();
    }

    String getName(final Element<? extends Definition> element) {
        final TextPropertyProvider provider = textPropertyProviderFactory.getProvider(element);
        return provider.getText(element);
    }

    public List<DRDNode> getGlobalRepositoryOfNodes() {
        if (repositoryOfNodes.isEmpty()) {
            final CanvasHandlerImpl canvasHandler = (CanvasHandlerImpl) dmnGraphUtils.getCanvasHandler();
            final AbstractCanvas canvas = canvasHandler.getCanvas();

            getGraphNodes().forEach(e -> {
                final DRDNode drdNode = new DRDNode();
                drdNode.node = e;
                drdNode.uuid = e.getUUID();
                drdNode.name = getName(e);
                drdNode.content = e.getContent();
                drdNode.shape = canvas.getShape(e.getUUID());
                repositoryOfNodes.add(drdNode);
            });
        }
        return repositoryOfNodes;
    }

    public List<Node> getGraphNodes() {
        return dmnGraphUtils.getNodeStream().collect(Collectors.toList());
    }

    public void show(final DMNDiagramElement drd) {
        final String drdId = drd.getId().getValue();
        final Stream<Node> nodeStream = dmnGraphUtils.getNodeStream();

        nodeStream.forEach((Node node) -> {

            final CanvasHandlerImpl canvasHandler = (CanvasHandlerImpl) dmnGraphUtils.getCanvasHandler();
            final AbstractCanvas canvas = canvasHandler.getCanvas();
            final AbstractCanvasView view = (AbstractCanvasView) canvas.getView();

            final Object content = node.getContent();
            if (content instanceof Definition) {
                final Object definition = ((Definition) content).getDefinition();
                if (definition instanceof DRGElement) {

                    final DRGElement drgElement = (DRGElement) definition;
                    final String dmnDiagramId = drgElement.getDmnDiagramId();
                    final Shape shape = canvas.getShape(node.getUUID());

//                    final List<Shape> inEdges = ((List<Edge>) node.getInEdges()).stream().map(e -> canvas.getShape(e.getUUID())).filter(Objects::nonNull).collect(Collectors.toList());
//                    final List<Shape> outEdges = ((List<Edge>) node.getOutEdges()).stream().map(e -> canvas.getShape(e.getUUID())).filter(Objects::nonNull).collect(Collectors.toList());

                    DomGlobal.console.log("[MULTIPLE DRDS] >>> ", drdId, dmnDiagramId, " = NODE: ", node.getUUID());
                    if (Objects.equals(dmnDiagramId, drdId)) {
                        DomGlobal.console.log(dmnDiagramId, "[MULTIPLE DRDS] SHOW > ", drgElement.getName().getValue());
//                        DomGlobal.console.log("[MULTIPLE DRDS] ==> show " + drgElement.getName().getValue() + "(" + inEdges.size() + ", " + outEdges.size() + ")");
                        shape.getShapeView().setAlpha(1);
//                        inEdges.forEach(e -> e.getShapeView().setAlpha(1));
//                        outEdges.forEach(e -> e.getShapeView().setAlpha(1));
                    } else {
                        DomGlobal.console.log(dmnDiagramId, "[MULTIPLE DRDS] HIDE > ", drgElement.getName().getValue());
//                        DomGlobal.console.log("[MULTIPLE DRDS] ==> hide " + drgElement.getName().getValue() + "(" + inEdges.size() + ", " + outEdges.size() + ")");
                        shape.getShapeView().setAlpha(0);
//                        inEdges.forEach(e -> e.getShapeView().setAlpha(0));
//                        outEdges.forEach(e -> e.getShapeView().setAlpha(0));
                    }
                }
            }
            view.onResize();
        });
    }

    class DRDNode {

        Object content;
        Node node;
        Shape shape;
        String uuid;
        String name;
    }
}
