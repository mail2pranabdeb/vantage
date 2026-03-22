/**
 * Menu Cache Service
 * Caches menu configuration for tab system
 * Auto-refreshes when menu changes
 */

class MenuCacheService {
    constructor() {
        this.cache = new Map();
        this.cacheTime = new Map();
        this.TTL = 5 * 60 * 1000; // 5 minutes
        this.listeners = [];
    }

    /**
     * Get page config from cache or fetch from DB
     */
    async getPageConfig(pathname) {
        // Check cache first
        const cached = this.cache.get(pathname);
        const cacheTime = this.cacheTime.get(pathname);

        if (cached && cacheTime && (Date.now() - cacheTime < this.TTL)) {
            return cached;
        }

        // Handle VantageJob routes
        if (pathname === '/vantage/job') {
            const jobConfig = {
                id: 'vantage-job',
                title: 'VantageJob Management',
                url: '/vantage/job',
                icon: '⏰'
            };
            this.cache.set(pathname, jobConfig);
            this.cacheTime.set(pathname, Date.now());
            return jobConfig;
        }

        // Fetch from DB
        try {
            const allMenus = await this.fetchAllMenus();
            const pageConfig = this.findPageConfig(allMenus, pathname);

            if (pageConfig) {
                this.cache.set(pathname, pageConfig);
                this.cacheTime.set(pathname, Date.now());
            }

            return pageConfig;
        } catch (error) {
            console.error('Failed to fetch menu config:', error);
            return null;
        }
    }

    /**
     * Fetch all menus from database
     */
    async fetchAllMenus() {
        const response = await fetch('/api/system/menu/tree');
        const data = await response.json();
        
        if (data.code === 200) {
            return data.data || [];
        }
        
        return [];
    }

    /**
     * Find page config in menu tree
     */
    findPageConfig(menus, pathname, parentPath = '') {
        for (const menu of menus) {
            const fullPath = parentPath + (menu.url || '');
            
            if (menu.url === pathname) {
                return {
                    id: menu.menuName.toLowerCase().replace(/\s+/g, '-'),
                    title: menu.menuName,
                    url: menu.url,
                    icon: this.getIconComponent(menu.icon)
                };
            }

            if (menu.children && menu.children.length > 0) {
                const childConfig = this.findPageConfig(menu.children, pathname, fullPath);
                if (childConfig) {
                    return childConfig;
                }
            }
        }
        
        return null;
    }

    /**
     * Get icon component from icon string
     */
    getIconComponent(iconStr) {
        // Map icon strings to Lucide components
        const iconMap = {
            'fa fa-gear': 'Settings',
            'fa fa-user-o': 'Users',
            'fa fa-users': 'Users',
            'fa fa-lock': 'Shield',
            'fa fa-list': 'Menu',
            'fa fa-dashboard': 'Home',
            'fa fa-bell': 'Bell',
            'fa fa-th-list': 'Menu',
            'fa fa-sun-o': 'Sun',
            'fa fa-bookmark-o': 'Bookmark',
            'fa fa-address-card-o': 'IdCard',
            'fa fa-file-image-o': 'Image',
            'fa fa-bullhorn': 'Megaphone',
            'fa fa-tasks': 'List',
            'fa fa-clock-o': 'Clock',
            'fa fa-file-text-o': 'FileText',
            'fa fa-code': 'Code',
        };

        return iconMap[iconStr] || 'Menu';
    }

    /**
     * Get all cached page configs
     */
    getAllPageConfigs() {
        return Array.from(this.cache.values());
    }

    /**
     * Clear cache for specific path
     */
    clear(pathname) {
        this.cache.delete(pathname);
        this.cacheTime.delete(pathname);
    }

    /**
     * Clear all cache
     */
    clearAll() {
        this.cache.clear();
        this.cacheTime.clear();
    }

    /**
     * Refresh cache
     */
    async refresh() {
        this.clearAll();
        await this.fetchAllMenus();
        this.notifyListeners();
    }

    /**
     * Add cache change listener
     */
    addListener(callback) {
        this.listeners.push(callback);
    }

    /**
     * Notify listeners of cache change
     */
    notifyListeners() {
        this.listeners.forEach(callback => callback());
    }
}

// Singleton instance
export const menuCache = new MenuCacheService();
