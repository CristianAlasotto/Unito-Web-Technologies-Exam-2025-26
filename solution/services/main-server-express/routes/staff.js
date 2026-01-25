var express = require('express');
var { MOCK_STAFF } = require('../lib/mockDb');
var router = express.Router();

router.get('/', function(req, res) {
  res.render('staff/list', { title: 'Staff', staff: MOCK_STAFF, currentPage: 'staff' });
});

router.get('/:id', function(req, res, next) {
  const staff = MOCK_STAFF.find(s => s.id === Number(req.params.id));
  if (!staff) return next();
  res.render('staff/detail', { title: staff.name, staff, currentPage: 'staff' });
});

module.exports = router;