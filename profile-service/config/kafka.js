const { Kafka } = require('kafkajs');

const kafka = new Kafka({
clientId: process.env.KAFKA_CLIENT_ID,
    brokers: [process.env.KAFKA_BROKER]
});

const producer = kafka.producer();
const consumerClassService = kafka.consumer({ groupId: "class-serivce" });

const produceMessage = async (topic, message) => {
    await producer.connect();
    await producer.send({
        topic,
        messages: [{ value: JSON.stringify(message) }],
    });
    console.log(`Message sent to ${topic}:`, message);
};

// Khởi động Consumer
const initConsumer = async () => {
    await consumerClassService.connect();
    console.log("Kafka Consumer is ready");

    // Đăng ký Consumer lắng nghe các sự kiện
    await consumerClassService.subscribe({ topics: ["class-service.groupstudent.findbygroup"] });

    await consumerClassService.run({
        eachMessage: async ({ topic, partition, message }) => {
            const data = JSON.parse(message.value.toString());
            console.log(`Received Kafka Event: ${topic} - ${JSON.stringify(data)}`);
        },
    });
};



module.exports = { produceMessage, initConsumer };
      