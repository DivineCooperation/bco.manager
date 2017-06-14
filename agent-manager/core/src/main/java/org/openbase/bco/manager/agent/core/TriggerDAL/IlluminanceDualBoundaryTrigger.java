package org.openbase.bco.manager.agent.core.TriggerDAL;

/*-
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
import com.google.protobuf.GeneratedMessage;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.manager.agent.core.TriggerJUL.GenericValueDualBoundaryTrigger;
import org.openbase.jul.exception.InstantiationException;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

/**
 *
 * @author <a href="mailto:tmichalski@techfak.uni-bielefeld.de">Timo Michalski</a>
 * @param <UR> UnitRemote
 * @param <DT> DataType
 */
public class IlluminanceDualBoundaryTrigger<UR extends AbstractUnitRemote, DT extends GeneratedMessage> extends GenericValueDualBoundaryTrigger<UR, DT> {

    public IlluminanceDualBoundaryTrigger(UR unitRemote, double upperBoundary, double lowerIllumination, TriggerOperation triggerOperation) throws InstantiationException {
        super(unitRemote, upperBoundary, lowerIllumination, triggerOperation, ServiceType.ILLUMINANCE_STATE_SERVICE, "getIlluminance");
    }
}
