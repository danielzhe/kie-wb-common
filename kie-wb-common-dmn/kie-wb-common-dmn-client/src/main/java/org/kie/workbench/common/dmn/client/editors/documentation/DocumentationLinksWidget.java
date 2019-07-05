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

package org.kie.workbench.common.dmn.client.editors.documentation;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.dmn.api.definition.v1_1.DRGElement;
import org.kie.workbench.common.dmn.api.property.dmn.DMNExternalLink;
import org.kie.workbench.common.dmn.api.property.dmn.DocumentationLinks;
import org.kie.workbench.common.dmn.client.editors.common.RemoveHelper;
import org.kie.workbench.common.dmn.client.editors.documentation.links.NameAndUrlPopoverView;
import org.kie.workbench.common.dmn.client.editors.types.common.HiddenHelper;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;

import static org.kie.workbench.common.dmn.client.resources.i18n.DMNEditorConstants.DMNDocumentationI18n_Add;

@Dependent
@Templated
public class DocumentationLinksWidget extends Composite implements HasValue<DocumentationLinks>,
                                                                   HasEnabled {

    @DataField("documentation-links-container")
    private final HTMLDivElement linksContainer;

    @DataField("none-container")
    private final HTMLDivElement noneContainer;

    @DataField("addButton")
    private final HTMLAnchorElement addButton;

    @DataField("addLink")
    private final HTMLElement addLink;

    private final CellEditorControlsView cellEditor;
    private final ManagedInstance<DocumentationLinkItem> listItems;
    private final NameAndUrlPopoverView.Presenter nameAndUrlPopover;
    private final TranslationService translationService;

    private boolean enabled;

    private DocumentationLinks value;

    @Inject
    public DocumentationLinksWidget(final ManagedInstance<DocumentationLinkItem> listItems,
                                    final TranslationService translationService,
                                    final HTMLDivElement linksContainer,
                                    final HTMLDivElement noneContainer,
                                    final HTMLAnchorElement addButton,
                                    final NameAndUrlPopoverView.Presenter nameAndUrlPopover,
                                    final CellEditorControlsView cellEditor,
                                    @Named("span") final HTMLElement addLink) {

        this.listItems = listItems;
        this.linksContainer = linksContainer;
        this.noneContainer = noneContainer;
        this.addButton = addButton;
        this.nameAndUrlPopover = nameAndUrlPopover;
        this.cellEditor = cellEditor;
        this.addLink = addLink;
        this.value = new DocumentationLinks();
        this.enabled = true;
        this.translationService = translationService;
    }

    @PostConstruct
    public void init() {
        nameAndUrlPopover.setOnExternalLinkCreated(this::onDMNExternalLinkCreated);
        addLink.textContent = translationService.getTranslation(DMNDocumentationI18n_Add);
    }

    void onDMNExternalLinkCreated(final DMNExternalLink externalLink) {
        getValue().addLink(externalLink);
        refresh();
    }

    @Override
    public DocumentationLinks getValue() {
        return value;
    }

    @Override
    public void setValue(final DocumentationLinks documentationLinks) {
        setValue(documentationLinks, false);
    }

    @Override
    public void setValue(final DocumentationLinks documentationLinks,
                         final boolean fireEvents) {
        value = documentationLinks;
    }

    @EventHandler("addButton")
    @SuppressWarnings("unused")
    public void onClickTypeButton(final ClickEvent clickEvent) {
        cellEditor.show(nameAndUrlPopover,
                        Optional.of(""),
                        clickEvent.getClientX(),
                        clickEvent.getClientY());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<DocumentationLinks> handler) {
        return addHandler(handler,
                          ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setDMNModel(final DRGElement model) {

        setValue(model.getLinksHolder().getValue());
        refresh();
    }

    void refresh() {

        final List<DMNExternalLink> all = getValue().getLinks();
        RemoveHelper.removeChildren(linksContainer);
        for (final DMNExternalLink link : all) {
            final DocumentationLinkItem listItem = listItems.get();
            listItem.init(link);
            listItem.setOnDeleted(this::onExternalLinkDeleted);
            linksContainer.appendChild(listItem.getElement());
        }

        refreshContainersVisibility();
    }

    void refreshContainersVisibility() {
        if (getValue().getLinks().size() == 0) {
            HiddenHelper.show(noneContainer);
            HiddenHelper.hide(linksContainer);
        } else {
            HiddenHelper.hide(noneContainer);
            HiddenHelper.show(linksContainer);
        }
    }

    void onExternalLinkDeleted(final DMNExternalLink externalLink) {
        getValue().getLinks().remove(externalLink);
        refresh();
    }
}