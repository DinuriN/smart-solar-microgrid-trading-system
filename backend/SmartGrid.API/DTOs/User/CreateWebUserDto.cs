/*
 * File Name    : CreateWebUserDto.cs
 * Description  : Used when a BackOffice user creates a GridOperator or another BackOffice user.
* Author       : E G S U Kantha
 * IT Number    : IT23231832
 * Date         : 2026-09-18
 */

using System.ComponentModel.DataAnnotations;
using SmartGrid.API.Models;

namespace SmartGrid.API.DTOs.User
{
    public class CreateWebUserDto
    {
        [Required(ErrorMessage = "Name is required.")]
        [StringLength(100, MinimumLength = 2, ErrorMessage = "Name must be between 2 and 100 characters.")]
        public string Name { get; set; } = string.Empty;

        [Required(ErrorMessage = "Email is required.")]
        [EmailAddress(ErrorMessage = "Invalid email address format.")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Phone number is required.")]
        [Phone(ErrorMessage = "Invalid phone number format.")]
        [StringLength(15, MinimumLength = 10, ErrorMessage = "Phone number must be between 10 and 15 characters.")]
        public string Phone { get; set; } = string.Empty;

        [Required(ErrorMessage = "Password is required.")]
        [StringLength(100, MinimumLength = 6, ErrorMessage = "Password must be at least 6 characters long.")]
        public string Password { get; set; } = string.Empty;

        [Required(ErrorMessage = "User Role is required.")]
        public UserRole Role { get; set; }
    }
}
