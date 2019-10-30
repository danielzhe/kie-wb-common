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

package org.kie.workbench.common.dmn.client.editors.types.imported;

import java.util.List;
import java.util.function.Consumer;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.editors.types.DataObject;
import org.kie.workbench.common.dmn.client.api.dataobjects.DMNDataObjectsClient;
import org.mockito.Mock;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ImportDataObjectModalTest {

    @Mock
    private ImportDataObjectModal.View view;

    @Mock
    private DMNDataObjectsClient client;

    private ImportDataObjectModal modal;

    @Before
    public void setup() {
        modal = spy(new ImportDataObjectModal(view, client));
    }

    @Test
    public void testShow() {

        final Consumer consumer = mock(Consumer.class);
        doReturn(consumer).when(modal).getConsumer();

        modal.show();

        verify(client).loadDataObjects(consumer);
    }

    @Test
    public void testGetConsumer() {

        doNothing().when(modal).superShow();
        final Consumer<List<DataObject>> consumer = modal.getConsumer();
        final List<DataObject> items = mock(List.class);
        when(items.isEmpty()).thenReturn(false);
        consumer.accept(items);

        verify(view).clear();
        verify(view).addItems(items);
        verify(modal).superShow();
    }
}