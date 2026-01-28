const { apiPostgres, apiMongo } = require('./apiClients'); // postgres db, usa il data-server-mongo

/* 
  curl http://localhost:3001/getfavs
  curl http://localhost:3001/getratings
  curl http://localhost:3001/getstats
*/

// Favorites without ID
exports.favsWithoutId = {
	list: async (req, res, next) => {
		try {
			//const endpoint = '/api/favs';
			const endpoint = 'getfavs';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/favs', {
				title: 'Favorites',
				favs: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};

// Ratings without ID
exports.ratingsWithoutId = {
	list: async (req, res, next) => {
		try {
			//const endpoint = '/api/ratings';
			const endpoint = 'getRatings';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/ragtings', {
				title: 'Ratings',
				ragtings: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};

// Stats without ID
exports.statsWithoutId = {
	list: async (req, res, next) => {
		try {
			//const endpoint = '/api/stats';
			const endpoint = 'getStats';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/stats', {
				title: 'Statistics',
				stats: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};


// Favorites list
exports.favs = {
	list: async (req, res, next) => {
		try {
			const { id } = req.params;
			const endpoint = id ? `/api/favs/${id}` : '/api/favs';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/favs', {
				title: 'Favorites',
				favs: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};

// Ratings list
exports.ragtings = {
	list: async (req, res, next) => {
		try {
			const { id } = req.params;
			const endpoint = id ? `/api/ratings/${id}` : '/api/ratings';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/ragtings', {
				title: 'Ratings',
				ragtings: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};

// Stats list
exports.stats = {
	list: async (req, res, next) => {
		try {
			const { id } = req.params;
			const endpoint = id ? `/api/stats/${id}` : '/api/stats';
			const response = await apiPostgres.get(endpoint);
			res.render('generalTest/stats', {
				title: 'Statistics',
				stats: response.data,
				currentPage: 'generalTest'
			});
		} catch (err) {
			next(err);
		}
	}
};

// UNICO HANDLER
// Pagina riepilogo /generalTest
exports.overview = async (req, res, next) => {
  try {
    const [favsRes, ratingsRes, statsRes] = await Promise.allSettled([
      apiMongo.get('/getfavs'),
      apiMongo.get('/getratings'),
      apiMongo.get('/getstats')
    ]);

    const viewModel = {
      title: 'General Test',
      currentPage: 'generalTest',
      favs: favsRes.status === 'fulfilled' ? favsRes.value.data : null,
      ratings: ratingsRes.status === 'fulfilled' ? ratingsRes.value.data : null,
      stats: statsRes.status === 'fulfilled' ? statsRes.value.data : null,
      favsError: favsRes.status === 'rejected',
      ratingsError: ratingsRes.status === 'rejected',
      statsError: statsRes.status === 'rejected'
    };

    res.render('generalTest', viewModel);
  } catch (err) {
    next(err);
  }
};