import axios from 'axios';

export const geocodingService = {
  // Convert address to latitude/longitude using OpenStreetMap Nominatim
  getAddressCoordinates: async (address) => {
    try {
      const response = await axios.get('https://nominatim.openstreetmap.org/search', {
        params: {
          q: address,
          format: 'json',
          limit: 1
        }
      });
      
      if (response.data && response.data.length > 0) {
        return {
          latitude: parseFloat(response.data[0].lat),
          longitude: parseFloat(response.data[0].lon),
          address: response.data[0].display_name
        };
      }
      throw new Error('Address not found');
    } catch (error) {
      throw new Error('Failed to geocode address: ' + error.message);
    }
  }
};

export default geocodingService;