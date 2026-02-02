const { apiPostgres } = require('./apiClients');

// Lista di tutto lo staff
exports.list = async (req, res, next) => {
	try {
		const page = req.query.page || 1;
		const pageSize = req.query.pageSize || 45;
		const response = await apiPostgres.get(`/api/person_details?page=${page}&pageSize=${pageSize}`);
		const staff = response.data.items;
		const totalPages = response.data.totalPages;

		res.render('staff/staff_list', {
			title: 'Staff',
			staff: staff,
			pagination: {
				currentPage: page,
				totalPages: totalPages,
				hasPrev: page > 1,
				prevPage: page - 1,
				hasNext: page < totalPages,
				nextPage: parseInt(page) + 1
			},
			warning: !staff || staff.length === 0 ? 'Nessuno staff trovato nel database.' : null
		});
	} catch (err) {
		res.render('staff/staff_list', {
			title: 'Staff',
			staff: [],
			currentPage: 'staff',
			error: 'Impossibile caricare i dati dello staff. Il server potrebbe non essere disponibile.'
		});
	}
};

// Dettaglio di uno staff
exports.detail = async (req, res, next) => {
	try {
		const { id } = req.params;
		const response = await apiPostgres.get(`/api/person_details/${id}`);
		res.render('staff/detail', {
			title: response.data.name,
			staff: response.data,
			currentPage: 'staff'
		});
	} catch (err) {
		next(err);
	}
};
