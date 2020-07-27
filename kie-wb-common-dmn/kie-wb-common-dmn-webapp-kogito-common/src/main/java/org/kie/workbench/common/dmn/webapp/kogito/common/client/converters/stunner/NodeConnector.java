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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import org.kie.workbench.common.dmn.api.definition.model.Association;
import org.kie.workbench.common.dmn.api.definition.model.AuthorityRequirement;
import org.kie.workbench.common.dmn.api.definition.model.InformationRequirement;
import org.kie.workbench.common.dmn.api.definition.model.KnowledgeRequirement;
import org.kie.workbench.common.dmn.api.property.dmn.Description;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.IdPropertyConverter;
import org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.dd.PointUtils;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dc.JSIPoint;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITAssociation;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITAuthorityRequirement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITBusinessKnowledgeModel;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDMNElement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDMNElementReference;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDecision;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDecisionService;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITInformationRequirement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITKnowledgeRequirement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITKnowledgeSource;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNEdge;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.ControlPoint;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.util.UUID;

import static org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.dd.PointUtils.upperLeftBound;
import static org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.dd.PointUtils.xOfBound;
import static org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.model.dd.PointUtils.yOfBound;
import static org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils.getDefinitionId;

@Dependent
public class NodeConnector {

    private final FactoryManager factoryManager;

    private static final double CENTRE_TOLERANCE = 1.0;

    private static final String INFO_REQ_ID = getDefinitionId(InformationRequirement.class);

    private static final String KNOWLEDGE_REQ_ID = getDefinitionId(KnowledgeRequirement.class);

    private static final String AUTH_REQ_ID = getDefinitionId(AuthorityRequirement.class);

    private static final String ASSOCIATION_ID = getDefinitionId(Association.class);

    @Inject
    public NodeConnector(final FactoryManager factoryManager) {
        this.factoryManager = factoryManager;
    }

