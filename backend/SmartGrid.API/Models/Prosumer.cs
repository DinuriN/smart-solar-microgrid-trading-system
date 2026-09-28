/*
 * File Name    : Prosumer.cs
 * Description  : Prosumer class that inherits from User.
* Author       : E G S U Kantha
 * IT Number    : IT23231832
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.Models
{
    public class Prosumer : User
    {
        public string Nic { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public bool DeactivationRequested { get; set; } = false;
        
        public Prosumer()
        {
            Role = UserRole.Prosumer; 
        }
    }
}
