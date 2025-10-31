exports.validateUser = (req, res, next) => {
    const { username, password, full_name, email } = req.body;

    if (!username || !password || !full_name || !email) {
        return res.status(400).json({ error: 'Missing required fields: username, password, full_name, email' });
    }

    next();
};
