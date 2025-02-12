namespace UnoHomeWebAPI.Models
{
    public class ThemeDto
    {
        public string Name { get; set; } = null!;
        public int UserId { get; set; }
        public List<int> DeviceIds { get; set; } = new List<int>();
    }
}
