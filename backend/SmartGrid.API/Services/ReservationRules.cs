/*
 * File Name    : ReservationRules.cs
 * Description  : Holds the reservation time rules (7-day scheduling window and 12-hour notice)
 *                and the exception raised when a rule is broken.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

namespace SmartGrid.API.Services
{
    // Thrown when a business rule is broken; the controller turns it into a 400 response
    public class ReservationRuleException : Exception
    {
        public ReservationRuleException(string message) : base(message) { }
    }

    public static class ReservationRules
    {
        // 7-day scheduling rule: applies to create, and to modify when the time changes
        public static void ValidateSchedulingWindow(DateTime scheduledDateTime)
        {
            var now = DateTime.UtcNow;

            if (scheduledDateTime < now)
                throw new ReservationRuleException("Scheduled time cannot be in the past.");

            if (scheduledDateTime > now.AddDays(7))
                throw new ReservationRuleException("Reservations must be scheduled within the next 7 days.");
        }

        // 12-hour notice rule: applies to modify and cancel
        public static void ValidateModificationWindow(DateTime scheduledDateTime)
        {
            if (scheduledDateTime - DateTime.UtcNow < TimeSpan.FromHours(12))
                throw new ReservationRuleException("Modifications and cancellations require at least 12 hours' notice.");
        }
    }
}