/*
 * File Name    : MenuItemDto.cs
 * Description  : Represents a single dynamic navigation link sent to the frontend.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-21
 */

namespace SmartGrid.API.DTOs.Auth
{
    public class MenuItemDto
    {
        public string Label { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public string Icon { get; set; } = string.Empty;
    }
}
