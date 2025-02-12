package com.adr.opsc7312_poe

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals


class DevicesPageTest {

    // method to test fetching devices related to a user profile
    @Test
    fun `fetch all devices by user id`() = runTest {
        // creating mock
        val serviceDevice = mock(ServiceDevice::class.java)
        val userId = 1
        val mockContext = mock(Context::class.java)

        // mock devices that will be "fetched"
        val mockDevices = listOf(
            ParseDevice(DeviceId = 1, Name = "Device 1", Status = false, Category = "Light"),
            ParseDevice(DeviceId = 2, Name = "Device 2", Status = true, Category = "Fan")
        )

        //when method called it should return the mock devices
        `when`(serviceDevice.GetAllUserDevices(mockContext, userId)).thenReturn(mockDevices)

        // calling method. Assigned to variable to verify outcomes
        val devices = serviceDevice.GetAllUserDevices(mockContext, userId)

        //verify
        assertEquals(2, devices?.size)
        assertEquals("Device 1", devices?.get(0)?.Name)
        assertEquals(false, devices?.get(0)?.Status)
        assertEquals("Device 2", devices?.get(1)?.Name)
        assertEquals(true, devices?.get(1)?.Status)
    }
}