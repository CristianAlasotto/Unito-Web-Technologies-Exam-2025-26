const mongoose = require('mongoose');

const server = 'localhost:27017';
const database = 'anime_dynamic';

mongoose.Promise = global.Promise;

const connectDB = async () => {
    try {
        await mongoose.connect(`mongodb://${server}/${database}`, {
            family: 4,
            serverSelectionTimeoutMS: 5000
        });
        console.log('data-server-mongo: MongoDB Connected successfully');
    } catch (err) {
        console.error('data-server-mongo: MongoDB Connection Error:', err);
        process.exit(1);
    }
};

module.exports = connectDB;
