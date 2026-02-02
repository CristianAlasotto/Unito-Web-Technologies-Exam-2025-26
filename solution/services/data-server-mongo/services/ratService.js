const Ratings = require('../models/Ratings');
const Stats = require('../models/Stats');

exports.fetchRatings = async (params) => {
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    // Convert numeric fields from strings to numbers
    const numericFields = ['anime_id', 'score', 'is_rewatching', 'num_watched_episodes'];
    numericFields.forEach(field => {
        if (filters[field] !== undefined) {
            const num = parseInt(filters[field]);
            if (!isNaN(num)) {
                filters[field] = num;
            }
        }
    });

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

    let total = null;
    let totalPages = null;

    if (filters.username || filters.anime_id) {
        total = await Ratings.countDocuments(filters);
        totalPages = Math.ceil(total / finalLimit);
    }

    return {
        items: items,
        total: total,
        totalPages: totalPages
    };
};

exports.createRating = async (data) => {
    const avg = await updateStats(data.anime_id, data.status, data.score);
    return { new_average_score: avg };
};

async function updateStats(mal_id, status, score) {
    const inc = { [status]: 1, total: 1 };
    if (score > 0) inc[`score_${score}_votes`] = 1;

    const stats = await Stats.findOneAndUpdate({ mal_id }, { $inc: inc }, { upsert: true, new: true });

    let totalVotes = 0, sum = 0;
    for (let i = 1; i <= 10; i++) {
        const v = stats[`score_${i}_votes`] || 0;
        totalVotes += v;
        sum += v * i;
    }

    const percentages = {};
    for (let i = 1; i <= 10; i++) {
        percentages[`score_${i}_percentage`] = totalVotes ? parseFloat(((stats[`score_${i}_votes`] / totalVotes) * 100).toFixed(2)) : 0;
    }

    await Stats.updateOne({ mal_id }, { $set: percentages });
    return totalVotes ? parseFloat((sum / totalVotes).toFixed(2)) : 0;
}