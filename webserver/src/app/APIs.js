const request = require('request')
const overpass = require('./overpass/export')
const LRU = require('lru-cache')
const pool = require('./database')

const options = {

max: 368,
maxSize: 5000,
sizeCalculation: (value, key) => {
  return 1
},

ttl: 1000 * 60 * 60,

allowState: false,
updateAgeOnGet: false,
updateAgeOnHas: false,

fetchMethod: async (key, staleValue, {options, signal}) => {}

}

const cache = new LRU(options)

const getWeather = async (req, res) => {
  const city = req.query.city
  let api
  let id
  let parsedId
  const parsedCity = city.charAt(0).toUpperCase() + city.slice(1)
  pool.query(`SELECT EXISTS(SELECT 1 FROM city WHERE name='${parsedCity}')`, async (err, response) => {
    if(response.rows[0].exists == true){
      id = await pool.query(`SELECT id FROM city WHERE name='${parsedCity}'`)
      parsedId = parseInt(id.rows[0].id)
      if(cache.has(parsedId) == true){
        res.json(cache.get(parsedId))
      }
      else{
        const url = 'https://api.meteo.lt/v1/places/' + city + '/forecasts/long-term'
        const encodedURL = encodeURI(url)
        request(encodedURL, (error, response, data) => {
        if (!error && response.statusCode == 200) {
          api = JSON.parse(data)
          cache.set(parsedId, api)
          res.send(api)
        }
        else{
              const error = {
                  code: "404",
                  message: "Not Found",
                  
              }
              res.status(404).json({error: error})
          }
        })
      }
    }
    else{
      const error = {
        code: "404",
        message: "City does not exist",
        
    }
    res.status(404).json({error: error})
    }
  })
}

const getVilniusEvents = async (req, res) => {
  let api
  if(cache.has(1) == true){
    res.json({Events: cache.get(1)})
  }
  else{
    const url = 'https://www.vilnius-events.lt/api/'
    const encodedURL = encodeURI(url)
    request(encodedURL, (error, response, data) => {
    if (!error && response.statusCode == 200) {
      api = JSON.parse(data)
      cache.set(1, api)
      res.json({Events: api})
    }
    else{
    res.status(404).json({error: 'error'})
    }
    })
  }
}

const getLocations = (req, res) => {
	const key = req.query.key
	const value = req.query.value
  async function main() {
	const query = new overpass.OverpassQuery()
		.setFormat('json')
		.setTimeout(30)
		.addElement1({
			type: 'node',
			tags: [{
				key: key,
				value: value,
				not: false,
			}],
		})
		.addElement2({
			type: 'way',
			tags: [{
				key: key,
				value: value,
				not: false,
			}],
		})

	  const response = await query.fetch();
    res.send(response)
}
main();
}

module.exports = {
    getWeather,
    getVilniusEvents,
    getLocations,
  }
