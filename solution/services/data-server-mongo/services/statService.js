const Stats = require('../models/Stats'); //

exports.fetchStats = async (params) => {
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    let query = Stats.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' ')); //
    }

    const finalLimit = parseInt(pageSize || limit || 20);
    const skip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(skip);

    return await query.lean().exec();
};

exports.saveStats = async (dataList) => {
    if (!Array.isArray(dataList)) {
        dataList = [dataList];
    }
    return await Stats.insertMany(dataList);
};