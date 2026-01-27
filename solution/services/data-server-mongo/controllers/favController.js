const favService = require("../services/favService");

exports.getFavs = async (params, maxAgeMs) => {
    const data = await favService.fetchFavorites(params);

    if (!data || data.length === 0) return null;

    const ageMs = Date.now() - new Date(data[0].createdAt).getTime();
    if (ageMs > maxAgeMs) return null;

    return data;
};

exports.saveFavs = async (dataList) => {
    return await favService.saveFavorites(dataList);
};