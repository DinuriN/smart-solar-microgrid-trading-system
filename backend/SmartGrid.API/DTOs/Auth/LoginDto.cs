/*
 * File Name    : LoginDto.cs
 * Description  : Used when ANY user logs into the system.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.DTOs.Auth
{
    public class LoginDto
    {
        public string EmailOrNic { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }
}
