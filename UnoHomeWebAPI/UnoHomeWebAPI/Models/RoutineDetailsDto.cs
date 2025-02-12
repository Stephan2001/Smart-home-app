namespace UnoHomeWebAPI.Models
{
    public class RoutineDetailsDto
    {
        public int RoutineId { get; set; }
        public string Name { get; set; }
        public string? Description { get; set; }
        public bool Status { get; set; }
        public string Scheduling { get; set; }
        public bool IsActive { get; set; }
        public List<DeviceDto> Devices { get; set; } 
    }
}
