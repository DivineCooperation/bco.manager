package org.openbase.bco.manager.device.binding.openhab.transform;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.rst.processing.MetaConfigVariableProvider;
import rst.homeautomation.service.ServiceConfigType;

/**
 *
 * @author Divine Threepwood
 */
public class ItemNameLoader {

    public static final String OPENHAB_BINDING_ITEM_ID = "OPENHAB_BINDING_ITEM_ID";

    public static String getItemName(final Service service, final ServiceConfigType.ServiceConfig serviceConfig) throws CouldNotPerformException {
        try {
            if (!serviceConfig.hasBindingServiceConfig()) {
                throw new NotAvailableException("binding service config");
            }

            if (!serviceConfig.getBindingServiceConfig().hasMetaConfig()) {
                throw new NotAvailableException("binding service config meta config");
            }

            MetaConfigVariableProvider metaConfigVariableProvider = new MetaConfigVariableProvider("BindingServiceConfig", serviceConfig.getBindingServiceConfig().getMetaConfig());
            
            
            return metaConfigVariableProvider.getValue(OPENHAB_BINDING_ITEM_ID);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate item name!", ex);
        }
    }
}