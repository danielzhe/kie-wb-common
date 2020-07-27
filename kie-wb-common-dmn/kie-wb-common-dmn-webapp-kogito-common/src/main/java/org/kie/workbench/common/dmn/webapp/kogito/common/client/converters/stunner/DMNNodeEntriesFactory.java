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

package org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.stunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import jsinterop.base.Js;
import org.kie.workbench.common.dmn.api.definition.HasComponentWidths;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.DMNMarshallerImportsHelperKogito;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.di.JSIDiagramElement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITArtifact;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITAssociation;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDRGElement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITImport;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITTextAnnotation;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNDiagram;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNEdge;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNShape;

@Dependent
public class DMNNodeEntriesFactory {

    private final StunnerNodeFactory nodeFactory;

    private final NodeConnector nodeConnector;

    private final DMNMarshallerImportsHelperKogito dmnMarshallerImportsHelper;

    @Inject
    public DMNNodeEntriesFactory(final StunnerNodeFactory nodeFactory,
                                 final NodeConnector nodeConnector,
                                 final DMNMarshallerImportsHelperKogito dmnMarshallerImportsHelper) {
        this.nodeFactory = nodeFactory;
        this.nodeConnector = nodeConnector;
        this.dmnMarshallerImportsHelper = dmnMarshallerImportsHelper;
    }

    public List<NodeEntry> makeNodes(final JSITDefinitions definitions,
                                     final Map<JSITImport, JSITDefinitions> importDefinitions,
                                     final BiConsumer<String, HasComponentWidths> componentWidthsConsumer) {

        final List<NodeEntry> nodeEntries = entriesBuilder()
                .withShapesByDiagramId(getShapesByDiagramId(definitions))
                .withDRGElements(getDRGElements(definitions))
                .withIncludedDRGElements(getIncludedDRGElements(importDefinitions))
                .withTextAnnotations(getTextAnnotations(definitions))
                .withComponentWidthsConsumer(componentWidthsConsumer)
                .buildEntries();

//        final List<JSIDMNDiagram> dmnDiagrams = definitions.getDMNDI().getDMNDiagram();

//        forEach(dmnDiagrams,
//                dmnDiagram -> {
//
//                    final List<JSIDMNEdge> edges = getEdges(dmnDiagram);
//                    final List<JSITAssociation> associations = getAssociations(definitions);
//                    final List<NodeEntry> nodes = nodeEntries.stream().filter(n -> Objects.equals(n.getDiagramId(), dmnDiagram.getId())).collect(Collectors.toList());
//
//                    nodeConnector.connect(edges, associations, nodes);
//                });

        return nodeEntries;
    }

    private NodeEntriesBuilder entriesBuilder() {
        return new NodeEntriesBuilder(nodeFactory);
    }

    private List<JSIDMNEdge> getEdges(final JSIDMNDiagram dmnDiagram) {

        final List<JSIDMNEdge> edges = new ArrayList<>();
        final List<JSIDiagramElement> jsiDiagramElements = dmnDiagram.getDMNDiagramElement();

        for (int j = 0; j < jsiDiagramElements.size(); j++) {
            final JSIDiagramElement jsiDiagramElement = Js.uncheckedCast(jsiDiagramElements.get(j));
            if (JSIDMNEdge.instanceOf(jsiDiagramElement)) {
                final JSIDMNEdge jsiEdge = Js.uncheckedCast(jsiDiagramElement);
                edges.add(jsiEdge);
            }
        }

        return edges;
    }

    private List<JSITAssociation> getAssociations(final JSITDefinitions definitions) {
        final List<JSITAssociation> associations = new ArrayList<>();
//        final List<JSITArtifact> jsiArtifacts = definitions.getArtifact();
//
//        for (int i = 0; i < jsiArtifacts.size(); i++) {
//            final JSITArtifact jsiArtifact = Js.uncheckedCast(associations.get(i));
//            if (JSITAssociation.instanceOf(jsiArtifact)) {
//                final JSITAssociation jsiAssociation = Js.uncheckedCast(jsiArtifact);
//                associations.add(jsiAssociation);
//            }
//        }
        return associations;
    }

    private Map<JSIDMNShape, String> getShapesByDiagramId(final JSITDefinitions definitions) {

        final Map<JSIDMNShape, String> dmnShapesByDiagramId = new HashMap<>();
        final List<JSIDMNDiagram> diagrams = definitions.getDMNDI().getDMNDiagram();

        for (int i = 0, size = diagrams.size(); i < size; i++) {
            final JSIDMNDiagram dmnDiagram = Js.uncheckedCast(diagrams.get(i));
            final String diagramId = dmnDiagram.getId();
            final List<JSIDiagramElement> diagramElements = dmnDiagram.getDMNDiagramElement();

            for (int j = 0; j < diagramElements.size(); j++) {
                final JSIDiagramElement jsiDiagramElement = Js.uncheckedCast(diagramElements.get(j));

                if (JSIDMNShape.instanceOf(jsiDiagramElement)) {
                    final JSIDMNShape shape = Js.uncheckedCast(jsiDiagramElement);
                    dmnShapesByDiagramId.put(shape, diagramId);
                }
            }
        }

        return dmnShapesByDiagramId;
    }

    private List<JSITDRGElement> getDRGElements(final JSITDefinitions definitions) {
        return definitions.getDrgElement();
    }

    private List<JSITDRGElement> getIncludedDRGElements(final Map<JSITImport, JSITDefinitions> importDefinitions) {
        return dmnMarshallerImportsHelper.getImportedDRGElements(importDefinitions);
    }

    private List<JSITTextAnnotation> getTextAnnotations(final JSITDefinitions definitions) {

        final List<JSITTextAnnotation> textAnnotations = new ArrayList<>();
        final List<JSITArtifact> artifacts = definitions.getArtifact();

        for (int i = 0; i < artifacts.size(); i++) {
            final JSITArtifact jsiArtifact = Js.uncheckedCast(artifacts.get(i));
            if (JSITTextAnnotation.instanceOf(jsiArtifact)) {
                final JSITTextAnnotation jsiTextAnnotation = Js.uncheckedCast(jsiArtifact);
                textAnnotations.add(jsiTextAnnotation);
            }
        }

        return textAnnotations;
    }
}
