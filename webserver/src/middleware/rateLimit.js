const rateLimit = require('express-rate-limit')
const STATUS = {
	ERROR: "Too many requests!"
}

const limiter = rateLimit({
	windowMs: 60 * 10 * 1000,
	max: 3,
	message: {STATUS},
	standardHeaders: true,
	legacyHeaders: false,
})

const limiter2 = rateLimit({
	windowMs: 60 * 60 * 1000,
	max: 1,
	message: {STATUS},
	standardHeaders: true,
	legacyHeaders: false,
})

module.exports = {
    limiter,
	limiter2
}