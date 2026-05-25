/**
 * Shared controller helpers for rendering paginated views.
 */

/**
 * Converts empty values to a display fallback.
 *
 * @param {unknown} value Raw value to display.
 * @returns {unknown|string} Original value or 'N/A' for empty values.
 */
const formatValue = (value) =>
  value === null || value === undefined || value === '' ? 'N/A' : value;

/**
 * Marks option objects as selected based on a target value.
 *
 * @param {Array<{value: string, label: string}>} options Select option list.
 * @param {string} selectedValue Current selected value.
 * @returns {Array<{value: string, label: string, selected: boolean}>} Options with selection state.
 */
const withSelectedOptions = (options, selectedValue) =>
  options.map((option) => ({
    ...option,
    selected: option.value === selectedValue
  }));

/**
 * Builds standard pagination metadata for templates and JSON responses.
 *
 * @param {number} currentPage Current one-based page number.
 * @param {number} totalPages Total available pages.
 * @returns {{currentPage: number, totalPages: number, hasPrev: boolean, prevPage: number, hasNext: boolean, nextPage: number}} Pagination model.
 */
const buildPagination = (currentPage, totalPages) => ({
  currentPage,
  totalPages,
  hasPrev: currentPage > 1,
  prevPage: currentPage - 1,
  hasNext: currentPage < totalPages,
  nextPage: currentPage + 1
});

/**
 * Builds a query suffix preserving active filters for pagination links.
 *
 * @param {Record<string, unknown>} query Incoming request query object.
 * @param {Object} [config] Query transformation options.
 * @returns {string} Query string prefixed with '&', or an empty string.
 */
const buildFiltersQuery = (query, config = {}) => {
  const excluded = new Set(['page', ...(config.exclude || [])]);
  const aliases = config.aliases || {};
  const paginationQuery = new URLSearchParams();

  Object.entries(query).forEach(([key, value]) => {
    if (!value || excluded.has(key)) {
      return;
    }

    const alias = aliases[key];
    if (alias) {
      if (!query[alias]) {
        paginationQuery.set(alias, value);
      }
      return;
    }

    paginationQuery.set(key, value);
  });

  const queryString = paginationQuery.toString();
  return queryString ? `&${queryString}` : '';
};

/**
 * Normalizes scalar, array, and serialized-array values into a clean list.
 *
 * @param {unknown} value Raw value from an API payload.
 * @returns {Array<string|unknown>} Normalized non-empty list.
 */
const normalizeList = (value) => {
  if (Array.isArray(value)) {
    return value.filter((item) => item !== null && item !== undefined && item !== '');
  }
  if (value === null || value === undefined) {
    return [];
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed === '' || trimmed === '[]') {
      return [];
    }
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      const normalized = trimmed.replace(/'/g, '"');
      try {
        const parsed = JSON.parse(normalized);
        if (Array.isArray(parsed)) {
          return parsed.filter((item) => item !== null && item !== undefined && item !== '');
        }
      } catch (err) {
        // Fallback to splitting below.
      }
      return trimmed
        .slice(1, -1)
        .split(',')
        .map((item) => item.trim().replace(/^["']|["']$/g, ''))
        .filter((item) => item !== '');
    }
    return [trimmed];
  }
  return [String(value)];
};

/**
 * Extracts a list from common API response shapes.
 *
 * @param {unknown} data API response body.
 * @returns {Array<unknown>} Items array or an empty array.
 */
const extractItems = (data) => {
  if (Array.isArray(data)) {
    return data;
  }
  if (data && Array.isArray(data.items)) {
    return data.items;
  }
  if (data && Array.isArray(data.data)) {
    return data.data;
  }
  return [];
};

/**
 * Extracts a list from a Promise.allSettled result.
 *
 * @param {Object} result Settled promise result.
 * @returns {Array<unknown>} Items array or an empty array.
 */
const extractSettledItems = (result) => {
  if (result.status === 'rejected') {
    return [];
  }
  return extractItems(result.value.data);
};

module.exports = {
  buildFiltersQuery,
  buildPagination,
  extractItems,
  extractSettledItems,
  formatValue,
  normalizeList,
  withSelectedOptions
};
