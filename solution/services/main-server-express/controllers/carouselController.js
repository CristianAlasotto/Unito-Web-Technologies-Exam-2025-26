const { apiPostgres } = require('./apiClients');

const getApiEndpoint = (type) => {
    switch (type) {
        case 'anime':
            return '/api/details';
        case 'character':
            return '/api/characters';
        case 'staff':
            return '/api/person_details';
        default:
            return null;
    }
};

exports.getCarouselData = async (req, res, next) => {
    try {
        const { type } = req.params;
        const page = parseInt(req.query.page || '1', 10);
        const pageSize = parseInt(req.query.pageSize || '7', 10);

        const endpoint = getApiEndpoint(type);
        if (!endpoint) {
            return res.status(400).json({ error: 'Invalid carousel type' });
        }

        const response = await apiPostgres.get(`${endpoint}?page=${page}&pageSize=${pageSize}`);
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