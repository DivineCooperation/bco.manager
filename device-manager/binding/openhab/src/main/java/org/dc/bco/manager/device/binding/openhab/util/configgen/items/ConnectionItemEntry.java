/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.device.binding.openhab.util.configgen.items;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.dc.bco.manager.device.binding.openhab.util.configgen.GroupEntry;
import static org.dc.bco.manager.device.binding.openhab.util.configgen.items.AbstractItemEntry.ITEM_SEGMENT_DELIMITER;
import static org.dc.bco.manager.device.binding.openhab.util.configgen.items.AbstractItemEntry.ITEM_SUBSEGMENT_DELIMITER;
import static org.dc.bco.manager.device.binding.openhab.util.configgen.items.LocationItemEntry.LOCATION_RSB_BINDING_CONFIG;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.rsb.scope.ScopeGenerator;
import org.dc.jul.processing.StringProcessor;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.spatial.ConnectionConfigType.ConnectionConfig;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class ConnectionItemEntry extends AbstractItemEntry {

    public static String CONNECTION_GROUP_LABEL = "Connections";

    public ConnectionItemEntry(final ConnectionConfig connectionConfig, final ServiceType serviceType) throws org.dc.jul.exception.InstantiationException {
        super();
        try {
            this.itemId = generateItemId(connectionConfig, serviceType);
            this.icon = "";
            this.commandType = getDefaultCommand(serviceType);
            this.label = connectionConfig.getLabel();
            this.itemHardwareConfig = "rsb=\"" + LOCATION_RSB_BINDING_CONFIG + ":" + connectionConfig.getId() + "\"";
            groups.add(CONNECTION_GROUP_LABEL);
            groups.add(GroupEntry.generateGroupID(connectionConfig.getScope()));
            calculateGaps();
        } catch (CouldNotPerformException ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    private String generateItemId(ConnectionConfig connectionConfig, ServiceType serviceType) throws CouldNotPerformException {
        return StringProcessor.transformToIdString("Connection")
                + ITEM_SEGMENT_DELIMITER
                + StringProcessor.transformUpperCaseToCamelCase(serviceType.name())
                + ITEM_SEGMENT_DELIMITER
                + ScopeGenerator.generateStringRepWithDelimiter(connectionConfig.getScope(), ITEM_SUBSEGMENT_DELIMITER);
    }
}