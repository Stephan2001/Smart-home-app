
# UnoHomeWebAPI

## Purpose of the API
This REST API enables users to control smart devices, view sensor readings, view log histories, set routines, and customize themes in their homes through our Mobile App.

---

## See our our App
- **URL**: https://github.com/HarrySquater/OPSC7312_POE?tab=readme-ov-file#unohome-app

---

## Architecture Design Considerations
- **Database**: Relational SQL Database hosted on Azure
- **API**: .NET Core REST API

---

## API Base URLs

- **Devices Base URL**: https://unohomewebapiappservice.azurewebsites.net/api/Devices
- **Routines Base URL**: https://unohomewebapiappservice.azurewebsites.net/api/Routine
- **Themes Base URL**: https://unohomewebapiappservice.azurewebsites.net/api/Themes
- **User Base URL**: https://unohomewebapiappservice.azurewebsites.net/api/User
- **Sensors Base URL**: https://unohomewebapiappservice.azurewebsites.net/api/Sensors

---

# Devices

This controller is responsible for managing user devices.

### 1. Get Device by ID
- **Endpoint**: GET /api/Devices/{id}
- **Example Request**: GET /api/Devices/1
- **Example Response**:
  ```json
  {
    "deviceId": 1,
    "name": "Smart Light",
    "status": true,
    "category": "Lighting"
  }
  ```

### 2. Toggle Device Status
- **Endpoint**: PUT /api/Devices/toggle/{id}
- **Example Request**: PUT /api/Devices/toggle/1
- **Example Response**:
  ```json
  {
    "message": "Device status updated successfully."
  }
  ```

### 3. Get Device Status
- **Endpoint**: GET /api/Devices/status/{id}
- **Example Request**: GET /api/Devices/status/1
- **Example Response**:
  ```json
  {
    "status": true
  }
  ```

### 4. Get All Device Statuses
- **Endpoint**: GET /api/Devices/status/all
- **Example Request**: GET /api/Devices/status/all
- **Example Response**:
  ```json
  [
    {
      "name": "Smart Light",
      "status": true
    },
    {
      "name": "Smart Thermostat",
      "status": false
    }
  ]
  ```
  
### 5. Get All Devices
- **Endpoint**: GET /api/Devices/all
- **Example Request**: GET /api/Devices/all
- **Example Response**:
  ```json
  [
    {
      "DeviceId": "Smart Light",
      "Name": true,
	  "Category": "Lighting"
    },
    {
      "DeviceId": "Button",
      "Name": true,
	  "Category": "Action"
    }
  ]
  ```
  
### 6. Add device to user profile
- **Endpoint**: GET /api/Devices/addDevice
- **Example Request**:
  ```json
  {
	 "UserId": 123,
	 "DeviceId": 456
  }
	```
- **Example Response**:
  ```json
  {
    "message": "Device added to user profile."
  }
  ```
  
### 7. Remove device from user profile
- **Endpoint**: GET /api/Devices/deleteDevice/userId/deviceId
- **Example Request**: /api/Devices/deleteDevice/1/101
- **Example Response**:
  ```json
  {
    "message": "Device removed from profile."
  }
  ```

---

# Routines

This controller is responsible for managing user routines.

### 1. Create Routine
- **Endpoint**: POST /api/Routine
- **Example Request**: 
  ```json
  {
    "name": "Morning Routine",
    "description": "Routine to start devices in the morning",
    "status": true,
    "scheduling": "2023-09-29T07:00:00",
    "isActive": true,
    "userId": 1,
    "deviceIds": [1, 2, 3]
  }
  ```
- **Example Response**:
  ```json
  {
    "message": "Routines created successfully with added devices"
  }
  ```

### 2. Get Routine by ID
- **Endpoint**: GET /api/Routine/{id}
- **Example Request**: GET /api/Routine/1
- **Example Response**:
  ```json
  {
    "routineId": 1,
    "name": "Morning Routine",
    "description": "Routine to start devices in the morning",
    "status": true,
    "scheduling": "2023-09-29T07:00:00",
    "isActive": true,
    "devices": [
      {
        "deviceId": 1,
        "name": "Smart Light",
        "status": true
      },
      {
        "deviceId": 2,
        "name": "Smart Thermostat",
        "status": false
      }
    ]
  }
  ```

### 3. Get All Routines by User
- **Endpoint**: GET /api/Routine/user/{userId}
- **Example Request**: GET /api/Routine/user/1
- **Example Response**:
  ```json
  [
    {
      "routineId": 1,
      "name": "Morning Routine",
      "description": "Routine to start devices in the morning",
      "status": true,
      "scheduling": "2023-09-29T07:00:00",
      "isActive": true,
      "devices": [
        {
          "deviceId": 1,
          "name": "Smart Light",
          "status": true
        },
        {
          "deviceId": 2,
          "name": "Smart Thermostat",
          "status": false
        }
      ]
    },
    {
      "routineId": 2,
      "name": "Night Routine",
      "description": "Routine to turn off all devices at night",
      "status": false,
      "scheduling": "2023-09-29T21:00:00",
      "isActive": false,
      "devices": [
        {
          "deviceId": 3,
          "name": "Smart Lock",
          "status": true
        }
      ]
    }
  ]
  ```

