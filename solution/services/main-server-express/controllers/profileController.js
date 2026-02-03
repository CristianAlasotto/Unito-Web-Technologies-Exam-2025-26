const { apiPostgres, apiMongo } = require('./apiClients');

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
            apiMongo.get(`/api/favorites`, { params: { username: username, fav_type: 'anime' } }),
            apiMongo.get(`/api/favorites`, { params: { username: username, fav_type: 'character' } }),
            apiMongo.get(`/api/ratings`, { params: { username: username, page: reviewPage, pageSize: reviewPageSize } })
        ]);

        if (profileResult.status === 'rejected') {
            throw profileResult.reason;
        }
        const profileData = profileResult.value.data;

        const extractArray = (result) => {
            if (result.status === 'rejected') return [];
            const data = result.value.data;
            if (Array.isArray(data)) return data;
            if (data && Array.isArray(data.items)) return data.items;
            if (data && Array.isArray(data.data)) return data.data;
            return [];
        };

        const allAnimeIds = extractArray(animeFavIdsResult);
        const allCharIds = extractArray(charFavIdsResult);
        const reviewsData = reviewsResult.status === 'fulfilled' ? reviewsResult.value.data : null;
        const userReviews = extractArray(reviewsResult);

        // Pagination for anime favorites (carousel)
        const animeStartIndex = (animePage - 1) * pageSize;
        const animeEndIndex = animePage * pageSize;
        const animeFavIds = allAnimeIds.slice(animeStartIndex, animeEndIndex);
        const animeTotalPages = Math.ceil(allAnimeIds.length / pageSize);

        // Pagination for character favorites (carousel)
        const charStartIndex = (characterPage - 1) * pageSize;
        const charEndIndex = characterPage * pageSize;
        const charFavIds = allCharIds.slice(charStartIndex, charEndIndex);
        const charTotalPages = Math.ceil(allCharIds.length / pageSize);

        // Pagination for reviews (AJAX will handle further pages)
        const reviewTotalPages = reviewsData?.totalPages
            || (reviewsData?.total ? Math.ceil(reviewsData.total / reviewPageSize) : Math.ceil(userReviews.length / reviewPageSize))
            || 1;

        // Fetch anime details
        let favoriteAnimes = [];
        if (animeFavIds.length > 0) {
            const animePromises = animeFavIds.map(async (fav) => {
                try {
                    const response = await apiPostgres.get(`/api/details/${fav.id}`);
                    return response.data;
                } catch (err) {
                    return null;
                }
            });
            const animeResults = await Promise.all(animePromises);
            favoriteAnimes = animeResults.filter(anime => anime !== null);
        }

        // Fetch character details
        let favoriteCharacters = [];
        if (charFavIds.length > 0) {
            const charPromises = charFavIds.map(async (fav) => {
                try {
                    const response = await apiPostgres.get(`/api/characters/${fav.id}`);
                    return response.data;
                } catch (err) {
                    return null;
                }
            });
            const charResults = await Promise.all(charPromises);
            favoriteCharacters = charResults.filter(char => char !== null);
        }

        // Enhance reviews with anime titles
        let enhancedReviews = [...userReviews];
        if (userReviews.length > 0 && userReviews.length <= 10) {
            const reviewPromises = userReviews.map(async (review) => {
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
            enhancedReviews = await Promise.all(reviewPromises);
        }

        res.render('profile/profile', {
            title: `${profileData.username}'s Profile`,
            currentPage: 'profile',
            profile: profileData,
            favoriteAnimes: favoriteAnimes,
            favoriteCharacters: favoriteCharacters,
            userReviews: enhancedReviews,
            animeCarousel: {
                currentPage: animePage,
                totalPages: animeTotalPages,
                hasPrev: animePage > 1,
                prevPage: animePage - 1,
                hasNext: animePage < animeTotalPages,
                nextPage: animePage + 1,
                pageSize: pageSize
            },
            characterCarousel: {
                currentPage: characterPage,
                totalPages: charTotalPages,
                hasPrev: characterPage > 1,
                prevPage: characterPage - 1,
                hasNext: characterPage < charTotalPages,
                nextPage: characterPage + 1,
                pageSize: pageSize
            },
            pagination: {
                page: reviewPage,
                totalPages: reviewTotalPages,
                hasPrev: false,
                hasNext: reviewPage < reviewTotalPages,
                nextPage: 2,
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

// New endpoint for ratings JSON (AJAX pagination)
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
        const userReviews = Array.isArray(reviewsData) ? reviewsData :
            (reviewsData.items || reviewsData.data || []);
        const totalPages = reviewsData.totalPages
            || (reviewsData.total ? Math.ceil(reviewsData.total / pageSize) : Math.ceil(userReviews.length / pageSize))
            || 1;
        const total = reviewsData.total || userReviews.length;

        // Enhance with anime titles (only for current page)
        let enhancedReviews = [...userReviews];
        if (userReviews.length > 0) {
            const reviewPromises = userReviews.map(async (review) => {
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
            enhancedReviews = await Promise.all(reviewPromises);
        }

        res.json({
            ratings: enhancedReviews,
            pagination: {
                currentPage: page,
                totalPages: totalPages,
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

async function handleCarouselRequest(req, res, username, pageSize) {
    const { carouselType, page } = req.query;
    const currentPage = parseInt(page || '1', 10);

    try {
        let items = [];
        let totalPages = 1;

        if (carouselType === 'anime') {
            const favResult = await apiMongo.get(`/api/favorites`, {
                params: { username: username, fav_type: 'anime' }
            });
            const allFavs = Array.isArray(favResult.data) ? favResult.data :
                (favResult.data?.items || favResult.data?.data || []);

            totalPages = Math.ceil(allFavs.length / pageSize);
            const startIndex = (currentPage - 1) * pageSize;
            const endIndex = currentPage * pageSize;
            const pageFavs = allFavs.slice(startIndex, endIndex);

            if (pageFavs.length > 0) {
                const animePromises = pageFavs.map(async (fav) => {
                    try {
                        const response = await apiPostgres.get(`/api/details/${fav.id}`);
                        return response.data;
                    } catch (err) {
                        return null;
                    }
                });
                const results = await Promise.all(animePromises);
                items = results.filter(anime => anime !== null);
            }

            return res.render('partials/anime_carousel_items', { layout: false, items }, (err, html) => {
                if (err) {
                    console.error('Error rendering partial:', err);
                    return res.status(500).json({ error: 'Error rendering carousel' });
                }
                res.json({
                    html,
                    currentPage,
                    totalPages,
                    hasPrev: currentPage > 1,
                    prevPage: currentPage - 1,
                    hasNext: currentPage < totalPages,
                    nextPage: currentPage + 1
                });
            });

        } else if (carouselType === 'character') {
            const favResult = await apiMongo.get(`/api/favorites`, {
                params: { username: username, fav_type: 'character' }
            });
            const allFavs = Array.isArray(favResult.data) ? favResult.data :
                (favResult.data?.items || favResult.data?.data || []);

            totalPages = Math.ceil(allFavs.length / pageSize);
            const startIndex = (currentPage - 1) * pageSize;
            const endIndex = currentPage * pageSize;
            const pageFavs = allFavs.slice(startIndex, endIndex);

            if (pageFavs.length > 0) {
                const charPromises = pageFavs.map(async (fav) => {
                    try {
                        const response = await apiPostgres.get(`/api/characters/${fav.id}`);
                        return response.data;
                    } catch (err) {
                        return null;
                    }
                });
                const results = await Promise.all(charPromises);
                items = results.filter(char => char !== null);
            }

            return res.render('partials/characters_carousel_items', { layout: false, items }, (err, html) => {
                if (err) {
                    console.error('Error rendering partial:', err);
                    return res.status(500).json({ error: 'Error rendering carousel' });
                }
                res.json({
                    html,
                    currentPage,
                    totalPages,
                    hasPrev: currentPage > 1,
                    prevPage: currentPage - 1,
                    hasNext: currentPage < totalPages,
                    nextPage: currentPage + 1
                });
            });
        }

        return res.status(400).json({ error: 'Invalid carousel type' });

    } catch (err) {
        console.error('Carousel error:', err);
        return res.status(500).json({ error: 'Failed to load carousel data' });
    }
}
