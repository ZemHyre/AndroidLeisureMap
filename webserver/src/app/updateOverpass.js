const request = require('request')
const geocoder = require('local-reverse-geocoder')
const pool = require('./database')


const updateCities = async (req, res) => {
  let data
  let parsedData
  data = await new Promise((resolve, reject) =>{
    request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22place%22=%22city%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
    resolve(data)
  })
  })
  parsedData = await JSON.parse(data)
    for(let i = 0; i < parsedData.elements.length - 1; i++){
        pool.query(`SELECT EXISTS(SELECT 1 FROM city WHERE id=${parsedData.elements[i].id})`, (error, response) => {
        if(response.rows[0].exists == false){
          pool.query(`INSERT INTO city (id, name, lat, lon, type) VALUES ($1, $2, $3, $4, 'city')`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].lat, parsedData.elements[i].lon], (error, results) => {
            if (error) {
              throw error
            }
          })
        }
      })
    }
    data = await new Promise((resolve, reject) => {
      request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22place%22=%22town%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
      resolve(data)
  })
    })
  parsedData = await JSON.parse(data)
      for(let i = 0; i < parsedData.elements.length - 1; i++){
        pool.query(`SELECT EXISTS(SELECT 1 FROM city WHERE id=${parsedData.elements[i].id})`, (error, response) => {
          if(response.rows[0].exists == false){
            pool.query(`INSERT INTO city (id, name, lat, lon, type) VALUES ($1, $2, $3, $4, 'town')`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].lat, parsedData.elements[i].lon], (error, results) => {
              if (error) {
                throw error
              }
            })
          }
        })
      }
  
    const STATUS = {
      UPDATE: `Overpass API updated successfully`
    }
    res.send({STATUS})
}

