/*
 * File Name    : AuthResponseDto.cs
 * Description  : What the API sends back after a successful login.
 * Author       : E G S U Kantha
 * IT Number    : IT23231832
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.DTOs.Auth
{
    public class AuthResponseDto
    {
        public string Token { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;


        // FAT Backend: Send the dynamic navigation menu to the Thin Client
        public System.Collections.Generic.List<MenuItemDto> Menu { get; set; } = new();
    }
}
