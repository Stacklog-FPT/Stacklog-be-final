const redis = require("redis");

const redisClient = redis.createClient({
    socket: {
        host: process.env.REDIS_HOST || "127.0.0.1",
        port: process.env.REDIS_PORT || 6379
    }
});

redisClient.on("error", (err) => {
    console.error("❌ Redis Error:", err);
});

redisClient.connect().then(() => {
    console.log("✅ Redis connected successfully");
});

module.exports = redisClient;