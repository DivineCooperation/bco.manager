package org.openbase.bco.manager.agent.core.preset;

/*
 * #%L
 * COMA AgentManager Core
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

import com.google.protobuf.GeneratedMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactoryImpl;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactory;
import org.openbase.bco.manager.agent.core.AbstractAgent;
import org.openbase.bco.manager.agent.core.AgentManagerController;
import org.openbase.bco.registry.device.lib.DeviceRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.rst.processing.MetaConfigVariableProvider;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import rst.homeautomation.control.agent.AgentConfigType.AgentConfig;
import rst.homeautomation.state.PowerStateType.PowerState;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class PowerStateSynchroniserAgent extends AbstractAgent {

    public static final String SOURCE_KEY = "SOURCE";
    public static final String TARGET_KEY = "TARGET";
    public static final String SOURCE_BEHAVIOUR_KEY = "SOURCE_BEHAVIOUR";
    public static final String TARGET_BEHAVIOUR_KEY = "TARGET_BEHAVIOUR";
    private static final PowerState ON = PowerState.newBuilder().setValue(PowerState.State.ON).build();
    private static final PowerState OFF = PowerState.newBuilder().setValue(PowerState.State.OFF).build();

    public enum PowerStateSyncBehaviour {

        ON,
        OFF,
        LAST_STATE;
    }

    private PowerState.State sourceLatestPowerState;
    /**
     * State that determines what the targets do if the source changes and vice
     * versa.
     *
     * OFF when all targets are off. ON when at least one target is one.
     */
    private PowerState.State targetLatestPowerState;
    private final List<UnitRemote> targetRemotes = new ArrayList<>();
    private UnitRemote sourceRemote;
    private PowerStateSyncBehaviour sourceBehaviour, targetBehaviour;
    private final UnitRemoteFactory factory;
    private final DeviceRegistry deviceRegistry;

    public PowerStateSynchroniserAgent() throws InstantiationException, CouldNotPerformException {
        super(true);
        this.factory = UnitRemoteFactoryImpl.getInstance();
        this.deviceRegistry = AgentManagerController.getInstance().getDeviceRegistry();
    }

    @Override
    public void init(AgentConfig config) throws InitializationException, InterruptedException {
        super.init(config);
        try {
            logger.info("Creating PowerStateSynchroniserAgent[" + config.getLabel() + "]");

//            final DeviceRegistryRemote deviceRegistryRemote = new DeviceRegistryRemote();
//            System.out.println("### init["+config.getLabel()+"]");
//            deviceRegistryRemote.init();
//            System.out.println("### activate["+config.getLabel()+"]");
//            deviceRegistryRemote.activate();
            MetaConfigVariableProvider configVariableProvider = new MetaConfigVariableProvider("PowerStateSynchroniserAgent", config.getMetaConfig());

            sourceRemote = factory.newInitializedInstance(deviceRegistry.getUnitConfigById(configVariableProvider.getValue(SOURCE_KEY)));
            int i = 1;
            String unitId;
            try {
                while (!(unitId = configVariableProvider.getValue(TARGET_KEY + "_" + i)).isEmpty()) {
                    logger.info("Found target id [" + unitId + "] with key [" + TARGET_KEY + "_" + i + "]");
                    targetRemotes.add(factory.newInitializedInstance(deviceRegistry.getUnitConfigById(unitId)));
                    i++;
                }
            } catch (NotAvailableException ex) {
                i--;
                logger.info("Found [" + i + "] target/s");
            }
            sourceBehaviour = PowerStateSyncBehaviour.valueOf(configVariableProvider.getValue(SOURCE_BEHAVIOUR_KEY));
            targetBehaviour = PowerStateSyncBehaviour.valueOf(configVariableProvider.getValue(TARGET_BEHAVIOUR_KEY));

            logger.info("Initializing observers");
            initObserver();
            //TODO mpohling: interrupted should be forwarded! Interface change needed!
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private void initObserver() {
        sourceRemote.addDataObserver(new Observer<GeneratedMessage>() {

            @Override
            public void update(final Observable<GeneratedMessage> source, GeneratedMessage data) throws Exception {
                sourceLatestPowerState = invokeGetPowerState(data).getValue();
                logger.info("Recieved new value [" + sourceLatestPowerState + "] for source");
                if (sourceLatestPowerState == PowerState.State.OFF) {
                    if (targetLatestPowerState != PowerState.State.OFF) {
                        for (UnitRemote targetRemote : targetRemotes) {
                            invokeSetPower(targetRemote, OFF);
                        }
                    }
                } else if (sourceLatestPowerState == PowerState.State.ON) {
                    switch (targetBehaviour) {
                        case OFF:
                            if (targetLatestPowerState != PowerState.State.OFF) {
                                for (UnitRemote targetRemote : targetRemotes) {
                                    invokeSetPower(targetRemote, OFF);
                                }
                            }
                            break;
                        case ON:
                            if (targetLatestPowerState != PowerState.State.ON) {
                                for (UnitRemote targetRemote : targetRemotes) {
                                    invokeSetPower(targetRemote, ON);
                                }
                            }
                            break;
                        case LAST_STATE:
                            break;
                    }
                }
            }
        });

        for (UnitRemote targetRemote : targetRemotes) {
            targetRemote.addDataObserver(new Observer<GeneratedMessage>() {

                @Override
                public void update(final Observable<GeneratedMessage> source, GeneratedMessage data) throws Exception {
                    PowerState.State newPowerState = invokeGetPowerState(data).getValue();
                    logger.info("Recieved new value [" + targetLatestPowerState + "] for target [" + source + "]");
                    if (!updateLatestTargetPowerState(newPowerState)) {
                        return;
                    }
                    if (targetLatestPowerState == PowerState.State.ON) {
                        if (sourceLatestPowerState != PowerState.State.ON) {
                            invokeSetPower(sourceRemote, ON);
                        }
                    } else if (targetLatestPowerState == PowerState.State.OFF) {
                        switch (sourceBehaviour) {
                            case OFF:
                                if (sourceLatestPowerState != PowerState.State.OFF) {
                                    invokeSetPower(sourceRemote, OFF);
                                }
                                break;
                            case ON:
                                if (sourceLatestPowerState != PowerState.State.ON) {
                                    invokeSetPower(sourceRemote, ON);
                                }
                                break;
                            case LAST_STATE:
                                break;
                        }
                    }
                }
            });
        }
    }

    /**
     *
     * @param powerState
     * @return if the latest target power state has changed
     * @throws CouldNotPerformException
     */
    private boolean updateLatestTargetPowerState(PowerState.State powerState) throws CouldNotPerformException {
        if (targetLatestPowerState == PowerState.State.UNKNOWN) {
            targetLatestPowerState = powerState;
            return true;
        }
        if (targetLatestPowerState == PowerState.State.OFF && powerState == PowerState.State.ON) {
            targetLatestPowerState = PowerState.State.ON;
            return true;
        }

        if (targetLatestPowerState == PowerState.State.ON && powerState == PowerState.State.OFF) {
            targetLatestPowerState = PowerState.State.OFF;
            for (UnitRemote targetRemote : targetRemotes) {
                if (invokeGetPowerState(targetRemote.getData()).getValue() == PowerState.State.ON) {
                    targetLatestPowerState = PowerState.State.ON;
                    break;
                }
            }
            return targetLatestPowerState == PowerState.State.OFF;
        }

        return false;
    }

    private void invokeSetPower(UnitRemote remote, PowerState powerState) {
        try {
            Method method = remote.getClass().getMethod("setPower", PowerState.class);
            method.invoke(remote, powerState);
        } catch (NoSuchMethodException ex) {
            logger.error("Remote [" + remote.getClass().getSimpleName() + "] has no set Power method");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.error("Could not invoke setPower method on remote [" + remote.getClass().getSimpleName() + "] with value [" + powerState + "]");
        }
    }

    private PowerState invokeGetPowerState(Object message) throws CouldNotPerformException {
        try {
            Method method = message.getClass().getMethod("getPowerState");
            return (PowerState) method.invoke(message);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not get powerState from message [" + message + "]", ex);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        logger.info("Activating [" + getClass().getSimpleName() + "]");
        super.activate();
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        logger.info("Deactivating [" + getClass().getSimpleName() + "]");
        super.deactivate();
    }

    @Override
    protected void execute() throws CouldNotPerformException, InterruptedException {
        logger.info("Executing PowerStateSynchroniser agent");
        sourceRemote.activate();
        sourceRemote.waitForData();
        String targetIds = "";
        targetLatestPowerState = PowerState.State.UNKNOWN;
        for (UnitRemote targetRemote : targetRemotes) {
            targetRemote.activate();
            targetRemote.waitForData();
            targetIds += "[" + targetRemote.getId() + "]";
            if ((targetLatestPowerState == PowerState.State.OFF || targetLatestPowerState == PowerState.State.UNKNOWN) && invokeGetPowerState(targetRemote.getData()).getValue() == PowerState.State.ON) {
                targetLatestPowerState = PowerState.State.ON;
            } else if (targetLatestPowerState == PowerState.State.UNKNOWN && invokeGetPowerState(targetRemote.getData()).getValue() == PowerState.State.OFF) {
                targetLatestPowerState = PowerState.State.OFF;
            }
        }
        logger.info("Source [" + sourceRemote.getId() + "], behaviour [" + sourceBehaviour + "]");
        logger.info("Targets [" + targetIds + "], behaviour [" + targetBehaviour + "]");
        sourceLatestPowerState = invokeGetPowerState(sourceRemote.getData()).getValue();
    }

    @Override
    protected void stop() throws CouldNotPerformException, InterruptedException {
        sourceRemote.deactivate();
        for (UnitRemote targetRemote : targetRemotes) {
            targetRemote.deactivate();
        }
    }

    public UnitRemote getSourceRemote() {
        return sourceRemote;
    }

    public List<UnitRemote> getTargetRemotes() {
        return targetRemotes;
    }

    public PowerStateSyncBehaviour getSourceBehaviour() {
        return sourceBehaviour;
    }

    public PowerStateSyncBehaviour getTargetBehaviour() {
        return targetBehaviour;
    }
}
