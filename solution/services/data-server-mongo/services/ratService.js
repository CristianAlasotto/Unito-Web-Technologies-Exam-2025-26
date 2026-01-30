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

    // status is a string field, so keep it as is (no conversion needed)

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

    // Only count if filters are applied (has indexed fields like username or anime_id)
    let total = null;
    let totalPages = null;

    // Only perform count if we have filters with indexes (username or anime_id)
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

exports.createRating = async (ratingData) => {
    // Insert the new rating
    const newRating = await Ratings.create(ratingData);

    // Update statistics
    await updateStats(ratingData.anime_id, ratingData.status, ratingData.score);

    return newRating;
};

async function updateStats(animeId, status, score) {
    // Map status string to stats field name
    const statusMap = {
        'watching': 'watching',
        'completed': 'completed',
        'on_hold': 'on_hold',
        'dropped': 'dropped',
        'plan_to_watch': 'plan_to_watch'
    };

    // Prepare the update object
    const updateObj = {
        $inc: {}
    };

    // Increment the appropriate status counter
    if (statusMap[status]) {
        updateObj.$inc[statusMap[status]] = 1;
        updateObj.$inc.total = 1;
    }

    // Increment the score votes if score is valid (1-10)
    if (score >= 1 && score <= 10) {
        updateObj.$inc[`score_${score}_votes`] = 1;
    }

    // Update or create the stats document
    await Stats.findOneAndUpdate(
        { mal_id: animeId },
        updateObj,
        { upsert: true, new: true }
    );

    // Recalculate percentages for all scores
    await recalculateScorePercentages(animeId);
}

async function recalculateScorePercentages(animeId) {
    const stats = await Stats.findOne({ mal_id: animeId });

    if (!stats) return;

    // Calculate total score votes
    let totalScoreVotes = 0;
    for (let i = 1; i <= 10; i++) {
        totalScoreVotes += stats[`score_${i}_votes`] || 0;
    }

    // If no votes, set all percentages to 0
    if (totalScoreVotes === 0) {
        const updateObj = {};
        for (let i = 1; i <= 10; i++) {
            updateObj[`score_${i}_percentage`] = 0;
        }
        await Stats.updateOne({ mal_id: animeId }, { $set: updateObj });
        return;
    }

    // Calculate and update percentages
    const updateObj = {};
    for (let i = 1; i <= 10; i++) {
        const votes = stats[`score_${i}_votes`] || 0;
        const percentage = (votes / totalScoreVotes) * 100;
        updateObj[`score_${i}_percentage`] = parseFloat(percentage.toFixed(2));
    }

    await Stats.updateOne({ mal_id: animeId }, { $set: updateObj });
}