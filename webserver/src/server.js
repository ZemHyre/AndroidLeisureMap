const express = require('express')
const bodyParser = require('body-parser')
const app = express()
const application = require('./app')
const test = require('./test')
const middleware = require('./middleware')
const port = 3000

app.use(bodyParser.json())
app.use(
  bodyParser.urlencoded({
    extended: true,
  })
)

//Authentication
app.get('/user/login', middleware.limiter.limiter, application.user.login)
app.get('/user/create', middleware.limiter.limiter2, application.user.createUser)
app.get('/user/update', middleware.limiter.limiter2, application.user.updateUser)

//Update Overpass API
//TODO 
// implement functions in a different way because Overpass sometimes sends XML data instead of JSON
// merge functions into one
app.get('/update/overpassapi/cities', application.overpass.updateCities)
app.get('/update/overpassapi/leisure', application.overpass.updateLeisure)
app.get('/update/overpassapi/restaurants', application.overpass.updateRestaurant)

//Testing
app.get('/test/connections', test.connections.testConnections)

//Get any location from Overpass API
app.get('/locations', application.api.getLocations)

//Vilnius events API
app.get('/events', application.api.getVilniusEvents)

//Meteo API to get weather forecast
app.get('/weather', application.api.getWeather)

//Database management
app.get('/table', application.db.getTable)
app.get('/view', application.db.getView)
app.get('/saveplace', application.db.savePlace)
app.get('/getfavourite', application.db.getFavPlace)
app.get('/deleteplace', application.db.deletePlace)
app.get('/rateplace', application.db.ratePlace)
app.post('/session', application.db.searchSession)

//Recommendation
app.get('/recommendation', application.recom.getRecommendation)

//Does not exist
app.get('*', (req, res) => {
  res.json({ERROR: `Route ${req.path} does not exist!`})
})

app.listen(port)