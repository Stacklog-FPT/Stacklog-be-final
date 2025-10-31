require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require("cors");
const morgan = require("morgan");
const userRoutes = require('./routes/userRoutes');
const { initConsumer } = require('./config/kafka');

const app = express();
app.use(express.json());
app.use(cors({
    origin: [
        'http://localhost:5173',
        'https://stacklog.io.vn',
        'https://www.stacklog.io.vn',
        'https://*.vercel.app'
    ],
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true
}));
app.use(morgan("dev"));

(async () => {
    await initConsumer();
})();

mongoose.connect(process.env.MONGO_URI)
    .then(() => console.log('MongoDB Connected'))
    .catch(err => console.error('MongoDB Connection Error:', err));

app.use('/user', userRoutes);

// const PORT = process.env.PORT || 5000;
// app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

module.exports = app;
