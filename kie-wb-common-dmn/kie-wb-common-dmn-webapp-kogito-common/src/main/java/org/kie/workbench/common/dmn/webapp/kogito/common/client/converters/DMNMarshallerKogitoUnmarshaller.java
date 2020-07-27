/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.dmn.webapp.kogito.common.client.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.kie.workbench.common.dmn.api.DMNDefinitionSet;
import org.kie.workbench.common.dmn.api.definition.HasComponentWidths;
import org.kie.workbench.common.dmn.api.definition.model.DMNDiagram;
import org.kie.workbench.common.dmn.api.definition.model.DMNModelInstrumentedBase;
import org.kie.workbench.common.dmn.api.definition.model.Definitions;
import org.kie.workbench.common.dmn.api.definition.model.ItemDefinition;
import org.kie.workbench.common.dmn.api.editors.included.PMMLDocumentMetadata;
import org.kie.workbench.common.dmn.client.docks.navigator.GraphDRDSwitchPOC;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.DefinitionsConverter;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.ItemDefinitionPropertyConverter;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.stunner.DMNNodeEntriesFactory;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.stunner.NodeEntry;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.utils.DMNDiagramElementsUtils;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.utils.DMNMarshallerUtils;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.MainJs;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.di.JSIDiagramElement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDMNElementReference;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDecisionService;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITImport;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITItemDefinition;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNDiagram;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.kie.JSITComponentsWidthsExtension;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JsUtils;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.util.UUID;
import org.kie.workbench.common.stunner.forms.client.event.FormFieldChanged;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.uberfire.client.promise.Promises;

@ApplicationScoped
public class DMNMarshallerKogitoUnmarshaller {

    private final FactoryManager factoryManager;
    private final DMNMarshallerImportsHelperKogito dmnMarshallerImportsHelper;

    private Promises promises;

    private final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;
    private final SessionManager sessionManager;
    private final GraphDRDSwitchPOC graphDRDSwitchPOC;
    private final DMNNodeEntriesFactory modelToStunnerConverter;
    private final DMNDiagramElementsUtils dmnDiagramElementsUtils;

    protected DMNMarshallerKogitoUnmarshaller() {
        this(null, null, null, null, null, null, null, null);
    }

    @Inject
    public DMNMarshallerKogitoUnmarshaller(final FactoryManager factoryManager,
                                           final DMNMarshallerImportsHelperKogito dmnMarshallerImportsHelper,
                                           final Promises promises,
                                           final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                                           final SessionManager sessionManager,
                                           final GraphDRDSwitchPOC graphDRDSwitchPOC,
                                           final DMNNodeEntriesFactory modelToStunnerConverter,
                                           final DMNDiagramElementsUtils dmnDiagramElementsUtils) {
        this.factoryManager = factoryManager;
        this.dmnMarshallerImportsHelper = dmnMarshallerImportsHelper;
        this.promises = promises;
        this.refreshFormPropertiesEvent = refreshFormPropertiesEvent;
        this.sessionManager = sessionManager;
        this.graphDRDSwitchPOC = graphDRDSwitchPOC;
        this.modelToStunnerConverter = modelToStunnerConverter;
        this.dmnDiagramElementsUtils = dmnDiagramElementsUtils;
    }

    @PostConstruct
    public void init() {
        MainJs.initializeJsInteropConstructors(MainJs.getConstructorsMap());
    }

    // ==================================
    // UNMARSHALL
    // ==================================

    @SuppressWarnings("unchecked")
    public Promise<Graph> unmarshall(final Metadata metadata,
                                     final JSITDefinitions jsiDefinitions) {

        return getImportDefinitions(metadata, jsiDefinitions)
                .then(importDefinitions -> unmarshall(metadata,
                                                      jsiDefinitions,
                                                      importDefinitions));
    }

    private Promise<Map<JSITImport, JSITDefinitions>> getImportDefinitions(final Metadata metadata,
                                                                           final JSITDefinitions jsiDefinitions) {
        final List<JSITImport> imports = jsiDefinitions.getImport();
        return dmnMarshallerImportsHelper.getImportDefinitionsAsync(metadata, imports);
    }

