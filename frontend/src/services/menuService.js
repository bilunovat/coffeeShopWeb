import apiClient from './apiClient.js';

class MenuService {
  /** Fetches all menu items and maps them to a frontend-friendly shape. */
  async getAllMenuItems() {
    try {
      const items = await apiClient.get('/api/v1/menu-item');
      return items.map((item) => ({
        id: item.menuItemId,
        name: item.name,
        description: item.description,
        regPrice: this.getTypeBySize(item.menuItemTypes, 'REGULAR')?.price || 0,
        largePrice: this.getTypeBySize(item.menuItemTypes, 'LARGE')?.price || 0,
        regTypeId: this.getTypeBySize(item.menuItemTypes, 'REGULAR')?.menuItemTypeId || null,
        largeTypeId: this.getTypeBySize(item.menuItemTypes, 'LARGE')?.menuItemTypeId || null,
      }));
    } catch (error) {
      throw new Error(error.message || 'Unable to load menu items right now.');
    }
  }

  /** Finds the matching size variant from a menu item's type list. */
  getTypeBySize(types = [], size = 'REGULAR') {
    if (!Array.isArray(types)) {
      return null;
    }

    return types.find((type) => String(type?.size || '').toUpperCase() === size);
  }

  /** Fetches a single menu item by ID (raw API shape). */
  async getMenuItemById(id) {
    try {
      return await apiClient.get(`/api/v1/menu-item/${id}`);
    } catch (error) {
      throw new Error(error.message || 'Unable to load this menu item right now.');
    }
  }
}

export default new MenuService();
