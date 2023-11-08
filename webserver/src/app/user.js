const pool = require('./database')
const bcrypt = require('bcryptjs')

const login = (req, res) => {
    const data = req.query.username
    const username = data.toLowerCase()
    const pass = req.query.pass

    pool.query(`SELECT EXISTS(SELECT 1 FROM users WHERE username='${username}')`, (error, results) => {
        if(results.rows[0].exists == true){
            pool.query(`SELECT password FROM users WHERE username='${username}'`, (error, results) => {
            if(bcrypt.compareSync(pass, results.rows[0].password) == true){
                const STATUS = {
                    LOGIN: true,
                    message: 'You logged in successfully!'
                }
                res.send({STATUS})
            }
             else {
                const STATUS = {
                LOGIN: false,
                message: 'Password is incorrect.'
             }
                res.send({STATUS})
        }
    })
        }
        else{
            const STATUS = {
                LOGIN: false,
                message: 'User does not exist'
            }
            res.send({STATUS})
        }
    })
}

const createUser = (req, res) => {
  const data = req.query.username
  const username = data.toLowerCase()
    const pass = req.query.pass


  pool.query(`SELECT EXISTS(SELECT 1 FROM users WHERE username='${username}')`, (error, results) => {
        if(results.rows[0].exists == false){
        pool.query('INSERT INTO users (username, password) VALUES ($1, $2)', [username, pass], async(error, results) => {
            if (error) {
              res.send(error)
            }
            else{
              await new Promise(async (resolve, reject) => {
                for(let i = 1; i <= 45; i++){
                  await new Promise((resolve, reject) => {
                    pool.query(`INSERT INTO searched_tags (tag_id, username, count) VALUES (${i}, '${username}', 0)`, (error, results) => {
                      if(error){
                        throw error
                      }
                      resolve()
                    })
                  })
                }
                resolve()
              })
              const STATUS = {
                SIGNUP: true,
                message: 'Account successfully created'
            }
              res.json({STATUS})
            }
          })
        }
        else{
          const STATUS = {
            SIGNUP: false,
            message: 'Account with that username already exists'
        }
          res.json({STATUS})
        }})

  }
  
const updateUser = (req, res) => {
    const data = req.query.username
    const username = data.toLowerCase()
    const oldpass = req.query.oldpass
    const newpass = req.query.newpass

    pool.query(`SELECT password FROM users WHERE username = '${username}'`, (error, results) => {
      if(error){
        throw error
      }
      if(bcrypt.compareSync(oldpass, results.rows[0].password) == true){
        pool.query(
          'UPDATE users SET password = $1 WHERE username = $2', [newpass, username], (error, results) => {
            if (error) {
              res.send(error)
            }
            else{
              const STATUS = {
                UPDATE: true,
                message: 'Password updated successfully!'
            }
              res.json({STATUS})
            }
          }
        )
      }
      else{
        const STATUS = {
          UPDATE: false,
          message: 'Old password is incorrect!'
      }
      res.send({STATUS})
      }
    })
  
}
  
module.exports = {
    login,
    createUser,
    updateUser,
}