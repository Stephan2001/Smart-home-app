
using Microsoft.EntityFrameworkCore;
using UnoHomeWebAPI.Models;

namespace UnoHomeWebAPI
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container.

            builder.Services.AddControllers();
            // Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            builder.Services.AddDbContext<UnoHomeDatabaseContext>(options =>
            {
                options.UseSqlServer(builder.Configuration.GetConnectionString("MyDBConnection"));
            });


            var app = builder.Build();

            app.UseSwagger();
            app.UseSwaggerUI(c =>
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "UnoHome API V1");
            });

            //app.UseHttpsRedirection();

            app.UseAuthorization();


            app.MapControllers();

            app.Run();
        }
    }
}
