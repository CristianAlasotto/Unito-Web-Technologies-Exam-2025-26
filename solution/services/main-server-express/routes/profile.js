import express from 'express';
import { dataExpressApi } from '../lib/api.js';
import { MOCK_PROFILE } from '../lib/mockDb.js';

const router = express.Router();

const USE_MOCK_DATA = (process.env.USE_MOCK_DATA || 'false').toLowerCase() === 'true';

// Profile route with username parameter
router.get('/:username', async (req, res) => {
  try {
    const { username } = req.params;

    if (USE_MOCK_DATA) {
      const profile = MOCK_PROFILE[username];
      
      if (!profile) {
        return res.status(404).render('error', { 
          message: 'Profile not found in mock data',
          clientLogJson: JSON.stringify({ username, error: 'not_found' })
        });
      }
      
      return res.render('profile/profile', {
        title: `Profile - ${username}`,
        profile: profile,
        warning: 'Mock data enabled (USE_MOCK_DATA=true)',
      });
    }

    // Real API call
    const profileResponse = await dataExpressApi.get(`/api/users/${username}`);
    
    return res.render('profile/profile', {
      title: `Profile - ${username}`,
      profile: profileResponse.data,
    });
  } catch (error) {
    console.error('Profile error:', error.message);
    return res.status(404).render('error', { 
      message: 'Profile not found',
      clientLogJson: JSON.stringify({ 
        username: req.params.username, 
        error: error.message 
      })
    });
  }
});

// Fallback route without username
router.get('/', (req, res) => {
  return res.status(400).render('error', {
    message: 'Invalid profile path. Use /profile/:username',
    clientLogJson: JSON.stringify('Username not provided in /profile/:username'),
  });
});

export default router;