/*
 * File Name    : DashboardController.cs
 * Description  : Provides operational dashboard statistics for Grid Operators.
 *                TODO: Confirm final reservation status values and authorization
 *                rules with Members 1 and 3 before final integration.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.Services;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/dashboard")]

    public class DashboardController : ControllerBase
    {
        private readonly GridOperationsService _gridOperationsService;

        // Initializes the controller with the Grid Operations service
        public DashboardController(
            GridOperationsService gridOperationsService)
        {
            _gridOperationsService = gridOperationsService;
        }

        // Retrieves pending and approved-future reservation statistics
        [HttpGet("stats")]
        public async Task<IActionResult> GetDashboardStats()
        {
            var stats =
                await _gridOperationsService.GetDashboardStatsAsync();

            return Ok(stats);
        }
    }
}
