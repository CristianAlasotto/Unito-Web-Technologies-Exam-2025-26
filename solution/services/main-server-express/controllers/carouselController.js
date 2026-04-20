/**
 * Carousel controller for asynchronous homepage carousel updates.
 *
 * Responsibilities:
 * - resolves the backend endpoint and query defaults for each carousel type
 * - fetches (with axios) paginated data from the PostgreSQL-backed service
 * - renders Handlebars partials and returns HTML plus pagination metadata
 */

const { apiPostgres } = require('./apiClients');

/**
 * Returns API path and default query params for a carousel type.
 *
 * @param {string} type Carousel type (anime, character, staff).
 * @returns {{path: string, params: Object}|null} Request config or null when invalid.
 */
const getApiRequestConfig = (type) => {
    switch (type) {
        case 'anime':
            return {
                path: '/api/details',
                params: {
                    fields: 'mal_id,title,title_english,title_japanese,image_url',
                    sort: '-popularity',
                },
            };
        case 'character':
            return {
                path: '/api/characters',
                params: {
                    fields: 'favorites, image, name_kanji, name, character_mal_id',
                    sort: '-favorites',
                },
            };
        case 'staff':
            return {
                path: '/api/person_details',
                params: {
                    fileds: 'favorites, image_url, name, person_mal_id, given_name',
                    sort: '-favorites',
                },
            };
        default:
            return null;
    }
};

/**
 * Fetches and renders a single carousel page.
 *
 * @param {import('express').Request} req Express request.
 * @param {import('express').Response} res Express response.
 * @param {import('express').NextFunction} next Express next middleware function.
 * @returns {Promise<void>} Resolves when HTML/JSON response is sent.
 */
exports.getCarouselData = async (req, res, next) => {
    try {
        const { type } = req.params;
        const page = parseInt(req.query.page || '1', 10);
        const pageSize = parseInt(req.query.pageSize || '7', 10);

        const requestConfig = getApiRequestConfig(type);
        if (!requestConfig) {
            return res.status(400).json({ error: 'Invalid carousel type' });
        }

        const query = new URLSearchParams({
            ...requestConfig.params,
            page: String(page),
            pageSize: String(pageSize),
        }).toString();
        const response = await apiPostgres.get(`${requestConfig.path}?${query}`);
        const items = response.data.items || [];
        const totalPages = response.data.totalPages || 1;

        res.render(`partials/${type}_carousel_items`, { items }, (err, html) => {
            if (err) {
                console.error('Error rendering partial:', err);
                return next(err);
            }
            res.json({
                html,
                currentPage: page,
                totalPages,
                hasPrev: page > 1,
                prevPage: page - 1,
                hasNext: page < totalPages,
                nextPage: page + 1,
            });
        });
    } catch (err) {
        console.error('Error fetching carousel data:', err);
        next(err);
    }
};
