using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class DeviceRoutine
{
    public int DeviceRoutineId { get; set; }

    public int? DeviceId { get; set; }

    public int? RoutineId { get; set; }

    public virtual Device? Device { get; set; }

    public virtual Routine? Routine { get; set; }
}
