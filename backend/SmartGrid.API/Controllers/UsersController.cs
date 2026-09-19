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
    }
}
