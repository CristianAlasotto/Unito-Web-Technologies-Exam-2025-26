const Favs = require('../models/Favs'); // Import the correct model

exports.fetchFavorites = async (params) => {
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    let query = Favs.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(finalSkip);

    return await query.lean().exec();
};

exports.saveFavorites = async (dataList) => {

    if (!Array.isArray(dataList)) {
        dataList = [dataList];
    }

    return await Favs.insertMany(dataList);
};