using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class UserDevice
{
    public int UserDevicesId { get; set; }

    public int? UserId { get; set; }

    public int? DeviceId { get; set; }

    public virtual Device? Device { get; set; }

    public virtual AppUser? User { get; set; }
}
