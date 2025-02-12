using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class Device
{
    public int DeviceId { get; set; }

    public string Name { get; set; } = null!;

    public bool Status { get; set; }

    public string? Category { get; set; }

    public virtual ICollection<DeviceRoutine> DeviceRoutines { get; set; } = new List<DeviceRoutine>();

    public virtual ICollection<DeviceTheme> DeviceThemes { get; set; } = new List<DeviceTheme>();

    public virtual ICollection<UserDevice> UserDevices { get; set; } = new List<UserDevice>();
}