const updateLeisure = async (req, res) => {
  let data
  let parsedData
  data = await new Promise((resolve, reject) => {
    request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22tourism%22=%22museum%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
    resolve(data)
  })
  })
  parsedData = await JSON.parse(data)
    for(let i = 0; i < parsedData.elements.length - 1; i++){
    parsedData.elements[i].tags['addr:city'] = await new Promise((resolve, reject) => {
      var point = { latitude: parsedData.elements[i].lat, longitude: parsedData.elements[i].lon }
      geocoder.lookUp(point, (err, res) => {
        if((res[0][0].name == 'Naujamiestis') || (res[0][0].name == 'Fabijoniškės') || (res[0][0].name == 'Šeškinė') || (res[0][0].name == 'Karoliniškės') ||
        (res[0][0].name == 'Justiniškės') || (res[0][0].name == 'Lazdynai') || (res[0][0].name == 'Santariškės') || (res[0][0].name == 'Žirmūnai') || 
        (res[0][0].name == 'Antakalnis') || (res[0][0].name == 'Viršuliškės') || (res[0][0].name == 'Senamiestis') || (res[0][0].name == 'Pašilaičiai') || (res[0][0].name == 'Baltupiai') ||
        (res[0][0].name == 'Jaruzalė') || (res[0][0].name == 'Žvėrynas') || (res[0][0].name == 'Naujininkai') || (res[0][0].name == 'Užupis') || (res[0][0].name == 'Paupys') ||
        (res[0][0].name == 'Bajorai') || (res[0][0].name == 'Visoriai')){
          resolve('Vilnius')
        }
        if(res[0][0].name == 'Akademija (Kaunas)'){
          resolve('Akademija')
        }
        if(res[0][0].name == 'Dainava (Kaunas)'){
          resolve('Kaunas')
        }
        else{
          resolve(res[0][0].name)
        }
      })
    })
    pool.query(`SELECT EXISTS(SELECT 1 FROM place WHERE id=${parsedData.elements[i].id})`, (error, response) => {
      if(response.rows[0].exists == false){
        pool.query(`INSERT INTO place (id, name, opening_hours, phone, website, city, lat, lon, type) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].tags.opening_hours, parsedData.elements[i].tags.phone, parsedData.elements[i].tags.website, parsedData.elements[i].tags['addr:city'], parsedData.elements[i].lat, parsedData.elements[i].lon, parsedData.elements[i].tags.tourism], (error, results) => {
          if(error) {
            throw error
          }
        })
      }
    })
  }
  
  data = await new Promise((resolve, reject) => {
    request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22leisure%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
    resolve(data)
  })
  })
  parsedData = await JSON.parse(data)
    for(let i = 0; i < parsedData.elements.length - 1; i++){
    parsedData.elements[i].tags['addr:city'] = await new Promise((resolve, reject) => {
      var point = { latitude: parsedData.elements[i].lat, longitude: parsedData.elements[i].lon }
      geocoder.lookUp(point, (err, res) => {
        if((res[0][0].name == 'Naujamiestis') || (res[0][0].name == 'Fabijoniškės') || (res[0][0].name == 'Šeškinė') || (res[0][0].name == 'Karoliniškės') ||
        (res[0][0].name == 'Justiniškės') || (res[0][0].name == 'Lazdynai') || (res[0][0].name == 'Santariškės') || (res[0][0].name == 'Žirmūnai') || 
        (res[0][0].name == 'Antakalnis') || (res[0][0].name == 'Viršuliškės') || (res[0][0].name == 'Senamiestis') || (res[0][0].name == 'Pašilaičiai') || (res[0][0].name == 'Baltupiai') ||
        (res[0][0].name == 'Jaruzalė') || (res[0][0].name == 'Žvėrynas') || (res[0][0].name == 'Naujininkai') || (res[0][0].name == 'Užupis') || (res[0][0].name == 'Paupys') ||
        (res[0][0].name == 'Bajorai') || (res[0][0].name == 'Visoriai')){
          resolve('Vilnius')
        }
        if(res[0][0].name == 'Akademija (Kaunas)'){
          resolve('Akademija')
        }
        if(res[0][0].name == 'Dainava (Kaunas)'){
          resolve('Kaunas')
        }
        else{
          resolve(res[0][0].name)
        }
      })
    })
    pool.query(`SELECT EXISTS(SELECT 1 FROM place WHERE id=${parsedData.elements[i].id})`, (error, response) => {
      if(response.rows[0].exists == false){
        pool.query(`INSERT INTO place (id, name, opening_hours, phone, website, city, lat, lon, type) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].tags.opening_hours, parsedData.elements[i].tags.phone, parsedData.elements[i].tags.website, parsedData.elements[i].tags['addr:city'], parsedData.elements[i].lat, parsedData.elements[i].lon, parsedData.elements[i].tags.leisure], (error, results) => {
          if (error) {
            throw error
          }
        })
      }
    })
  }

res.redirect('/update/overpassapi/restaurants')
}

