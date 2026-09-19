/*
 * File Name    : Program.cs
 * Description  : The entry point of the ASP.NET Core application, configuring services and the HTTP request pipeline.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using DotNetEnv;
using SmartGrid.API.Database;

// Load environment variables from the .env file
Env.Load();

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// Register Controllers so ASP.NET knows how to handle API routes

builder.Services.AddControllers();

// Register the MongoDbContext as a Singleton service
builder.Services.AddSingleton<MongoDbContext>();
//auth service
builder.Services.AddScoped<SmartGrid.API.Services.AuthService>();


// Configure Swagger/OpenAPI for API documentation and testing

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(

    c =>
{
    // Add the "Authorize" button to Swagger UI
    c.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
    {
        Description = "Paste your JWT token here (you don't need to type 'Bearer' anymore!).",
        Name = "Authorization",
        In = Microsoft.OpenApi.Models.ParameterLocation.Header,
        Type = Microsoft.OpenApi.Models.SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });

    c.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
}
);


//Configure JWT Authentication
var jwtKey = Environment.GetEnvironmentVariable("JWT_KEY");
var jwtIssuer = Environment.GetEnvironmentVariable("JWT_ISSUER");
var jwtAudience = Environment.GetEnvironmentVariable("JWT_AUDIENCE");

var key = System.Text.Encoding.ASCII.GetBytes(jwtKey!);
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.RequireHttpsMetadata = false;
    options.SaveToken = true;
    options.TokenValidationParameters = new Microsoft.IdentityModel.Tokens.TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new Microsoft.IdentityModel.Tokens.SymmetricSecurityKey(key),
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidIssuer = jwtIssuer,
        ValidAudience = jwtAudience
    };
});



var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}




//Enable Authentication and Authorization

app.UseAuthentication();
app.UseAuthorization();



app.MapControllers();

// Run the Data Seeder to ensure we have a defualt backoffice admin
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    await SmartGrid.API.Database.DataSeeder.SeedAdminUserAsync(services);
}

app.Run();
