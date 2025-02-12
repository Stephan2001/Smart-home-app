using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Routing;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;
using UnoHomeWebAPI.Models;

namespace UnoHomeWebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ThemesController : ControllerBase
    {
        private readonly UnoHomeDatabaseContext _context;

        public ThemesController(UnoHomeDatabaseContext context)
        {
            _context = context;
        }

        //create theme
        [HttpPost("Create")]
        public async Task<IActionResult> CreateTheme([FromBody] ThemeDto createThemeDto)
        {
            //need validated user to createa a theme
            var user = await _context.AppUsers.FindAsync(createThemeDto.UserId);
            if (user == null)
            {
                return NotFound("User not found");
            }

            //creating theme
            var theme = new Theme
            {
                Name = createThemeDto.Name,
                UserId = createThemeDto.UserId,
                IsActive = false
            };

            //save created theme to database
            _context.Themes.Add(theme);
            await _context.SaveChangesAsync();


            // add devices to theme. used a foreach so we can populate using a list of devices (users will add multiple devices)
            foreach (var deviceId in createThemeDto.DeviceIds)
            {
                var deviceTheme = new DeviceTheme
                {
                    ThemeId = theme.ThemeId,
                    DeviceId = deviceId
                };
                _context.DeviceThemes.Add(deviceTheme);
            }

            //save to database
            await _context.SaveChangesAsync();

            return Ok("Theme created successfully with added devices");
        }



        //turn theme on or off
        [HttpPut("ToggleStatus")]
        public async Task<IActionResult> ToggleThemeStatus(int ThemeID)
        {
            //gets the theme with the devices
            var theme = await _context.Themes
                .Include(t => t.DeviceThemes)
                .ThenInclude(dt => dt.Device)
                .FirstOrDefaultAsync(t => t.ThemeId == ThemeID);

            //if theme doesnt exist
            if (theme == null)
            {
                return NotFound("Theme not found");
            }
            if (theme.IsActive == true)
            {
                theme.IsActive = false;
            }
            else { theme.IsActive = true; }

            // turns devices on or off in the theme
            foreach (var deviceTheme in theme.DeviceThemes)
            {
                if (deviceTheme.Device != null)
                {
                    //set to on or off
                    deviceTheme.Device.Status = theme.IsActive; 
                }
            }

            //updates IsActive attribute
            await _context.SaveChangesAsync();

            return Ok(new
            {
                ThemeName = theme.Name,
                Status = theme.IsActive ? "on" : "off"
            });
        }



        [HttpGet("User/{userId}")]
        public async Task<IActionResult> GetThemesByUserId(int userId)
        {
            
            var themes = await _context.Themes
                    .Where(t => t.UserId == userId)
                    .Include(t => t.DeviceThemes)
                    .ThenInclude(dt => dt.Device)
                    .ToListAsync();

                if (themes == null || !themes.Any())
                {
                    return NotFound("No themes found");
                }

                // Map to RoutineDetailsDto to return the response
                var routineDto = themes.Select(theme => new ThemeDetailsDto
                {
                    ThemeId = theme.ThemeId,
                    Name = theme.Name,
                    IsActive = theme.IsActive,
                    Devices = theme.DeviceThemes.Select(dr => new DeviceDto
                    {
                        DeviceId = dr.Device.DeviceId,
                        Name = dr.Device.Name,
                        Status = dr.Device.Status
                    }).ToList()
                }).ToList();

            return Ok(routineDto);
            
        }

        //delete theme
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteTheme(int id)
        {
            //find theme being deleted
            var theme = await _context.Themes
                .Include(t => t.DeviceThemes)
                .FirstOrDefaultAsync(t => t.ThemeId == id);

            //if theme isnt found
            if (theme == null)
            {
                return NotFound("Theme not found");
            }

            // remove DeviceTheme entries
            _context.DeviceThemes.RemoveRange(theme.DeviceThemes);

            // remove theme entry
            _context.Themes.Remove(theme);

            //update table
            await _context.SaveChangesAsync();

            //if deleted successfully
            return NoContent();
        }

        // need to add a enpoint for adding mroe devices to theme
    }
}
