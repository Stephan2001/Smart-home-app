using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class Routine
{
    public int RoutineId { get; set; }

    public int? UserId { get; set; }

    public string Name { get; set; } = null!;

    public string? Description { get; set; }

    public bool Status { get; set; }

    public string? Scheduling { get; set; }

    public bool IsActive { get; set; }

    public virtual ICollection<DeviceRoutine> DeviceRoutines { get; set; } = new List<DeviceRoutine>();

    public virtual AppUser? User { get; set; }
}