### 4. Remove Routine
- **Endpoint**: DELETE /api/Routine/{id}
- **Example Request**: DELETE /api/Routine/1
- **Example Response**:
  ```json
  {
    "message": "Routine removed successfully."
  }
  ```
  
### 5. Toggle routine by routineId
- **Endpoint**: Put /api/Routine/toggle/{RoutineID}
- **Example Request**: Put /api/Routine/toggle/2
- **Example Response**:
  ```json
  {
    "message": "Routine status updated. Devices have been turned On"
  }
  ```
  
### 6. Update routine active status by routineId
- **Endpoint**: Put /api/Routine/toggle/status?id&newStatus
- **Example Request**: api/Routine/status?id=1&newStatus=true
- **Example Response**:
  ```json
  {
    "message": "Routine status updated to active"
  }
  ```

### 7. Toggle routine scheduling by routineId
- **Endpoint**: Put /api/Routine/1/scheduling?newScheduling
- **Example Request**: api/api/Routine/1/scheduling?newScheduling=08:00
- **Example Response**:
  ```json
  {
    "message": "Routine scheduling updated to 08:00"
  }
  ```

---

# Themes

This controller is responsible for managing user themes.

### 1. Create Theme
- **Endpoint**: POST /api/Themes/Create
- **Example Request**: 
  ```json
  {
    "Name": "Living Room Lights",
    "UserId": 1,
    "DeviceIds": [101, 102, 103]
  }
  ```
- **Example Response**:
  ```json
  {
    "message": "Theme created successfully with added devices."
  }
  ```

### 2. Toggle Theme Status
- **Endpoint**: PUT /api/Themes/ToggleStatus
- **Example Request**: PUT /api/Themes/ToggleStatus?ThemeID=1
- **Example Response**:
  ```json
  {
    "message": "Theme status updated. Devices have been turned on."
  }
  ```
  
### 3. Get Themes By UserId
- **Endpoint**: PUT /api/Themes/GetThemesByUserId
- **Example Request**: PUT /api/Themes/GetThemesByUserId?ThemeID=1
- **Example Response**:
  ```json
  {
    "ThemeId": 1,
    "Name": "Living Room Lights",
    "IsActive": true,
    "Devices": [
      {
        "DeviceId": 101,
        "Name": "Smart Bulb",
        "Status": true
      },
      {
        "DeviceId": 102,
        "Name": "Smart Speaker",
        "Status": true
      }
    ]
  }
  ```
  
### 4. Delete Theme
- **Endpoint**: DELETE /api/Themes/DeleteTheme
- **Example Request**: DELETE /api/Themes/DeleteTheme?ThemeID=1
- **Example Response**:
  ```json
  {
    "message": "Theme deleted."
  }
  ```

---

# User

This controller is responsible for managing user accounts.

### 1. Create User
- **Endpoint**: POST /api/User/Create
- **Example Request**:
  ```json
  {
    "Email": "john.doe@example.com"
  }
  ```
- **Example Response**:
  ```json
  {
    "message": "User already exists."
  }
  ```

### 2. Validate User
- **Endpoint**: GET /api/User/validate?email={email}
- **Example Request**: GET /api/User/validate?email=john.doe@example.com
- **Example Response**:
  ```json
  {
    "userId": 12
  }
  ```
 
---

  # Sensors

This controller is responsible for posting and retreiving sensor readings

### 1. Create Sensor Reading
- **Endpoint**: POST /api/Sensors/create
- **Example Request**:
  ```json
  {
    "reading": 97,
    "deviceName": "Gas Sensor"
  }
  ```


### 2. Get All Device Sensor Readings
- **Endpoint**: POST /api/Sensors/readings/{deviceName}
- **Example Request**:
  ```json
  {
    "deviceName": "Gas Sensor"
  }
  ```


- **Example Response**:
  ```json
  {
    "sensorId": 1,
    "sensorName": "Gas Sensor",
    "sensorReading1": 99,
    "sensorDate": "2024-11-02",
    "sensorTime": "14:54:44.2214831"
  },
  {
    "sensorId": 2,
    "sensorName": "Gas Sensor",
    "sensorReading1": 93,
    "sensorDate": "2024-11-02",
    "sensorTime": "14:55:22.9514662"
  },
  ```

### 3. Get Latest Reading for Sensor Device
- **Endpoint**: POST /api/Sensors/latestreading/{deviceName}
- **Example Request**:
  ```json
  {
    "deviceName": "Gas Sensor"
  }

- **Example Response**:
  ```json
  {
    "sensorId": 31,
    "sensorName": "Soil Sensor",
    "sensorReading1": 52,
    "sensorDate": "2024-11-03",
    "sensorTime": "00:11:34.7645155"
  }
  ```
