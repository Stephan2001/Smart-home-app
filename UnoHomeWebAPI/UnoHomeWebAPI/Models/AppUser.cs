using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class AppUser
{
    public int UserId { get; set; }

    public string Email { get; set; } = null!;

    public string UserPassword { get; set; } = null!;

    public virtual ICollection<Routine> Routines { get; set; } = new List<Routine>();

    public virtual ICollection<Theme> Themes { get; set; } = new List<Theme>();

    public virtual ICollection<UserDevice> UserDevices { get; set; } = new List<UserDevice>();
}
