process.env.NODE_ENV = 'test'

const db = require('../app/databaseQueries')
let chai = require('chai')
let chaiHttp = require('chai-http')
let expect = chai.expect
let server = require('../server')
const { json } = require('body-parser')
const { assert } = require('chai')
let should = chai.should()

chai.use(chaiHttp)

describe('Creating user that does not exist', () => {
  it('it should should create new user', (done) => {
    chai.request(server)
        .get('/user/create?username=gediminas&pass=lapinas')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-length']).to.not.have.string('39')
              done()
        })
  })
})

describe('Creating user that already exists', () => {
  it('it shouldnt create new account because user already exists', (done) => {
    chai.request(server)
        .get('/user/create?username=arthur&pass=vilkas')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-length']).to.have.string('39')
        })
  })
})

/*describe('/GET table place that exists', () => {
    it('it should GET all places from database', (done) => {
      chai.request(server)
          .get('/table?name=place')
          .end((err, res) => {
                res.should.have.status(200)
                expect(res.headers['content-type']).to.have.string('application/json')
            done()
          })
    })
})

describe('/GET table locations that does not exist', () => {
  it('it should GET all places from database', (done) => {
    chai.request(server)
        .get('/table?name=locations')
        .end((err, res) => {
          res.should.have.status(200)
        })
  })
})


describe('/GET cities', () => {
  it('it should GET all cities', (done) => {
    chai.request(server)
        .get('/table?name=city')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-type']).to.have.string('application/json')
          done()
        })
  })
})

/*describe('/GET weather', () => {
  it('it should GET weather forecast for Vilnius city', (done) => {
    chai.request(server)
        .get('/weather?city=vilnius')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-type']).to.have.string('application/json')
          done()
        })
  })
})

describe('/GET events', () => {
  it('it should GET Vilnius Events data', (done) => {
    chai.request(server)
        .get('/events')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-type']).to.have.string('application/json')
          done()
        })
  })
})

describe('/GET restaurants', () => {
  it('it should GET restaurant data from Overpass API', (done) => {
    chai.request(server)
        .get('/locations?key=amenity&value=restaurant')
        .end((err, res) => {
              res.should.have.status(200)
              expect(res.headers['content-type']).to.have.string('application/json')
          done()
        })
  })
})*/