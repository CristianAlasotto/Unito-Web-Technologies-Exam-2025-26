var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var indexRouter = require('./routes/index');
var animeRouter = require('./routes/anime');
var usersRouter = require('./routes/users');
var charactersRouter = require('./routes/characters');
var staffRouter = require('./routes/staff');
var profileRouter = require('./routes/profile');
var favouritesRouter = require('./routes/favourites');

var app = express();

require('dotenv').config();
const mockDataStatus = process.env.USE_MOCK_DATA === 'true';
app.MockDataStatus = mockDataStatus;

// view engine setup just for server start
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');
app.set('view options', { layout: 'layout/main' });

// Register custom handlebars helpers
const hbs = require('hbs');
hbs.registerPartials(path.join(__dirname, 'views/partials'));
hbs.registerHelper('json', function(obj) {
  return JSON.stringify(obj, null, 2);
});


hbs.registerHelper('any', function() {
  const args = Array.prototype.slice.call(arguments);
  const options = args.pop();
  for (let i = 0; i < args.length; i++) {
    if (args[i]) {
      return options.fn(this);
    }
  }
  return options.inverse(this);
});

hbs.registerHelper('isHttpUrl', function(value) {
  if (typeof value !== 'string') {
    return false;
  }
  const trimmed = value.trim();
  return /^https?:\/\//i.test(trimmed);
});

// middleware -> pipeline richieste
app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/anime', animeRouter);
app.use('/users', usersRouter);
app.use('/characters', charactersRouter);
app.use('/staff', staffRouter);
app.use('/profile', profileRouter);
app.use('/favourites', favouritesRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
