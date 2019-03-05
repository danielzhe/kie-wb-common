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

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class YearsMonthsValueConverterTest {

    private YearsMonthsValueConverter converter;

    @Mock
    private ClientTranslationService translationService;

    @Before
    public void setup() {
        converter = spy(new YearsMonthsValueConverter(translationService));

        when(translationService.getValue(YearsMonthsValueConverter.YEARS_TRANSLATION_KEY)).thenReturn("years");
        when(translationService.getValue(YearsMonthsValueConverter.MONTHS_TRANSLATION_KEY)).thenReturn("months");

        when(translationService.getValue(YearsMonthsValueConverter.YEARS_ABBREVIATED_TRANSLATION_KEY)).thenReturn("yrs");
        when(translationService.getValue(YearsMonthsValueConverter.MONTHS_ABBREVIATED_TRANSLATION_KEY)).thenReturn("mos");
    }

    @Test
    public void testFromDMNStringYearsMonthsSingleDigit() {

        final String input = "duration(\"P1Y2M\")";
        testFromDMNString(input, "1", "2");
    }

    @Test
    public void testFromDMNStringYearsMonthsMultipleDigits() {

        final String input = "duration(\"P12Y11M\")";
        testFromDMNString(input, "12", "11");
    }

    @Test
    public void testFromDMNStringYearsMonthsOnlyYearSingleDigit() {

        final String input = "duration(\"P1Y\")";
        testFromDMNString(input, "1", "");
    }

    @Test
    public void testFromDMNStringYearsMonthsOnlyYear() {

        final String input = "duration(\"P12Y\")";
        testFromDMNString(input, "12", "");
    }

    @Test
    public void testFromDMNStringYearsMonthsOnlyMonthSingleDigit() {

        final String input = "duration(\"P1M\")";
        testFromDMNString(input, "", "1");
    }

    @Test
    public void testFromDMNStringYearsMonthsOnlyMonth() {

        final String input = "duration(\"P12M\")";
        testFromDMNString(input, "", "12");
    }

    @Test
    public void testToDMNStringEmpty() {

        testToDMNString("", "", "");
    }

    @Test
    public void testToDMNStringOnlyMonth() {

        testToDMNString("", "1", "duration(\"P1M\")");
    }

    @Test
    public void testToDMNStringOnlyYear() {

        testToDMNString("1", "", "duration(\"P1Y\")");
    }

    @Test
    public void testToDMNStringYearAndMonth() {

        testToDMNString("2", "1", "duration(\"P2Y1M\")");
    }

    private void testToDMNString(final String years,
                                 final String months,
                                 final String expected) {

        final String actual = converter.toDMNString(years, months);

        assertEquals(expected, actual);
        verify(converter).matchSigns(any());
    }

    private void testFromDMNString(final String input,
                                   final String expectedYears,
                                   final String expectedMonths) {

        final YearsMonthsValue actual = converter.fromDMNString(input);

        assertEquals(expectedYears, actual.getYears());
        assertEquals(expectedMonths, actual.getMonths());
    }

    @Test
    public void testToDisplayOnlyMonths() {

        final String expected = "1 mos";
        final YearsMonthsValue value = new YearsMonthsValue();
        value.setMonths("1");

        testToDisplayValue(value, expected);
    }

    @Test
    public void testToDisplayOnlyYears() {

        final String expected = "1 yrs";
        final YearsMonthsValue value = new YearsMonthsValue();
        value.setYears("1");

        testToDisplayValue(value, expected);
    }

    @Test
    public void testToDisplayYearsAndMonths() {

        final String expected = "1 years, 2 months";
        final YearsMonthsValue value = new YearsMonthsValue();
        value.setYears("1");
        value.setMonths("2");

        testToDisplayValue(value, expected);
    }

    public void testToDisplayValue(final YearsMonthsValue yearsMonthsValue,
                                   final String expected) {

        final String actual = converter.toDisplayValue(yearsMonthsValue);

        assertEquals(expected, actual);
    }

    @Test
    public void testRemovePrefixAndSuffix() {

        final String input = "duration(\"P1Y2M\")";
        final String expected = "1Y2M";

        final String actual = converter.removePrefixAndSuffix(input);

        assertEquals(expected, actual);
    }

    @Test
    public void testAddPrefixAndSuffix() {

        final String expected = "duration(\"P1Y2M\")";
        final String input = "1Y2M";

        final String actual = converter.addPrefixAndSuffix(input);

        assertEquals(expected, actual);
    }

    @Test
    public void testMatchSignsNegativeYears() {

        testMatchSigns("-2", "1", -2, -1);
    }

    @Test
    public void testMatchSignsNegativeMonths() {

        testMatchSigns("2", "-1", -2, -1);
    }

    @Test
    public void testMatchSignsBothNegative() {

        testMatchSigns("-2", "-1", -2, -1);
    }

    @Test
    public void testMatchSignsBothPositive() {

        testMatchSigns("2", "1", 2, 1);
    }

    private void testMatchSigns(final String inputYear,
                                final String inputMonth,
                                final int expectedYear,
                                final int expectedMonth) {

        final YearsMonthsValue value = new YearsMonthsValue();
        value.setYears(inputYear);
        value.setMonths(inputMonth);

        converter.matchSigns(value);

        final int years = Integer.parseInt(value.getYears());
        final int months = Integer.parseInt(value.getMonths());

        assertEquals(expectedYear, years);
        assertEquals(expectedMonth, months);
    }
}