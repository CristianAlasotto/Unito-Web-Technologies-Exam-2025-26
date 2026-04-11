const mongoose = require('mongoose');

const server = 'localhost:27017';
const database = 'anime_dynamic';

const connectDB = async () => {
    try {
        await mongoose.connect(`mongodb://${server}/${database}`, {
            family: 4
        });
        console.log('data-server-mongo: MongoDB Connected successfully');
    } catch (err) {
        console.error('data-server-mongo: MongoDB Connection Error:', err);
    }
};

module.exports = connectDB;