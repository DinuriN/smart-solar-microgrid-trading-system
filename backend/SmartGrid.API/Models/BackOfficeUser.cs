/*
 * File Name    : BackOfficeUser.cs
 * Description  : Back Office User class that inherits from User.
 * Author       : E G S U Kantha
 * IT Number    : IT23231832
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.Models
{
    public class BackOfficeUser : User
    {
        public string CreatedBy { get; set; } = string.Empty;

        public BackOfficeUser()
        {
            Role = UserRole.BackOfficeUser;
        }
    }
}
