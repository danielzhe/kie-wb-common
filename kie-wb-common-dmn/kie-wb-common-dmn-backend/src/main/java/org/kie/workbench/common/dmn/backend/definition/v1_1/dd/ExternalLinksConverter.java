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

package org.kie.workbench.common.dmn.backend.definition.v1_1.dd;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.kie.dmn.backend.marshalling.v1_2.xstream.DMNModelInstrumentedBaseConverter;
import org.kie.dmn.model.api.DMNModelInstrumentedBase;

public class ExternalLinksConverter extends DMNModelInstrumentedBaseConverter {


    public ExternalLinksConverter(final XStream xstream) {
        super(xstream);
    }


    @Override
    protected void assignChildElement(final Object parent,
                                      final String nodeName,
                                      final Object child) {
        super.assignChildElement(parent, nodeName, child);



    }

    @Override
    protected void assignAttributes(final HierarchicalStreamReader reader,
                                    final Object parent) {
        super.assignAttributes(reader, parent);

        final ExternalLink componentWidths = (ExternalLink) parent;

        componentWidths.setName(reader.getAttribute("name"));
        componentWidths.setUrl(reader.getAttribute("url"));
    }

    @Override
    protected DMNModelInstrumentedBase createModelObject() {
        return new ExternalLink();
    }


    @Override
    protected void writeAttributes(final HierarchicalStreamWriter writer,
                                   final Object parent) {
        super.writeAttributes(writer, parent);
        final ExternalLink externalLink = (ExternalLink) parent;

        writer.addAttribute("url", externalLink.getUrl());
        writer.addAttribute("name", externalLink.getName());
    }

    @Override
    protected void writeChildren(final HierarchicalStreamWriter writer,
                                 final MarshallingContext context,
                                 final Object parent) {
        super.writeChildren(writer, context, parent);
    }

    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(ExternalLink.class);
    }
}
