FROM node:20-slim
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 2001
CMD ["node", "./bin/www"]
