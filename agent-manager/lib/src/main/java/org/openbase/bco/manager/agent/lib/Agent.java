package org.openbase.bco.manager.agent.lib;

/*
 * #%L
 * COMA AgentManager Library
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Configurable;
import org.openbase.jul.iface.Identifiable;
import rst.homeautomation.control.agent.AgentConfigType;
import rst.homeautomation.state.ActivationStateType;

/**
 *
 * @author mpohling
 */
public interface Agent extends Identifiable<String>, Configurable<String, AgentConfigType.AgentConfig> {

    public void setActivationState(ActivationStateType.ActivationState activation) throws CouldNotPerformException;
}