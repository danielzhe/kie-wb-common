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

package org.kie.workbench.common.dmn.client.session;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.mockito.Mock;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class DMNCanvasHandlerTest {

    @Mock
    private DMNCanvasHandler canvasHandler;

    @Mock
    private Element parent;

    @Mock
    private Element child;

    @Mock
    private AbstractCanvas canvas;

    @Mock
    private Shape shape;

    private final String parentUuid = "parent uuid";

    @Before
    public void setup() {
        when(parent.getUUID()).thenReturn(parentUuid);
        doCallRealMethod().when(canvasHandler).addChild(parent, child);
    }

    @Test
    public void testAddChildWhenItIsElementFromThisCanvas() {

        doReturn(false).when(canvasHandler).isCanvasRoot(parent);
        doReturn(canvas).when(canvasHandler).getCanvas();
        when(canvas.getShape(parentUuid)).thenReturn(shape);

        canvasHandler.addChild(parent, child);

        verify(canvasHandler).superAddChild(parent, child);
    }

    @Test
    public void testAddChildWhenItIsNotElementFromThisCanvas() {

        doReturn(false).when(canvasHandler).isCanvasRoot(parent);
        doReturn(canvas).when(canvasHandler).getCanvas();
        when(canvas.getShape(parentUuid)).thenReturn(null);

        canvasHandler.addChild(parent, child);

        verify(canvasHandler, never()).superAddChild(parent, child);
    }

    @Test
    public void testAddChildWhenItIsCanvasRoot() {

        doReturn(true).when(canvasHandler).isCanvasRoot(parent);
        doReturn(canvas).when(canvasHandler).getCanvas();
        when(canvas.getShape(parentUuid)).thenReturn(null);

        canvasHandler.addChild(parent, child);

        verify(canvasHandler).superAddChild(parent, child);
    }
}