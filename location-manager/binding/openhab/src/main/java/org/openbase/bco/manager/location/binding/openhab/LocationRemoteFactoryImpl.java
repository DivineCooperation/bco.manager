package org.openbase.bco.manager.location.binding.openhab;

/*
 * #%L
 * COMA SceneManager Binding OpenHAB
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
import org.openbase.bco.manager.location.binding.openhab.execution.OpenHABCommandFactory;
import org.openbase.bco.manager.location.remote.LocationRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import static org.openbase.jul.extension.openhab.binding.AbstractOpenHABRemote.ITEM_SEGMENT_DELIMITER;
import static org.openbase.jul.extension.openhab.binding.AbstractOpenHABRemote.ITEM_SUBSEGMENT_DELIMITER;
import org.openbase.jul.extension.openhab.binding.interfaces.OpenHABRemote;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.processing.StringProcessor;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.LocationDataType.LocationData;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class LocationRemoteFactoryImpl implements Factory<LocationRemote, LocationConfig> {

    private OpenHABRemote openHABRemote;

    public LocationRemoteFactoryImpl() {
    }

    public void init(final OpenHABRemote openHABRemote) {
        this.openHABRemote = openHABRemote;
    }

    @Override
    public LocationRemote newInstance(LocationConfig config) throws org.openbase.jul.exception.InstantiationException, InterruptedException {
        LocationRemote locationRemote = new LocationRemote();
        try {
            locationRemote.addDataObserver(new Observer<LocationData>() {

                @Override
                public void update(final Observable<LocationData> source, LocationData data) throws Exception {
                    openHABRemote.postUpdate(OpenHABCommandFactory.newHSBCommand(data.getColor()).setItem(generateItemId(config, ServiceType.COLOR_SERVICE)).build());
                    openHABRemote.postUpdate(OpenHABCommandFactory.newOnOffCommand(data.getPowerState().getValue()).setItem(generateItemId(config, ServiceType.POWER_SERVICE)).build());
                    openHABRemote.postUpdate(OpenHABCommandFactory.newDecimalCommand(data.getPowerConsumptionState().getConsumption()).setItem(generateItemId(config, ServiceType.POWER_CONSUMPTION_PROVIDER)).build());
                }
            });
            locationRemote.init(config);
            locationRemote.activate();

            return locationRemote;
        } catch (CouldNotPerformException ex) {
            throw new org.openbase.jul.exception.InstantiationException(locationRemote, ex);
        }
    }

    //TODO: method is implemented in the openhab config generator and should be used from there
    private String generateItemId(LocationConfig locationConfig, ServiceType serviceType) throws CouldNotPerformException {
        return StringProcessor.transformToIdString("Location")
                + ITEM_SEGMENT_DELIMITER
                + StringProcessor.transformUpperCaseToCamelCase(serviceType.name())
                + ITEM_SEGMENT_DELIMITER
                + ScopeGenerator.generateStringRepWithDelimiter(locationConfig.getScope(), ITEM_SUBSEGMENT_DELIMITER);
    }
}