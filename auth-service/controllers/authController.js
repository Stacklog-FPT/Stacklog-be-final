const User = require("../models/User");
const jwt = require("jsonwebtoken");
const redisClient = require("../config/redis");
const { sendKafkaEvent } = require("../config/kafka");
const { decodedTokenGoogle } = require('../utils/helperMethod');

const generateToken = (user) => {
  return jwt.sign(
    { id: user._id, username: user.username, role: user.role },
    process.env.JWT_SECRET,
    { expiresIn: "1d" }
  );
};



// Đăng nhập người dùng
const login = async (req, res) => {
  const { email, password } = req.body;

  try {
    const user = await User.findOne({ email });
    if (!user || !(await user.matchPassword(password))) {

      return res.status(401).json({ message: "Invalid credentials" });
    }

    const token = generateToken(user);

    // Lưu token vào Redis với TTL 1 ngày
    await redisClient.setEx(`auth:session:${user._id}:web`, process.env.SESSION_EXPIRY, token);
    console.log(JSON.stringify(user));

    // Gửi event người dùng đăng nhập vào kafka
    await sendKafkaEvent("auth-service.user.loginned", { email: user.email, role: user.role, timestamp: Date.now() });

    return res.json({
      _id: user._id, username: user.username, email: user.email, role: user.role, token
    });

  } catch (error) {
    console.error("Login Error:", error);
    res.status(500).json({ message: "Server error", error });
  }
};

const logout = async (req, res) => {
  try {

    const token = req.headers["authorization"]?.split(" ")[1];
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const id = decoded.id;
    console.log(id)
    await redisClient.del(`auth:session:${id}:web`);

    sendKafkaEvent("UserLoggedOut", { email: decoded.email, timestamp: Date.now() });

    res.json({ message: "Logged out successfully" });
  } catch (error) {
    res.status(500).json({ message: "Server error", error });
  }
};

// validate token
const validate = async (req, res) => {
  console.log(req.headers["Authorization"]?.split(" ")[1]);
  const token = req.headers["Authorization"]?.split(" ")[1];
  console.log(token)
  if (!token) {
    return res.status(401).json({ message: "Unauthorized" });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const userRole = User[decoded.email]?.role || "guest";

    res.setHeader("X-User-Role", userRole);
    res.setHeader("Content-Length", "0"); // Đảm bảo không có body trả về
    return res.sendStatus(200);
  } catch (err) {
    console.log(err);
    return res.status(402).json({ message: "Invalid Token" });
  }
}

// Login Google
const loginGoogle = async (req, res) => {
  try {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).send({ errMsg: 'Missing idToken!' });

    const payload = await decodedTokenGoogle(idToken);

    if (!payload?.email) return res.status(400).json({ errMsg: 'Invalid Google Token!' });

    let user = await User.findOne({ email: payload.email });
    if (!user) {
      user = await User.create({
        name: payload.name,
        email: payload.email,
        avatar: payload.picture,
        role: 'STUDENT', // Ko biet nen e de tam
      });

      await sendKafkaEvent('auth-service.user.created', {
        email: user.email,
        name: user.name,
        avatar: user.avatar,
        role: user.role,
        createdAt: new Date().toISOString(),
      });

    }
    const token = generateToken(user);

    await redisClient.setEx(`auth:session:${user._id}:web`, process.env.SESSION_EXPIRY, token);

    console.log(JSON.stringify(user));

    await sendKafkaEvent('auth-service.user.loginned', {
      email: user.email,
      role: user.role,
      timestamp: Date.now(),
    });

    return res.json({
      _id: user._id, username: user.username, email: user.email, role: user.role, token
    });
  } catch (e) {
    return res.status(500).send({ errMsg: 'Something is wrong!' });
  }
};

module.exports = { logout, login, validate, loginGoogle };