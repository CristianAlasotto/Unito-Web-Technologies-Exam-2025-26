const ratService = require("../services/ratService");

exports.getRats = async (params, maxAgeMs) => {
    // data is { items, total, totalPages }
    const data = await ratService.fetchRatings(params);
    
    // Check if items exist inside the data object
    if (!data || !data.items || data.items.length === 0) return null;

    // Check cache age
    const ageMs = Date.now() - new Date(data.items[0].createdAt).getTime();
    if (ageMs > maxAgeMs) return null;

    return data;
};

exports.saveRats = async (dataList) => {
    return await ratService.saveRatings(dataList);
};