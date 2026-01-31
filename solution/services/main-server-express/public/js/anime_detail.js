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

    document.getElementById('filter-minScore').value = filters.minScore;
    document.getElementById('filter-maxScore').value = filters.maxScore;
    document.getElementById('filter-status').value = filters.status;
    document.getElementById('filter-rewatching').value = filters.rewatching;
    document.getElementById('filter-sortBy').value = filters.sortBy;
    document.getElementById('filter-sortOrder').value = filters.sortOrder;

    function showMessage(message, type) {
        const messageEl = document.getElementById('rating-message');
        messageEl.textContent = message;
        messageEl.style.display = 'block';
        messageEl.style.background = type === 'success' ? '#4caf50' : '#f44336';
        messageEl.style.color = '#fff';

        // Auto-hide after 5 seconds
        setTimeout(() => {
            messageEl.style.display = 'none';
        }, 5000);
    }

    // Handle rating form submission
    document.getElementById('add-rating-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const submitBtn = document.getElementById('submit-rating-btn');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Submitting...';

        const formData = {
            username: document.getElementById('rating-username').value,
            anime_id: parseInt(animeId),
            score: parseInt(document.getElementById('rating-score').value),
            status: document.getElementById('rating-status').value,
            num_watched_episodes: parseInt(document.getElementById('rating-episodes').value),
            is_rewatching: parseInt(document.getElementById('rating-rewatching').value)
        };

        try {
            console.log('Submitting rating:', formData);

            const response = await fetch(`/anime/${animeId}/ratings`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);

            const responseText = await response.text();
            console.log('Response text:', responseText);

            let result;
            try {
                result = JSON.parse(responseText);
            } catch (e) {
                console.error('Response is not JSON:', responseText);
                throw new Error('Server returned non-JSON response: ' + responseText.substring(0, 100));
            }

            console.log('Response data:', result);

            if (!response.ok) {
                const errorMsg = result.message || result.error || 'Failed to submit rating (status ' + response.status + ')';
                throw new Error(errorMsg);
            }

            showMessage('Rating submitted successfully!', 'success');

            document.getElementById('add-rating-form').reset();

            // Reload ratings to show the new one
            loadRatings(1);

        } catch (error) {
            console.error('Error submitting rating:', error);
            showMessage('Error: ' + error.message, 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Rating';
        }
    });

    async function loadRatings(page = 1) {
        const params = new URLSearchParams({ page: page });

        const minScore = document.getElementById('filter-minScore').value;
        const maxScore = document.getElementById('filter-maxScore').value;
        const status = document.getElementById('filter-status').value;
        const rewatching = document.getElementById('filter-rewatching').value;
        const sortBy = document.getElementById('filter-sortBy').value;
        const sortOrder = document.getElementById('filter-sortOrder').value;

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
            document.getElementById('ratings-container').innerHTML = '<div style="text-align: center; padding: 2rem; color: #aaa;">Loading...</div>';

            // Fetch ratings via AJAX
            const response = await fetch(`/anime/${animeId}/ratings-json?${params.toString()}`);
            const data = await response.json();

            // Update ratings table
            renderRatings(data.ratings, data.pagination, params);
        } catch (error) {
            console.error('Error loading ratings:', error);
            document.getElementById('ratings-container').innerHTML = '<p style="color: #f44; margin-bottom: 2rem;">Error loading ratings.</p>';
        }
    }

    // Function to render ratings HTML
    function renderRatings(ratings, pagination, params) {
        const container = document.getElementById('ratings-container');

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
            html += `<td style="padding: 12px 15px;"><span style="color: #ff9800; font-weight: bold;">⭐ ${rating.score}</span></td>`;
            html += `<td style="padding: 12px 15px; text-transform: capitalize;">${rating.status}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.is_rewatching ? '<span style="color: #4caf50; font-weight: bold;">Yes</span>' : '<span style="color: #888;">No</span>'}</td>`;
            html += `<td style="padding: 12px 15px;">${rating.num_watched_episodes}</td>`;
            html += '</tr>';
        });

        html += '</tbody></table></div>';

        // Add pagination
        if (pagination.totalPages > 1) {
            html += '<div class="pagination" style="display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1rem;">';

            if (pagination.hasPrev) {
                html += `<button class="pagination-btn" data-page="${pagination.currentPage - 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">← Previous</button>`;
            }

            html += `<span class="page-info" style="color: #ccc;">Page ${pagination.currentPage} of ${pagination.totalPages}</span>`;

            if (pagination.hasNext) {
                html += `<button class="pagination-btn" data-page="${pagination.currentPage + 1}" style="padding: 0.5rem 1rem; background: #333; color: #fff; border: none; border-radius: 4px; cursor: pointer;">Next →</button>`;
            }

            html += '</div>';
        }

        container.innerHTML = html;

        // Add click handlers for pagination buttons
        document.querySelectorAll('.pagination-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const page = parseInt(this.dataset.page);
                loadRatings(page);
            });
        });
    }

    // Apply filters button
    document.getElementById('apply-filters-btn').addEventListener('click', function() {
        loadRatings(1); // Reset to page 1 when applying filters
    });

    // Clear filters button
    document.getElementById('clear-filters-btn').addEventListener('click', function() {
        document.getElementById('filter-minScore').value = '';
        document.getElementById('filter-maxScore').value = '';
        document.getElementById('filter-status').value = '';
        document.getElementById('filter-rewatching').value = '';
        document.getElementById('filter-sortBy').value = 'score';
        document.getElementById('filter-sortOrder').value = 'desc';
        loadRatings(1);
    });
})();