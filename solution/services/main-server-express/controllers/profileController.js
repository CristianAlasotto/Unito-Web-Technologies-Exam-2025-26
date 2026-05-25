/**
 * Profile controller for user profile pages.
 *
 * Responsibilities:
 * - renders the profile page with favorite anime/characters and ratings
 * - serves AJAX endpoints for ratings pagination and carousel pages
 * - aggregates data from PostgreSQL-backed and MongoDB-backed services
 */

const { apiPostgres, apiMongo } = require('./apiClients');
const {
    buildPagination,
    extractItems,
    extractSettledItems
} = require('./controllerUtils');

const PROFILE_CAROUSELS = {
    anime: {
        favType: 'anime',
        detailPath: (id) => `/api/details/${id}`,
        partial: 'partials/anime_carousel_items'
    },
    character: {
        favType: 'character',
        detailPath: (id) => `/api/characters/${id}`,
        partial: 'partials/characters_carousel_items'
    }
};

/**
 * Returns the items that belong to one paginated page.
 *
 * @param {Array<Object>} items Full item list.
 * @param {number} currentPage One-based page number.
 * @param {number} pageSize Number of items per page.
 * @returns {Array<Object>} Items for the selected page.
 */
function getItemsPage(items, currentPage, pageSize) {
    const startIndex = (currentPage - 1) * pageSize;
    return items.slice(startIndex, startIndex + pageSize);
}

/**
 * Fetches favorite ids for a user and favorite type.
 *
 * @param {string} username Profile username.
 * @param {'anime'|'character'} favType Favorite category.
 * @returns {Promise<Object>} Axios response from the favorites API.
 */
function fetchFavorites(username, favType) {
    return apiMongo.get('/api/favorites', {
        params: { username: username, fav_type: favType }
    });
}

/**
 * Fetches detailed records for a page of favorite ids.
 *
 * @param {Array<{id: number|string}>} favorites Favorite records containing item ids.
 * @param {Function} detailPath Function that returns the backend detail path.
 * @returns {Promise<Array<Object>>} Detail records that were fetched successfully.
 */
async function fetchFavoriteDetails(favorites, detailPath) {
    if (favorites.length === 0) {
        return [];
    }

    const detailPromises = favorites.map(async (fav) => {
        try {
            const response = await apiPostgres.get(detailPath(fav.id));
            return response.data;
        } catch (err) {
            return null;
        }
    });
    const results = await Promise.all(detailPromises);
    return results.filter((item) => item !== null);
}

/**
 * Adds anime titles to rating records when available.
 *
 * @param {Array<Object>} reviews Rating records to enrich.
 * @returns {Promise<Array<Object>>} Ratings with anime_title when lookup succeeds.
 */
async function enhanceReviewsWithAnimeTitles(reviews) {
    if (reviews.length === 0) {
        return [];
    }

    const reviewPromises = reviews.map(async (review) => {
        try {
            const animeResponse = await apiPostgres.get(`/api/details/${review.anime_id}`);
            return {
                ...review,
                anime_title: animeResponse.data.title
            };
        } catch (err) {
            return review;
        }
    });
    return Promise.all(reviewPromises);
}

/**
 * Renders a user profile page or handles profile carousel AJAX requests.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware function.
 * @returns {Promise<void>} Resolves when HTML/JSON response is sent.
 */
