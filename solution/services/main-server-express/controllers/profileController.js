const { apiPostgres } = require('./apiClients');

exports.showProfile = async (req, res, next) => {
    const username = req.params.username;

    try {
        const response = await apiPostgres.get(`/api/profiles/${username}`);
        const profileData = response.data;

        res.render('profile/profile', {
            title: `${profileData.username}'s Profile`,
            currentPage: 'profile',
            profile: profileData
        });
    } catch (err) {
        res.render('profile/profile', {
            title: 'Profile Error',
            error: 'The requested profile could not be loaded.',
            currentPage: 'profile'
        });
    }
};