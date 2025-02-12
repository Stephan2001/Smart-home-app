using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Threading.Tasks;
using UnoHomeWebAPI.Models;

namespace UnoHomeWebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DeviceController : ControllerBase
    {
        private readonly UnoHomeDatabaseContext _context;

        public DeviceController(UnoHomeDatabaseContext context)
        {
            _context = context;
        }

        //get device by its ID (api/Devices/id)
        [HttpGet("{id}")]
        public async Task<IActionResult> GetDeviceById(int id)
        {
            //TESTED (WORKS)

            //changed this to use a select query because it was returning empty arrays for themes, routines and userdevices
            //select the device's id, name, status and category
            var device = await _context.Devices
                .Where(d => d.DeviceId == id)
                .Select(d => new
                {
                    d.DeviceId,
                    d.Name,
                    d.Status,
                    d.Category
                })
                .FirstOrDefaultAsync();

            //if no device found with the id
            if (device == null)
            {
                return NotFound();
            }

            //return device details
            return Ok(device);
        }


        //toggle device on or off and update database
        [HttpPut("toggle/{id}")]
        public async Task<IActionResult> ChangeDeviceStatus(int id)
        {
            //TESTED (WORKS)

            // find device
            var device = await _context.Devices.FindAsync(id);

            // if device not found
            if (device == null)
            {
                return NotFound("Device not found.");
            }

            //turn device off if its on
            if(device.Status == true)
            {
                device.Status = false;
            }
            //turn device on if its off
            else if (device.Status == false)
            {
                device.Status = true;
            }

            // save status to database
            await _context.SaveChangesAsync();

            return Ok(new
            {
                Name = device.Name,
                Status = device.Status ? "on" : "off"
            });
        }



        //get the device status to update arduino and app UI. I want this its own method so its easier on the arduino and for when we start doing "offline mode"
        [HttpGet("status/{id}")]
        public async Task<IActionResult> GetDeviceStatus(int id)
        {
            //TESTED (WORKS)

            // find device
            var device = await _context.Devices.FindAsync(id);

            // if device not found
            if (device == null)
            {
                return NotFound("Device not found.");
            }

            //Im not sure if i should just return the status or the id and name too? Depends how we use it in the app and arduino
            // think only returning status is fine for this endpoint
            return Ok(new
            {
                device.Status
            });
        }


        [HttpGet("status/all")]
        public async Task<IActionResult> GetAllDeviceStatuses()
        {
            var excludedIds = new List<int> { 3, 4, 5, 6, 7, 8, 11 };

            // get device name and status
            var devices = await _context.Devices
                .Where(d => !excludedIds.Contains(d.DeviceId))
                .Select(d => new
                {
                    d.Name,
                    d.Status
                })
                .ToListAsync();

            // reutrn device name and status
            return Ok(devices);
        }


        //endpoint to get all devices (api/Devices/all)
        [HttpGet("all")]
        public async Task<IActionResult> GetAllDevices()
        {
            //TESTED (WORKS)

            var excludedIds = new List<int> { 3, 4, 5, 6, 7, 8, 11, 15 };
            //select all devices' name, id and category
            var devices = await _context.Devices
                .Where(d => !excludedIds.Contains(d.DeviceId))
                .Select(d => new
                {
                    d.DeviceId,
                    d.Name,
                    d.Category
                })
                .ToListAsync();

            return Ok(devices);
        }



        // add device to user profile (api/Devices/addDevice)
        [HttpPost("addDevice")]
        public async Task<IActionResult> AddDeviceToUserProfile([FromBody] UserDeviceDto userDeviceDto)
        {
            //TESTED (WORKS)

            //see if the deviceId exists (error handling)
            var existingDeivce = await _context.Devices.FirstOrDefaultAsync(d => d.DeviceId == userDeviceDto.DeviceId);
            if (existingDeivce == null)
            {
                return NotFound("Device not found.");
            }

            // see if the userId exists (error handling)
            var existingUser = await _context.AppUsers.FirstOrDefaultAsync(u => u.UserId == userDeviceDto.UserId);
            if (existingUser == null)
            {
                return NotFound("User not found.");
            }

            // see if the device has been added to the user profile
            var existingUserDevice = await _context.UserDevices.FirstOrDefaultAsync(ud => ud.UserId == userDeviceDto.UserId && ud.DeviceId == userDeviceDto.DeviceId);
            if (existingUserDevice != null)
            {
                return Conflict("Device has already been added to profile");
            }

            // create the relationship between user and device
            var userDevice = new UserDevice
            {
                UserId = userDeviceDto.UserId,
                DeviceId = userDeviceDto.DeviceId
            };

            //save to database
            _context.UserDevices.Add(userDevice);
            await _context.SaveChangesAsync();

            //if successfull
            return Ok("Device added to user profile.");
        }



        // delete device from profile (api/Devices/deleteDevice/userId/deviceId)
        [HttpDelete("deleteDevice/{userId}/{deviceId}")]
        public async Task<IActionResult> RemoveDevice(int userId, int deviceId)
        {

            //TESTED (WORKS)

            // see if the device is on the users profile (error handling)
            var userDevice = await _context.UserDevices.FirstOrDefaultAsync(ud => ud.UserId == userId && ud.DeviceId == deviceId);

            //if device is not on profile
            if (userDevice == null)
            {
                return NotFound("Device not added to profile");
            }

            // delete device from profile in database
            _context.UserDevices.Remove(userDevice);
            await _context.SaveChangesAsync();

            //if successful
            return Ok("Device removed from profile.");
        }



        // get devices linked to user
        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetAllUserDevices(int userId)
        {

            //TESTED (WORKS)

            // see if user exists (error handling)
            var userExists = await _context.AppUsers.FirstOrDefaultAsync(u => u.UserId == userId);
            if (userExists == null)
            {
                return NotFound("User not found.");
            }

            // fetch devices linked to user
            var devices = await _context.UserDevices
                .Where(ud => ud.UserId == userId)
                .Select(ud => new
                {
                    ud.DeviceId,
                    ud.Device.Name,
                    ud.Device.Status,
                    ud.Device.Category
                })
                .ToListAsync();

            //if no devices linked to profile
            if (devices.Count == 0)
            {
                return NotFound("No devices found");
            }

            //return devices
            return Ok(devices);
        }

        [HttpPut("panic")]
        public async Task<IActionResult> ChangeDeviceStatus()
        {
            var device = await _context.Devices.FindAsync(15);

            // Check if device exists
            if (device == null)
            {
                return NotFound("Device not found.");
            }

            // Set status to true
            device.Status = true;
            await _context.SaveChangesAsync();

            //background task for database
            _ = Task.Factory.StartNew(async () =>
            {
                using var scope = HttpContext.RequestServices.CreateScope();
                var scopedContext = scope.ServiceProvider.GetRequiredService<UnoHomeDatabaseContext>();

                await Task.Delay(10000); // 10 seconds
                var scopedDevice = await scopedContext.Devices.FindAsync(15);

                if (scopedDevice != null)
                {
                    scopedDevice.Status = false;
                    await scopedContext.SaveChangesAsync();
                }
            }, TaskCreationOptions.LongRunning);

            return Ok();
        }


    }
}
