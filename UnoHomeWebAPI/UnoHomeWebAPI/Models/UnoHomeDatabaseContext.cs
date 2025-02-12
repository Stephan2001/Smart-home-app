using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace UnoHomeWebAPI.Models;

public partial class UnoHomeDatabaseContext : DbContext
{
    public UnoHomeDatabaseContext()
    {
    }

    public UnoHomeDatabaseContext(DbContextOptions<UnoHomeDatabaseContext> options)
        : base(options)
    {
    }

    public virtual DbSet<AppUser> AppUsers { get; set; }

    public virtual DbSet<Device> Devices { get; set; }

    public virtual DbSet<DeviceRoutine> DeviceRoutines { get; set; }

    public virtual DbSet<DeviceTheme> DeviceThemes { get; set; }

    public virtual DbSet<Routine> Routines { get; set; }

    public virtual DbSet<SensorReading> SensorReadings { get; set; }

    public virtual DbSet<Theme> Themes { get; set; }

    public virtual DbSet<UserDevice> UserDevices { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<AppUser>(entity =>
        {
            entity.HasKey(e => e.UserId).HasName("PK__AppUser__1788CCAC3456A5FB");

            entity.ToTable("AppUser");

            entity.Property(e => e.UserId).HasColumnName("UserID");
            entity.Property(e => e.Email).HasMaxLength(255);
            entity.Property(e => e.UserPassword).HasMaxLength(255);
        });

        modelBuilder.Entity<Device>(entity =>
        {
            entity.HasKey(e => e.DeviceId).HasName("PK__Device__49E12331B652DEBC");

            entity.ToTable("Device");

            entity.Property(e => e.DeviceId).HasColumnName("DeviceID");
            entity.Property(e => e.Category).HasMaxLength(255);
            entity.Property(e => e.Name).HasMaxLength(255);
        });

        modelBuilder.Entity<DeviceRoutine>(entity =>
        {
            entity.HasKey(e => e.DeviceRoutineId).HasName("PK__Device_R__A6DAFA5AE254AD21");

            entity.ToTable("Device_Routine");

            entity.Property(e => e.DeviceRoutineId).HasColumnName("DeviceRoutineID");
            entity.Property(e => e.DeviceId).HasColumnName("DeviceID");
            entity.Property(e => e.RoutineId).HasColumnName("RoutineID");

            entity.HasOne(d => d.Device).WithMany(p => p.DeviceRoutines)
                .HasForeignKey(d => d.DeviceId)
                .HasConstraintName("FK__Device_Ro__Devic__2BFE89A6");

            entity.HasOne(d => d.Routine).WithMany(p => p.DeviceRoutines)
                .HasForeignKey(d => d.RoutineId)
                .OnDelete(DeleteBehavior.Cascade)
                .HasConstraintName("FK__Device_Ro__Routi__2CF2ADDF");
        });

        modelBuilder.Entity<DeviceTheme>(entity =>
        {
            entity.HasKey(e => e.DeviceThemeId).HasName("PK__Device_T__16FE9B5BEB890514");

            entity.ToTable("Device_Theme");

            entity.Property(e => e.DeviceThemeId).HasColumnName("DeviceThemeID");
            entity.Property(e => e.DeviceId).HasColumnName("DeviceID");
            entity.Property(e => e.ThemeId).HasColumnName("ThemeID");

            entity.HasOne(d => d.Device).WithMany(p => p.DeviceThemes)
                .HasForeignKey(d => d.DeviceId)
                .HasConstraintName("FK__Device_Th__Devic__282DF8C2");

            entity.HasOne(d => d.Theme).WithMany(p => p.DeviceThemes)
                .HasForeignKey(d => d.ThemeId)
                .OnDelete(DeleteBehavior.Cascade)
                .HasConstraintName("FK__Device_Th__Theme__29221CFB");
        });

        modelBuilder.Entity<Routine>(entity =>
        {
            entity.HasKey(e => e.RoutineId).HasName("PK__Routine__A6E3E51A9EF82EC4");

            entity.ToTable("Routine");

            entity.Property(e => e.RoutineId).HasColumnName("RoutineID");
            entity.Property(e => e.Description).HasMaxLength(500);
            entity.Property(e => e.Name).HasMaxLength(255);
            entity.Property(e => e.Scheduling).HasMaxLength(50);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.Routines)
                .HasForeignKey(d => d.UserId)
                .OnDelete(DeleteBehavior.Cascade)
                .HasConstraintName("FK__Routine__UserID__25518C17");
        });

        modelBuilder.Entity<SensorReading>(entity =>
        {
            entity.HasKey(e => e.SensorId).HasName("PK__SensorRe__D809841AD9B708A3");

            entity.Property(e => e.SensorId).HasColumnName("SensorID");
            entity.Property(e => e.SensorName).HasMaxLength(100);
            entity.Property(e => e.SensorReading1).HasColumnName("SensorReading");
        });

        modelBuilder.Entity<Theme>(entity =>
        {
            entity.HasKey(e => e.ThemeId).HasName("PK__Theme__FBB3E4B9595D05EF");

            entity.ToTable("Theme");

            entity.Property(e => e.ThemeId).HasColumnName("ThemeID");
            entity.Property(e => e.Devices).HasMaxLength(255);
            entity.Property(e => e.Name).HasMaxLength(255);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.Themes)
                .HasForeignKey(d => d.UserId)
                .OnDelete(DeleteBehavior.Cascade)
                .HasConstraintName("FK__Theme__UserID__2180FB33");
        });

        modelBuilder.Entity<UserDevice>(entity =>
        {
            entity.HasKey(e => e.UserDevicesId).HasName("PK__UserDevi__417377680498D1A5");

            entity.Property(e => e.UserDevicesId).HasColumnName("UserDevicesID");
            entity.Property(e => e.DeviceId).HasColumnName("DeviceID");
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.Device).WithMany(p => p.UserDevices)
                .HasForeignKey(d => d.DeviceId)
                .HasConstraintName("FK__UserDevic__Devic__30C33EC3");

            entity.HasOne(d => d.User).WithMany(p => p.UserDevices)
                .HasForeignKey(d => d.UserId)
                .OnDelete(DeleteBehavior.Cascade)
                .HasConstraintName("FK__UserDevic__UserI__2FCF1A8A");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