const updateRestaurant = async (req, res) => {
  let data
  let parsedData
  data = await new Promise((resolve, reject) => {
    request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22amenity%22=%22fast_food%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
    resolve(data)
  })
  })
  parsedData = await JSON.parse(data)
    for(let i = 0; i < parsedData.elements.length - 1; i++){
    parsedData.elements[i].tags['addr:city'] = await new Promise((resolve, reject) => {
      var point = { latitude: parsedData.elements[i].lat, longitude: parsedData.elements[i].lon }
      geocoder.lookUp(point, (err, res) => {
        if((res[0][0].name == 'Naujamiestis') || (res[0][0].name == 'Fabijoniškės') || (res[0][0].name == 'Šeškinė') || (res[0][0].name == 'Karoliniškės') ||
        (res[0][0].name == 'Justiniškės') || (res[0][0].name == 'Lazdynai') || (res[0][0].name == 'Santariškės') || (res[0][0].name == 'Žirmūnai') || 
        (res[0][0].name == 'Antakalnis') || (res[0][0].name == 'Viršuliškės') || (res[0][0].name == 'Senamiestis') || (res[0][0].name == 'Pašilaičiai') || (res[0][0].name == 'Baltupiai') ||
        (res[0][0].name == 'Jaruzalė') || (res[0][0].name == 'Žvėrynas') || (res[0][0].name == 'Naujininkai') || (res[0][0].name == 'Užupis') || (res[0][0].name == 'Paupys') ||
        (res[0][0].name == 'Bajorai') || (res[0][0].name == 'Visoriai')){
          resolve('Vilnius')
        }
        if(res[0][0].name == 'Akademija (Kaunas)'){
          resolve('Akademija')
        }
        if(res[0][0].name == 'Dainava (Kaunas)'){
          resolve('Kaunas')
        }
        else{
          resolve(res[0][0].name)
        }
      })
    })
    pool.query(`SELECT EXISTS(SELECT 1 FROM place WHERE id=${parsedData.elements[i].id})`, (error, response) => {
      if(response.rows[0].exists == false){
        pool.query(`INSERT INTO place (id, name, opening_hours, phone, website, city, lat, lon, type) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].tags.opening_hours, parsedData.elements[i].tags.phone, parsedData.elements[i].tags.website, parsedData.elements[i].tags['addr:city'], parsedData.elements[i].lat, parsedData.elements[i].lon, parsedData.elements[i].tags.amenity], (error, results) => {
          if (error) {
            throw error
          }
        })
      }
    })
  }
  
  data = await new Promise((resolve, reject) => {
    request('https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area(id:3600072596)-%3E.searchArea;(node[%22amenity%22=%22restaurant%22](area.searchArea););out%20body;%3E;out%20skel%20qt;', async (error, response, data) => {
    resolve(data)
  })
  })
  parsedData = await JSON.parse(data)
    for(let i = 0; i < parsedData.elements.length - 1; i++){
    parsedData.elements[i].tags['addr:city'] = await new Promise((resolve, reject) => {
      var point = { latitude: parsedData.elements[i].lat, longitude: parsedData.elements[i].lon }
      geocoder.lookUp(point, (err, res) => {
        if((res[0][0].name == 'Naujamiestis') || (res[0][0].name == 'Fabijoniškės') || (res[0][0].name == 'Šeškinė') || (res[0][0].name == 'Karoliniškės') ||
        (res[0][0].name == 'Justiniškės') || (res[0][0].name == 'Lazdynai') || (res[0][0].name == 'Santariškės') || (res[0][0].name == 'Žirmūnai') || 
        (res[0][0].name == 'Antakalnis') || (res[0][0].name == 'Viršuliškės') || (res[0][0].name == 'Senamiestis') || (res[0][0].name == 'Pašilaičiai') || (res[0][0].name == 'Baltupiai') ||
        (res[0][0].name == 'Jaruzalė') || (res[0][0].name == 'Žvėrynas') || (res[0][0].name == 'Naujininkai') || (res[0][0].name == 'Užupis') || (res[0][0].name == 'Paupys') ||
        (res[0][0].name == 'Bajorai') || (res[0][0].name == 'Visoriai')){
          resolve('Vilnius')
        }
        if(res[0][0].name == 'Akademija (Kaunas)'){
          resolve('Akademija')
        }
        if(res[0][0].name == 'Dainava (Kaunas)'){
          resolve('Kaunas')
        }
        else{
          resolve(res[0][0].name)
        }
      })
    })
    pool.query(`SELECT EXISTS(SELECT 1 FROM place WHERE id=${parsedData.elements[i].id})`, (error, response) => {
      if(response.rows[0].exists == false){
        pool.query(`INSERT INTO place (id, name, opening_hours, phone, website, city, lat, lon, type) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`, [parsedData.elements[i].id, parsedData.elements[i].tags.name, parsedData.elements[i].tags.opening_hours, parsedData.elements[i].tags.phone, parsedData.elements[i].tags.website, parsedData.elements[i].tags['addr:city'], parsedData.elements[i].lat, parsedData.elements[i].lon, parsedData.elements[i].tags.amenity], (error, results) => {

        })
      }
    })
  }

  const STATUS = {
    UPDATE: `Overpass API updated successfully`
  }
  res.send({STATUS})
}


module.exports = {
    updateCities,
    updateLeisure,
    updateRestaurant,
}