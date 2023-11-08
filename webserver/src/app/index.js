const db = require('./databaseQueries')
const api = require('./APIs')
const overpass = require('./updateOverpass')
const user = require('./user')
const recom = require('./recommendation')

module.exports = {
    db,
    api,
    overpass,
    user,
    recom
}