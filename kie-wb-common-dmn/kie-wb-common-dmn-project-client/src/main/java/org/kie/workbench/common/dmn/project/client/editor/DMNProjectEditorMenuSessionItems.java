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

package org.kie.workbench.common.dmn.project.client.editor;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.kie.workbench.common.dmn.api.qualifiers.DMNEditor;
import org.kie.workbench.common.dmn.project.client.session.DMNEditorSessionCommands;
import org.kie.workbench.common.stunner.client.widgets.menu.MenuUtils;
import org.kie.workbench.common.stunner.core.client.session.command.impl.PerformAutomaticLayoutCommand;
import org.kie.workbench.common.stunner.core.i18n.CoreTranslationMessages;
import org.kie.workbench.common.stunner.project.client.editor.AbstractProjectEditorMenuSessionItems;
import org.kie.workbench.common.widgets.client.menu.FileMenuBuilder;
import org.uberfire.workbench.model.menu.MenuItem;

@Dependent
@Typed(DMNProjectEditorMenuSessionItems.class)
@DMNEditor
public class DMNProjectEditorMenuSessionItems extends AbstractProjectEditorMenuSessionItems<DMNProjectDiagramEditorMenuItemsBuilder> {

    private final PerformAutomaticLayoutCommand performAutomaticLayoutCommand;

    @Inject
    public DMNProjectEditorMenuSessionItems(final DMNProjectDiagramEditorMenuItemsBuilder itemsBuilder,
                                            final @DMNEditor DMNEditorSessionCommands sessionCommands,
                                            final PerformAutomaticLayoutCommand performAutomaticLayoutCommand) {
        super(itemsBuilder,
              sessionCommands);
        this.performAutomaticLayoutCommand = performAutomaticLayoutCommand;
    }

    @Override
    public void populateMenu(final FileMenuBuilder menu) {
        super.populateMenu(menu);
        final MenuItem performAutomaticLayoutMenuItem = newPerformAutomaticLayout();
        menu.addNewTopLevelMenu(performAutomaticLayoutMenuItem);
    }

    private MenuItem newPerformAutomaticLayout() {
        final MenuUtils.HasEnabledIsWidget buttonWrapper = MenuUtils.buildHasEnabledWidget(new Button() {{
            setSize(ButtonSize.SMALL);
            setText(getTranslationService().getValue(CoreTranslationMessages.PERFORM_AUTOMATIC_LAYOUT));
            addClickHandler(clickEvent -> performAutomaticLayoutCommand.execute());
        }});
        return MenuUtils.buildItem(buttonWrapper);
    }
}