    void connect(final List<JSIDMNEdge> edges,
                 final List<JSITAssociation> associations,
                 final List<NodeEntry> nodeEntries) {

        final Map<String, NodeEntry> entriesById = makeNodeIndex(nodeEntries);

//        for (final NodeEntry nodeEntry : nodeEntries) {
//
//            final JSITDMNElement element = nodeEntry.getDmnElement();
//            final Node node = nodeEntry.getNode();
//
//            // For imported nodes, we don't have its connections
//            if (nodeEntry.isIncluded()) {
//                continue;
//            }
//
//            // DMN spec table 2: Requirements
//            if (JSITDecision.instanceOf(element)) {
//                final JSITDecision decision = Js.uncheckedCast(element);
//                final List<JSITInformationRequirement> jsiInformationRequirements = decision.getInformationRequirement();
//
//                if (decision.getName().equals("dddddd")) {
//                    DomGlobal.console.log("decision ~> dddddd " + jsiInformationRequirements.size());
//                }
//
//                for (int i = 0; i < jsiInformationRequirements.size(); i++) {
//                    final JSITInformationRequirement ir = Js.uncheckedCast(jsiInformationRequirements.get(i));
//                    connectEdgeToNodes(INFO_REQ_ID,
//                                       ir,
//                                       ir.getRequiredInput(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                    connectEdgeToNodes(INFO_REQ_ID,
//                                       ir,
//                                       ir.getRequiredDecision(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//                final List<JSITKnowledgeRequirement> jsiKnowledgeRequirements = decision.getKnowledgeRequirement();
//                for (int i = 0; i < jsiKnowledgeRequirements.size(); i++) {
//                    final JSITKnowledgeRequirement kr = Js.uncheckedCast(jsiKnowledgeRequirements.get(i));
//                    connectEdgeToNodes(KNOWLEDGE_REQ_ID,
//                                       kr,
//                                       kr.getRequiredKnowledge(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//                final List<JSITAuthorityRequirement> jsiAuthorityRequirements = decision.getAuthorityRequirement();
//                for (int i = 0; i < jsiAuthorityRequirements.size(); i++) {
//                    final JSITAuthorityRequirement ar = Js.uncheckedCast(jsiAuthorityRequirements.get(i));
//                    connectEdgeToNodes(AUTH_REQ_ID,
//                                       ar,
//                                       ar.getRequiredAuthority(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//            } else if (JSITBusinessKnowledgeModel.instanceOf(element)) {
//                final JSITBusinessKnowledgeModel bkm = Js.uncheckedCast(element);
//                final List<JSITKnowledgeRequirement> jsiKnowledgeRequirements = bkm.getKnowledgeRequirement();
//                for (int i = 0; i < jsiKnowledgeRequirements.size(); i++) {
//                    final JSITKnowledgeRequirement kr = Js.uncheckedCast(jsiKnowledgeRequirements.get(i));
//                    connectEdgeToNodes(KNOWLEDGE_REQ_ID,
//                                       kr,
//                                       kr.getRequiredKnowledge(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//                final List<JSITAuthorityRequirement> jsiAuthorityRequirements = bkm.getAuthorityRequirement();
//                for (int i = 0; i < jsiAuthorityRequirements.size(); i++) {
//                    final JSITAuthorityRequirement ar = Js.uncheckedCast(jsiAuthorityRequirements.get(i));
//                    connectEdgeToNodes(AUTH_REQ_ID,
//                                       ar,
//                                       ar.getRequiredAuthority(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//            } else if (JSITKnowledgeSource.instanceOf(element)) {
//                final JSITKnowledgeSource ks = Js.uncheckedCast(element);
//                final List<JSITAuthorityRequirement> jsiAuthorityRequirements = ks.getAuthorityRequirement();
//                for (int i = 0; i < jsiAuthorityRequirements.size(); i++) {
//                    final JSITAuthorityRequirement ar = Js.uncheckedCast(jsiAuthorityRequirements.get(i));
//                    connectEdgeToNodes(AUTH_REQ_ID,
//                                       ar,
//                                       ar.getRequiredInput(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                    connectEdgeToNodes(AUTH_REQ_ID,
//                                       ar,
//                                       ar.getRequiredDecision(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                    connectEdgeToNodes(AUTH_REQ_ID,
//                                       ar,
//                                       ar.getRequiredAuthority(),
//                                       entriesById,
//                                       edges,
//                                       node);
//                }
//            } else if (JSITDecisionService.instanceOf(element)) {
//                final JSITDecisionService ds = Js.uncheckedCast(element);
//                final List<JSITDMNElementReference> jsiEncapsulatedDecisions = ds.getEncapsulatedDecision();
//                for (int i = 0; i < jsiEncapsulatedDecisions.size(); i++) {
//                    final JSITDMNElementReference er = Js.uncheckedCast(jsiEncapsulatedDecisions.get(i));
//                    final String reqInputID = getId(er);
//                    final Node requiredNode = getRequiredNode(entriesById, reqInputID);
//                    if (Objects.nonNull(requiredNode)) {
//                        connectDSChildEdge(node, requiredNode);
//                    }
//                }
//                final List<JSITDMNElementReference> jsiOutputDecisions = ds.getOutputDecision();
//                for (int i = 0; i < jsiOutputDecisions.size(); i++) {
//                    final JSITDMNElementReference er = Js.uncheckedCast(jsiOutputDecisions.get(i));
//                    final String reqInputID = getId(er);
//                    final Node requiredNode = getRequiredNode(entriesById, reqInputID);
//                    if (Objects.nonNull(requiredNode)) {
//                        connectDSChildEdge(node, requiredNode);
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < associations.size(); i++) {
//            final JSITAssociation jsiAssociation = Js.uncheckedCast(associations.get(i));
//            final String sourceId = getId(jsiAssociation.getSourceRef());
//            final Node sourceNode = Optional.ofNullable(entriesById.get(sourceId)).map(NodeEntry::getNode).orElseThrow(() -> new UnsupportedOperationException("Text Annotation is associated with an invalid node"));
//
//            final String targetId = getId(jsiAssociation.getTargetRef());
//            final Node targetNode = Optional.ofNullable(entriesById.get(targetId)).map(NodeEntry::getNode).orElseThrow(() -> new UnsupportedOperationException("Text Annotation is associated with an invalid node"));
//
//            @SuppressWarnings("unchecked")
//            final Edge<View<Association>, ?> myEdge = (Edge<View<Association>, ?>) factoryManager.newElement(idOfDMNorWBUUID(jsiAssociation),
//                                                                                                             ASSOCIATION_ID).asEdge();
//
//            final Id id = IdPropertyConverter.wbFromDMN(jsiAssociation.getId());
//            final Description description = new Description(jsiAssociation.getDescription());
//            final Association definition = new Association(id, description);
//            myEdge.getContent().setDefinition(definition);
//
//            connectEdge(myEdge,
//                        sourceNode,
//                        targetNode);
//            setConnectionMagnets(myEdge, edges, jsiAssociation.getId());
//        }
    }

    private Map<String, NodeEntry> makeNodeIndex(final List<NodeEntry> nodeEntries) {

        final Map<String, NodeEntry> map = new HashMap<>();

        nodeEntries.forEach(nodeEntry -> {
            map.put(nodeEntry.getDmnElement().getId(), nodeEntry);
        });

        return map;
    }

    /**
     * Stunner's factoryManager is only used to create Nodes that are considered part of a "Definition Set" (a collection of nodes visible to the User e.g. BPMN2 StartNode, EndNode and DMN's DecisionNode etc).
     * Relationships are not created with the factory.
     * This method specializes to connect with an Edge containing a Child relationship the target Node.
     */
    private void connectDSChildEdge(final Node dsNode,
                                    final Node requiredNode) {
        final String uuid = dsNode.getUUID() + "er" + requiredNode.getUUID();
        final Edge<Child, Node> myEdge = new EdgeImpl<>(uuid);
        myEdge.setContent(new Child());
        connectEdge(myEdge,
                    dsNode,
                    requiredNode);
    }