    Promise<Graph> unmarshall(final Metadata metadata,
                              final JSITDefinitions dmnDefinitions,
                              final Map<JSITImport, JSITDefinitions> importDefinitions) {

        final Map<String, HasComponentWidths> hasComponentWidthsMap = new HashMap<>();
        final BiConsumer<String, HasComponentWidths> hasComponentWidthsConsumer = (uuid, hcw) -> {
            if (Objects.nonNull(uuid)) {
                hasComponentWidthsMap.put(uuid, hcw);
            }
        };

        // Get external PMML model information
        final Map<JSITImport, PMMLDocumentMetadata> pmmlDocuments = getPmmlDocuments(metadata, dmnDefinitions);

        // TODO: Create DRG.

        dmnDiagramElementsUtils.ensureDRGElementPresence(dmnDefinitions);

        final Definitions wbDefinitions = DefinitionsConverter.wbFromDMN(dmnDefinitions, importDefinitions, pmmlDocuments);
        final List<NodeEntry> nodeEntries = modelToStunnerConverter.makeNodes(dmnDefinitions, importDefinitions, hasComponentWidthsConsumer);
        final List<JSITDecisionService> dmnDecisionServices = getDecisionServices(nodeEntries);
//        final Map<String, NodeEntry> entriesById = makeNodeIndex(nodeEntries);

        final Diagram diagram = factoryManager.newDiagram("prova",
                                                          BindableAdapterUtils.getDefinitionSetId(DMNDefinitionSet.class),
                                                          metadata);
        final Graph graph = diagram.getGraph();

        nodeEntries.forEach(nodeEntry -> graph.addNode(nodeEntry.getNode()));

        final Node<?, ?> dmnDiagramRoot = DMNMarshallerUtils.findDMNDiagramRoot(graph);

        loadImportedItemDefinitions(wbDefinitions, importDefinitions);

        ((View<DMNDiagram>) dmnDiagramRoot.getContent()).getDefinition().setDefinitions(wbDefinitions);

        //Only connect Nodes to the Diagram that are not referenced by DecisionServices
        final List<String> references = new ArrayList<>();
        final List<JSITDecisionService> lstDecisionServices = new ArrayList<>(dmnDecisionServices);
        for (int iDS = 0; iDS < lstDecisionServices.size(); iDS++) {
            final JSITDecisionService jsiDecisionService = Js.uncheckedCast(lstDecisionServices.get(iDS));
            final List<JSITDMNElementReference> jsiEncapsulatedDecisions = jsiDecisionService.getEncapsulatedDecision();
            if (Objects.nonNull(jsiEncapsulatedDecisions)) {
                for (int i = 0; i < jsiEncapsulatedDecisions.size(); i++) {
                    final JSITDMNElementReference jsiEncapsulatedDecision = Js.uncheckedCast(jsiEncapsulatedDecisions.get(i));
                    references.add(jsiEncapsulatedDecision.getHref());
                }
            }

            final List<JSITDMNElementReference> jsiOutputDecisions = jsiDecisionService.getOutputDecision();
            if (Objects.nonNull(jsiOutputDecisions)) {
                for (int i = 0; i < jsiOutputDecisions.size(); i++) {
                    final JSITDMNElementReference jsiOutputDecision = Js.uncheckedCast(jsiOutputDecisions.get(i));
                    references.add(jsiOutputDecision.getHref());
                }
            }
        }

//        final Map<JSITDRGElement, Node> elementsToConnectToRoot = new HashMap<>();
//        for (NodeEntry nodeEntry : entriesById.values()) {
//            final JSITDRGElement element = Js.uncheckedCast(nodeEntry.getDmnElement());
//
//            if (JSITTextAnnotation.instanceOf(element)) {
//                continue;
//            }
//
//            if (!references.contains("#" + element.getId())) {
//                elementsToConnectToRoot.put(element, nodeEntry.getNode());
//            }
//        }
//        entriesById.values().forEach(nodeEntry -> connectRootWithChild(dmnDiagramRoot, nodeEntry.getNode()));

        nodeEntries.forEach(ne -> {
            connectRootWithChild(dmnDiagramRoot, ne.getNode());
        });

        //Copy ComponentWidths information
        final List<JSITComponentsWidthsExtension> extensions = findComponentsWidthsExtensions(dmnDefinitions.getDMNDI().getDMNDiagram());

//        extensions.forEach(componentsWidthsExtension -> {
//            //This condition is required because a node with ComponentsWidthsExtension
//            //can be imported from another diagram but the extension is not imported or present in this diagram.
//            if (Objects.nonNull(componentsWidthsExtension.getComponentWidths())) {
//                hasComponentWidthsMap.entrySet().forEach(es -> {
//                    final List<JSITComponentWidths> jsiComponentWidths = componentsWidthsExtension.getComponentWidths();
//                    for (int i = 0; i < jsiComponentWidths.size(); i++) {
//                        final JSITComponentWidths jsiWidths = Js.uncheckedCast(jsiComponentWidths.get(i));
//                        if (Objects.equals(jsiWidths.getDmnElementRef(), es.getKey())) {
//                            final List<Double> widths = es.getValue().getComponentWidths();
//                            if (Objects.nonNull(jsiWidths.getWidth())) {
//                                widths.clear();
//                                for (int w = 0; w < jsiWidths.getWidth().size(); w++) {
//                                    final double width = jsiWidths.getWidth().get(w).doubleValue();
//                                    widths.add(width);
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//        });

        return promises.resolve(graph);
    }

