using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UnoHomeWebAPI.Models;

namespace UnoHomeWebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly UnoHomeDatabaseContext _context;

        public UserController(UnoHomeDatabaseContext context)
        {
            _context = context;
        }

        [HttpPost("Create")]
        public async Task<IActionResult> CreateUser([FromBody] AppUserDto userDto)
        {
            //if user already exists
            var existingUser = await _context.AppUsers
                    .FirstOrDefaultAsync(u => u.Email == userDto.Email);
            if (existingUser != null)
            {
                return Ok("User already exists");
            }

            //creating user -> not sure here, do we even specify null or leave them
            var Appuser = new AppUser
            {
                Email = userDto.Email,
                UserPassword = "I amm empty for now :("
            };

            //save created theme to database
            _context.AppUsers.Add(Appuser);
            await _context.SaveChangesAsync();

            return Ok("User created successfully");
        }

        [HttpGet("validate")]
        public async Task<IActionResult> ValidateUser(string email)
        {
            email = email.ToLower();
            if (string.IsNullOrEmpty(email))
            {
                return BadRequest("Email is required.");
            }

            // Look for a user with the given email
            var user = await _context.AppUsers.FirstOrDefaultAsync(u => u.Email == email);

            if (user == null)
            {
                return NotFound("User does not exist.");
            }

            return Ok(user.UserId);
        }

    }
}
