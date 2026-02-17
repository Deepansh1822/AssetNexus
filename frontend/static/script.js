document.addEventListener('DOMContentLoaded', () => {
    // Initialize Asset Distribution Chart
    // Chart initialization moved to specific page scripts (dashboard)

    // Add search bar focus effect
    const searchInput = document.getElementById('topSearchInput');
    const searchWrapper = searchInput ? searchInput.closest('.search-wrapper') : null;

    if (searchInput && searchWrapper) {
        searchInput.addEventListener('focus', () => {
            searchWrapper.style.boxShadow = '0 0 0 3px rgba(99, 102, 241, 0.2)';
            searchWrapper.style.borderColor = 'var(--primary)';
            if (searchInput.value.trim().length >= 2) {
                const searchDropdown = document.getElementById('globalSearchResults');
                if (searchDropdown) searchDropdown.style.display = 'block';
            }
        });

        searchInput.addEventListener('blur', () => {
            searchWrapper.style.boxShadow = 'none';
            searchWrapper.style.borderColor = 'rgba(255, 255, 255, 0.4)';
            // Delay closing to allow clicking results
            setTimeout(() => {
                const searchDropdown = document.getElementById('globalSearchResults');
                if (searchDropdown) searchDropdown.style.display = 'none';
            }, 200);
        });

        // Global Search Logic
        let searchTimeout = null;
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            const searchDropdown = document.getElementById('globalSearchResults');
            if (!searchDropdown) return;

            clearTimeout(searchTimeout);

            if (query.length < 2) {
                searchDropdown.style.display = 'none';
                return;
            }

            searchTimeout = setTimeout(async () => {
                try {
                    const res = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
                    const results = await res.json();

                    searchDropdown.innerHTML = '';

                    if (results.length === 0) {
                        searchDropdown.innerHTML = `<div class="search-no-results">No matches found for "${query}"</div>`;
                    } else {
                        results.forEach(item => {
                            const resultItem = document.createElement('div');
                            resultItem.className = 'search-result-item';
                            resultItem.innerHTML = `
                                <div class="res-type">${item.type}</div>
                                <div class="res-title">${item.title}</div>
                                <div class="res-subtitle">${item.subtitle}</div>
                            `;
                            resultItem.onclick = () => {
                                window.location.href = item.link;
                            };
                            searchDropdown.appendChild(resultItem);
                        });
                    }
                    searchDropdown.style.display = 'block';
                } catch (err) {
                    console.error('Search error:', err);
                }
            }, 300);
        });
    }

    // Submenu Toggle
    const submenuToggles = document.querySelectorAll('.submenu-toggle');
    submenuToggles.forEach(toggle => {
        toggle.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation(); // Prevent parent toggles from triggering
            const parent = toggle.closest('.has-submenu');
            if (parent) {
                const isOpen = parent.classList.contains('open');

                // Close siblings only at the same level
                const siblings = parent.parentElement.querySelectorAll(':scope > .has-submenu');
                siblings.forEach(sibling => {
                    if (sibling !== parent) {
                        sibling.classList.remove('open');
                    }
                });

                parent.classList.toggle('open', !isOpen);
            }
        });
    });

    // Handle "Add Asset" button click (Dashboard specific)
    const addAssetBtns = document.querySelectorAll('.header-actions .btn-action');
    addAssetBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            if (window.location.pathname !== '/assets/add') {
                window.location.href = '/assets/add';
            }
        });
    });

    // Global Profile Update
    fetch('/api/auth/me')
        .then(res => res.json())
        .then(user => {
            const profileImg = document.querySelector('.admin-profile img');
            const profileName = document.querySelector('.admin-profile .name');
            const profileRole = document.querySelector('.admin-profile .role');

            if (profileImg && user) {
                profileImg.src = user.hasImage ? `/api/employees/${user.id}/image` : `https://ui-avatars.com/api/?name=${user.name}&background=6366f1&color=fff`;
            }
            if (profileName && user) profileName.textContent = user.name;
            if (profileRole && user) profileRole.textContent = user.userRole;
        })
        .catch(err => console.error('Error fetching current user:', err));
});

