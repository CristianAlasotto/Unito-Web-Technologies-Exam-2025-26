const { apiPostgres } = require('./apiClients');

const getApiEndpoint = (type) => {
  switch (type) {
    case 'anime':
      return '/api/details';
    case 'character':
      return '/api/characters';
    case 'staff':
      return '/api/person_details';
    default:
      return null;
  }
};

exports.preview = async (req, res, next) => {
  try {
    const { carouselType, page, pageSize: pageSizeQuery } = req.query;
    const pageSize = parseInt(pageSizeQuery || "7", 10);

    // Handle AJAX request for a single carousel
    if (carouselType) {
      const endpoint = getApiEndpoint(carouselType);
      if (!endpoint) {
        return res.status(400).json({ error: 'Invalid carousel type' });
      }

      const currentPage = parseInt(page || '1', 10);
      const response = await apiPostgres.get(`${endpoint}?page=${currentPage}&pageSize=${pageSize}`);
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

    const popularAnimesPromise = apiPostgres.get(
      `/api/details?page=${animesPage}&pageSize=${pageSize}`
    );
    const popularCharactersPromise = apiPostgres.get(
      `/api/characters?page=${charactersPage}&pageSize=${pageSize}`
    );
    const popularStaffPromise = apiPostgres.get(
      `/api/person_details?page=${staffPage}&pageSize=${pageSize}`
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