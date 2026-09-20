/*
 * File Name    : VerifyQrRequestDto.cs
 * Description  : Represents the request data required to verify a prosumer transaction QR code.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

namespace SmartGrid.API.DTOs.GridOperations
{
    public class VerifyQrRequestDto
    {
        public string QrCode { get; set; } = string.Empty;
        public string OperatorId { get; set; } = string.Empty;
    }
}
