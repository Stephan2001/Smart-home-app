namespace UnoHomeWebAPI.Models
{
    public class ThemeDetailsDto
    {
        public int ThemeId { get; set; }

        public string Name { get; set; } = null!;

        public bool IsActive { get; set; }
        public List<DeviceDto> Devices { get; set; }
    }
}
