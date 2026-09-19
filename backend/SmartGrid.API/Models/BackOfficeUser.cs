/*
 * File Name    : BackOfficeUser.cs
 * Description  : Back Office User class that inherits from User.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
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