    private String idOfDMNorWBUUID(final JSITDMNElement dmn) {
        return Objects.nonNull(dmn.getId()) ? dmn.getId() : UUID.uuid();
    }

    private String getId(final JSITDMNElementReference er) {
        String href = er.getHref();
        return href.contains("#") ? href.substring(href.indexOf('#') + 1) : href;
    }

    private void connectEdgeToNodes(final String connectorTypeId,
                                    final JSITDMNElement jsiDMNElement,
                                    final JSITDMNElementReference jsiDMNElementReference,
                                    final Map<String, NodeEntry> entriesById,
                                    final List<JSIDMNEdge> edges,
                                    final Node currentNode) {
        if (Objects.nonNull(jsiDMNElementReference)) {
            final String reqInputID = getId(jsiDMNElementReference);
            final Node requiredNode = getRequiredNode(entriesById, reqInputID);
            final Edge myEdge = factoryManager.newElement(idOfDMNorWBUUID(jsiDMNElement),
                                                          connectorTypeId).asEdge();
            connectEdge(myEdge,
                        requiredNode,
                        currentNode);
            setConnectionMagnets(myEdge, edges, jsiDMNElement.getId());
        }
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

    private void setConnectionMagnets(final Edge edge,
                                      final List<JSIDMNEdge> edges,
                                      final String dmnEdgeElementRef) {
        final ViewConnector connectionContent = (ViewConnector) edge.getContent();
        final Optional<JSIDMNEdge> dmnEdge = edges
                .stream()
                .filter(e -> Objects.equals(e.getDmnElementRef().getLocalPart(), dmnEdgeElementRef)).findFirst();

        if (dmnEdge.isPresent()) {
            final JSIDMNEdge e = Js.uncheckedCast(dmnEdge.get());
            final JSIPoint source = Js.uncheckedCast(e.getWaypoint().get(0));
            final Node<View<?>, Edge> sourceNode = edge.getSourceNode();
            if (null != sourceNode) {
                setConnectionMagnet(sourceNode,
                                    source,
                                    connectionContent::setSourceConnection);
            }
            final JSIPoint target = Js.uncheckedCast(e.getWaypoint().get(e.getWaypoint().size() - 1));
            final Node<View<?>, Edge> targetNode = edge.getTargetNode();
            if (null != targetNode) {
                setConnectionMagnet(targetNode,
                                    target,
                                    connectionContent::setTargetConnection);
            }
            if (e.getWaypoint().size() > 2) {
                connectionContent.setControlPoints(e.getWaypoint()
                                                           .subList(1, e.getWaypoint().size() - 1)
                                                           .stream()
                                                           .map(p -> ControlPoint.build(PointUtils.dmndiPointToPoint2D(p)))
                                                           .toArray(ControlPoint[]::new));
            }
        } else {
            // Set the source connection, if any.
            final Node sourceNode = edge.getSourceNode();
            if (null != sourceNode) {
                connectionContent.setSourceConnection(MagnetConnection.Builder.atCenter(sourceNode));
            }
            // Set the target connection, if any.
            final Node targetNode = edge.getTargetNode();
            if (null != targetNode) {
                connectionContent.setTargetConnection(MagnetConnection.Builder.atCenter(targetNode));
            }
        }
    }

    private void setConnectionMagnet(final Node<View<?>, Edge> node,
                                     final JSIPoint magnetPoint,
                                     final Consumer<Connection> connectionConsumer) {
        final View<?> view = node.getContent();
        final double viewX = xOfBound(upperLeftBound(view));
        final double viewY = yOfBound(upperLeftBound(view));
        final double magnetRelativeX = magnetPoint.getX() - viewX;
        final double magnetRelativeY = magnetPoint.getY() - viewY;
        final double viewWidth = view.getBounds().getWidth();
        final double viewHeight = view.getBounds().getHeight();
        if (isCentre(magnetRelativeX,
                     magnetRelativeY,
                     viewWidth,
                     viewHeight)) {
            connectionConsumer.accept(MagnetConnection.Builder.atCenter(node));
        } else {
            connectionConsumer.accept(MagnetConnection.Builder.at(magnetRelativeX, magnetRelativeY).setAuto(true));
        }
    }

    private boolean isCentre(final double magnetRelativeX,
                             final double magnetRelativeY,
                             final double viewWidth,
                             final double viewHeight) {
        return Math.abs((viewWidth / 2) - magnetRelativeX) < CENTRE_TOLERANCE &&
                Math.abs((viewHeight / 2) - magnetRelativeY) < CENTRE_TOLERANCE;
    }

    private Node getRequiredNode(final Map<String, NodeEntry> entriesById,
                                 final String reqInputID) {
        if (entriesById.containsKey(reqInputID)) {
            final NodeEntry nodeEntry = entriesById.get(reqInputID);
            return nodeEntry.getNode();
        } else {
            final Optional<String> match = entriesById.keySet().stream()
                    .filter(k -> k.contains(reqInputID))
                    .findFirst();
            if (match.isPresent()) {
                return entriesById.get(match.get()).getNode();
            }
        }
        return null;
    }
}
