const { apiPostgres } = require('./apiClients');

// Lista di tutto lo staff
exports.list = async (req, res, next) => {
	try {
		const response = await apiPostgres.get('/api/staff');
		const staff = response.data;

		res.render('staff/staff_list', {
			title: 'Staff',
			staff: staff,
			currentPage: 'staff',
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
		const response = await apiPostgres.get(`/api/staff/${id}`);
		res.render('staff/detail', {
			title: response.data.name,
			staffMember: response.data,
			currentPage: 'staff'
		});
	} catch (err) {
		next(err);
	}
};
