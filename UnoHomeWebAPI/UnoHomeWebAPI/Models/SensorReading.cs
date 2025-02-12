using System;
using System.Collections.Generic;

namespace UnoHomeWebAPI.Models;

public partial class SensorReading
{
    public int SensorId { get; set; }

    public string SensorName { get; set; } = null!;

    public double? SensorReading1 { get; set; }

    public DateOnly? SensorDate { get; set; }

    public TimeOnly? SensorTime { get; set; }
}
