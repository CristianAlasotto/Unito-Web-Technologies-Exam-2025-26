const { apiPostgres } = require('./apiClients');

const getApiRequestConfig = (type) => {
  switch (type) {
    case 'anime':
      return {
        path: '/api/details',
        params: {
          fields: 'mal_id,title,title_english,title_japanese,image_url',
          sort: 'popularity',
        },
      };
    case 'character':
      return {
        path: '/api/characters',
        params: {
          fields: 'character_mal_id,image,name',
          sort: '-favorites',
        },
      };
    case 'staff':
      return {
        path: '/api/person_details',
        params: {
          sort: '-favorites',
        },
      };
    default:
      return null;
  }
};

exports.preview = async (req, res, next) => {
  try {
    const { carouselType, page, pageSize: pageSizeQuery } = req.query;
    const pageSize = parseInt(pageSizeQuery || "5", 10);

    // Handle AJAX request for a single carousel
    if (carouselType) {
      const requestConfig = getApiRequestConfig(carouselType);
      if (!requestConfig) {
        return res.status(400).json({ error: 'Invalid carousel type' });
      }

      const currentPage = parseInt(page || '1', 10);
      const query = new URLSearchParams({
        ...requestConfig.params,
        page: String(currentPage),
        pageSize: String(pageSize),
      }).toString();
      const response = await apiPostgres.get(`${requestConfig.path}?${query}`);
      const items = response.data.items || [];
      const totalPages = response.data.totalPages || 1;

      const partialName = carouselType === 'character' ? 'characters' : carouselType;

      return res.render(`partials/${partialName}_carousel_items`, { layout: false, items }, (err, html) => {
        if (err) {
          console.error('Error rendering partial:', err);
          return next(err);
        }
        res.json({
          html,
          currentPage,
          totalPages,
          hasPrev: currentPage > 1,
          prevPage: currentPage - 1,
          hasNext: currentPage < totalPages,
          nextPage: currentPage + 1,
        });
      });
    }

    // Handle initial page load
    const animesPage = parseInt(req.query.animesPage || "1", 10);
    const charactersPage = parseInt(req.query.charactersPage || "1", 10);
    const staffPage = parseInt(req.query.staffPage || "1", 10);

    const animeConfig = getApiRequestConfig('anime');
    const characterConfig = getApiRequestConfig('character');
    const staffConfig = getApiRequestConfig('staff');

    const popularAnimesPromise = apiPostgres.get(
      `${animeConfig.path}?${new URLSearchParams({
        ...animeConfig.params,
        page: String(animesPage),
        pageSize: String(pageSize),
      }).toString()}`
    );
    const popularCharactersPromise = apiPostgres.get(
      `${characterConfig.path}?${new URLSearchParams({
        ...characterConfig.params,
        page: String(charactersPage),
        pageSize: String(pageSize),
      }).toString()}`
    );
    const popularStaffPromise = apiPostgres.get(
      `${staffConfig.path}?${new URLSearchParams({
        ...staffConfig.params,
        page: String(staffPage),
        pageSize: String(pageSize),
      }).toString()}`
    );

    const [
      popularAnimesRes,
      popularCharactersRes,
      popularStaffRes
    ] = await Promise.all([
      popularAnimesPromise,
      popularCharactersPromise,
      popularStaffPromise
    ]);

    const popularAnimes = popularAnimesRes.data.items || [];
    const popularCharacters = popularCharactersRes.data.items || [];
    const popularStaff = popularStaffRes.data.items || [];
    
    const animesTotalPages = popularAnimesRes.data.totalPages || 1;
    const charactersTotalPages = popularCharactersRes.data.totalPages || 1;
    const staffTotalPages = popularStaffRes.data.totalPages || 1;

    res.render("index", {
      title: "Home",
      popularAnimes,
      popularCharacters,
      popularStaff,
      animeCarousel: {
        currentPage: animesPage,
        totalPages: animesTotalPages,
        hasPrev: animesPage > 1,
        prevPage: animesPage - 1,
        hasNext: animesPage < animesTotalPages,
        nextPage: animesPage + 1,
        pageSize: pageSize
      },
      characterCarousel: {
        currentPage: charactersPage,
        totalPages: charactersTotalPages,
        hasPrev: charactersPage > 1,
        prevPage: charactersPage - 1,
        hasNext: charactersPage < charactersTotalPages,
        nextPage: charactersPage + 1,
        pageSize: pageSize
      },
      staffCarousel: {
        currentPage: staffPage,
        totalPages: staffTotalPages,
        hasPrev: staffPage > 1,
        prevPage: staffPage - 1,
        hasNext: staffPage < staffTotalPages,
        nextPage: staffPage + 1,
        pageSize: pageSize
      },
      warningPopular:
        popularAnimes.length === 0 ? "Nessun anime popolare trovato." : null
    });
  } catch (err) {
    console.error(err);
    // Check if it was an AJAX request to send appropriate error
    if (req.query.carouselType) {
        return res.status(500).json({ error: 'Could not fetch data for carousel.' });
    }
    res.render("index", {
      title: "Home",
      popularAnimes: [],
      popularCharacters: [],
      popularStaff: [],
      error: "Could not fetch data for carousels."
    });
  }
};
