const Ratings = require('../models/Ratings');

exports.fetchRatings = async (params) => {
    // Aggiungiamo pageSize nel destructuring
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    let query = Ratings.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    // Logica identica per la paginazione
    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(finalSkip);

    return await query.lean().exec();
}