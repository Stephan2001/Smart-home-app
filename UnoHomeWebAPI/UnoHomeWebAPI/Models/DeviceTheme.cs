using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class DeviceTheme
{
    public int DeviceThemeId { get; set; }

    public int? DeviceId { get; set; }

    public int? ThemeId { get; set; }

    public virtual Device? Device { get; set; }

    public virtual Theme? Theme { get; set; }
}
