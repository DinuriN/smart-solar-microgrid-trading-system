/*
* File Name    : CreateNodeDto.cs
* Description  : Input validation model for creating new microgrid nodes.
* Author       : De Silva W.Y.S
* IT Number    : IT23235038
* Date         : 2026-09-20
*/
using System.ComponentModel.DataAnnotations;

namespace SmartGrid.API.DTOs.Nodes
{
    public class CreateNodeDto
    {
        [Required]
        [StringLength(100)]
        public string NodeName { get; set; } = string.Empty;

        [Required]
        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Required]
        [Range(-180, 180)]
        public double Longitude { get; set; }

        public string? Address { get; set; }

        [Required]
        [Range(0.1, double.MaxValue)]
        public double CapacityKWh { get; set; }
    }
}