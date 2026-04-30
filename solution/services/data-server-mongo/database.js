const mongoose = require('mongoose');

const mongoUrl = process.env.MONGO_URL || 'mongodb://localhost:27017/anime_dynamic';

const connectDB = async () => {
    try {
        await mongoose.connect(mongoUrl, {
            family: 4
        });
        console.log('data-server-mongo: MongoDB Connected successfully');
    } catch (err) {
        console.error('data-server-mongo: MongoDB Connection Error:', err);
    }
};

module.exports = connectDB;
