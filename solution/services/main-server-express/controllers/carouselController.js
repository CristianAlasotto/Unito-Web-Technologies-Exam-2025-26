const { apiPostgres } = require('./apiClients');

const getApiRequestConfig = (type) => {
    switch (type) {
        case 'anime':
            return {
                path: '/api/details',
                params: {
                    fields: 'anime_id,title,title_english,title_japanese,image_url',
                    sort: '-popularity',
                },
            };
        case 'character':
            return {
                path: '/api/characters',
                params: {
                    sort: '-favorites',
                },
            };
        case 'staff':
            return {
                path: '/api/person_details',
                params: {
                    sort: '-favorites',
                },
            };
        default:
            return null;
    }
};

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
