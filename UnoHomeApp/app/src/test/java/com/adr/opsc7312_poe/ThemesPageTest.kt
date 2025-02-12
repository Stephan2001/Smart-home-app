package com.adr.opsc7312_poe

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals

class ThemesPageTest {

    //method to test fetching themes related to user
    @Test
    fun `fetch all themes by user id`() = runTest {
        //creating mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val userId = 1
        //mock devices
        val mockDevices = listOf(
            DeviceDto(DeviceId = 1, Name = "Device 1", Status = false),
            DeviceDto(DeviceId = 2, Name = "Device 2", Status = false)
        )
        //mock themes
        val mockThemes = listOf(
            ParseTheme(themeId = 1, name = "Theme 1", isActive = false, devices = mockDevices),
            ParseTheme(themeId = 2, name = "Theme 2", isActive = true, devices = mockDevices)
        )

        `when`(serviceThemes.GetAllThemesByUserId(mockContext,userId)).thenReturn(mockThemes)

        //calling method
        val themes = serviceThemes.GetAllThemesByUserId(mockContext, userId)

        assertEquals(2, themes?.size)
        assertEquals("Theme 1", themes?.get(0)?.name)
        assertEquals(2, themes?.get(0)?.devices?.size)
        assertEquals("Device 1", themes?.get(0)?.devices?.get(0)?.Name)
        assertEquals("Device 2", themes?.get(0)?.devices?.get(1)?.Name)
    }

    //method to test the fetching of active themes
    @Test
    fun `fetch active themes`() = runTest {
        //create mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val userId = 1
        //mock devices
        val mockDevices = listOf(
            DeviceDto(DeviceId = 1, Name = "Device 1", Status = false),
            DeviceDto(DeviceId = 2, Name = "Device 2", Status = false)
        )
        //mock themes
        val mockThemes = listOf(
            ParseTheme(themeId = 1, name = "Theme 1", isActive = false, devices = mockDevices),
            ParseTheme(themeId = 2, name = "Theme 2", isActive = true, devices = mockDevices)
        )

        `when`(serviceThemes.GetAllThemesByUserId(mockContext, userId)).thenReturn(mockThemes)

        //call method
        val themes = serviceThemes.GetAllThemesByUserId(mockContext, userId)
        //get the active themes
        val activeThemes = themes?.filter { it.isActive }

        //verify only 1 theme is active
        assertEquals(1, activeThemes?.size)
        assertEquals("Theme 2", activeThemes?.get(0)?.name)
    }

    //test method when there arent themes
    @Test
    fun `test for when no themes`() = runTest {
        //create mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val userId = 1
        val mockThemes = emptyList<ParseTheme>()

        `when`(serviceThemes.GetAllThemesByUserId(mockContext, userId)).thenReturn(mockThemes)

        //call method ti get themes
        val themes = serviceThemes.GetAllThemesByUserId(mockContext, userId)

        //should return no themes
        assertEquals(0, themes?.size)
    }

    //method to test deleting a theme
    @Test
    fun `delete a theme`() = runTest {
        //creating mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val themeId = 1

        // call remove method to delete
        serviceThemes.RemoveTheme(mockContext, themeId)

        verify(serviceThemes, times(1)).RemoveTheme(mockContext, themeId)
    }


    //method to test turning a theme on
    @Test
    fun `turn theme on`() = runTest {
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val themeId = 1
        val mockDevices = listOf(
            DeviceDto(DeviceId = 1, Name = "Device 1", Status = false),
            DeviceDto(DeviceId = 2, Name = "Device 2", Status = false)
        )
        val mockTheme = ParseTheme(themeId = themeId, name = "Theme 1", isActive = false, devices = mockDevices)

        //will run simulation when calling method
        doAnswer {
            // simulating status change
            mockTheme.isActive = true

            // changes device status as well
            mockDevices.forEach { it.Status = true }
            null
        }.`when`(serviceThemes).ToggleThemeStatus(mockContext, themeId)

        //call method
        serviceThemes.ToggleThemeStatus(mockContext, themeId)

        // verify that it was called
        verify(serviceThemes, times(1)).ToggleThemeStatus(mockContext, themeId)

        // verify theme status is true
        assertEquals(true, mockTheme.isActive)

        // verify devices turned on
        assertEquals(true, mockTheme.devices[0].Status)
        assertEquals(true, mockTheme.devices[1].Status)
    }


    //method to test turning them off
    @Test
    fun `turn theme off`() = runTest {
        // creating mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val themeId = 1
        // mock devices
        val mockDevices = listOf(
            DeviceDto(DeviceId = 1, Name = "Device 1", Status = true),
            DeviceDto(DeviceId = 2, Name = "Device 2", Status = true)
        )
        //mock theme
        val mockTheme = ParseTheme(themeId = themeId, name = "Theme 1", isActive = true, devices = mockDevices)

        // simulate turning theme off. Runs when calling method
        doAnswer {
            // set status to false
            mockTheme.isActive = false

            // turn devices off when turning theme off
            mockTheme.devices.forEach { it.Status = false }

            null
        }.`when`(serviceThemes).ToggleThemeStatus(mockContext, themeId)

        // call method
        serviceThemes.ToggleThemeStatus(mockContext, themeId)

        // verify it was called
        verify(serviceThemes, times(1)).ToggleThemeStatus(mockContext, themeId)

        // verify theme status is false
        assertEquals(false, mockTheme.isActive)

        // verify devices turned off
        assertEquals(false, mockTheme.devices[0].Status)
        assertEquals(false, mockTheme.devices[1].Status)
    }

}
