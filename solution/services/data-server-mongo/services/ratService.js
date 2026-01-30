const Ratings = require('../models/Ratings');

exports.fetchRatings = async (params) => {
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    // 1. Get Total Count for Pagination
    const total = await Ratings.countDocuments(filters);

    let query = Ratings.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(finalSkip);

    const items = await query.lean().exec();

    // 2. Return Object with Items and Metadata
    return {
        items: items,
        total: total,
        totalPages: Math.ceil(total / finalLimit)
    };
};

exports.saveRatings = async (dataList) => {
    if (!Array.isArray(dataList)) {
        dataList = [dataList];
    }
    return await Ratings.insertMany(dataList);
};