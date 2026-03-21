# ============================================================
# ShopZone Frontend — Multi-Stage Dockerfile
# ============================================================

# ==================== STAGE 1: BUILD ====================
# Uses Node.js to install dependencies and build static files
FROM node:22-alpine AS build

WORKDIR /app

# Copy package files first (for dependency caching)
COPY package.json package-lock.json ./

# Install dependencies (cached unless package.json changes)
RUN npm ci

# Copy source code
COPY . .

# Set the API URL for production build
# In Docker, Nginx proxies /api/ to the backend, so we use relative URL
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_URL}

# Pass Stripe public key at build time
ARG VITE_STRIPE_PUBLIC_KEY
ENV VITE_STRIPE_PUBLIC_KEY=${VITE_STRIPE_PUBLIC_KEY}

# Build the production bundle
RUN npm run build

# ==================== STAGE 2: RUNTIME ====================
# Uses Nginx to serve the built static files — tiny ~25MB image
FROM nginx:1.27-alpine AS runtime

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy our custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built static files from the build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 (Nginx default)
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Nginx runs as its own user by default, no need to change
CMD ["nginx", "-g", "daemon off;"]