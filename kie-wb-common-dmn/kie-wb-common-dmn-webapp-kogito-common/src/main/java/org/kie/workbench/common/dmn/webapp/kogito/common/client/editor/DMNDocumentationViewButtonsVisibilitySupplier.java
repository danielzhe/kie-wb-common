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

package org.kie.workbench.common.dmn.webapp.kogito.common.client.editor;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import org.appformer.kogito.bridge.client.context.EditorContextProvider;

import static org.appformer.kogito.bridge.client.context.KogitoChannel.VSCODE;

@Dependent
@Alternative
public class DMNDocumentationViewButtonsVisibilitySupplier extends org.kie.workbench.common.dmn.client.editors.documentation.DMNDocumentationViewButtonsVisibilitySupplier {

    private final EditorContextProvider contextProvider;

    public DMNDocumentationViewButtonsVisibilitySupplier(final EditorContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public boolean isButtonsVisible() {
        return !Objects.equals(contextProvider.getChannel(), VSCODE);
    }
}
