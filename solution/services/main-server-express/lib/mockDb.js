const MOCK_ANIME_HOME = [
  {
    anime_id: 1,
    title: "Attack on Titan",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    episodes: 25,
  },
  {
    anime_id: 2,
    title: "Death Note",
    image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
    score: 8.6,
    type: "TV",
    episodes: 37,
  },
  {
    anime_id: 3,
    title: "One Piece",
    image_url: "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
    score: 8.7,
    type: "TV",
    episodes: 1000,
  },
  {
    anime_id: 4,
    title: "Fullmetal Alchemist: Brotherhood",
    image_url: "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
    score: 9.1,
    type: "TV",
    episodes: 64,
  },
  {
    anime_id: 5,
    title: "Steins;Gate",
    image_url: "https://cdn.myanimelist.net/images/anime/5/73199.jpg",
    score: 9.0,
    type: "TV",
    episodes: 24,
  },
  {
    anime_id: 6,
    title: "Hunter x Hunter",
    image_url: "https://cdn.myanimelist.net/images/anime/11/33657.jpg",
    score: 9.0,
    type: "TV",
    episodes: 148,
  },
];

const MOCK_ANIME_LIST = [
  {
    anime_id: 1,
    title: "Attack on Titan",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    episodes: 25,
    popularity: 1,
  },
  {
    anime_id: 2,
    title: "Fullmetal Alchemist: Brotherhood",
    image_url: "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
    score: 9.1,
    type: "TV",
    episodes: 64,
    popularity: 2,
  },
];

const MOCK_ANIME_DETAIL = {
  1: {
    anime_id: 1,
    title: "Attack on Titan",
    title_japanese: "進撃の巨人",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    status: "Finished Airing",
    episodes: 25,
    start_date: "2013-04-07",
    end_date: "2013-09-29",
    synopsis: "Centuries ago, mankind was slaughtered to near extinction...",
    genres: ["Action", "Drama", "Fantasy", "Mystery"],
    themes: ["Gore", "Military", "Survival"],
    studios: ["Wit Studio"],
    demographics: ["Shounen"],
    rank: 1,
    popularity: 1,
    members: 3500000,
  },
  2: {
    anime_id: 2,
    title: "Fullmetal Alchemist: Brotherhood",
    image_url: "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
    score: 9.1,
    type: "TV",
    status: "Finished Airing",
    episodes: 64,
  },
};

const MOCK_CHARACTERS_LIST = [
  {
    character_id: 1,
    name: "Monkey D. Luffy",
    name_kanji: "モンキー・D・ルフィ",
    image_url: "https://cdn.myanimelist.net/images/characters/9/310307.jpg",
    favorites: 210000
  },
  {
    character_id: 2,
    name: "Levi Ackerman",
    name_kanji: "リヴァイ・アッカーマン",
    image_url: "https://cdn.myanimelist.net/images/characters/2/241413.jpg",
    favorites: 190000
  },
  {
    character_id: 3,
    name: "Naruto Uzumaki",
    name_kanji: "うずまき ナルト",
    image_url: "https://cdn.myanimelist.net/images/characters/10/284121.jpg",
    favorites: 230000
  },
  {
    character_id: 4,
    name: "Satoru Gojo",
    name_kanji: "五条 悟",
    image_url: "https://cdn.myanimelist.net/images/characters/15/422826.jpg",
    favorites: 175000
  },
  {
    character_id: 5,
    name: "Lelouch Lamperouge",
    name_kanji: "ルルーシュ・ランペルージ",
    image_url: "https://cdn.myanimelist.net/images/characters/8/406163.jpg",
    favorites: 160000
  }
];

const MOCK_CHARACTERS_DETAIL = {
  1: {
    character_id: 1,
    url: "https://myanimelist.net/character/1/Monkey_D_Luffy",
    name: "Monkey D. Luffy",
    name_kanji: "モンキー・D・ルフィ",
    image_url: "https://cdn.myanimelist.net/images/characters/9/310307.jpg",
    favorites: 210000,
    about: "Captain of the Straw Hat Pirates. His dream is to become the Pirate King. He possesses the power of the Gomu Gomu no Mi."
  },
  2: {
    character_id: 2,
    url: "https://myanimelist.net/character/40/Levi_Ackerman",
    name: "Levi Ackerman",
    name_kanji: "リヴァイ・アッカーマン",
    image_url: "https://cdn.myanimelist.net/images/characters/2/241413.jpg",
    favorites: 190000,
    about: "Captain of the Special Operations Squad. Known as humanity's strongest soldier and famous for his exceptional combat skills."
  },
  3: {
    character_id: 3,
    url: "https://myanimelist.net/character/17/Naruto_Uzumaki",
    name: "Naruto Uzumaki",
    name_kanji: "うずまき ナルト",
    image_url: "https://cdn.myanimelist.net/images/characters/10/284121.jpg",
    favorites: 230000,
    about: "A shinobi of Konohagakure who dreams of becoming Hokage. Host of the Nine-Tailed Fox."
  },
  4: {
    character_id: 4,
    url: "https://myanimelist.net/character/45627/Gojou_Satoru",
    name: "Satoru Gojo",
    name_kanji: "五条 悟",
    image_url: "https://cdn.myanimelist.net/images/characters/15/422826.jpg",
    favorites: 175000,
    about: "A jujutsu sorcerer and teacher at Tokyo Jujutsu High. Widely regarded as the strongest sorcerer alive."
  },
  5: {
    character_id: 5,
    url: "https://myanimelist.net/character/417/Lelouch_Lamperouge",
    name: "Lelouch Lamperouge",
    name_kanji: "ルルーシュ・ランペルージ",
    image_url: "https://cdn.myanimelist.net/images/characters/8/406163.jpg",
    favorites: 160000,
    about: "An exiled prince of the Holy Britannian Empire who gains the power of Geass and seeks to overthrow the empire."
  }
};

const MOCK_FAVOURITES = {
  // esempio: user identificato da username
  cristian: {
    username: "cristian",
    items: [{ anime_id: 1, added_at: "2026-01-10T10:00:00Z" }],
    total: 1,
  },
};

const MOCK_PROFILE = {
  cristian: {
    username: "cristian",
    display_name: "Cristian",
    email: "cristian@example.com",
    bio: "Anime enthusiast and developer",
    avatar_url: "/images/default-avatar.png",
    joined: "2024-01-15",
    watching: 15,
    completed: 42,
    plan_to_watch: 23
  },
  davide: {
    username: "davide",
    display_name: "Davide",
    email: "davide@example.com",
    bio: "Manga reader and anime lover",
    avatar_url: "/images/default-avatar.png",
    joined: "2024-02-20",
    watching: 8,
    completed: 67,
    plan_to_watch: 31
  }
};

const MOCK_STAFF = [
  { id: 1, name: 'Alice Tanaka', position: 'Director', email: 'alice@studio.jp', department: 'Production' },
  { id: 2, name: 'Marco Rossi', position: 'Animator', email: 'marco@studio.jp', department: 'Animation' }
];

module.exports = {
  MOCK_ANIME_HOME,
  MOCK_ANIME_LIST,
  MOCK_ANIME_DETAIL,
  MOCK_CHARACTERS_LIST,
  MOCK_CHARACTERS_DETAIL,
  MOCK_FAVOURITES,
  MOCK_PROFILE,
  MOCK_STAFF
};