    private List<JSITDecisionService> getDecisionServices(final List<NodeEntry> nodeEntries) {
        return nodeEntries
                .stream()
                .filter(nodeEntry -> JSITDecisionService.instanceOf(nodeEntry.getDmnElement()))
                .map(nodeEntry -> (JSITDecisionService) Js.uncheckedCast(nodeEntry.getDmnElement()))
                .collect(Collectors.toList());
    }

    public void onBla(@Observes FormFieldChanged formFieldChanged) {
        DomGlobal.console.log("OBSERVEDss " + formFieldChanged.getClass().getSimpleName());
        refreshFormPropertiesEvent.fire(new RefreshFormPropertiesEvent(sessionManager.getCurrentSession(), formFieldChanged.getUuid()));
    }

    private Map<JSITImport, PMMLDocumentMetadata> getPmmlDocuments(final Metadata metadata, final JSITDefinitions jsiDefinitions) {
        return dmnMarshallerImportsHelper.getPMMLDocuments(metadata, jsiDefinitions.getImport());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void connectRootWithChild(final Node dmnDiagramRoot,
                                      final Node child) {
        final String uuid = UUID.uuid();
        final Edge<Child, Node> edge = new EdgeImpl<>(uuid);
        edge.setContent(new Child());
        connectEdge(edge, dmnDiagramRoot, child);
        final Definitions definitions = ((DMNDiagram) ((View) dmnDiagramRoot.getContent()).getDefinition()).getDefinitions();
        final DMNModelInstrumentedBase childDRG = (DMNModelInstrumentedBase) ((View) child.getContent()).getDefinition();
        childDRG.setParent(definitions);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void connectEdge(final Edge edge,
                             final Node source,
                             final Node target) {
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        source.getOutEdges().add(edge);
        target.getInEdges().add(edge);
    }

    private List<JSITComponentsWidthsExtension> findComponentsWidthsExtensions(final List<JSIDMNDiagram> dmnDDDiagrams) {

        final List<JSITComponentsWidthsExtension> componentsWidthsExtensions = new ArrayList<>();

        for (int index = 0, dmnDiagram1Size = dmnDDDiagrams.size(); index < dmnDiagram1Size; index++) {

            final JSIDMNDiagram jsiDiagram = Js.uncheckedCast(dmnDDDiagrams.get(index));
            final JSIDiagramElement.JSIExtension dmnDDExtensions = Js.uncheckedCast(jsiDiagram.getExtension());

            if (Objects.isNull(dmnDDExtensions)) {
                break;
            }
            if (Objects.isNull(dmnDDExtensions.getAny())) {
                break;
            }
            final List<Object> extensions = dmnDDExtensions.getAny();
            if (!Objects.isNull(extensions)) {
                for (int i = 0; i < extensions.size(); i++) {
                    final Object wrapped = extensions.get(i);
                    final Object extension = JsUtils.getUnwrappedElement(wrapped);
                    if (JSITComponentsWidthsExtension.instanceOf(extension)) {
                        final JSITComponentsWidthsExtension jsiExtension = Js.uncheckedCast(extension);
                        componentsWidthsExtensions.add(jsiExtension);
                    }
                }
            }
        }
        return componentsWidthsExtensions;
    }

    private void loadImportedItemDefinitions(final Definitions definitions,
                                             final Map<JSITImport, JSITDefinitions> importDefinitions) {
        definitions.getItemDefinition().addAll(getWbImportedItemDefinitions(importDefinitions));
    }

    private List<ItemDefinition> getWbImportedItemDefinitions(final Map<JSITImport, JSITDefinitions> importDefinitions) {
        final List<ItemDefinition> definitions = new ArrayList<>();
        final List<JSITItemDefinition> importedDefinitions = dmnMarshallerImportsHelper.getImportedItemDefinitions(importDefinitions);
        for (int i = 0; i < importedDefinitions.size(); i++) {
            final JSITItemDefinition definition = Js.uncheckedCast(importedDefinitions.get(i));
            final ItemDefinition converted = ItemDefinitionPropertyConverter.wbFromDMN(definition);
            converted.setAllowOnlyVisualChange(true);
            definitions.add(converted);
        }
        return definitions;
    }
}
