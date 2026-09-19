/*
 * File Name    : RegisterProsumerDto.cs
 * Description  : Used when a Prosumer registers for the first time.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.DTOs.Prosumer
{
    public class RegisterProsumerDto
    {
        public string Nic { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }
}
