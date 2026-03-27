/**
 * AssetNexus Frontend Layout Engine
 * Handles dynamic fragment loading and shared logic (Auth/Role UI)
 */
const API_BASE = 'http://localhost:8085/api'; // Change for Prod

class LayoutEngine {
    constructor() {
        this.fragments = ['head', 'sidebar', 'topbar', 'footer', 'scripts'];
        this.currentUser = null;
    }

    async init(activePageId) {
        // 1. Fetch Auth first (to decide what to show/hide)
        await this.fetchUser();

        // 2. Load basic layout parts
        await this.loadLayoutParts(activePageId);

        // 3. Initialize common UI listeners 
        this.initEventListeners();
        
        // 4. Handle global search and logout
        this.initGlobalSearch();
    }

    async fetchUser() {
        try {
            const res = await fetch(`${API_BASE}/auth/me`, { cache: 'no-store' });
            if (res.ok) {
                this.currentUser = await res.json();
                console.log("Logged in user:", this.currentUser);
            } else if (res.status === 401 || res.status === 403) {
                // If we aren't on the login page, redirect to it
                if (!window.location.pathname.includes('login')) {
                    window.location.href = '/login.html';
                }
            }
        } catch (err) {
            console.error("Auth fetch failed:", err);
            // Default to minimal guest profile if API is down
        }
    }

    async loadLayoutParts(activePageId) {
        // Load Head (meta, links) - typically should be already present but good for updates
        // Actually, we usually want these in the main HTML to avoid FOUC.
        
        // Load Sidebar
        const sidebarHtml = await this.getFragment('sidebar');
        const sidebarContainer = document.getElementById('sidebar-container');
        if (sidebarContainer) {
            sidebarContainer.innerHTML = sidebarHtml;
            this.prepareSidebar(activePageId);
        }

        // Load Topbar
        const topbarHtml = await this.getFragment('topbar');
        const topbarContainer = document.getElementById('topbar-container');
        if (topbarContainer) {
            topbarContainer.innerHTML = topbarHtml;
        }

        // Load Footer
        const footerHtml = await this.getFragment('footer');
        const footerContainer = document.getElementById('footer-container');
        if (footerContainer) {
            footerContainer.innerHTML = footerHtml;
        }

        // Load Modals/Global Scripts
        const scriptsHtml = await this.getFragment('scripts');
        const scriptsContainer = document.getElementById('global-scripts-container');
        if (scriptsContainer) {
            scriptsContainer.innerHTML = scriptsHtml;
        }

        // --- Post-injection role handling ---
        this.applyRoleControls();
    }

    async getFragment(name) {
        try {
            const res = await fetch(`/fragments/${name}.html`);
            return await res.text();
        } catch (e) {
            console.error(`Failed to load fragment: ${name}`, e);
            return '';
        }
    }

    prepareSidebar(activePageId) {
        // Mark active
        if (activePageId) {
            const activeLi = document.querySelector(`.sidebar-nav [data-page="${activePageId}"]`);
            if (activeLi) {
                activeLi.classList.add('active');
                // If it's in a submenu, open it
                const parentSub = activeLi.closest('.has-submenu');
                if (parentSub) parentSub.classList.add('open');
            }
        }

        // Populate profile
        if (this.currentUser) {
            const profImg = document.getElementById('profileImg');
            const profName = document.getElementById('profileName');
            const profRole = document.getElementById('profileRole');
            
            if (profName) profName.textContent = this.currentUser.name;
            if (profRole) profRole.textContent = this.currentUser.userRole === 'ADMIN' ? 'Administrator' : 'Staff Member';
            if (profImg && this.currentUser.hasImage) {
                profImg.src = `${API_BASE}/employees/${this.currentUser.id}/image`;
            } else if (profImg) {
                profImg.src = `https://ui-avatars.com/api/?name=${this.currentUser.name}&background=6366f1&color=fff`;
            }
        }
    }

    applyRoleControls() {
        const isAdmin = this.currentUser && this.currentUser.userRole === 'ADMIN';
        const isStaff = this.currentUser && this.currentUser.userRole === 'EMPLOYEE';

        document.querySelectorAll('.admin-only').forEach(el => {
            el.style.display = isAdmin ? '' : 'none';
        });

        document.querySelectorAll('.employee-only').forEach(el => {
            el.style.display = isStaff ? '' : 'none';
        });
    }

    initEventListeners() {
        // Sidebar Toggles
        document.addEventListener('click', (e) => {
            // Submenu toggles
            if (e.target.closest('.submenu-toggle')) {
                const li = e.target.closest('.has-submenu');
                li.classList.toggle('open');
            }

            // Mobile Open
            if (e.target.closest('#mobileMenuOpen')) {
                document.querySelector('.sidebar').classList.add('active');
                document.getElementById('sidebarOverlay').classList.add('active');
            }

            // Mobile Close
            if (e.target.closest('#sidebarCloseToggle') || e.target.closest('#sidebarOverlay')) {
                document.querySelector('.sidebar').classList.remove('active');
                document.getElementById('sidebarOverlay').classList.remove('active');
            }
            
            // Logout
            if (e.target.closest('#logoutBtn')) {
                this.performLogout();
            }
        });
    }

    async performLogout() {
        try {
            await fetch('http://localhost:8080/logout', { method: 'POST' });
            window.location.href = '/login.html';
        } catch (e) {
            // Fallback for session
            window.location.href = 'http://localhost:8080/logout';
        }
    }

    initGlobalSearch() {
       // Incorporate logic from existing script.js if needed in layout context
    }
}

// Export for use in pages
window.LayoutSystem = new LayoutEngine();
