const jwt = require("jsonwebtoken");
const redisClient = require("../config/redis");

const protect = async (req, res, next) => {
    let token = req.headers.authorization;

    if (!token || !token.startsWith("Bearer ")) {
        return res.status(401).json({ message: "Unauthorized" });
    }

    try {
        token = token.split(" ")[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // Kiểm tra token trong Redis
        const sessionToken = await redisClient.get(`auth:session:${decoded.id}:web`);
        if (!sessionToken || sessionToken !== token) {
            return res.status(401).json({ message: "Session expired or invalid" });
        }

        req.user = decoded;
        next();
    } catch (error) {
        return res.status(401).json({ message: "Invalid token" });
    }
};

module.exports = protect;