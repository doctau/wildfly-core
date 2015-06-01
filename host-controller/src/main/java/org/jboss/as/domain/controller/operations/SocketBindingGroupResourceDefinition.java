/*
 * JBoss, Home of Professional Open Source.
 * Copyright ${year}, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.domain.controller.operations;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.ListAttributeDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PrimitiveListAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.resource.AbstractSocketBindingGroupResourceDefinition;
import org.jboss.as.domain.controller.resources.SocketBindingResourceDefinition;
import org.jboss.as.server.services.net.LocalDestinationOutboundSocketBindingResourceDefinition;
import org.jboss.as.server.services.net.RemoteDestinationOutboundSocketBindingResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Kabir Khan
 */
public class SocketBindingGroupResourceDefinition extends AbstractSocketBindingGroupResourceDefinition {

    public static SocketBindingGroupResourceDefinition INSTANCE = new SocketBindingGroupResourceDefinition();

    public static final ListAttributeDefinition INCLUDES = new PrimitiveListAttributeDefinition.Builder(ModelDescriptionConstants.INCLUDES, ModelType.STRING)
            .setAllowNull(true)
            .setMinSize(0)
            .setMaxSize(Integer.MAX_VALUE)
            .setElementValidator(new StringLengthValidator(1, true))
            .setAttributeMarshaller(new AttributeMarshaller() {
                @Override
                public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
                    if (isMarshallable(attribute, resourceModel)) {
                        if (!isMarshallable(attribute, resourceModel, marshallDefault)) {
                            return;
                        }
                        boolean first = true;
                        StringBuilder sb = new StringBuilder();
                        for (ModelNode include : resourceModel.get(ModelDescriptionConstants.INCLUDES).asList()) {
                            if (first) {
                                first = false;
                            } else {
                                sb.append(" ");
                            }
                            sb.append(include.asString());
                        }
                        writer.writeAttribute(attribute.getXmlName(), sb.toString());
                    }
                }
            })
            .build();

    private SocketBindingGroupResourceDefinition() {
        super(SocketBindingGroupAddHandler.INSTANCE, DomainSocketBindingGroupRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        resourceRegistration.registerReadWriteAttribute(INCLUDES, null, createReferenceValidationHandler());
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(SocketBindingResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(RemoteDestinationOutboundSocketBindingResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(LocalDestinationOutboundSocketBindingResourceDefinition.INSTANCE);
    }

    public static OperationStepHandler createReferenceValidationHandler() {
        return new DomainReferenceValidationWriteAttributeHandler(INCLUDES);
    }

}
