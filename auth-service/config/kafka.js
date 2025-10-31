const { Kafka } = require("kafkajs");
const User = require("../models/User");
require("dotenv").config();

const KAFKA_BROKER = process.env.KAFKA_BROKER || "localhost:9092";
const CLIENT_ID = process.env.KAFKA_CLIENT_ID || "auth-service";
const GROUP_ID = process.env.KAFKA_GROUP_ID || "auth-group";

// Khởi tạo Kafka Client
const kafka = new Kafka({
    clientId: CLIENT_ID,
    brokers: [KAFKA_BROKER],
});

// Khởi tạo Producer
const producer = kafka.producer();
const consumer = kafka.consumer({ groupId: GROUP_ID });

// Khởi động Producer
const initProducer = async () => {
    await producer.connect();
    console.log("Kafka Producer is ready");
};

// Khởi động Consumer
const initConsumer = async () => {
    await consumer.connect();
    console.log("Kafka Consumer is ready");

    // Đăng ký Consumer lắng nghe các sự kiện
    await consumer.subscribe({ topics: ["user.created", "user.updated", "user.deleted"] });

    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            const data = JSON.parse(message.value.toString());
            console.log(`Received Kafka Event: ${topic} - ${JSON.stringify(data)}`);

            switch (topic) {
                case "user.created":
                    userData = {
                        _id: data._id,
                        username: data.work_id,
                        email: data.email,
                        password: "abcd1234",
                        role: data.role
                    }
                    // Chờ add hoàn tất
                    try {
                        const user = new User(userData);
                        await user.save();
                        console.log("User added successfully");
                    } catch (error) {
                        console.log(error.message);
                    }
                    break;
                case "user.updated":
                    console.log(`User Updated: ${data.email}`);
                    break;
                case "user.deleted":
                    console.log(`User Deleted: ${data.email}`);
                    break;
                default:
                    console.log(`Unknown Kafka Event: ${topic}`);
            }
        },
    });
};

// Gửi sự kiện Kafka
const sendKafkaEvent = async (topic, message) => {
    try {
        await producer.send({
            topic,
            messages: [{ value: JSON.stringify(message) }],
        });
        console.log(`Kafka Event Sent: ${topic} - ${JSON.stringify(message)}`);
    } catch (err) {
        console.error("Kafka Send Error:", err);
    }
};


module.exports = { initProducer, initConsumer, sendKafkaEvent };
