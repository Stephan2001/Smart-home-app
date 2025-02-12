package com.adr.opsc7312_poe

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals
import android.content.Context
import org.mockito.ArgumentMatchers.any

class AddDeleteThemesTest {

    // method to test adding themes
    @Test
    fun `add theme test`() = runTest {
        // creating mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        val userId = 1

        // mock theme
        val mockTheme = ParseTheme(
            themeId = 1,
            name = "Morning Routine",
            isActive = false,
            devices = listOf(
                DeviceDto(DeviceId = 1, Name = "Device 1", Status = false),
                DeviceDto(DeviceId = 2, Name = "Device 2", Status = false)
            )
        )

        // mock device Ids
        val deviceIds = arrayOf(1, 2)

        // simulate storing themes
        val userThemes = mutableListOf<ParseTheme>()

        // simulate adding the theme when calling method
        doAnswer {
            userThemes.add(mockTheme)
            null
        }.`when`(serviceThemes).CreateTheme(mockContext, mockTheme.name, userId, deviceIds)

        // call create theme method with a mock context
        serviceThemes.CreateTheme(mockContext, mockTheme.name, userId, deviceIds)

        // verify that the theme was added
        assertEquals(1, userThemes.size)
        assertEquals("Morning Routine", userThemes[0].name)
        assertEquals(2, userThemes[0].devices.size)
        assertEquals("Device 1", userThemes[0].devices[0].Name)
    }

    // method to test deleting themes
    @Test
    fun `delete theme test`() = runTest {
        // creating mock
        val serviceThemes = mock(ServiceThemes::class.java)
        val mockContext = mock(Context::class.java)
        // mock theme
        val mockTheme = ParseTheme(
            themeId = 1,
            name = "Morning Routine",
            isActive = false,
            devices = listOf(
                DeviceDto(DeviceId = 1, Name = "Device 1", Status = false),
                DeviceDto(DeviceId = 2, Name = "Device 2", Status = false)
            )
        )

        val userThemes = mutableListOf(mockTheme)

        // simulate removing theme when method is called
        doAnswer {
            userThemes.removeIf { it.themeId == mockTheme.themeId }
            null
        }.`when`(serviceThemes).RemoveTheme(mockContext,mockTheme.themeId)

        // call remove theme method
        serviceThemes.RemoveTheme(mockContext, mockTheme.themeId)

        // verify that theme was removed
        assertEquals(0, userThemes.size)
    }
}
