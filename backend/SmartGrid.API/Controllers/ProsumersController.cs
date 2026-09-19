/*
 * File Name    : ProsumersController.cs
 * Description  : Thin client controller that routes HTTP requests to the Prosumer Service.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.Prosumer;
using SmartGrid.API.Services;
using System;
using System.Threading.Tasks;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ProsumersController : ControllerBase
    {
        private readonly ProsumerService _prosumerService;

        // Constructor injects the business logic service.
        public ProsumersController(ProsumerService prosumerService)
        {
            _prosumerService = prosumerService;
        }

        // POST /api/prosumers/register
        // Registers a new prosumer.
        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterProsumerDto dto)
        {
            try
            {
                var result = await _prosumerService.RegisterAsync(dto);
                return Ok(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                // The service throws an exception if the NIC is duplicate. We catch it here and return HTTP 400.
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        // GET /api/prosumers/{nic}
        // Gets a prosumer's profile by their NIC.
        [HttpGet("{nic}")]
        public async Task<IActionResult> GetProfile(string nic)
        {
            var result = await _prosumerService.GetByNicAsync(nic);
            if (result == null)
                return NotFound(new { success = false, message = "Prosumer not found" });

            return Ok(new { success = true, data = result });
        }

        // PUT /api/prosumers/{nic}/profile
        // Updates a prosumer's profile (name, phone, address).
        [HttpPut("{nic}")]
        [HttpPut("{nic}/profile")]
        public async Task<IActionResult> UpdateProfile(string nic, [FromBody] UpdateProsumerDto dto)
        {
            var updated = await _prosumerService.UpdateProfileAsync(nic, dto);
            if (!updated)
                return NotFound(new { success = false, message = "Prosumer not found or no changes made." });

            return Ok(new { success = true, message = "Profile updated successfully." });
        }

        [HttpPatch("{nic}/request-deactivation")]
        public async Task<IActionResult> RequestDeactivation(string nic)
        {
            try
            {
                var prosumer = await _prosumerService.RequestDeactivationAsync(nic);
                return Ok(new { success = true, message = "Account deactivation requested successfully." });
            }
            catch (Exception ex)
            {
                return NotFound(new { success = false, message = ex.Message });
            }
        }

        // PATCH /api/prosumers/{nic}/deactivate
        // Deactivates a prosumer account (Grid Operator action).
        [HttpPatch("{nic}/deactivate")]
        [Authorize(Roles = "BackOfficeUser,GridOperator")]
        public async Task<IActionResult> Deactivate(string nic)
        {
            var success = await _prosumerService.SetActiveStatusAsync(nic, false);
            if (!success) return NotFound();
            return Ok(new { success = true, message = "Prosumer deactivated." });
        }

        // PATCH /api/prosumers/{nic}/reactivate
        // Reactivates a prosumer account.
        [HttpPatch("{nic}/reactivate")]
        [Authorize(Roles = "BackOfficeUser")]
        public async Task<IActionResult> Reactivate(string nic)
        {
            var success = await _prosumerService.SetActiveStatusAsync(nic, true);
            if (!success) return NotFound();
            return Ok(new { success = true, message = "Prosumer reactivated." });
        }
    }
}
