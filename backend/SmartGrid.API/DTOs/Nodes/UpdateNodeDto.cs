/*
* File Name    : UpdateNodeDto.cs
* Description  : Input validation model for updating microgrid nodes.
* Author       : De Silva W.Y.S
* IT Number    : IT23235038
* Date         : 2026-09-20
*/
using System.ComponentModel.DataAnnotations;
using System.Collections.Generic;

namespace SmartGrid.API.DTOs.Nodes
{
    public class UpdateNodeDto
    {
        [StringLength(100)]
        public string? NodeName { get; set; }

        [Range(-90, 90)]
        public double? Latitude { get; set; }

        [Range(-180, 180)]
        public double? Longitude { get; set; }

        public string? Address { get; set; }

        [Range(0.1, double.MaxValue)]
        public double? CapacityKWh { get; set; }

        public List<string>? Schedule { get; set; }
    }
}