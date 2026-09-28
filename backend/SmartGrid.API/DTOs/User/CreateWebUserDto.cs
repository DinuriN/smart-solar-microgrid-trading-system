/*
 * File Name    : CreateWebUserDto.cs
 * Description  : Used when a BackOffice user creates a GridOperator or another BackOffice user.
 * Author       : E G S U Kantha
 * IT Number    : IT23231832
 * Date         : 2026-09-18
 */

using SmartGrid.API.Models;

namespace SmartGrid.API.DTOs.User
{
    public class CreateWebUserDto
    {
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public UserRole Role { get; set; }
    }
}
