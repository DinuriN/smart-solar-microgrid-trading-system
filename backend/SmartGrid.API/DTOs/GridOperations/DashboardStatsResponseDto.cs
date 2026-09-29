/*
 * File Name    : DashboardStatsResponseDto.cs
 * Description  : Represents operational statistics displayed on the Grid Operator dashboard.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

namespace SmartGrid.API.DTOs.GridOperations
{
    public class DashboardStatsResponseDto
    {
        public long PendingReservations { get; set; }
        public long ApprovedFutureReservations { get; set; }
    }
}
