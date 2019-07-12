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

package org.kie.workbench.common.dmn.backend.definition.v1_1;

import java.util.Objects;

import org.kie.dmn.model.api.DMNElement;
import org.kie.dmn.model.api.DRGElement;
import org.kie.dmn.model.v1_1.TDMNElement;
import org.kie.workbench.common.dmn.api.property.dmn.DMNExternalLink;
import org.kie.workbench.common.dmn.api.property.dmn.DocumentationLinks;
import org.kie.workbench.common.dmn.backend.definition.v1_1.dd.ExternalLink;
import org.kie.workbench.common.dmn.backend.definition.v1_1.dd.ExternalLinkExtensions;

class DMNExternalLinksToExtensionElements {

    static void loadExternalLinksFromExtensionElements(final org.kie.dmn.model.api.DRGElement source,
                                                       final org.kie.workbench.common.dmn.api.definition.v1_1.DRGElement target) {

        if (!Objects.isNull(source.getExtensionElements())) {
            for (final Object obj : source.getExtensionElements().getAny()) {

                if (obj instanceof ExternalLink) {
                    ExternalLink el = (ExternalLink) obj;
                    final DMNExternalLink external = new DMNExternalLink();
                    external.setDescription(el.getName());
                    external.setUrl(el.getUrl());
                    target.getLinksHolder().getValue().addLink(external);
                }
            }
        }
    }

    static void loadExternalLinksIntoExtensionElements(final org.kie.workbench.common.dmn.api.definition.v1_1.DRGElement source,
                                                       final org.kie.dmn.model.api.DRGElement target) {

        if (Objects.isNull(source.getLinksHolder()) || Objects.isNull(source.getLinksHolder().getValue())) {
            return;
        }

        final DocumentationLinks links = source.getLinksHolder().getValue();

        final DMNElement.ExtensionElements elements = getOrCreateExtensionElements(target);

        //elements.getAny()

        ExternalLinkExtensions extensions = new ExternalLinkExtensions();

        for (final DMNExternalLink link : links.getLinks()) {
            final ExternalLink external = new ExternalLink();
            external.setName(link.getDescription());
            external.setUrl(link.getUrl());
            extensions.getExternalLinks().add(external);
        }
        elements.getAny().add(extensions);

        target.setExtensionElements(elements);
    }

    static DMNElement.ExtensionElements getOrCreateExtensionElements(final DRGElement target) {
        return target.getExtensionElements() == null
                ? new TDMNElement.TExtensionElements()
                : target.getExtensionElements();
    }
}
