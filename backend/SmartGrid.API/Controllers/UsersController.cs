/*
 * File Name    : UsersController.cs
 * Description  : Thin client controller for Web User creation.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.User;
using SmartGrid.API.Services;
using System;
using System.Security.Claims;
using System.Threading.Tasks;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/users")]
    public class UsersController : ControllerBase
    {
        private readonly UserService _userService;

        public UsersController(UserService userService)
        {
            _userService = userService;
        }

        [HttpPost]
        [Authorize(Roles = "BackOfficeUser")]
        public async Task<IActionResult> CreateWebUser([FromBody] CreateWebUserDto dto)
        {
            try
            {
                var createdBy = User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? "Unknown";
                
                var result = await _userService.CreateWebUserAsync(dto, createdBy);
                return Ok(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }
//fetch all web users
        [HttpGet]
        [Authorize(Roles="BackOfficeUser")]
        public async Task<IActionResult> GetAllWebUsers()
        {
            try{
                var users = await _userService.GetAllWebUsersAsync();
                return Ok (new{success= true, data = users});
            }
            catch(Exception ex)
            {
                return StatusCode(500,new{success = false, message = ex.Message});
            }
        }
//reset users password
        [HttpPut("{id}/reset-password")]
        [Authorize(Roles="BackOfficeUser")]
        public async Task<IActionResult> ResetPassword(string id, [FromBody] UpdatePasswordDto dto)
        {
            try{
                var success = await _userService.ResetPasswordAsync(id, dto.NewPassword);
                if(!success) return NotFound(new {success = false, message ="User not found or password not reset."});
                return Ok(new {success=true, message="Password reset successfully."});
            }
            catch(Exception ex)
            {
                return BadRequest(new{success =false, message= ex.Message});
            }
        }
        



    }
}
