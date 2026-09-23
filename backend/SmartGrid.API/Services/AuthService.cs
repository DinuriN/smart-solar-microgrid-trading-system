/*
 * File Name    : AuthService.cs
 * Description  : Contains logic for Authentication and JWT Generation.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.Auth;
using SmartGrid.API.Models;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;

namespace SmartGrid.API.Services
{
    public class AuthService
    {
        private readonly IMongoCollection<User> _usersCollection;
        private readonly IConfiguration _configuration;

        public AuthService(MongoDbContext context, IConfiguration configuration)
        {
            _usersCollection = context.Users;
            _configuration = configuration;
        }

        public async Task<AuthResponseDto> LoginAsync(LoginDto dto)
        {
            var user = await _usersCollection.Find(u => u.Email == dto.EmailOrNic || (u is Prosumer && ((Prosumer)u).Nic == dto.EmailOrNic)).FirstOrDefaultAsync();

            if (user == null || !user.IsActive)
                throw new Exception("Invalid credentials or account is inactive.");

            if (!BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
                throw new Exception("Invalid credentials.");

            var token = GenerateJwtToken(user);

            // FAT Backend: Dynamically calculate the navigation menu
            var menu = new List<MenuItemDto>();
            if (user.Role == UserRole.BackOfficeUser)
            {
                menu.Add(new MenuItemDto { Label = "Prosumers", Path = "/admin/prosumers", Icon = "users" });
                menu.Add(new MenuItemDto { Label = "Web Users", Path = "/admin/users", Icon = "shield" });
            }
            else if (user.Role == UserRole.GridOperator)
            {
                menu.Add(new MenuItemDto { Label = "Microgrid Nodes", Path = "/admin/nodes", Icon = "nodes" });
                menu.Add(new MenuItemDto { Label = "Reservations", Path = "/admin/reservations", Icon = "reservations" });
            }
           

            return new AuthResponseDto
            {
                Token = token,
                Role = user.Role.ToString(),
                Name = user.Name,
                Menu = menu
            };
        }

        private string GenerateJwtToken(User user)
        {
            var jwtKey = Environment.GetEnvironmentVariable("JWT_KEY");
            var jwtIssuer = Environment.GetEnvironmentVariable("JWT_ISSUER");
            var jwtAudience = Environment.GetEnvironmentVariable("JWT_AUDIENCE");

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey!));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new List<Claim>
            {
                new Claim(JwtRegisteredClaimNames.Sub, user.Id),
                new Claim(ClaimTypes.Role, user.Role.ToString())
            };

              // If the user is a Prosumer, add NIC in the token!
            if (user is Prosumer prosumer)
            {
                claims.Add(new Claim("nic", prosumer.Nic));
            }
            var token = new JwtSecurityToken(
                issuer: jwtIssuer,
                audience: jwtAudience,
                claims: claims,
                expires: DateTime.UtcNow.AddHours(2),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
