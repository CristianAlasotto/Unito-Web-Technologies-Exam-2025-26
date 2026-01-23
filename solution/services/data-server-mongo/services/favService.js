const Favs = require("../models/Favs");

exports.getAll = async (params) => {
    let { fields, sort, limit, offset, page, pageSize, ...filters } = params;

    // Support finding by ID regardless of type (Number vs String)
    if (filters.id) {
        filters.id = !isNaN(filters.id) ? Number(filters.id) : filters.id;
    }

    let query = Favs.find(filters);

    if (fields) query.select(fields.split(',').join(' '));
    if (sort) query.sort(sort);

    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    return await query.limit(finalLimit).skip(finalSkip);
};

exports.getByUser = async (username) => {
    return await Favs.find({ username: username });
};

exports.getCountByAnime = async (animeId) => {
    const id = !isNaN(animeId) ? Number(animeId) : animeId;
    return await Favs.countDocuments({ id: id });
};

exports.getPopular = async (n = 10) => {
    return await Favs.aggregate([
        { $group: { _id: "$id", count: { $sum: 1 } } },
        { $sort: { count: -1 } },
        { $limit: parseInt(n) }
    ]);
};