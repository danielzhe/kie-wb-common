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

package org.kie.workbench.common.dmn.webapp.kogito.common.client.converters.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dc.JSIBounds;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dc.JSIPoint;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.di.JSIDiagramElement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.di.JSIShape;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNDiagram;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNEdge;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmndi12.JSIDMNShape;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.JsUtils;

import static org.kie.workbench.common.dmn.webapp.kogito.common.client.common.JsInteropHelpers.forEach;
import static org.kie.workbench.common.dmn.webapp.kogito.common.client.common.JsInteropHelpers.jsCopy;
import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class DMNDiagramElementsUtils {

    private static final String DRG = "DRG";

    public JSIDMNDiagram ensureDRGElementPresence(final JSITDefinitions dmnDefinitions) {

        final List<JSIDMNDiagram> dmnDiagrams = getValidDMNDiagrams(dmnDefinitions);
        final Optional<JSIDMNDiagram> drgDiagramElement = findDRGDiagramElement(dmnDiagrams);

        if (drgDiagramElement.isPresent()) {
            return Js.uncheckedCast(drgDiagramElement.get());
        }

        if (dmnDiagrams.size() == 1) {
            return renameDiagramElement(dmnDiagrams);
        }

        final JSIDMNDiagram drgElement = generateDRGElement(dmnDefinitions);
        dmnDefinitions.getDMNDI().addDMNDiagram(drgElement);
        return drgElement;
    }

    private List<JSIDMNDiagram> getValidDMNDiagrams(final JSITDefinitions dmnDefinitions) {
        final List<JSIDMNDiagram> dmnDiagrams = dmnDefinitions.getDMNDI().getDMNDiagram();

        forEach(dmnDiagrams,
                dmnDiagram -> {
                    if (isEmpty(dmnDiagram.getId())) {
                        dmnDiagram.setId(newId());
                    }
                });

        return dmnDiagrams;
    }

    private JSIDMNDiagram generateDRGElement(final JSITDefinitions dmnDefinitions) {
        final JSIDMNDiagram drg = new JSIDMNDiagram();
        final double[] referenceX = {0};

        drg.setId(newId());
        drg.setName(DRG);

        forEach(dmnDefinitions.getDMNDI().getDMNDiagram(),
                dmnDiagram -> {
                    final List<JSIDiagramElement> dmnDiagramElements = dmnDiagram.getDMNDiagramElement();
                    final double[] currentReferenceX = {referenceX[0]};

                    forEach(dmnDiagramElements, element -> {
                        DomGlobal.console.log("BBB >>>>>>>>>  " + element.getTYPE_NAME());

                        final JSIDiagramElement copy = Js.uncheckedCast(jsCopy(element));

                        if (JSIDMNShape.instanceOf(copy)) {
                            final JSIDMNShape shape = Js.uncheckedCast(copy);
                            shape.setId(new Id().getValue());
                            final JSIBounds bounds = shape.getBounds();
//                            DomGlobal.console.log(" >>>>>>>>>>>>>>>>>>>>>>>>>>> " + currentReferenceX[0]);
                            bounds.setX(bounds.getX() + currentReferenceX[0]);
                        }

                        // todo other shapes and other edges?
                        if (JSIDMNEdge.instanceOf(copy)) {
                            final JSIDMNEdge shape = Js.uncheckedCast(copy);
                            final JSIDMNEdge shape2 = Js.uncheckedCast(element);
                            forEach(shape.getWaypoint(),
                                    jsiPoint -> {
                                        DomGlobal.console.log("CCCC     >>>>>>>>>>>>>>>>>>>>>>>>>>> " + currentReferenceX[0]);
                                        jsiPoint.setX(jsiPoint.getX() + currentReferenceX[0]);
                                    });

                            DomGlobal.console.log("waypoint source", shape2.getWaypoint());
                            DomGlobal.console.log("waypoint target", shape.getWaypoint());
                        }

                        referenceX[0] = getMaxX(referenceX[0], element);

                        drg.addDMNDiagramElement(Js.uncheckedCast(JsUtils.getWrappedElement(copy)));
                    });
                });

        return drg;
    }

    private double getMaxX(final double currentMax,
                           final JSIDiagramElement element) {
        final boolean b = JSIDMNShape.instanceOf(element);
//        DomGlobal.console.log("BBB >>>>>>>>> " + element.getTYPE_NAME() + " == " + b);
        if (b) {
            final JSIDMNShape shape = Js.uncheckedCast(element);
            final JSIBounds bounds = shape.getBounds();
            final double newMax = bounds.getX() + bounds.getWidth();
            final double aaa = newMax > currentMax ? newMax : currentMax;
//            DomGlobal.console.log("BBB >>>>>>>>> getMaxX " + aaa);
            return aaa;
        }
//        DomGlobal.console.log("BBB >>>>>>>>> getMaxX " + currentMax);
        return currentMax;
    }

    private JSIDMNDiagram renameDiagramElement(final List<JSIDMNDiagram> dmnDiagramElements) {
        final JSIDMNDiagram dmnDiagramElement = Js.uncheckedCast(dmnDiagramElements.get(0));
        dmnDiagramElement.setName(DRG);
        return dmnDiagramElement;
    }

    private Optional<JSIDMNDiagram> findDRGDiagramElement(final List<JSIDMNDiagram> dmnDiagramElements) {

        final List<JSIDMNDiagram> eligibleAsDRGs = findEligibleDRGs(dmnDiagramElements);
        final Optional<JSIDMNDiagram> drg = eligibleAsDRGs.stream().findFirst();

        if (drg.isPresent() && eligibleAsDRGs.size() > 1) {
            renameOtherEligibleDRGs(eligibleAsDRGs, Js.uncheckedCast(drg.get()));
        }

        return drg;
    }

    public void renameOtherEligibleDRGs(final List<JSIDMNDiagram> eligibleAsDRGs,
                                        final JSIDMNDiagram drg) {

        final String drgId = drg.getId();
        final AtomicInteger index = new AtomicInteger();

        forEach(eligibleAsDRGs,
                eligibleAsDRG -> {
                    final String currentId = eligibleAsDRG.getId();

                    if (!Objects.equals(currentId, drgId)) {
                        eligibleAsDRG.setName(eligibleAsDRG.getName() + "-" + index.incrementAndGet());
                    }
                });
    }

    private List<JSIDMNDiagram> findEligibleDRGs(final List<JSIDMNDiagram> dmnDiagramElements) {

        final List<JSIDMNDiagram> eligibleDRGs = new ArrayList<>();

        forEach(dmnDiagramElements,
                dmnDiagramElement -> {
                    if (Objects.equals(dmnDiagramElement.getName(), DRG)) {
                        eligibleDRGs.add(dmnDiagramElement);
                    }
                });

        return eligibleDRGs;
    }

    private String newId() {
        return new Id().getValue();
    }
}
