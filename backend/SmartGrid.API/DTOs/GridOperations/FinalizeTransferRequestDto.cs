/*
 * File Name    : FinalizeTransferRequestDto.cs
 * Description  : Represents the request data required to finalize an energy transfer.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

namespace SmartGrid.API.DTOs.GridOperations
{
    public class FinalizeTransferRequestDto
    {
        public string ReservationId { get; set; } = string.Empty;
        public string OperatorId { get; set; } = string.Empty;
    }
}
