using Microsoft.AspNetCore.Mvc;
using UnoHomeWebAPI.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Routing;
using Microsoft.AspNetCore.Http;

namespace UnoHomeWebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class RoutineController : ControllerBase
    {
        private readonly UnoHomeDatabaseContext _context;

        public RoutineController(UnoHomeDatabaseContext context)
        {
            _context = context;
        }

        [HttpPost]
        public async Task<IActionResult> CreateRoutine([FromBody] RoutineDto routineDto)
        {
            // Validate the user
            var user = await _context.AppUsers.FindAsync(routineDto.UserId);
            if (user == null)
            {
                return NotFound("User not found.");
            }


            // Create the routine
            var routine = new Routine
            {
                Name = routineDto.Name,
                Description = routineDto.Description,
                Status = routineDto.Status,
                Scheduling = routineDto.Scheduling,
                IsActive = routineDto.IsActive,
                UserId = routineDto.UserId
            };

            _context.Routines.Add(routine);
            await _context.SaveChangesAsync();

            // Validate and create associations with devices
            if (routineDto.DeviceIds != null && routineDto.DeviceIds.Any())
            {
                foreach (var deviceId in routineDto.DeviceIds)
                {
                    var deviceRoutine = new DeviceRoutine
                    {
                        DeviceId = deviceId,
                        RoutineId = routine.RoutineId
                    };
                    _context.DeviceRoutines.Add(deviceRoutine);
                }

                await _context.SaveChangesAsync();
            }

            return Ok(routine.RoutineId);
        }

        // GET: api/Routine/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetRoutineById(int id)
        {
            var routine = await _context.Routines
                .Include(r => r.DeviceRoutines)
                    .ThenInclude(dr => dr.Device) 
                .FirstOrDefaultAsync(r => r.RoutineId == id);

            if (routine == null)
            {
                return NotFound();
            }

            // Map to DTO to return the response
            var routineDto = new RoutineDetailsDto
            {
                RoutineId = routine.RoutineId,
                Name = routine.Name,
                Description = routine.Description,
                Status = routine.Status,
                Scheduling = routine.Scheduling,
                IsActive = routine.IsActive,
                Devices = routine.DeviceRoutines.Select(dr => new DeviceDto
                {
                    DeviceId = dr.Device.DeviceId,
                    Name = dr.Device.Name,
                    Status = dr.Device.Status
                }).ToList()
            };

            return Ok(routineDto);
        }

        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetAllRoutinesByUserId(int userId)
        {
            // Fetch routines for the specified user
            var routines = await _context.Routines
                .Where(r => r.UserId == userId)
                .Include(r => r.DeviceRoutines)
                    .ThenInclude(dr => dr.Device)
                .ToListAsync();

            if (routines == null || !routines.Any())
            {
                return NotFound($"No routines found for user with ID {userId}");
            }

            // Map to RoutineDetailsDto to return the response
            var routineDto = routines.Select(routine => new RoutineDetailsDto
            {
                RoutineId = routine.RoutineId,
                Name = routine.Name,
                Description = routine.Description,
                Status = routine.Status,
                Scheduling = routine.Scheduling,
                IsActive = routine.IsActive,
                Devices = routine.DeviceRoutines.Select(dr => new DeviceDto
                {
                    DeviceId = dr.Device.DeviceId,
                    Name = dr.Device.Name,
                    Status = dr.Device.Status
                }).ToList()
            }).ToList();

            return Ok(routineDto);
        }


        // DELETE: api/Routine/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> RemoveRoutine(int id)
        {
            // Find the routine by id
            var routine = await _context.Routines.FindAsync(id);
            if (routine == null)
            {
                return NotFound("Routine not found.");
            }

            // Remove the routine from the database
            _context.Routines.Remove(routine);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        //Added this just so we can update UI/active status while routine is actively running
        [HttpPut("toggle/{RoutineID}")]
        public async Task<IActionResult> ToggleRoutineStatus(int RoutineID)
        {
            var routine = await _context.Routines
                .Include(r => r.DeviceRoutines)
                .ThenInclude(dr => dr.Device)
                .FirstOrDefaultAsync(t => t.RoutineId == RoutineID);

            if (routine == null)
            {
                return NotFound("Routine not found.");
            }

            routine.IsActive = !routine.IsActive;

            foreach (var deviceRoutine in routine.DeviceRoutines)
            {
                if (deviceRoutine.Device != null)
                {
                    deviceRoutine.Device.Status = routine.IsActive;
                }
            }

            await _context.SaveChangesAsync();

            return Ok(new
            {
                Name = routine.Name,
                Status = routine.IsActive ? "on" : "off"
            });
        }

        // need to add a enpoint for adding mroe devices to routine

        // PUT: api/Routine/status
        [HttpPut("status")]
        public async Task<IActionResult> UpdateRoutineStatus([FromQuery] int id, [FromQuery] bool newStatus)
        {
            // Find the routine by its ID
            var routine = await _context.Routines.FindAsync(id);
            if (routine == null)
            {
                return NotFound("Routine not found.");
            }

            // Update the status
            routine.Status = newStatus;

            // Save the changes
            await _context.SaveChangesAsync();

            return Ok($"Routine status updated to {(newStatus ? "active" : "inactive")}.");
        }

        // PUT: api/Routine/{id}/scheduling
        [HttpPut("{id}/scheduling")]
        public async Task<IActionResult> UpdateRoutineScheduling(int id, [FromQuery] string newScheduling)
        {
            // Find the routine by its ID
            var routine = await _context.Routines.FindAsync(id);
            if (routine == null)
            {
                return NotFound("Routine not found.");
            }

            // Update the scheduling field
            routine.Scheduling = newScheduling;

            // Save the changes
            await _context.SaveChangesAsync();

            return Ok($"Routine scheduling updated to {newScheduling}.");
        }
    }

}
