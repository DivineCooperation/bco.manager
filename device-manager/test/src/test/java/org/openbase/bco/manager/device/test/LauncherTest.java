package org.openbase.bco.manager.device.test;

/*
 * #%L
 * COMA DeviceManager Test
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


import org.openbase.bco.manager.device.core.DeviceManagerLauncher;
import org.openbase.bco.registry.mock.MockRegistry;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.state.MotionStateType;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class LauncherTest {

    private static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

    private static MockRegistry registry;

    public LauncherTest() {
    }

    @BeforeClass
    public static void setUpClass() throws org.openbase.jul.exception.InstantiationException, JPServiceException {
        registry = new MockRegistry();
    }

    @AfterClass
    public static void tearDownClass() {
        if (registry != null) {
            registry.shutdown();
        }
    }

    @Before
    public void setUp() throws InitializationException, org.openbase.jul.exception.InstantiationException {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of deactivate method, of class DeviceManagerLauncher.
     */
    @Test
    public void testShutdown() throws InitializationException, org.openbase.jul.exception.InstantiationException, CouldNotPerformException, Exception {
        DeviceManagerLauncher instance = new DeviceManagerLauncher();
        try {
            instance.launch();
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, logger);
        }
        instance.shutdown();
    }

    @Test
    public void testProtobuf() {
        MotionStateType.MotionState.Builder builder = MotionStateType.MotionState.newBuilder();
        builder.setValue(MotionStateType.MotionState.State.UNKNOWN);
        Assert.assertTrue(builder.getValue() == MotionStateType.MotionState.State.UNKNOWN);
        Assert.assertTrue(builder.build().getValue() == MotionStateType.MotionState.State.UNKNOWN);
        builder.setValue(MotionStateType.MotionState.State.MOVEMENT);
        Assert.assertTrue(builder.getValue() == MotionStateType.MotionState.State.MOVEMENT);
        Assert.assertTrue(builder.build().getValue() == MotionStateType.MotionState.State.MOVEMENT);
        builder.build();

        MotionStateType.MotionState.Builder clone = builder.clone();

        builder.setValue(MotionStateType.MotionState.State.NO_MOVEMENT);
        Assert.assertTrue(builder.getValue() == MotionStateType.MotionState.State.NO_MOVEMENT);
        Assert.assertTrue(builder.build().getValue() == MotionStateType.MotionState.State.NO_MOVEMENT);
        Assert.assertTrue(clone.getValue() == MotionStateType.MotionState.State.MOVEMENT);
        Assert.assertTrue(clone.build().getValue() == MotionStateType.MotionState.State.MOVEMENT);
        clone.setValue(MotionStateType.MotionState.State.UNKNOWN);
        Assert.assertTrue(builder.getValue() == MotionStateType.MotionState.State.NO_MOVEMENT);
        Assert.assertTrue(builder.build().getValue() == MotionStateType.MotionState.State.NO_MOVEMENT);
        Assert.assertTrue(clone.getValue() == MotionStateType.MotionState.State.UNKNOWN);
        Assert.assertTrue(clone.build().getValue() == MotionStateType.MotionState.State.UNKNOWN);
    }
}