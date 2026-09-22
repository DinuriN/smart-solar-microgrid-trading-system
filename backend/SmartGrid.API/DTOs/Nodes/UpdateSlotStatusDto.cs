namespace SmartGrid.API.DTOs.Nodes
{
    public class UpdateSlotStatusDto
    {
        public string Status { get; set; } = string.Empty; // e.g., "Available", "Booked", "Maintenance"
    }
}