// Global Custom Alert Function using Toastify
function showAlert(message, type = 'success') {
    Toastify({
        text: message,
        duration: 3000,
        close: true,
        gravity: "top", // `top` or `bottom`
        position: "right", // `left`, `center` or `right`
        stopOnFocus: true, // Prevents dismissing of toast on hover
        style: {
            background: type === 'success' ? "linear-gradient(to right, #00b09b, #96c93d)" : "linear-gradient(to right, #ff5f6d, #ffc371)",
            borderRadius: "10px",
            boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
            fontFamily: "'Outfit', sans-serif"
        }
    }).showToast();
}

// Global Custom Confirm Modal Logic
let confirmCallback = null;

function showConfirm(message, callback) {
    confirmCallback = callback;
    const modal = document.getElementById('customConfirmModal');
    const msgElem = document.getElementById('confirmMessage');
    if (modal && msgElem) {
        msgElem.textContent = message;
        modal.style.display = 'flex';
    } else {
        // Fallback if modal not in DOM
        if (confirm(message)) {
            callback();
        }
    }
}

function handleConfirm(result) {
    const modal = document.getElementById('customConfirmModal');
    if (modal) modal.style.display = 'none';
    if (result && confirmCallback) {
        confirmCallback();
    }
    confirmCallback = null;
}

// Global Event Listeners for Logout Confirmation
document.addEventListener('submit', (e) => {
    if (e.target && e.target.getAttribute('action') === '/logout') {
        const hasCustomConfirm = e.target.dataset.confirmed === 'true';
        if (!hasCustomConfirm) {
            e.preventDefault();
            showConfirm('Are you sure you want to end your session?', () => {
                e.target.dataset.confirmed = 'true';
                e.target.submit();
            });
        }
    }
});

// Barcode Global Logic
function showAssetBarcode(asset) {
    const modal = document.getElementById('globalBarcodeModal');
    if (!modal) return;

    // Set static data
    document.getElementById('bc_assetName').textContent = asset.name || 'N/A';
    document.getElementById('bc_assetTag').textContent = asset.assetTag || 'N/A';
    document.getElementById('bc_category').textContent = asset.category ? asset.category.name : 'Uncategorized';
    document.getElementById('bc_serial').textContent = asset.serialNumber || 'N/A';
    document.getElementById('bc_model').textContent = asset.modelName || 'N/A';

    // Generate Barcode
    JsBarcode("#globalBarcodeOutput", asset.assetTag || "000000", {
        format: "CODE128",
        lineColor: "#000",
        width: 4, // Increased width for better scaling
        height: 100, // Taller barcode
        displayValue: true,
        fontSize: 20,
        fontOptions: "bold",
        textMargin: 10 // Space between bars and text
    });

    // Set global asset ID for actions
    currentShareAssetId = asset.id;

    modal.style.display = 'flex';
}

function openEmailShareModal() {
    document.getElementById('emailShareModal').style.display = 'flex';
    document.getElementById('shareRecipientEmail').value = '';
    document.getElementById('shareRecipientEmail').focus();
}

let currentShareAssetId = null;

function closeEmailModal() {
    document.getElementById('emailShareModal').style.display = 'none';
}

// Button Loading State Helper
function setBtnLoading(btn, isLoading, loadingText = 'Processing...') {
    if (isLoading) {
        btn.dataset.originalContent = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingText}`;
    } else {
        btn.disabled = false;
        btn.innerHTML = btn.dataset.originalContent || btn.innerHTML;
    }
}

function submitEmailShare() {
    const email = document.getElementById('shareRecipientEmail').value;
    const btn = document.getElementById('submitEmailBtn');

    if (!email || !email.includes('@')) {
        showAlert('Please enter a valid email address', 'error');
        return;
    }

    if (currentShareAssetId) {
        setBtnLoading(btn, true, 'Sending...');

        fetch(`/api/assets/${currentShareAssetId}/share?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        })
            .then(res => {
                if (res.ok) {
                    showAlert('Email sent successfully!');
                    closeEmailModal();
                } else {
                    showAlert('Failed to send email. Check SMTP settings.', 'error');
                }
            })
            .catch(err => {
                showAlert('Error sending email', 'error');
                console.error(err);
            })
            .finally(() => {
                setBtnLoading(btn, false);
            });
    }
}

function closeBarcodeModal() {
    const modal = document.getElementById('globalBarcodeModal');
    if (modal) modal.style.display = 'none';
    currentShareAssetId = null;
}

function downloadAssetPdf() {
    if (currentShareAssetId) {
        window.location.href = `/api/assets/${currentShareAssetId}/pdf`;
    }
}



