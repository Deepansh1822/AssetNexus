/**
 * AssetNexus Frontend Layout Loader
 * Loads common components (Sidebar, Topbar, Footer) and handles global interactivity.
 */
const API_BASE_URL = 'http://localhost:8085/api';

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Identify active page for sidebar highlighting and header purification
    const activePage = document.body.dataset.page || 'dashboard';

    // 2. Load layout parts and global modals
    const topbarFile = document.body.dataset.module === 'labour' ? '/components/topbar-labour.html' : '/components/topbar.html';
    await Promise.all([
        loadComponent('sidebar-container', '/components/sidebar.html'),
        loadComponent('topbar-container', topbarFile),
        loadComponent('footer-container', '/components/footer.html'),
        loadGlobals()
    ]);

    // 3. Post-load initialization
    highlightActive(activePage);
    handleRoleVisibility();
    initGlobalInteractivity();
});

async function loadComponent(id, url) {
    const container = document.getElementById(id);
    if (!container) return;
    try {
        const response = await fetch(url);
        if (response.ok) {
            container.innerHTML = await response.text();
            window.dispatchEvent(new CustomEvent('componentLoaded', { detail: { id, url } }));
        }
    } catch (e) {
        console.error(`Failed to load component ${url}:`, e);
    }
}

async function loadGlobals() {
    // Create a container for global modals if it doesn't exist
    let container = document.getElementById('global-scripts-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'global-scripts-container';
        document.body.appendChild(container);
    }
    try {
        const res = await fetch('/fragments/scripts.html');
        if (res.ok) {
            container.innerHTML = await res.text();
        }
    } catch (e) { console.error("Failed to load global modals:", e); }
}

function highlightActive(page) {
    // 1. Highlight current page
    const activeLi = document.querySelector(`.sidebar-nav [data-page="${page}"]`);
    if (activeLi) {
        activeLi.classList.add('active');
    }

    // 2. Ensure Submenu States are closed initially
    document.querySelectorAll('.has-submenu[data-submenu-id]').forEach(el => {
        el.classList.remove('open');
    });

    // 3. (Optional) Highlight Active Parent - We removed the force-open fallback
    // to satisfy the user request for submenus to stay closed at starting.
}

async function handleRoleVisibility() {
    const controller = new AbortController();
    window.addEventListener('beforeunload', () => controller.abort());
    
    try {
        const res = await fetch(`${API_BASE_URL}/auth/me`, { signal: controller.signal });
        if (!res.ok) {
            if (res.status === 401) {
                window.location.href = '/login.html';
                return;
            }
            throw new Error("Session check refused");
        }
        
        const user = await res.json();
        window.userContext = user; // Global Access
        const isAdmin = user.userRole === 'ADMIN';
        const isManager = user.userRole === 'SITE_MANAGER';
        
        document.querySelectorAll('.admin-only').forEach(el => {
            const isManagerAllowed = el.classList.contains('manager-only');
            el.style.display = (isAdmin || (isManager && isManagerAllowed)) ? '' : 'none';
        });
        document.querySelectorAll('.employee-only').forEach(el => {
            el.style.display = (user.userRole === 'EMPLOYEE') ? '' : 'none';
        });
        document.querySelectorAll('.manager-only:not(.admin-only)').forEach(el => {
            el.style.display = isManager ? '' : 'none';
        });

        // Special case: Site Managers see Labour module but not Asset Global config
        if (isManager) {
            document.querySelectorAll('[data-module="labour"]').forEach(el => el.style.display = '');
        }

        // Update profile info
        // Global Identity Elements
        const pNames = document.querySelectorAll('#profileName');
        const pRoles = document.querySelectorAll('#profileRole');
        const pImgs = document.querySelectorAll('#profileImg');
        
        pNames.forEach(el => el.textContent = user.name || 'User');
        pRoles.forEach(el => el.textContent = user.userRole || 'Staff');
        
        const avatarUrl = user.hasImage 
            ? `${API_BASE_URL}/employees/${user.id}/image` 
            : `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=6366f1&color=fff&size=80`;
            
        pImgs.forEach(el => el.src = avatarUrl);

        // Update Dynamic Labels
        const assetsLabel = document.getElementById('assetsLabel');
        const reportsLabel = document.getElementById('reportsLabel');
        if (user.userRole === 'EMPLOYEE') {
            if (assetsLabel) assetsLabel.textContent = 'My Assets';
            if (reportsLabel) reportsLabel.textContent = 'My Reports';
        } else {
            if (assetsLabel) assetsLabel.textContent = 'All Assets';
            if (reportsLabel) reportsLabel.textContent = 'Global Reports';
        }
    } catch (e) {
        if (e.name !== 'AbortError') console.log("No auth session found or API down. Redirecting to login track.");
    }
}

