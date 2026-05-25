/**
 * Initializes the anime detail page interactions.
 *
 * Handles rating submission, rating filters/pagination, and lazy loading of
 * related character cards for the current anime detail page.
 */
(function() {
    const animeId = window.location.pathname.split('/')[2]; // Extract anime ID from URL
    let currentPage = 1;

    const urlParams = new URLSearchParams(window.location.search);
    const filters = {
        minScore: urlParams.get('minScore') || '',
        maxScore: urlParams.get('maxScore') || '',
        status: urlParams.get('status') || '',
        rewatching: urlParams.get('rewatching') || '',
        sortBy: urlParams.get('sortBy') || 'score',
        sortOrder: urlParams.get('sortOrder') || 'desc'
    };
    currentPage = parseInt(urlParams.get('page')) || 1;

    const filterMinScore = document.getElementById('filter-minScore');
    const filterMaxScore = document.getElementById('filter-maxScore');
    const filterStatus = document.getElementById('filter-status');
    const filterRewatching = document.getElementById('filter-rewatching');
    const filterSortBy = document.getElementById('filter-sortBy');
    const filterSortOrder = document.getElementById('filter-sortOrder');
    const ratingForm = document.getElementById('rating-form') || document.getElementById('add-rating-form');
    const submitRatingBtn = document.getElementById('submit-rating-btn');
    const applyFiltersBtn = document.getElementById('apply-filters-btn');
    const clearFiltersBtn = document.getElementById('clear-filters-btn');
    const ratingsContainer = document.getElementById('ratings-container');
    const ratingMessage = document.getElementById('rating-message');

    const hasFiltersUI = Boolean(
        filterMinScore &&
        filterMaxScore &&
        filterStatus &&
        filterRewatching &&
        filterSortBy &&
        filterSortOrder &&
        applyFiltersBtn &&
        clearFiltersBtn &&
        ratingsContainer
    );
    const hasRatingForm = Boolean(ratingForm && submitRatingBtn);

    if (hasFiltersUI) {
        filterMinScore.value = filters.minScore;
        filterMaxScore.value = filters.maxScore;
        filterStatus.value = filters.status;
        filterRewatching.value = filters.rewatching;
        filterSortBy.value = filters.sortBy;
        filterSortOrder.value = filters.sortOrder;
    }

    /**
     * Displays a transient rating feedback message.
     *
     * @param {string} message Message text to render.
     * @param {'success'|'error'} type Message style variant.
     * @returns {void}
     */
    function showMessage(message, type) {
        const messageEl = ratingMessage;
        if (!messageEl) {
            return;
        }
        messageEl.textContent = message;
        messageEl.style.display = 'block';
        messageEl.style.background = type === 'success' ? '#4caf50' : '#f44336';
        messageEl.style.color = '#fff';

        // Auto-hide after 5 seconds
        setTimeout(() => {
            messageEl.style.display = 'none';
        }, 5000);
    }

    if (hasRatingForm) {
        // Handle rating form submission
        /**
         * Submits a new user rating and refreshes the ratings list on success.
         *
         * @param {SubmitEvent} e Rating form submit event.
         * @returns {Promise<void>}
         */
        ratingForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            submitRatingBtn.disabled = true;
            submitRatingBtn.textContent = 'Submitting...';

            const rewatchingInput = document.getElementById('rating-rewatching');
            const formData = {
                username: document.getElementById('rating-username').value.trim(),
                anime_id: parseInt(animeId, 10),
                score: parseInt(document.getElementById('rating-score').value, 10),
                status: document.getElementById('rating-status').value,
                num_watched_episodes: parseInt(document.getElementById('rating-episodes').value, 10),
                is_rewatching: rewatchingInput && rewatchingInput.type === 'checkbox'
                    ? (rewatchingInput.checked ? 1 : 0)
                    : parseInt(rewatchingInput?.value || '0', 10)
            };

            try {
                console.log('Submitting rating:', formData);

                const response = await axios.post(`/anime/${animeId}/ratings`, formData);

                console.log('Response status:', response.status);
                console.log('Response data:', response.data);

                showMessage('Rating submitted successfully!', 'success');

                ratingForm.reset();

                // Reload ratings to show the new one
                loadRatings(1);

            } catch (error) {
                const errorMsg =
                    error.response?.data?.message ||
                    error.response?.data?.error ||
                    error.message;
                console.error('Error submitting rating:', error);
                showMessage('Error: ' + errorMsg, 'error');
            } finally {
                submitRatingBtn.disabled = false;
                submitRatingBtn.textContent = 'Submit Rating';
            }
        });
    }

    /**
     * Loads filtered ratings for the selected page via AJAX.
     *
     * @param {number} [page=1] Ratings page to load.
     * @returns {Promise<void>}
     */
    async function loadRatings(page = 1) {
        if (!hasFiltersUI) {
            return;
        }
        const params = new URLSearchParams({ page: page });

        const minScore = filterMinScore.value;
        const maxScore = filterMaxScore.value;
        const status = filterStatus.value;
        const rewatching = filterRewatching.value;
        const sortBy = filterSortBy.value;
        const sortOrder = filterSortOrder.value;

        if (minScore) params.append('minScore', minScore);
        if (maxScore) params.append('maxScore', maxScore);
        if (status) params.append('status', status);
        if (rewatching) params.append('rewatching', rewatching);
        if (sortBy) params.append('sortBy', sortBy);
        if (sortOrder) params.append('sortOrder', sortOrder);

        // Update URL without reload
        const newUrl = window.location.pathname + '?' + params.toString();
        window.history.pushState({}, '', newUrl);

        try {
            // Show loading indicator
            ratingsContainer.innerHTML = '<div style="text-align: center; padding: 2rem; color: #aaa;">Loading...</div>';

            // Fetch ratings via AJAX
            const response = await axios.get(`/anime/${animeId}/ratings-json?${params.toString()}`);
            const data = response.data;

            // Update ratings table
            renderRatings(data.ratings, data.pagination, params);
        } catch (error) {
            console.error('Error loading ratings:', error);
            ratingsContainer.innerHTML = '<p style="color: #f44; margin-bottom: 2rem;">Error loading ratings.</p>';
        }
    }

    /**
     * Renders ratings rows and pagination controls into the ratings container.
     *
     * @param {Array<Object>} ratings Ratings returned by the API.
     * @param {Object} pagination Pagination metadata returned by the API.
     * @param {URLSearchParams} params Active filter and pagination query params.
     * @returns {void}
     */
    function renderRatings(ratings, pagination, params) {
        const container = ratingsContainer;
        const paginationData = pagination || { currentPage: 1, totalPages: 1 };

        if (!ratings || ratings.length === 0) {
            container.innerHTML = '<p style="color: #888; font-style: italic; margin-bottom: 2rem;">No ratings available for this anime yet.</p>';
            return;
        }

        let html = '<div style="overflow-x: auto;">';
        html += '<table style="width: 100%; border-collapse: collapse; background: #1a1a1a; border-radius: 8px; overflow: hidden; margin-bottom: 2rem;">';
        html += '<thead><tr style="background: #333; color: #fff; text-align: left;">';
        html += '<th style="padding: 12px 15px;">User</th>';
        html += '<th style="padding: 12px 15px;">Score</th>';
        html += '<th style="padding: 12px 15px;">Status</th>';
        html += '<th style="padding: 12px 15px;">Is Rewatching</th>';
        html += '<th style="padding: 12px 15px;">Watched Episodes</th>';
        html += '</tr></thead><tbody>';

        ratings.forEach(rating => {
            html += '<tr style="border-bottom: 1px solid #2d2d2d; color: #ccc;">';
            html += `<td style="padding: 12px 15px;"><span style="font-weight: bold; color: #fff;">${rating.username}</span></td>`;
            html += `<td style="padding: 12px 15px;"><span style="color: #d50415; font-weight: bold;">⭐ ${rating.score}</span></td>`;
            html += `<td style="padding: 12px 15px; text-transform: capitalize;">${rating.status}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.is_rewatching ? '<span style="color: #4caf50; font-weight: bold;">Yes</span>' : '<span style="color: #888;">No</span>'}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.num_watched_episodes}</td>`;
            html += '</tr>';
        });

        html += '</tbody></table></div>';

        // Add pagination
        if (paginationData.totalPages > 1) {
            const hasPrev = typeof paginationData.hasPrev === 'boolean'
                ? paginationData.hasPrev
                : paginationData.currentPage > 1;
            const hasNext = typeof paginationData.hasNext === 'boolean'
                ? paginationData.hasNext
                : paginationData.currentPage < paginationData.totalPages;

            html += '<div class="pagination" style="display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem;">';

            if (hasPrev) {
                html += `<button class="pagination-btn" data-page="${paginationData.currentPage - 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">← Previous</button>`;
            }

            html += `<span class="page-info" style="color: #ccc;">Page ${paginationData.currentPage} of ${paginationData.totalPages}</span>`;

            if (hasNext) {
                html += `<button class="pagination-btn" data-page="${paginationData.currentPage + 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">Next →</button>`;
            }

            html += '</div>';
        }

        container.innerHTML = html;

        // Add click handlers for pagination buttons
        document.querySelectorAll('.pagination-btn').forEach(btn => {
            /**
             * Loads the selected ratings page from a pagination button.
             *
             * @returns {void}
             */
            btn.addEventListener('click', function() {
                const page = parseInt(this.dataset.page);
                loadRatings(page);
            });
        });
    }

    window.loadRatings = loadRatings;

    if (hasFiltersUI) {
        // Apply filters button
        /**
         * Applies the current filter values and resets ratings pagination.
         *
         * @returns {void}
         */
        applyFiltersBtn.addEventListener('click', function() {
            loadRatings(1); // Reset to page 1 when applying filters
        });

        // Clear filters button
        /**
         * Clears all filter controls and reloads the first ratings page.
         *
         * @returns {void}
         */
        clearFiltersBtn.addEventListener('click', function() {
            filterMinScore.value = '';
            filterMaxScore.value = '';
            filterStatus.value = '';
            filterRewatching.value = '';
            filterSortBy.value = 'score';
            filterSortOrder.value = 'desc';
            loadRatings(1);
        });

        loadRatings(currentPage);
    }

    /**
     * Checks whether a value is an absolute HTTP or HTTPS URL.
     *
     * @param {unknown} value Value to check.
     * @returns {boolean} True when the value is an HTTP(S) URL.
     */
    function isHttpUrl(value) {
        return typeof value === 'string' && /^https?:\/\//i.test(value.trim());
    }

    /**
     * Normalizes related character API payloads into a character array.
     *
     * @param {Array<Object>|Object|null|undefined} payload Raw API response payload.
     * @returns {Array<Object>} Normalized related character list.
     */
    function normalizeRelatedCharacters(payload) {
        if (!payload) {
            return [];
        }
        if (Array.isArray(payload)) {
            return payload;
        }
        return payload.characters || payload.items || payload.related_characters || [];
    }

    /**
     * Builds the HTML markup for a related character card.
     *
     * @param {Object} character Related character data.
     * @param {number|string} [character.character_mal_id] MyAnimeList character id.
     * @param {number|string} [character.character_id] Local character id fallback.
     * @param {string} [character.image] Character image URL.
     * @param {string} [character.image_url] Character image URL fallback.
     * @param {string} [character.name] Character display name.
     * @returns {string} Character card HTML string.
     */
    function buildCharacterCard(character) {
        const characterId = character.character_mal_id || character.character_id;
        const href = characterId ? `/characters/${characterId}` : '#';
        const imageSrc = isHttpUrl(character.image)
            ? character.image
            : isHttpUrl(character.image_url)
                ? character.image_url
                : null;

        return `
            <div class="card">
                <a href="${href}">
                    ${imageSrc
                        ? `<img src="${imageSrc}" alt="${character.name || 'Character'}">`
                        : `<div class="card-image-placeholder">
                                <img src="/images/logo.png" alt="Logo">
                           </div>`}
                    <div class="card-body">
                        <p>${character.name || 'Unknown'}</p>
                    </div>
                </a>
            </div>
        `;
    }

    /**
     * Renders related characters into the related characters section.
     *
     * @param {Array<Object>} characters Related character list.
     * @returns {void}
     */
    function renderRelatedCharacters(characters) {
        const section = document.getElementById('related-characters-section');
        if (!section) {
            return;
        }

        let container = document.getElementById('related-characters-container');
        let emptyMessage = document.getElementById('related-characters-empty');

        if (!characters || characters.length === 0) {
            if (container) {
                container.innerHTML = '';
            }
            if (!emptyMessage) {
                emptyMessage = document.createElement('p');
                emptyMessage.id = 'related-characters-empty';
                section.appendChild(emptyMessage);
            }
            emptyMessage.textContent = 'No related characters available.';
            return;
        }

        if (!container) {
            container = document.createElement('div');
            container.id = 'related-characters-container';
            container.className = 'cardsContainer';
            section.appendChild(container);
        }

        container.innerHTML = characters.map(buildCharacterCard).join('');
        if (emptyMessage) {
            emptyMessage.remove();
        }
    }

    /**
     * Loads related characters when they are not already present in the DOM.
     *
     * @returns {Promise<void>}
     */
    async function loadRelatedCharacters() {
        const section = document.getElementById('related-characters-section');
        if (!section || !animeId) {
            return;
        }
        const existingContainer = document.getElementById('related-characters-container');
        if (existingContainer && existingContainer.children.length > 0) {
            return;
        }

        try {
            const response = await axios.get(`/anime/${animeId}/characters`, {
                headers: { Accept: 'application/json' }
            });
            const data = response.data;
            const characters = normalizeRelatedCharacters(data);
            renderRelatedCharacters(characters);
        } catch (error) {
            console.error('Error loading related characters:', error);
        }
    }

    loadRelatedCharacters();
})();
