/*
 * File Name    : GridOperator.cs
 * Description  : Grid Operator class that inherits from User.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

namespace SmartGrid.API.Models
{
    public class GridOperator : User
    {
        public GridOperator()
        {
            Role = UserRole.GridOperator;
        }
    }
}
