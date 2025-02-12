package com.adr.opsc7312_poe


import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals

class AddDeleteRoutineTest {

    // method to test adding a routine
    @Test
    fun `add routine`() = runTest {
        // creating mock
        val serviceRoutine = mock(ServiceRoutine::class.java)
        val userId = 1
        val mockContext = mock(Context::class.java)

        // mock routine
        val mockRoutine = ParseRoutine(RoutineId = 1, Name = "Morning Routine", Description = "Routine", Status = true, Scheduling = "08:00", IsActive = true, Devices = listOf())

        // mock device to add to routine
        val mockDevice = DeviceDto(DeviceId = 1, Name = "Fan", Status = false)


        // simulate storing routine
        val userRoutines = mutableListOf<ParseRoutine>()

        // simulate adding routine when method is called
        doAnswer {
            userRoutines.add(mockRoutine.apply {
                Devices = Devices + mockDevice
            })
            null
        }.`when`(serviceRoutine).AddRoutine(mockContext, mockRoutine.Name, mockRoutine.Description ?: "", mockRoutine.Status, mockRoutine.Scheduling ?: "", mockRoutine.IsActive, userId, arrayOf(mockDevice.DeviceId), 1,1)

        // call add routine method
        serviceRoutine.AddRoutine(mockContext, mockRoutine.Name, mockRoutine.Description ?: "", mockRoutine.Status, mockRoutine.Scheduling ?: "", mockRoutine.IsActive, userId, arrayOf(mockDevice.DeviceId), 1,1)

        // verify that routine is added with the devices
        assertEquals(1, userRoutines.size)
        assertEquals("Morning Routine", userRoutines[0].Name)
        assertEquals("Routine", userRoutines[0].Description)
        assertEquals(true, userRoutines[0].Status)
        assertEquals("08:00", userRoutines[0].Scheduling)
        assertEquals(mockDevice.DeviceId, userRoutines[0].Devices[0].DeviceId)
        assertEquals(mockDevice.Name, userRoutines[0].Devices[0].Name)
        assertEquals(mockDevice.Status, userRoutines[0].Devices[0].Status)
    }

    // method to test deleting a routine
    @Test
    fun `delete routine`() = runTest {
        // creating mock
        val serviceRoutine = mock(ServiceRoutine::class.java)
        val mockContext = mock(Context::class.java)

        // mock routine
        val mockRoutine = ParseRoutine(RoutineId = 1, Name = "Morning Routine", Description = "A routine to start the day", Status = true, Scheduling = "08:00 AM", IsActive = true, Devices = listOf())

        // simulate storing routine
        val userRoutines = mutableListOf(mockRoutine)

        // simulate deleting a routine when calling method
        doAnswer {
            userRoutines.removeIf { it.RoutineId == mockRoutine.RoutineId }
            null
        }.`when`(serviceRoutine).RemoveRoutine(mockContext, mockRoutine.RoutineId)

        // Call remove routine method
        serviceRoutine.RemoveRoutine(mockContext, mockRoutine.RoutineId)

        // verify that routine was deleted
        assertEquals(0, userRoutines.size)
    }
}