/*
 * File Name    : GridOperator.cs
 * Description  : Grid Operator class that inherits from User.
 * Author       : E G S U Kantha
 * IT Number    : IT23231832
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
