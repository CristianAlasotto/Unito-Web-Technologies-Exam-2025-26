/**
 * Initializes profile review pagination.
 */
(function() {
    const ratingsContainer = document.getElementById('ratings-container');
    if (!ratingsContainer) {
        return;
    }

    const username = ratingsContainer.dataset.username;
    const pageSize = parseInt(ratingsContainer.dataset.pageSize || '10', 10);
    let currentPage = parseInt(ratingsContainer.dataset.currentPage || '1', 10);

    if (!username) {
        return;
    }

    /**
     * Loads one page of profile ratings via AJAX.
     *
     * @param {number} [page=1] Ratings page to load.
     * @returns {Promise<void>}
     */
    async function loadRatings(page = 1) {
        try {
            ratingsContainer.innerHTML = '<div style="text-align: center; padding: 2rem; color: #aaa;">Loading...</div>';

            const response = await axios.get(`/profile/${username}/ratings-json`, {
                params: {
                    page,
                    pageSize
                }
            });
            const data = response.data;

            renderRatings(data.ratings, data.pagination);
            currentPage = data.pagination.currentPage;
            ratingsContainer.dataset.currentPage = currentPage;
        } catch (error) {
            console.error('Error loading ratings:', error);
            ratingsContainer.innerHTML = '<p style="color: #f44;">Error loading ratings</p>';
        }
    }

    /**
     * Renders profile ratings and pagination controls.
     *
     * @param {Array<Object>} ratings Rating records returned by the API.
     * @param {{currentPage: number, totalPages: number}} pagination Pagination metadata.
     * @returns {void}
     */
    function renderRatings(ratings, pagination) {
        if (!ratings || ratings.length === 0) {
            ratingsContainer.innerHTML = '<p style="color: #888; font-style: italic;">No reviews yet.</p>';
            return;
        }

        let html = '<div style="overflow-x: auto;">';
        html += '<table style="width: 100%; border-collapse: collapse; background: #1a1a1a; border-radius: 8px; overflow: hidden; margin-bottom: 2rem;">';
        html += '<thead><tr style="background: #333; color: #fff; text-align: left;">';
        html += '<th style="padding: 12px 15px;">Anime</th>';
        html += '<th style="padding: 12px 15px;">Score</th>';
        html += '<th style="padding: 12px 15px;">Status</th>';
        html += '<th style="padding: 12px 15px;">Rewatching</th>';
        html += '<th style="padding: 12px 15px;">Episodes</th>';
        html += '</tr></thead><tbody>';

        ratings.forEach((rating) => {
            html += '<tr style="border-bottom: 1px solid #2d2d2d; color: #ccc;">';
            html += `<td style="padding: 12px 15px;"><a href="/anime/${rating.anime_id}" style="color: #fff; text-decoration: none; font-weight: bold;">${rating.anime_title || 'Anime #' + rating.anime_id}</a></td>`;
            html += `<td style="padding: 12px 15px;"><span style="color: #ffffff; font-weight: bold;">&#11088; ${rating.score}</span></td>`;
            html += `<td style="padding: 12px 15px; text-transform: capitalize;">${rating.status}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.is_rewatching ? '<span style="color: #4caf50; font-weight: bold;">Yes</span>' : '<span style="color: #888;">No</span>'}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.num_watched_episodes}</td>`;
            html += '</tr>';
        });

        html += '</tbody></table></div>';

        if (pagination.totalPages > 1) {
            html += '<div class="pagination" style="display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem;">';

            if (pagination.currentPage > 1) {
                html += `<button class="pagination-btn" data-page="${pagination.currentPage - 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">&larr; Previous</button>`;
            }

            html += `<span class="page-info" style="color: #ccc;">Page ${pagination.currentPage} of ${pagination.totalPages}</span>`;

            if (pagination.currentPage < pagination.totalPages) {
                html += `<button class="pagination-btn" data-page="${pagination.currentPage + 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">Next &rarr;</button>`;
            }

            html += '</div>';
        }

        ratingsContainer.innerHTML = html;

        document.querySelectorAll('.pagination-btn').forEach((btn) => {
            /**
             * Loads the page referenced by a profile rating pagination button.
             *
             * @returns {void}
             */
            btn.addEventListener('click', function() {
                const page = parseInt(this.dataset.page, 10);
                loadRatings(page);
            });
        });
    }

    window.loadProfileRatings = loadRatings;
    loadRatings(currentPage);
})();
