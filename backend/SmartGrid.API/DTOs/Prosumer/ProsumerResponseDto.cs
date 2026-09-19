/*
 * File Name    : ProsumerResponseDto.cs
 * Description  : Used when the API sends Prosumer data back to React/Android.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.DTOs.Prosumer
{
    public class ProsumerResponseDto
    {
        public string Nic { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public bool IsActive { get; set; }
        public bool DeactivationRequested { get; set; }
        public string Role { get; set; } = string.Empty;
    }
}
