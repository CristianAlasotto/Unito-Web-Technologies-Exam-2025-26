const mongoose = require('mongoose');

const server = 'mongo:27017';
const database = 'anime_dynamic';

const connectDB = async () => {
    try {
        await mongoose.connect(`mongodb://${server}/${database}`, {
            family: 4
        });
        console.log('MongoDB Connected successfully');
    } catch (err) {
        console.error('MongoDB Connection Error:', err);
    }
};

module.exports = connectDB;