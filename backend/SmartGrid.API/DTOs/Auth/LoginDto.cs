/*
 * File Name    : LoginDto.cs
 * Description  : Used when ANY user logs into the system.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using System.ComponentModel.DataAnnotations;

namespace SmartGrid.API.DTOs.Auth
{
    public class LoginDto
    {
        [Required(ErrorMessage = "Email or NIC is required.")]
        public string EmailOrNic { get; set; } = string.Empty;

        [Required(ErrorMessage = "Password is required.")]
        public string Password { get; set; } = string.Empty;
    }
}
