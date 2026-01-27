const { apiPostgres } = require('./apiClients');

exports.preview = async (req, res, next) => {
  try {
    const animesPage = parseInt(req.query.animesPage || "1", 10);
    const charactersPage = parseInt(req.query.charactersPage || "1", 10);
    const staffPage = parseInt(req.query.staffPage || "1", 10);
    const pageSize = parseInt(req.query.pageSize || "7", 10);

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
    res.render("index", {
      title: "Home",
      popularAnimes: [],
      popularCharacters: [],
      popularStaff: [],
      error: "Could not fetch data for carousels."
    });
  }
};