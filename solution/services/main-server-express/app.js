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

var app = express();

const swaggerJSDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'API del progetto Anime',
      version: '1.0.0',
      description: 'Documentazione delle API del progetto',
    },
    servers: [{ url: 'http://localhost:3000' }],
  },

  apis: ['./routes/*.js'],
};

const swaggerSpec = swaggerJSDoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

require('dotenv').config();

// view engine setup just for server start
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');
app.set('view options', { layout: 'layout/main' });

// Register custom handlebars helpers
const hbs = require('hbs');
hbs.registerPartials(path.join(__dirname, 'views/partials'));

/**
 * Serializes an object for display inside Handlebars templates.
 *
 * @param {unknown} obj Value to serialize.
 * @returns {string} Pretty-printed JSON string.
 */
hbs.registerHelper('json', function(obj) {
  return JSON.stringify(obj, null, 2);
});


/**
 * Renders the truthy branch when at least one argument is truthy.
 *
 * @param {...unknown} args Values followed by the Handlebars options object.
 * @returns {string} Rendered Handlebars block output.
 */
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

/**
 * Checks whether a Handlebars value is an absolute HTTP or HTTPS URL.
 *
 * @param {unknown} value Value to validate.
 * @returns {boolean} True when the value is an HTTP(S) URL.
 */
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
app.use('/jsdoc', express.static(path.join(__dirname, 'docs', 'jsdoc')));
app.use('/vendor/axios', express.static(path.join(__dirname, 'node_modules', 'axios', 'dist')));

app.use('/', indexRouter);
app.use('/anime', animeRouter);
app.use('/users', usersRouter);
app.use('/characters', charactersRouter);
app.use('/staff', staffRouter);
app.use('/profile', profileRouter);

// catch 404 and forward to error handler
/**
 * Converts unmatched requests into HTTP 404 errors.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware.
 * @returns {void}
 */
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
/**
 * Renders the generic error page for application errors.
 *
 * @param {Error} err Error passed by previous middleware.
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware.
 * @returns {void}
 */
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
