const Stats = require('../models/Stats');

exports.fetchStats = async (params) => {
    // Aggiungiamo pageSize nel destructuring
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    let query = Stats.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    // CORREZIONE: Usiamo .sort() e non .select()
    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    // Usiamo pageSize se presente, altrimenti limit, altrimenti default 20
    const finalLimit = parseInt(pageSize || limit || 20);
    const skip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(skip);

    return await query.lean().exec();
}