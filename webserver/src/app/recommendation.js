const pcc = require("calculate-correlation")
const pool = require('./database')


const getRecommendation = (req, res) => {
    const data = req.query.username
    const username = data.toLowerCase()
    let userArray = []
    let similarUserArray = []
    let mostSimilar = {
        user: '',
        coefficient: null
    }
    let places = []
    let arrlength = -1
    let max = -2
    pool.query(`SELECT * FROM getTags WHERE username='${username}'`, (error, results) => {
        if(error){
            throw error
        }
        for(let i = 0; i <= 44; i++){
            userArray[i] = results.rows[0][`${i + 1}`]
        }
        pool.query(`SELECT * FROM getTags WHERE username NOT IN ('${username}')`, async (error, results2) => {
            if(error){
                throw error
            }
            for(let i = 0; i < results2.rows.length; i++){
                for(let y = 0; y <= 44; y++){
                    similarUserArray[y] = results2.rows[i][`${y + 1}`]
                }
                let correlation = pcc(userArray, similarUserArray)
                    if(correlation > max){
                        max = correlation
                        mostSimilar.user = results2.rows[i].username
                        mostSimilar.coefficient = correlation
                    }
            }
            pool.query(`SELECT * FROM week_sessions WHERE username='${mostSimilar.user}'`, async (error, results) => {
            await new Promise(async(resolve, reject) => {
                for(let i = 0; i < results.rows.length; i++){
                    await new Promise((resolve, reject) => {
                        pool.query(`SELECT * FROM searched_places WHERE session_id='${results.rows[i].session_id}'`, (error, results2) => {
                            for(let y = 0; y < results2.rows.length; y++){
                                places[arrlength + 1] = results2.rows[y].place_id
                                arrlength++
                            }
                            resolve()
                        })
                    })
                }
                resolve()
            })
                

            await new Promise(async (resolve, reject) => {
                for(let i = 0; i <= arrlength; i++){
                    await new Promise((resolve, reject) => {
                        pool.query(`SELECT * FROM place WHERE id='${places[i]}'`, (error, res) => {
                            places[i] = res.rows[0]
                            resolve()
                        })
                        resolve
                    })
            }
            resolve()
            })
            
            
            res.json({mostSimilar, Places: places}) 
            
            })
        })
    })
}

module.exports = {
    getRecommendation
}