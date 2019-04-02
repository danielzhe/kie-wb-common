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

package org.kie.workbench.common.dmn.client.decision;

import java.util.ArrayList;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.dmn.api.definition.v1_1.Decision;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.DMNShapeSet;
import org.kie.workbench.common.dmn.client.decision.tree.DecisionNavigatorTreePresenter;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.event.BuildCanvasShapeEvent;
import org.kie.workbench.common.stunner.core.client.canvas.controls.event.CanvasShapeDragStartEvent;
import org.kie.workbench.common.stunner.core.client.canvas.controls.event.CanvasShapeDragUpdateEvent;
import org.kie.workbench.common.stunner.core.client.components.drag.DragProxy;
import org.kie.workbench.common.stunner.core.client.components.drag.DragProxyCallback;
import org.kie.workbench.common.stunner.core.client.components.glyph.ShapeGlyphDragHandler;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinition;
import org.kie.workbench.common.stunner.core.client.shape.factory.ShapeFactory;
import org.kie.workbench.common.stunner.core.definition.shape.Glyph;

@Templated
public class DecisionNavigatorView implements DecisionNavigatorPresenter.View {

    private final Event<BuildCanvasShapeEvent> buildCanvasShapeEvent;
    private final Event<CanvasShapeDragStartEvent> canvasShapeDragStartEvent;
    private final Event<CanvasShapeDragUpdateEvent> canvasShapeDragUpdateEvent;
    private final DMNShapeSet dmnShapeSet;
    private final SessionManager sessionManager;
    private final ShapeGlyphDragHandler shapeGlyphDragHandler;

    @DataField("main-tree")
    private HTMLDivElement mainTree;

    @DataField("happy-button")
    private HTMLButtonElement happyButton;

    private DecisionNavigatorPresenter presenter;
    private DragProxy itemDragProxy;

    @Inject
    public DecisionNavigatorView(final HTMLDivElement mainTree,
                                 final Event<BuildCanvasShapeEvent> buildCanvasShapeEvent,
                                 final Event<CanvasShapeDragStartEvent> canvasShapeDragStartEvent,
                                 final Event<CanvasShapeDragUpdateEvent> canvasShapeDragUpdateEvent,
                                 final HTMLButtonElement happyButton,
                                 final DMNShapeSet dmnShapeSet,
                                 final SessionManager sessionManager,
                                 final ShapeGlyphDragHandler shapeGlyphDragHandler) {
        this.mainTree = mainTree;
        this.buildCanvasShapeEvent = buildCanvasShapeEvent;
        this.happyButton = happyButton;
        this.canvasShapeDragStartEvent = canvasShapeDragStartEvent;
        this.canvasShapeDragUpdateEvent = canvasShapeDragUpdateEvent;
        this.dmnShapeSet = dmnShapeSet;
        this.sessionManager = sessionManager;
        this.shapeGlyphDragHandler = shapeGlyphDragHandler;
    }

    @Override
    public void init(final DecisionNavigatorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setupMainTree(final DecisionNavigatorTreePresenter.View mainTreeComponent) {
        mainTree.appendChild(mainTreeComponent.getElement());
    }

    @EventHandler("happy-button")
    public void happyButtonOnClick(final ClickEvent clickEvent){

showDragProxy(clickEvent.getX(),
              clickEvent.getY());


    }

    void showDragProxy(int x, int y){
        ShapeFactory factory = dmnShapeSet.getShapeFactory();

         final Glyph glyph =  factory.getGlyph("org.kie.workbench.common.dmn.api.definition.v1_1.Decision");

        final String itemId = "fakeDecision";
        itemDragProxy = shapeGlyphDragHandler.show(new ShapeGlyphDragHandler.Item() {
                                                       @Override
                                                       public Glyph getShape() {
                                                           return glyph;
                                                       }

                                                       @Override
                                                       public int getWidth() {
                                                           return (int) 16;
                                                       }

                                                       @Override
                                                       public int getHeight() {
                                                           return (int) 16;
                                                       }
                                                   },
                                                   (int) x,
                                                   (int) y,
                                                   new DragProxyCallback() {
                                                       @Override
                                                       public void onStart(int x,
                                                                           int y) {
//                                                           presenter.onDragStart(itemId,
//                                                                                 x,
//                                                                                 y);
                                                       }

                                                       @Override
                                                       public void onMove(int x,
                                                                          int y) {
//                                                           presenter.onDragProxyMove(itemId,
//                                                                                     (double) x,
//                                                                                     (double) y);
                                                       }

                                                       @Override
                                                       public void onComplete(int x,
                                                                              int y) {

                                                                   final DefaultPaletteDefinition def = new DefaultPaletteDefinition(new ArrayList<>(), "defPalette");

        Decision decision = new Decision();
        decision.setName(new Name("whatever.Blabla"));


        //decision.setExternalId("WeAreHappy.Now!");
        buildCanvasShapeEvent.fire(new BuildCanvasShapeEvent((AbstractCanvasHandler)sessionManager.getCurrentSession().getCanvasHandler(),
                                                                     decision,
                                                                     factory,
                                                                     x,
                                                                     y));

//                                                           presenter.onDragProxyComplete(itemId,
//                                                                                         (double) x,
//                                                                                         (double) y);
                                                       }
                                                   });
    }
}
