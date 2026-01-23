const Favs = require("../models/Favs");

exports.fetchFavorites = async (params) => {
    let { fields, sort, limit, offset, page, pageSize, search, ...filters } = params;

    let mongoQuery = { ...filters };

    if (filters.id) filters.id = Number(filters.id);

    let query = Favs.find(mongoQuery);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(finalSkip);

    return await query.exec();
};