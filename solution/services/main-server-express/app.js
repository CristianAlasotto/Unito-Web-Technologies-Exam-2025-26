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
var generalTestsRouter = require('./routes/general-tests');

var app = express();

// view engine setup just for server start
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');
app.set('view options', { layout: 'layout/main' });

// Register custom handlebars helpers
const hbs = require('hbs');
hbs.registerHelper('json', function(obj) {
  return JSON.stringify(obj, null, 2);
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
app.use('/general-tests', generalTestsRouter);

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
