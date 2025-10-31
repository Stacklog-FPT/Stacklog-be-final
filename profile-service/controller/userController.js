const User = require('../models/User');
const { produceMessage } = require('../config/kafka');

// Create a new user
exports.createUser = async (req, res) => {
    try {
        const user = await User.create(req.body);
        await produceMessage('user.created', user);
        res.status(201).json(user);
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
};

// Update an existing user
exports.updateUser = async (req, res) => {
    try {
        const user = await User.findByIdAndUpdate(req.params.id, req.body, { new: true });
        if (!user) return res.status(404).json({ error: 'User not found' });
        await produceMessage('user.updated', user);
        res.json(user);
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
};

// Delete user (soft delete)
exports.deleteUser = async (req, res) => {
    try {
        const user = await User.findByIdAndUpdate(req.params.id, { isDeleted: true }, { new: true });
        if (!user) return res.status(404).json({ error: 'User not found' });
        await produceMessage('user.deleted', user);
        res.json({ message: 'User deleted successfully' });
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
};

// find user
exports.findByEmail = async (req, res) => {
    try {
        const user = await User.findByEmail(req.params.email);
        if (!user) return res.status(404).json({ error: 'User not found' });
        res.status(200).json({ user });
    } catch (error) {
        res.status(400).json({ error: err.message });
    }
}

exports.findByRole = async (req, res) => {
    const { role } = req.params;
    try {
        const users = await User.find({ role: role.toUpperCase(), isDeleted: false });
        if (users.length === 0) return res.status(404).json({ error: `No ${role} found` });
        res.status(200).json({ users });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
};


exports.findByGroupId = async (req, res) => {
    const { groupId } = req.params;
    try {
        const users = await User.find({ groupId: groupId, isDeleted: false });
        if (users.length === 0) return res.status(404).json({ error: `No ${role} found` });
        res.status(200).json({ users });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
}

exports.findById = async (req, res) => {
    const { userId } = req.params;
    try {
        const user = await User.findOne({ _id: userId, isDeleted: false });
        if (user == null) return res.status(404).json({ error: `No ${userId} found` });
        res.status(200).json({ user });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
}
