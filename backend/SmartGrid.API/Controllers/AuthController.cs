/*
 * File Name    : AuthController.cs
 * Description  : Thin client controller for Authentication.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.Auth;
using SmartGrid.API.Services;
using System;
using System.Threading.Tasks;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly AuthService _authService;

        public AuthController(AuthService authService)
        {
            _authService = authService;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto dto)
        {
            try
            {
                var result = await _authService.LoginAsync(dto);
                return Ok(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Unauthorized(new { success = false, message = ex.Message });
            }
        }
    }
}
