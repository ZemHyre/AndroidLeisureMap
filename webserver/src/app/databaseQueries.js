const pool = require('./database')
const math = require('exact-math')

const getTable = (req, res) => {
  const name = req.query.name
  pool.query(`SELECT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = '${name}')`, (error, results) => {
    if(error){
      throw error
    }
    if(results.rows[0].exists == false){
      res.json({ERROR: `Table ${name} does not exist`})
    }
    else{
      pool.query(`SELECT * FROM ${name}`, (error, results) => {
        if (error) {
          res.send(err)
        }
        else{
        res.send({Table: results.rows})
        } 
      })
    }
  })
}

const getView = (req, res) => {
  const name = req.query.name
  pool.query(`SELECT EXISTS (SELECT FROM pg_views WHERE schemaname = 'public' AND viewname = '${name}')`, (error, results) => {
    if(error){
      throw error
    }
    if(results.rows[0].exists == false){
      res.json({ERROR: `View ${name} does not exist`})
    }
    else{
      pool.query(`SELECT * FROM ${name}`, (error, results) => {
        if (error) {
          res.send(err)
        }
        else{
        res.send({Table: results.rows})
        } 
      })
    }
  })
}

const savePlace = (req, res) => {
  const data = req.query.username
  const username = data.toLowerCase()
  const id = req.query.id

  pool.query(`SELECT EXISTS (SELECT 1 as exists FROM favourite_places WHERE username='${username}' AND place_id=${id})`, (error, results) => {
    if(error){
      throw error
    }
    if(results.rows[0].exists == 1){
      const STATUS = 'Place is already saved!'
      res.json({STATUS})
    }
    else{
      pool.query(`INSERT INTO favourite_places (username, place_id) VALUES ($1, $2)`, [username, id], (error) => {
        if(error){
          throw error
        }
        const STATUS = 'Place saved successfully!'
        res.json({STATUS})
      })
    }
  })
}

const deletePlace = (req, res) => {
  const data = req.query.username
  const username = data.toLowerCase()
  const id = req.query.id

  pool.query(`SELECT 1 as exists FROM favourite_places WHERE username='${username}' AND place_id=${id}`, (error, results) => {
    if(error){
      throw error
    }
    if(results.rows[0].exists == 0){
      const STATUS = 'Place is not saved!'
      res.json({STATUS})
    }
    else{
      pool.query(`DELETE FROM favourite_places (username, place_id) VALUES ($1, $2)`, [username, id], (error) => {
        if(error){
          throw error
        }
        const STATUS = 'Place deleted successfully!'
        res.json({STATUS})
      })
    }
  })
}

const getFavPlace = (req, res) => {
  const data = req.query.username
  const username = data.toLowerCase()
  const favPlaces = []

  pool.query(`SELECT * FROM favourite_places WHERE username='${username}'`, async (error, results) => {
    if(error){
      throw error
    }
    await new Promise(async (resolve, reject) => {
      for(let i = 0; i < results.rows.length; i++) {
        await new Promise((resolve, reject) => {
          pool.query(`SELECT * FROM place WHERE id=${results.rows[i].place_id}`, (error, results2) => {
            if(error){
              throw error
            }
            favPlaces[i] = results2.rows[0]
            resolve()
          })
        })
      }
      resolve()
    })
    res.json({Places: favPlaces})
  })
}

const ratePlace = (req, res) => {
  const id = req.query.id
  const rating = req.query.rating

  pool.query(`SELECT * FROM place_rating WHERE place_id=${id}`, (error, results) => {
    if(error){
      throw error
    }

    const newRating = math.div(math.add(math.mul(results.rows[0].rating, results.rows[0].count), rating), results.rows[0].count + 1)

    pool.query(`UPDATE place_rating SET rating=${newRating}, count=${results.rows[0].count + 1} WHERE place_id=${id}`, (error) => {
      if(error){
        throw error
      }
      const STATUS = `Thank you fro your feedback!`
      res.json({STATUS})
    })
  })
  
}

const searchSession = async (req, res) => {
  const data = req.body.username
  const username = data.toLowerCase()
  const start = req.body.start
  const end = req.body.end
  const places = req.body.places
  const tags = req.body.tags

  await new Promise((resolve, reject) => {
    pool.query(`INSERT INTO sessions (username, start_time, end_time) VALUES ($1, $2, $3)`, [username, start, end], (error, results) => {
      if(error){
        throw error
      }
      resolve()
    })
  })
  pool.query(`SELECT * FROM sessions WHERE username='${username}' ORDER BY session_id ASC`, async (error, results) => {
    if(error){
      throw error
    }
    let lastId = results.rows[results.rows.length - 1].session_id
    let date = results.rows[results.rows.length - 1].date
    await new Promise(async (resolve, reject) => {
      for(let i = 0; i < places.length; i++){
        await new Promise((resolve, reject) => {
          pool.query(`INSERT INTO searched_places (session_id, place_id, date) VALUES ($1, $2, $3)`, [lastId, places[i], date], (error, results) => {
            if(error){
              throw error
            }
            resolve()
          })
        })
      }
      resolve()
    })
  })

  await new Promise(async (resolve, reject) => {
    for(let i = 0; i < tags.length; i++){
      await new Promise((resolve, reject) => {
        pool.query(`SELECT * FROM tags WHERE tag='${tags[i]}'`, (error, results) => {
          let tagid = results.rows[0].tag_id
          pool.query(`UPDATE searched_tags set count=count + 1 WHERE username='${username}' AND tag_id=${tagid}`, (error, results) => {
            if(error){
                throw error
            }
            resolve()
        })
        })
      })
    }
    resolve()
  })

  res.json({STATUS: 'Done'})
}

module.exports = {
  getTable,
  getView,
  savePlace,
  ratePlace,
  getFavPlace,
  deletePlace,
  searchSession
}
