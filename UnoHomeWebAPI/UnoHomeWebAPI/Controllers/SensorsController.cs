using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UnoHomeWebAPI.Models;

namespace UnoHomeWebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class SensorsController : ControllerBase
    {
        private readonly UnoHomeDatabaseContext _context;

        public SensorsController(UnoHomeDatabaseContext context)
        {
            _context = context;
        }

        //post a sensor reading
        [HttpPost("create")]
        public async Task<IActionResult> CreateSensorReading([FromBody] SensorReadingDto sensorReadingDto)
        {
            //sa time
            TimeZoneInfo southAfricaTimeZone = TimeZoneInfo.FindSystemTimeZoneById("South Africa Standard Time");

            //convert to sa time
            DateTime southAfricaDateTime = TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, southAfricaTimeZone);

            SensorReading sensorReading = new SensorReading
            {
                SensorDate = DateOnly.FromDateTime(southAfricaDateTime),
                SensorTime = TimeOnly.FromDateTime(southAfricaDateTime),
                SensorReading1 = sensorReadingDto.Reading,
                SensorName = sensorReadingDto.DeviceName
            };

            _context.SensorReadings.Add(sensorReading);
            await _context.SaveChangesAsync();

            return Ok("Reading created successfully");
        }


        //update sensor reading (probably wont use)
        [HttpPut("update/{id}")]
        public async Task<IActionResult> UpdateSensorReading(int id, [FromBody] double sensorReading)
        {
            var nowUtc = DateTime.UtcNow;

            TimeZoneInfo userTimeZone = TimeZoneInfo.Local;

            var userLocalTime = TimeZoneInfo.ConvertTimeFromUtc(nowUtc, userTimeZone);
            // find sensor
            var sensor = await _context.SensorReadings.FindAsync(id);

            // if device not found
            if (sensor == null)
            {
                return NotFound("Sensor not found.");
            }

            sensor.SensorReading1 = sensorReading;
            sensor.SensorDate = DateOnly.FromDateTime(userLocalTime);
            sensor.SensorTime = TimeOnly.FromDateTime(userLocalTime);

            await _context.SaveChangesAsync();
            

            return Ok();
        }

        //get all sensor readings of device
        [HttpGet("readings/{deviceName}")]
        public async Task<IActionResult> GetSensorReadingsByDeviceName(string deviceName)
        {
            var sensorReadings = await _context.SensorReadings
                .Where(sr => sr.SensorName == deviceName)
                .ToListAsync();

            if (sensorReadings == null || sensorReadings.Count == 0)
            {
                return NotFound($"No sensor readings found for device '{deviceName}'.");
            }

            return Ok(sensorReadings);
        }


        //get latest device sensor reading
        [HttpGet("latestreading/{deviceName}")]
        public async Task<IActionResult> GetLatestSensorReading(string deviceName)
        {
            var latestSensorReading = await _context.SensorReadings
                .Where(sr => sr.SensorName == deviceName)
                .OrderByDescending(sr => sr.SensorDate)
                .ThenByDescending(sr => sr.SensorTime)
                .FirstOrDefaultAsync();

            if (latestSensorReading == null)
            {
                return NotFound($"No sensor readings found for device '{deviceName}'.");
            }

            return Ok(latestSensorReading);
        }
    }
}
