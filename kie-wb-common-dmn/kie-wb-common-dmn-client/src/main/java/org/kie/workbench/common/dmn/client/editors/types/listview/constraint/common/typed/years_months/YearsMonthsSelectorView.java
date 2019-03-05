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

package org.kie.workbench.common.dmn.client.editors.types.listview.constraint.common.typed.years_months;

import java.util.Objects;
import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.BlurEvent;
import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.dmn.client.editors.types.listview.constraint.common.typed.date.DateSelectorView;

@Templated
@Dependent
public class YearsMonthsSelectorView implements YearsMonthsSelector.View {

    private DateSelectorView presenter;

    @DataField("years-input")
    private final HTMLInputElement yearInput;

    @DataField("months-input")
    private final HTMLInputElement monthInput;

    private final YearsMonthsValueConverter converter;

    private Consumer<BlurEvent> onValueInputBlur;

    @Inject
    public YearsMonthsSelectorView(final HTMLInputElement yearInput,
                                   final HTMLInputElement monthInput,
                                   final YearsMonthsValueConverter converter) {
        this.yearInput = yearInput;
        this.monthInput = monthInput;
        this.converter = converter;
    }

    @Override
    public void init(final DateSelectorView presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getValue() {
        return converter.toDMNString(yearInput.value, monthInput.value);
    }

    @Override
    public void setValue(final String value) {

        final YearsMonthsValue yearsMonths = converter.fromDMNString(value);
        yearInput.value = yearsMonths.getYears();
        monthInput.value = yearsMonths.getMonths();
    }

    @Override
    public void setPlaceHolder(final String placeholder) {

        final String attribute = "placeholder";
        yearInput.setAttribute(attribute, placeholder);
        monthInput.setAttribute(attribute, placeholder);
    }

    @EventHandler("years-input")
    public void onYearsInputBlur(final BlurEvent blurEvent) {
        handle(blurEvent);
    }

    @EventHandler("months-input")
    public void onMonthsInputBlur(final BlurEvent blurEvent) {
        handle(blurEvent);
    }

    void handle(final BlurEvent blurEvent) {

        final Object target = getEventTarget(blurEvent);
        if (!Objects.isNull(onValueInputBlur) && !isYearsOrMonthsInput(target)) {
            onValueInputBlur.accept(blurEvent);
        }
    }

    boolean isYearsOrMonthsInput(final Object object) {
        return yearInput == object || monthInput == object;
    }

    @Override
    public void onValueChanged(final Consumer<Event> onValueChanged) {

        yearInput.onchange = event -> {
            onValueChanged.accept(event);
            return this;
        };

        monthInput.onchange = event -> {
            onValueChanged.accept(event);
            return this;
        };
    }

    @Override
    public void onValueInputBlur(final Consumer<BlurEvent> blurEvent) {
        this.onValueInputBlur = blurEvent;
    }

    @Override
    public void select() {
        yearInput.select();
    }

    Object getEventTarget(final BlurEvent blurEvent) {
        return blurEvent.getNativeEvent().getRelatedEventTarget();
    }
}
