package com.adr.opsc7312_poe

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals

class AddDeleteDeviceTest {

    // method to test adding a device to a user profile
    @Test
    fun `add device to user profile`() = runTest {
        // creating mock
        val serviceDevice = mock(ServiceDevice::class.java)
        val mockContext = mock(Context::class.java)
        val userId = 1
        val deviceId = 8

        // simulating list of devices
        val userDevices = mutableListOf<ParseDevice>()

        // mock device
        val mockDevice = ParseDevice(DeviceId = deviceId, Name = "Device 1", Status = true, Category = "Light")

        // simulate adding device when method is called
        doAnswer {
            userDevices.add(mockDevice)
            null
        }.`when`(serviceDevice).AddDevice(mockContext, userId, deviceId)

        // call add device method
        serviceDevice.AddDevice(mockContext, userId, deviceId)

        // verify method called with correct parameters
        verify(serviceDevice).AddDevice(mockContext, userId, deviceId)

        // verify that device has been added
        assertEquals(1, userDevices.size)
        assertEquals(mockDevice, userDevices[0])
        assertEquals("Device 1", userDevices[0].Name)
    }

    // method to test removing device from a user profile
    @Test
    fun `remove device from user profile`() = runTest {
        // creating mock
        val serviceDevice = mock(ServiceDevice::class.java)
        val userId = 1
        val deviceId = 100
        val mockContext = mock(Context::class.java)

        // mock device added to user profile
        val userDevices = mutableListOf(
            ParseDevice(DeviceId = deviceId, Name = "Mock Device", Status = true, Category = "Light")
        )

        // simulate removing device when method is called
        doAnswer {
            userDevices.removeIf { it.DeviceId == deviceId }
            null
        }.`when`(serviceDevice).RemoveDevice(mockContext, userId, deviceId)

        // call remove device method
        serviceDevice.RemoveDevice(mockContext, userId, deviceId)

        // verify method is called with correct parameters
        verify(serviceDevice).RemoveDevice(mockContext, userId, deviceId)

        // verify that device was removed
        assertEquals(0, userDevices.size)
    }

}