const Stats = require('../models/Stats');

exports.fetchStats = async (params) => {
    let { fields, sort, limit, offset, page, search, ...filters } = params;

    let mongoQuery = { ...filters };

    let query = Stats.find(mongoQuery);

    if(fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort)
        query = query.select(sort.split(',').join(' '));

    limit = parseInt(limit || 20);
    const skip = page ? (parseInt(page) - 1) * limit : parseInt(offset || 0);

    query = query.limit(limit).skip(skip);

    return await query.exec();
}
