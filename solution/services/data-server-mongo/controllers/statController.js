const statService = require('../services/statService'); //

exports.getStats = async (params, maxAgeMs) => {
    const data = await statService.fetchStats(params);
    if (!data || data.length === 0) return null;

    const ageMs = Date.now() - new Date(data[0].createdAt).getTime();
    if (ageMs > maxAgeMs) return null;

    return data;
};

exports.getStatById = async (id, maxAgeMs) => {

    const data = await statService.fetchStats({ mal_id: id });

    if (!data || data.length === 0) return null;

    const ageMs = Date.now() - new Date(data[0].createdAt).getTime();
    if (ageMs > maxAgeMs) return null;

    return data[0];
};

exports.saveStats = async (dataList) => {
    return await statService.saveStats(dataList);
};