/**
 * Handles all global clicks and interactions via event delegation
 */
function initGlobalInteractivity() {
    const searchController = new AbortController();
    
    document.addEventListener('click', (e) => {
        // 1. Submenu toggles
        const submenuToggle = e.target.closest('.submenu-toggle');
        if (submenuToggle) {
            e.preventDefault();
            const parent = submenuToggle.closest('.has-submenu');
            if (parent) {
                const isOpen = parent.classList.contains('open');
                const newStatus = !isOpen;
                
                // Toggle UI
                parent.classList.toggle('open', newStatus);

                // Persist State
                const submenuId = parent.getAttribute('data-submenu-id');
                if (submenuId) {
                    const savedStates = JSON.parse(localStorage.getItem('sidebar_submenus') || '{}');
                    savedStates[submenuId] = newStatus;
                    localStorage.setItem('sidebar_submenus', JSON.stringify(savedStates));
                }
            }
        }

        // 2. Mobile Menu Open
        if (e.target.closest('#mobileMenuOpen')) {
            const sidebar = document.querySelector('.sidebar');
            const overlay = document.getElementById('sidebarOverlay');
            if (sidebar) sidebar.classList.add('active');
            if (overlay) overlay.classList.add('active');
        }

        // 3. Mobile Menu Close
        if (e.target.closest('#sidebarCloseToggle') || e.target.closest('#sidebarOverlay')) {
            const sidebar = document.querySelector('.sidebar');
            const overlay = document.getElementById('sidebarOverlay');
            if (sidebar) sidebar.classList.remove('active');
            if (overlay) overlay.classList.remove('active');
        }

        // 4. Logout Logic
        if (e.target.closest('#logoutBtn')) {
            e.preventDefault();
            if (confirm('Are you sure you want to logout?')) {
                fetch('http://localhost:8085/logout', { method: 'POST' })
                    .finally(() => window.location.href = '/login.html');
            }
        }
    });

    // 5. Global Search Logic
    const searchInput = document.getElementById('topSearchInput');
    const searchDropdown = document.getElementById('globalSearchResults');
    if (searchInput && searchDropdown) {
        let searchTimeout = null;
        let currentSearchAbort = null;

        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            clearTimeout(searchTimeout);
            if (currentSearchAbort) currentSearchAbort.abort();

            if (query.length < 2) {
                searchDropdown.style.display = 'none';
                return;
            }
            searchTimeout = setTimeout(async () => {
                currentSearchAbort = new AbortController();
                try {
                    const res = await fetch(`${API_BASE_URL}/search?q=${encodeURIComponent(query)}`, { signal: currentSearchAbort.signal });
                    const results = await res.json();
                    searchDropdown.innerHTML = results.length ? results.map(item => `
                        <div class="search-result-item" onclick="window.location.href='${item.link}'">
                            <div class="res-type">${item.type}</div>
                            <div class="res-title">${item.title}</div>
                            <div class="res-subtitle">${item.subtitle}</div>
                        </div>
                    `).join('') : '<div class="search-no-results">No matches found for institutional records.</div>';
                    searchDropdown.style.display = 'block';
                } catch (err) { 
                    if (err.name !== 'AbortError') console.error('Search error:', err);
                }
            }, 300);
        });

        // Hide search when clicking outside
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target)) searchDropdown.style.display = 'none';
        });
    }
}

/**
 * Filter out specifically noisy extension messages that we can't control
 */
window.addEventListener('error', (e) => {
    if (e.message && e.message.includes('A listener indicated an asynchronous response')) {
        e.stopImmediatePropagation();
        e.preventDefault();
        return false;
    }
}, true);

window.addEventListener('unhandledrejection', (e) => {
    if (e.reason && e.reason.message && e.reason.message.includes('message channel closed before a response')) {
        e.stopImmediatePropagation();
        e.preventDefault();
        return false;
    }
}, true);
