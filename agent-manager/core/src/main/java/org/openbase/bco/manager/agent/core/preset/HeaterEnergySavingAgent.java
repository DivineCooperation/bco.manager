package org.openbase.bco.manager.agent.core.preset;

/*
 * #%L
 * BCO Manager Agent Core
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.dal.remote.unit.connection.ConnectionRemote;
import org.openbase.bco.dal.remote.unit.location.LocationRemote;
import org.openbase.bco.manager.agent.core.AbstractAgentController;
import org.openbase.bco.manager.agent.core.TriggerDAL.AgentTriggerPool;
import org.openbase.bco.manager.agent.core.TriggerJUL.GenericTrigger;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.TemperatureStateType.TemperatureState;
import rst.domotic.state.WindowStateType.WindowState;
import rst.domotic.unit.UnitConfigType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.domotic.unit.connection.ConnectionConfigType;
import rst.domotic.unit.connection.ConnectionDataType.ConnectionData;

/**
 *
 * @author <a href="mailto:tmichalski@techfak.uni-bielefeld.de">Timo Michalski</a>
 */
public class HeaterEnergySavingAgent extends AbstractAgentController {

    private LocationRemote locationRemote;
    private Future<Void> setTemperatureFuture;
    private Map<UnitConfigType.UnitConfig, TemperatureState> previousTemperatureState;
    private final Observer<ActivationState> triggerHolderObserver;
    private final WindowState.State triggerState = WindowState.State.OPEN;

    public HeaterEnergySavingAgent() throws InstantiationException {
        super(HeaterEnergySavingAgent.class);

        triggerHolderObserver = (Observable<ActivationState> source, ActivationState data) -> {
            if (data.getValue().equals(ActivationState.State.ACTIVE)) {
                regulateHeater();
            } else if (setTemperatureFuture != null) {
                setTemperatureFuture.cancel(true);
                restoreTemperatureState();
            }
        };
    }

    @Override
    public void init(final UnitConfigType.UnitConfig config) throws InitializationException, InterruptedException {
        try {
            super.init(config);

            try {
                locationRemote = Units.getUnit(getConfig().getPlacementConfig().getLocationId(), true, Units.LOCATION);
            } catch (NotAvailableException ex) {
                throw new InitializationException("LocationRemote not available.", ex);
            }

            for (ConnectionRemote connectionRemote : locationRemote.getConnectionList(true)) {
                if (connectionRemote.getConfig().getConnectionConfig().getType().equals(ConnectionConfigType.ConnectionConfig.ConnectionType.WINDOW)) {
                    try {
                        GenericTrigger<ConnectionRemote, ConnectionData, WindowState.State> trigger = new GenericTrigger(connectionRemote, triggerState, ServiceType.WINDOW_STATE_SERVICE);
                        agentTriggerHolder.addTrigger(trigger, AgentTriggerPool.TriggerOperation.OR);
                    } catch (CouldNotPerformException ex) {
                        throw new InitializationException("Could not add agent to agentpool", ex);
                    }
                }
            }

            agentTriggerHolder.registerObserver(triggerHolderObserver);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException("Could not initialize Agent.", ex);
        }
    }

    @Override
    protected void execute() throws CouldNotPerformException, InterruptedException {
        logger.info("Activating [" + getConfig().getLabel() + "]");
        previousTemperatureState = new HashMap<>();
        agentTriggerHolder.activate();
    }

    @Override
    protected void stop() throws CouldNotPerformException, InterruptedException {
        logger.info("Deactivating [" + getConfig().getLabel() + "]");
        agentTriggerHolder.deactivate();
    }

    @Override
    public void shutdown() {
        agentTriggerHolder.deregisterObserver(triggerHolderObserver);
        agentTriggerHolder.shutdown();
        super.shutdown();
    }

    private void regulateHeater() {
        try {
            for (UnitConfigType.UnitConfig temperatureControllerConfig : Registries.getLocationRegistry().getUnitConfigsByLocationLabel(UnitType.TEMPERATURE_CONTROLLER, locationRemote.getLabel())) {
                try {
                    previousTemperatureState.put(temperatureControllerConfig, Units.getUnit(temperatureControllerConfig, true, Units.TEMPERATURE_CONTROLLER).getTargetTemperatureState());
                } catch (NotAvailableException | InterruptedException ex) {
                    logger.error("Could not set targetTemperatureState of [ " + temperatureControllerConfig.getId() + "]");
                }
            }
        } catch (CouldNotPerformException | InterruptedException ex) {
            logger.error("Could not get TemperatureControllerConfigs.");
        }

        try {
            setTemperatureFuture = locationRemote.setTargetTemperatureState(TemperatureState.newBuilder().setTemperature(13.0).build());
        } catch (CouldNotPerformException ex) {
            logger.error("Could not set targetTemperatureState.");
        }
    }

    private void restoreTemperatureState() {
        if (!previousTemperatureState.isEmpty()) {
            for (Map.Entry<UnitConfigType.UnitConfig, TemperatureState> entry : previousTemperatureState.entrySet()) {
                try {
                    Units.getUnit(entry.getKey(), true, Units.TEMPERATURE_CONTROLLER).setTargetTemperatureState(entry.getValue());
                } catch (InterruptedException | CouldNotPerformException ex) {
                    logger.error("Could not set targetTemperatureState of [ " + entry.getKey().getId() + "]");
                }
            }
        }
    }
}
