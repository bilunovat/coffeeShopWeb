class ApiClient {
  constructor() {
    this.baseURL = '';
    this.defaultHeaders = {
      'Content-Type': 'application/json',
    };
  }

  /** Core HTTP request method with auth injection and error normalisation. */
  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      headers: { ...this.defaultHeaders, ...options.headers },
      ...options,
    };

    // Add auth token if available
    const token = this.getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const errorPayload = await this.parseErrorResponse(response);
        const userMessage = this.getErrorMessage(response.status, errorPayload?.message);
        const requestError = new Error(userMessage);
        requestError.status = response.status;
        requestError.serverMessage = errorPayload?.message || null;
        requestError.fieldErrors = errorPayload?.errors || null;
        requestError.endpoint = endpoint;
        throw requestError;
      }

      // Handle empty responses
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }

      return response;
    } catch (error) {
      if (error.name === 'TypeError') {
        const networkError = new Error(
          'Unable to reach the server. Please check your connection and try again.',
        );
        networkError.status = 0;
        networkError.endpoint = endpoint;
        throw networkError;
      }

      throw error;
    }
  }

  /** Attempts to extract a JSON or text body from an error response. */
  async parseErrorResponse(response) {
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
      try {
        return await response.json();
      } catch {
        return null;
      }
    }

    try {
      const text = await response.text();
      return text ? { message: text } : null;
    } catch {
      return null;
    }
  }

  /** Maps an HTTP status code to a user-facing error message. */
  getErrorMessage(status, serverMessage) {
    switch (status) {
      case 400:
        return serverMessage || 'Some details are invalid. Please review and try again.';
      case 401:
        return serverMessage || 'Some details are invalid. Please review and try again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 409:
        return serverMessage || 'This action conflicts with existing data.';
      case 422:
        return serverMessage || 'Some input values are not valid.';
      case 500:
      case 502:
      case 503:
      case 504:
        return 'The server is currently unavailable. Please try again shortly.';
      default:
        return serverMessage || 'Something went wrong. Please try again.';
    }
  }

  /** Sends a GET request. */
  async get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  /** Sends a POST request with a JSON body. */
  async post(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  /** Sends a PUT request with a JSON body. */
  async put(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  /** Sends a DELETE request. */
  async delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }

  /** Persists or clears the JWT auth token in localStorage. */
  setAuthToken(token) {
    if (token) {
      localStorage.setItem('authToken', token);
    } else {
      localStorage.removeItem('authToken');
    }
  }

  /** Returns the stored auth token, if any. */
  getAuthToken() {
    return localStorage.getItem('authToken');
  }

  /** Removes the stored auth token. */
  clearAuthToken() {
    localStorage.removeItem('authToken');
  }
}

export default new ApiClient();
