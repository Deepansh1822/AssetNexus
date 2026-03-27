/**
 * AssetNexus Frontend Layout Loader
 * Loads common components (Sidebar, Topbar, Footer) and handles global interactivity.
 */
const API_BASE_URL = 'http://localhost:8085/api';

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Identify active page for sidebar highlighting
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
    const activeLi = document.querySelector(`.sidebar-nav [data-page="${page}"]`);
    if (activeLi) {
        activeLi.classList.add('active');
        // Open all ancestor submenus
        let currentParent = activeLi.parentElement;
        while (currentParent && !currentParent.classList.contains('sidebar-nav')) {
            if (currentParent.classList.contains('has-submenu')) {
                currentParent.classList.add('open');
            }
            currentParent = currentParent.parentElement;
        }
    }
}

async function handleRoleVisibility() {
    try {
        const res = await fetch(`${API_BASE_URL}/auth/me`);
        if (res.ok) {
            const user = await res.json();
            const isAdmin = user.userRole === 'ADMIN';
            
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = isAdmin ? '' : 'none';
            });
            document.querySelectorAll('.employee-only').forEach(el => {
                el.style.display = !isAdmin ? '' : 'none';
            });

            // Update profile info
            const profImg = document.getElementById('profileImg');
            const profName = document.getElementById('profileName');
            const profRole = document.getElementById('profileRole');
            if (profName) profName.textContent = user.name;
            if (profRole) profRole.textContent = user.userRole === 'ADMIN' ? 'Administrator' : 'Staff Member';
            if (profImg) {
                profImg.src = user.hasImage ? `${API_BASE_URL}/employees/${user.id}/image` : `https://ui-avatars.com/api/?name=${user.name}&background=6366f1&color=fff`;
            }
        }
    } catch (e) {
        console.log("No auth session found or API down.");
    }
}

/**
 * Handles all global clicks and interactions via event delegation
 */
function initGlobalInteractivity() {
    document.addEventListener('click', (e) => {
        // 1. Submenu toggles
        const submenuToggle = e.target.closest('.submenu-toggle');
        if (submenuToggle) {
            e.preventDefault();
            const parent = submenuToggle.closest('.has-submenu');
            if (parent) {
                const isOpen = parent.classList.contains('open');
                // Toggle current without closing others
                parent.classList.toggle('open', !isOpen);
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
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            clearTimeout(searchTimeout);
            if (query.length < 2) {
                searchDropdown.style.display = 'none';
                return;
            }
            searchTimeout = setTimeout(async () => {
                try {
                    const res = await fetch(`${API_BASE_URL}/search?q=${encodeURIComponent(query)}`);
                    const results = await res.json();
                    searchDropdown.innerHTML = results.length ? results.map(item => `
                        <div class="search-result-item" onclick="window.location.href='${item.link}'">
                            <div class="res-type">${item.type}</div>
                            <div class="res-title">${item.title}</div>
                            <div class="res-subtitle">${item.subtitle}</div>
                        </div>
                    `).join('') : '<div class="search-no-results">No matches found.</div>';
                    searchDropdown.style.display = 'block';
                } catch (err) { console.error('Search error:', err); }
            }, 300);
        });

        // Hide search when clicking outside
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target)) searchDropdown.style.display = 'none';
        });
    }
}
