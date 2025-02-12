namespace UnoHomeWebAPI.Models
{
    public class RoutineDto
    {
        public string Name { get; set; }
        public string? Description { get; set; }
        public bool Status { get; set; }
        public String Scheduling { get; set; }
        public bool IsActive { get; set; }
        public int? UserId { get; set; }
        public List<int>? DeviceIds { get; set; } 
    }

}
