const mongoose = require('mongoose');

const StatSchema = new mongoose.Schema({
    mal_id: Number,
    watching: Number,
    completed: Number,
    on_hold: Number,
    dropped: Number,
    plan_to_watch: Number,
    total: Number,

    score_1_votes: Number,
    score_1_percentage: Number,
    score_2_votes: Number,
    score_2_percentage: Number,
    score_3_votes: Number,
    score_3_percentage: Number,
    score_4_votes: Number,
    score_4_percentage: Number,
    score_5_votes: Number,
    score_5_percentage: Number,
    score_6_votes: Number,
    score_6_percentage: Number,
    score_7_votes: Number,
    score_7_percentage: Number,
    score_8_votes: Number,
    score_8_percentage: Number,
    score_9_votes: Number,
    score_9_percentage: Number,
    score_10_votes: Number,
    score_10_percentage: Number,

}, {
    collection: 'stats'
});

module.exports = mongoose.model('Stats', StatSchema);