const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';

// PROVVISORIO
var temp_userName = 'Folleh';

async function profileController(req, res, next) {
    try {
        const username = req.query.user || temp_userName;
        
        const response = await axios.get(`${API_BASE_URL}/profiles/${username}`);
        const profile = response.data;
        
        res.render('profile/profile', {
            title: 'Profile',
            profile,
            currentPage: 'profile'
        });
    } catch (error) {
        if (error.response?.status === 404) {
            // return next();
            const username = req.query.user || temp_userName;
            res.status(404).render('profile/profile', {
                title: 'Profile Not Found',
                warning: `Profile for user "${username}" not found.`,
                currentPage: 'profile',
                profile: {}
            });
            return;
        }
        next(error);
    }
}

module.exports = profileController;