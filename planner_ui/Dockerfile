# syntax=docker/dockerfile:1

# ============================================================
# STAGE 1 — "builder": Node + npm compiles React into static files
# ============================================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency manifests first (cache layer).
# If only source changes, npm install doesn't re-run.
COPY package.json package-lock.json ./
RUN npm ci

# Copy rest of the frontend source and build.
# `npm run build` produces a `dist/` folder with static HTML/JS/CSS.
COPY . .
RUN npm run build

# ============================================================
# STAGE 2 — "runtime": tiny nginx image serving the static files
# ============================================================
FROM nginx:1.27-alpine

# Copy built static files from stage 1 into nginx's default web root.
COPY --from=builder /app/dist /usr/share/nginx/html

# Replace nginx's default config with ours (handles /api proxy + React Router).
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
