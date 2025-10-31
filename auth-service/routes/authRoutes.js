const express = require('express');
const { logout, login, validate, addOne, loginGoogle } = require('../controllers/authController');
const protect = require('../middleware/authMiddleware');

const router = express.Router();

router.post('/login', login);
router.post('/logout', protect, logout);
router.post('/login-google', loginGoogle);
router.get('/validate', validate);
module.exports = router;
