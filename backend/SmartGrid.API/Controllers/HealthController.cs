using Microsoft.AspNetCore.Mvc;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class HealthController : ControllerBase
    {
        [HttpGet]
        [Route("/api/health")]
        public IActionResult Get()
        {
            return Ok(new { status = "API is running." });
        }
    }
}
