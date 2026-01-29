const ratService = require("../services/ratService");

exports.getRats = async (params, maxAgeMs) => {
    const data = await ratService.fetchRatings(params);
    if (!data || data.length === 0) return null;

    const ageMs = Date.now() - new Date(data[0].createdAt).getTime();
    if (ageMs > maxAgeMs) return null;

    return data;
};

exports.saveRats = async (dataList) => {
    return await ratService.saveRatings(dataList);
};