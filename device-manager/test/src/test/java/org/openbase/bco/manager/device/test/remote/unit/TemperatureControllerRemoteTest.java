package org.openbase.bco.manager.device.test.remote.unit;

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
import java.util.concurrent.TimeUnit;
import org.openbase.bco.dal.lib.data.Location;
import org.openbase.bco.registry.mock.MockRegistryHolder;
import org.openbase.jps.core.JPService;
import org.openbase.bco.dal.lib.jp.JPHardwareSimulationMode;
import org.openbase.bco.dal.lib.layer.unit.TemperatureControllerController;
import org.openbase.bco.dal.remote.unit.TemperatureControllerRemote;
import org.openbase.bco.manager.device.core.DeviceManagerLauncher;
import org.openbase.bco.registry.mock.MockRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Remote;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thuxohl
 */
public class TemperatureControllerRemoteTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TemperatureControllerRemoteTest.class);

    private static TemperatureControllerRemote temperatureControllerRemote;
    private static DeviceManagerLauncher deviceManagerLauncher;
    private static MockRegistry registry;
    private static Location location;
    private static String label;

    public TemperatureControllerRemoteTest() {
    }

    @BeforeClass
    public static void setUpClass() throws InitializationException, InvalidStateException, InstantiationException, CouldNotPerformException, InterruptedException {
        JPService.registerProperty(JPHardwareSimulationMode.class, true);
        registry = MockRegistryHolder.newMockRegistry();

        deviceManagerLauncher = new DeviceManagerLauncher();
        deviceManagerLauncher.launch();
        deviceManagerLauncher.getDeviceManager().waitForInit(30, TimeUnit.SECONDS);

        location = new Location(registry.getLocation());
        label = MockRegistry.TEMPERATURE_CONTROLLER_LABEL;

        temperatureControllerRemote = new TemperatureControllerRemote();
        temperatureControllerRemote.init(label, location);
        temperatureControllerRemote.activate();
    }

    @AfterClass
    public static void tearDownClass() throws CouldNotPerformException {
        if (deviceManagerLauncher != null) {
            deviceManagerLauncher.shutdown();
        }
        if (temperatureControllerRemote != null) {
            temperatureControllerRemote.shutdown();
        }
        MockRegistryHolder.shutdownMockRegistry();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of notifyUpdated method, of class TemperatureSensorRemote.
     */
    @Ignore
    public void testNotifyUpdated() {
    }

    /**
     * Test of getTemperature method, of class TemperatureSensorRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testSetTargetTemperature() throws Exception {
        System.out.println("setTargetTemperature");
        double temperature = 42.0F;
        temperatureControllerRemote.setTargetTemperature(temperature).get();
        temperatureControllerRemote.requestData().get();
        Assert.assertEquals("The getter for the target temperature returns the wrong value!", temperature, temperatureControllerRemote.getTargetTemperature(), 0.1);
    }

    /**
     * Test of getTemperature method, of class TemperatureSensorRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testGetTargetTemperature() throws Exception {
        System.out.println("getTargetTemperature");
        double temperature = 3.141F;
        temperatureControllerRemote.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);
        ((TemperatureControllerController) deviceManagerLauncher.getDeviceManager().getUnitControllerRegistry().get(temperatureControllerRemote.getId())).updateTargetTemperatureProvider(temperature);
        temperatureControllerRemote.requestData().get();
        Assert.assertEquals("The getter for the target temperature returns the wrong value!", temperature, temperatureControllerRemote.getTargetTemperature(), 0.1);
    }

    /**
     * Test of getTemperature method, of class TemperatureSensorRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testGetTemperature() throws Exception {
        System.out.println("getTemperature");
        double temperature = 37.0F;
        temperatureControllerRemote.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);
        ((TemperatureControllerController) deviceManagerLauncher.getDeviceManager().getUnitControllerRegistry().get(temperatureControllerRemote.getId())).updateTemperatureProvider(temperature);
        temperatureControllerRemote.requestData().get();
        Assert.assertEquals("The getter for the temperature returns the wrong value!", temperature, temperatureControllerRemote.getTemperature(), 0.1);
    }
}