exports.showProfile = async (req, res, next) => {
    const username = req.params.username;
    const pageSize = 6; // 6 items per carousel (3x2 grid)
    const reviewPageSize = 10;

    try {
        // Handle AJAX request for carousel (anime or character only)
        if (req.query.carouselType) {
            return await handleCarouselRequest(req, res, username, pageSize);
        }

        // Initial page load
        const animePage = parseInt(req.query.animePage) || 1;
        const characterPage = parseInt(req.query.characterPage) || 1;
        const reviewPage = 1; // Initial load, page 1

        const [profileResult, animeFavIdsResult, charFavIdsResult, reviewsResult] = await Promise.allSettled([
            apiPostgres.get(`/api/profiles/${username}`),
            fetchFavorites(username, PROFILE_CAROUSELS.anime.favType),
            fetchFavorites(username, PROFILE_CAROUSELS.character.favType),
            apiMongo.get(`/api/ratings`, { params: { username: username, page: reviewPage, pageSize: reviewPageSize } })
        ]);

        if (profileResult.status === 'rejected') {
            throw profileResult.reason;
        }
        const profileData = profileResult.value.data;

        const allAnimeIds = extractSettledItems(animeFavIdsResult);
        const allCharIds = extractSettledItems(charFavIdsResult);
        const reviewsData = reviewsResult.status === 'fulfilled' ? reviewsResult.value.data : null;
        const userReviews = extractSettledItems(reviewsResult);

        const animeTotalPages = Math.ceil(allAnimeIds.length / pageSize);
        const animeFavIds = getItemsPage(allAnimeIds, animePage, pageSize);
        const charTotalPages = Math.ceil(allCharIds.length / pageSize);
        const charFavIds = getItemsPage(allCharIds, characterPage, pageSize);

        // Pagination for reviews (AJAX will handle further pages)
        const reviewTotalPages = reviewsData?.totalPages
            || (reviewsData?.total ? Math.ceil(reviewsData.total / reviewPageSize) : Math.ceil(userReviews.length / reviewPageSize))
            || 1;

        const [favoriteAnimes, favoriteCharacters] = await Promise.all([
            fetchFavoriteDetails(animeFavIds, PROFILE_CAROUSELS.anime.detailPath),
            fetchFavoriteDetails(charFavIds, PROFILE_CAROUSELS.character.detailPath)
        ]);

        const enhancedReviews = userReviews.length <= 10
            ? await enhanceReviewsWithAnimeTitles(userReviews)
            : userReviews;

        res.render('profile/profile', {
            title: `${profileData.username}'s Profile`,
            currentPage: 'profile',
            profile: profileData,
            favoriteAnimes: favoriteAnimes,
            favoriteCharacters: favoriteCharacters,
            userReviews: enhancedReviews,
            animeCarousel: {
                ...buildPagination(animePage, animeTotalPages),
                pageSize: pageSize
            },
            characterCarousel: {
                ...buildPagination(characterPage, charTotalPages),
                pageSize: pageSize
            },
            pagination: {
                page: reviewPage,
                ...buildPagination(reviewPage, reviewTotalPages),
                pageSize: reviewPageSize
            }
        });

    } catch (err) {
        console.error(err);
        res.render('profile/profile', {
            title: 'Profile Error',
            currentPage: 'profile',
            error: 'The requested profile could not be loaded.',
            profile: null
        });
    }
};

/**
 * Returns paginated user ratings as JSON for asynchronous profile updates.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @returns {Promise<void>} Resolves when JSON response is sent.
 */
exports.getRatingsJson = async (req, res) => {
    try {
        const { username } = req.params;
        const page = parseInt(req.query.page || '1', 10);
        const pageSize = parseInt(req.query.pageSize || '10', 10);

        // Get all ratings for user
        const reviewsResult = await apiMongo.get(`/api/ratings`, {
            params: { username: username, page: page, pageSize: pageSize }
        });

        const reviewsData = reviewsResult.data || {};
        const userReviews = extractItems(reviewsData);
        const totalPages = reviewsData.totalPages
            || (reviewsData.total ? Math.ceil(reviewsData.total / pageSize) : Math.ceil(userReviews.length / pageSize))
            || 1;
        const total = reviewsData.total || userReviews.length;

        const enhancedReviews = await enhanceReviewsWithAnimeTitles(userReviews);

        res.json({
            ratings: enhancedReviews,
            pagination: {
                ...buildPagination(page, totalPages),
                total: total
            }
        });
    } catch (err) {
        console.error('Error fetching ratings:', err.message);
        res.status(500).json({
            ratings: [],
            pagination: { currentPage: 1, totalPages: 1, total: 0 },
            error: 'Unable to load ratings'
        });
    }
};

/**
 * Handles AJAX requests for anime/character profile carousels.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {string} username Profile username.
 * @param {number} pageSize Number of items per carousel page.
 * @returns {Promise<void>} Resolves when JSON response is sent.
 */
async function handleCarouselRequest(req, res, username, pageSize) {
    const { carouselType, page } = req.query;
    const currentPage = parseInt(page || '1', 10);
    const carouselConfig = PROFILE_CAROUSELS[carouselType];

    if (!carouselConfig) {
        return res.status(400).json({ error: 'Invalid carousel type' });
    }

    try {
        const favResult = await fetchFavorites(username, carouselConfig.favType);
        const allFavs = extractItems(favResult.data);
        const totalPages = Math.ceil(allFavs.length / pageSize);
        const pageFavs = getItemsPage(allFavs, currentPage, pageSize);
        const items = await fetchFavoriteDetails(pageFavs, carouselConfig.detailPath);

        return res.render(carouselConfig.partial, { layout: false, items }, (err, html) => {
            if (err) {
                console.error('Error rendering partial:', err);
                return res.status(500).json({ error: 'Error rendering carousel' });
            }
            res.json({
                html,
                ...buildPagination(currentPage, totalPages)
            });
        });

    } catch (err) {
        console.error('Carousel error:', err);
        return res.status(500).json({ error: 'Failed to load carousel data' });
    }
}
