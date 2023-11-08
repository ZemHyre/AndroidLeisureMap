const request = require('request')
const pool = require('../app/database')
const testConnections = async(req, res) => {
var isConnectionSuccessful = {
    Database: false,
    VilniusEventsAPI: false,
    MeteoAPI: false,
    OverpassAPI: false 
}
pool.query('SELECT 1+1 AS solution', (error) =>{
    if(!error){
        isConnectionSuccessful.Database = true
    }
})

request('https://www.vilnius-events.lt/api/', (error, response) => {
    if(response.statusCode == 200){
        isConnectionSuccessful.VilniusEventsAPI = true
    }
})
request('https://api.meteo.lt/v1/places/vilnius/forecasts/long-term', (error, response) => {
    if(response.statusCode == 200){
        isConnectionSuccessful.MeteoAPI = true
    }
})
request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22shop%22=%22outdoor%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', (error, response) => {
    if(response.statusCode == 200){
        isConnectionSuccessful.OverpassAPI = true
        res.send({isConnectionSuccessful: isConnectionSuccessful})
    }
})

}

module.exports = {
    testConnections,
}