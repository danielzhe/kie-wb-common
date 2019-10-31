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
package org.kie.workbench.common.dmn.client.editors.types.imported.treelist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.Node;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.uberfire.client.mvp.UberElemental;

@Dependent
public class TreeListItem {

    private View view;
    private final List<TreeListSubItem> subItems;
    private String description;
    private boolean isSelected;

    @Inject
    public TreeListItem(final View view) {
        this.view = view;
        this.subItems = new ArrayList<>();
    }

    @PostConstruct
    void setup() {
        view.init(this);
    }

    public Node getElement() {
        return view.getElement();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addSubItem(final TreeListSubItem subItem) {
        subItems.add(subItem);
    }

    public List<TreeListSubItem> getSubItems() {
        return subItems;
    }

    public void updateView() {
        this.view.populate(this);
    }

    public void setIsSelected(final boolean value) {
        this.isSelected = value;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public interface View extends UberElemental<TreeListItem>,
                                  IsElement {

        void populate(final TreeListItem treeListItem);
    }
}
