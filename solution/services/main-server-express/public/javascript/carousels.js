/**
 * Initializes AJAX pagination for every carousel on the page.
 */
(function() {
    /**
     * Creates, updates, or hides a carousel navigation button.
     *
     * @param {HTMLElement} carousel Carousel root element.
     * @param {HTMLElement} container Carousel item container.
     * @param {string} selector Button selector to find an existing button.
     * @param {boolean} enabled Whether the button should be visible.
     * @param {number|string} page Target page number.
     * @param {string} className CSS class assigned to a new button.
     * @param {'before'|'after'} position Button placement relative to the container.
     * @returns {void}
     */
    function updateButton(carousel, container, selector, enabled, page, className, position) {
        let button = carousel.querySelector(selector);

        if (enabled) {
            if (!button) {
                button = document.createElement('button');
                button.className = className;
                if (position === 'before') {
                    carousel.insertBefore(button, container);
                } else {
                    carousel.appendChild(button);
                }
            }

            button.innerHTML = position === 'before' ? '&larr;' : '&rarr;';
            if (position === 'before') {
                button.dataset.prevPage = page;
            } else {
                button.dataset.nextPage = page;
            }
            button.style.display = 'block';
            return;
        }

        if (button) {
            button.style.display = 'none';
        }
    }

    /**
     * Fetches a carousel page and updates its HTML and navigation buttons.
     *
     * @param {HTMLElement} carousel Carousel root element.
     * @param {number|string} page Page number to load.
     * @returns {Promise<void>}
     */
    async function fetchCarouselData(carousel, page) {
        const carouselId = carousel.id;
        const pageSize = carousel.dataset.pageSize;
        const type = carouselId.split('-')[0];
        const endpoint = carousel.dataset.carouselEndpoint || window.location.pathname;
        const carouselContainer = carousel.querySelector('.carousel-container');

        try {
            const response = await axios.get(endpoint, {
                params: {
                    carouselType: type,
                    page,
                    pageSize
                }
            });
            const data = response.data;

            carouselContainer.innerHTML = data.html;
            carousel.dataset.currentPage = data.currentPage;

            updateButton(
                carousel,
                carouselContainer,
                '.btn-left',
                data.hasPrev,
                data.prevPage,
                'btn-left',
                'before'
            );
            updateButton(
                carousel,
                carouselContainer,
                '.btn-right',
                data.hasNext,
                data.nextPage,
                'btn-right',
                'after'
            );
        } catch (error) {
            console.error('Failed to fetch carousel data:', error);
            carouselContainer.innerHTML = '<p class="error-message">Error loading items.</p>';
        }
    }

    /**
     * Binds click handling for previous and next buttons on a carousel.
     *
     * @param {HTMLElement} carousel Carousel root element.
     * @returns {void}
     */
    function bindCarousel(carousel) {
        carousel.addEventListener('click', (event) => {
            const button = event.target.closest('.btn-left, .btn-right');
            if (!button || !carousel.contains(button)) {
                return;
            }

            const page = button.classList.contains('btn-left')
                ? button.dataset.prevPage
                : button.dataset.nextPage;

            if (page) {
                fetchCarouselData(carousel, page);
            }
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('.carousel').forEach(bindCarousel);
    });
})();
