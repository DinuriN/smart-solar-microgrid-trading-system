/*
 * File Name    : UpdateProsumerDto.cs
 * Description  : Used when a Prosumer updates their profile.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.DTOs.Prosumer
{
    public class UpdateProsumerDto
    {
        public string Name { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
    }
}
