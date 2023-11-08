const Pool = require('pg').Pool
const request = require('request')
const pool = new Pool({
  user: 'admin',
  host: '...',
  port: 9170,
  database: 'pblDB',
  password: 'admin'
})

const testConnections = async(req, res) => {
var Connections = {
    Database: 'null',
    VilniusEventsAPI: 'null',
    MeteoAPI: 'null',
    OverpassAPI: 'null' 
}
pool.query('SELECT 1+1 AS solution', (error) =>{
    if(!error){
        Connections.Database = 'Connection with database was successful'
    }
    else{
        console.log(error)
        Connections.Database = 'Connection with database was unsuccessful'
    }
})

request('https://www.vilnius-events.lt/api/', (error, response) => {
    if(response.statusCode == 200){
        Connections.VilniusEventsAPI = 'Vilnius Events API is working'
    }
    else{
        Connections.VilniusEventsAPI = 'Vilnius Events API is down'
    }
})
request('https://api.meteo.lt/v1/places/vilnius/forecasts/long-term', (error, response) => {
    if(response.statusCode == 200){
        Connections.MeteoAPI = 'Meteo API is working'
    }
    else{
        Connections.MeteoAPI = 'Meteo API is down'
    }
})
request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22shop%22=%22outdoor%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', (error, response) => {
    if(response.statusCode == 200){
        Connections.OverpassAPI = 'Overpass API is working'
        res.send({Results: Connections})
    }
    else{
        Connections.OverpassAPI = 'Overpass API is down'
        res.send({Results: Connections})
    }
})

}

module.exports = {
    testConnections,
}