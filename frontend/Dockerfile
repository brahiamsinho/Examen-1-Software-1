# ---- Build stage ----
FROM node:20-alpine AS builder

WORKDIR /app

# Cache npm install layer
COPY package.json package-lock.json* ./
RUN npm install --silent

COPY . .
RUN npm run build:prod

# ---- Runtime stage ----
FROM nginx:1.25-alpine

# Remove default nginx page
RUN rm -rf /usr/share/nginx/html/*

# Copy Angular build output
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copy nginx SPA config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
