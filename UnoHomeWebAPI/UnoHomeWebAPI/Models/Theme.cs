using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class Theme
{
    public int ThemeId { get; set; }

    public int? UserId { get; set; }

    public string Name { get; set; } = null!;

    public bool Status { get; set; }

    public string? Devices { get; set; }

    public bool IsActive { get; set; }

    public virtual ICollection<DeviceTheme> DeviceThemes { get; set; } = new List<DeviceTheme>();

    public virtual AppUser? User { get; set; }
}
