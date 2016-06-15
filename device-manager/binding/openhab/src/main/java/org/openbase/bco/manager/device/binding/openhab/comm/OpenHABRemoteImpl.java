package org.openbase.bco.manager.device.binding.openhab.comm;

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
import org.openbase.bco.dal.lib.jp.JPHardwareSimulationMode;
import org.openbase.bco.manager.device.binding.openhab.execution.OpenHABCommandExecutor;
import org.openbase.bco.manager.device.binding.openhab.transform.OpenhabCommandTransformer;
import org.openbase.bco.manager.device.core.DeviceManagerController;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.extension.openhab.binding.AbstractOpenHABRemote;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;

/**
 * @author thuxohl
 * @author mpohling
 */
public class OpenHABRemoteImpl extends AbstractOpenHABRemote {

    private OpenHABCommandExecutor commandExecutor;

    public OpenHABRemoteImpl() throws InstantiationException, JPNotAvailableException {
        super(JPService.getProperty(JPHardwareSimulationMode.class).getValue());
    }

    @Override
    public void init(String itemFilter) throws InitializationException, InterruptedException {
        try {
            this.commandExecutor = new OpenHABCommandExecutor(DeviceManagerController.getDeviceManager().getUnitControllerRegistry());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
        super.init(itemFilter);
    }

    @Override
    public void internalReceiveUpdate(final OpenhabCommand command) throws CouldNotPerformException {
        try {
            //Ignore commands that are not for the device manager but for example for the scene registry
            if(!command.getItemBindingConfig().isEmpty()) {
                logger.debug("Ignoring item ["+command.getItem()+"] because itemBindingConfig ["+command.getItemBindingConfig()+"] is not empty");
                return;
            }
            commandExecutor.receiveUpdate(command);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Skip item update [" + command.getItem() + " = " + OpenhabCommandTransformer.getCommandData(command) + "]!", ex);
        }
    }

    @Override
    public void internalReceiveCommand(OpenhabCommand command) throws CouldNotPerformException {
        //TODO: this is just a hack
        // Why do all items from knx publish their new values as commands, check if this is configurable in ets
//        if(command.getItem().startsWith("Hager") || command.getItem().startsWith("Gire")) {
//            internalReceiveUpdate(command);
//        }
        // do nothing...
    }
}