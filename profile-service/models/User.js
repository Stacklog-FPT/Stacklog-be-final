const mongoose = require('mongoose');
const { v4: uuidv4 } = require('uuid');

const UserSchema = new mongoose.Schema({
    user_id: {
        type: String,
        default: () => uuidv4(), // Tự sinh khi tạo mới
        unique: true
    },
    full_name: { type: String, required: true },
    work_id: { type: String, required: true, unique: true },
    avatar_link: { type: String },
    email: { type: String, required: true },
    description: { type: String },
    last_login: { type: Date, default: Date.now },
    isActive: { type: Boolean, default: true },
    isDeleted: { type: Boolean, default: false },
    personal_score: { type: Number, default: 0 },
    role: {type: String, required: true},
    groupId: {type: String}
}, { timestamps: true });

UserSchema.statics.findByEmail = function (email) {
    return this.findOne({ email, isDeleted: false });
};

UserSchema.statics.findByGroupId = function (groupId) {
    return this.find({ groupId, isDeleted: false });
}

module.exports = mongoose.model('User', UserSchema);
