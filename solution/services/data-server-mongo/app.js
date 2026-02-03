var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var cors = require('cors');

const swaggerJSDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');

var connectDB = require('./database');
var apiRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var app = express();

connectDB();

const swaggerOptions = {
  definition: {
    openapi: '3.0.0', // Specification (OAS) version
    info: {
      title: 'MongoDB Anime API',
      version: '1.0.0',
      description: 'API for accessing dynamic anime data (reviews, ratings) stored in MongoDB',
      contact: {
        name: 'Alasotto Collura Correndo', // Add your group name here
      },
    },
    servers: [
      {
        url: 'http://localhost:3001',
        description: 'Local MongoDB Server',
      },
    ],
  },
  apis: ['./routes/*.js'],
};

// Initialize swagger-jsdoc
const swaggerSpec = swaggerJSDoc(swaggerOptions);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(cors());
app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.use('/api', apiRouter);
app.use('/users', usersRouter);

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