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

package org.kie.workbench.common.dmn.client.editors.expressions.types.function;

import java.util.ArrayList;
import java.util.List;

import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridColumn;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseHeaderMetaData;
import org.uberfire.ext.wires.core.grids.client.widget.dnd.IsRowDragHandle;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.columns.GridColumnRenderer;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.columns.impl.StringColumnRenderer;

class EmptyColumn extends BaseGridColumn<String> implements IsRowDragHandle {

    public EmptyColumn() {
        this(new ArrayList<HeaderMetaData>() {{
                 add(new BaseHeaderMetaData("#"));
             }},
             new StringColumnRenderer());
    }

    public EmptyColumn(final List<HeaderMetaData> headerMetaData) {
        this(headerMetaData,
             new StringColumnRenderer());
    }

    public EmptyColumn(final List<HeaderMetaData> headerMetaData,
                       final GridColumnRenderer<String> columnRenderer) {
        super(headerMetaData,
              columnRenderer,
              50.0);
    }
}