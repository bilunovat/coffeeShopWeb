import apiClient from './apiClient.js';

class StationService {
  /** Fetches all stations. */
  async getAllStations() {
    try {
      return await apiClient.get('/api/v1/station');
    } catch (error) {
      throw new Error(error.message || 'Unable to load station information right now.');
    }
  }
  /** Fetches a single station by ID. */
  async getStationById(stationId) {
    try {
      return await apiClient.get(`/api/v1/station/${stationId}`);
    } catch (error) {
      throw new Error(error.message || 'Unable to load station information right now.');
    }
  }
  /** Updates station opening hours. */
  async updateStationHours(stationId, payload) {
    try {
      return await apiClient.put(`/api/v1/station/${stationId}`, payload);
    } catch (error) {
      throw new Error(error.message || 'Unable to update station hours right now.');
    }
  }
}

export default new StationService();
