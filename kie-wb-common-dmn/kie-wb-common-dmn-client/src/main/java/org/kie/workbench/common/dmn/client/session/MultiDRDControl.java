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

package org.kie.workbench.common.dmn.client.session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.client.docks.navigator.GraphDRDSwitchPOC;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.controls.AbstractCanvasControl;

@ApplicationScoped
public class MultiDRDControl extends AbstractCanvasControl<AbstractCanvas> {

    @Inject
    private GraphDRDSwitchPOC switchPOC;

    @Override
    protected void doInit() {
        switchPOC.setMultiDRDControl(this);
    }

    public AbstractCanvas getCanvas() {
        return canvas;
    }
}
