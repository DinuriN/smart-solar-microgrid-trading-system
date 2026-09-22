/*
 * File Name    : MongoDbContext.cs
 * Description  : Handles the connection to the MongoDB database and exposes collections.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using MongoDB.Driver;


using SmartGrid.API.Models;

namespace SmartGrid.API.Database
{
    public class MongoDbContext
    {
        private readonly IMongoDatabase _database;
        //MongoDB connection using the configuration settings
        public MongoDbContext(IConfiguration configuration)
        {
            var connectionString = Environment.GetEnvironmentVariable("MONGO_URI");
            var databaseName = Environment.GetEnvironmentVariable("MONGO_DB_NAME");

            var client = new MongoClient(connectionString);
            _database = client.GetDatabase(databaseName);
        }
// Expose the Users collection
        public IMongoCollection<User> Users => _database.GetCollection<User>("users");

//Expose the Reservations collection
        public IMongoCollection<EnergyReservation> Reservations => _database.GetCollection<EnergyReservation>("reservations");

        public IMongoDatabase Database => _database;
    }
}
