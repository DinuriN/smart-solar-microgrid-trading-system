/*
 * File Name    : UpdateProsumerDto.cs
 * Description  : Used when a Prosumer updates their profile.
 * Author       : E G S U Kantha
 * IT Number    : IT23231832
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
