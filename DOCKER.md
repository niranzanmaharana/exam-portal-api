# Docker Deployment Guide

This guide explains how to build and deploy the Exam Portal API as a Docker container.

## Prerequisites

- Docker installed (version 20.10+)
- Docker Compose installed (version 2.0+)
- At least 2GB free disk space

## Quick Start

### Option 1: Using Docker Compose (Recommended)

This will start the API, MySQL database, and phpMyAdmin:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f exam-portal-api

# Stop services
docker-compose down
```

The API will be available at: `http://localhost:9090/exam-portal-api`

### Option 2: Build and Run Manually

#### Build the Docker Image

```bash
cd exam-portal-api
docker build -t exam-portal-api:latest .
```

#### Run the Container

```bash
# Run with external MySQL database
docker run -d \
  --name exam-portal-api \
  -p 9090:9090 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/exam_portal \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e JWT_SECRET=your-secret-key-min-256-bits \
  -e SPRING_PROFILES_ACTIVE=docker \
  exam-portal-api:latest
```

## Configuration

### Environment Variables

The application can be configured using environment variables. Key variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL connection URL | `jdbc:mysql://mysql:3306/exam_portal` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `exam_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `exam_password` |
| `JWT_SECRET` | JWT signing secret | (required) |
| `JWT_EXPIRATION` | JWT expiration in ms | `86400000` |
| `SPRING_MAIL_ENABLED` | Enable email sending | `false` |
| `SPRING_MAIL_USERNAME` | SMTP username | - |
| `SPRING_MAIL_PASSWORD` | SMTP password | - |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:4200` |
| `APP_FRONTEND_URL` | Frontend URL | `http://localhost:4200` |
| `SERVER_PORT` | Server port | `9090` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |

### Using docker-compose.yml

Edit `docker-compose.yml` to customize:

1. **Database credentials**
2. **JWT secret** (IMPORTANT: Change in production!)
3. **Email configuration**
4. **CORS origins** (add your frontend URL)

## Dockerfile Options

### Standard Dockerfile (JAR with embedded Tomcat)

The main `Dockerfile` builds the application as an executable JAR with embedded Tomcat. This is the recommended approach for Docker.

**Build:**
```bash
docker build -t exam-portal-api:latest .
```

### Alternative: WAR with Tomcat

If you prefer to deploy as WAR to Tomcat, use `Dockerfile.war`:

**Build:**
```bash
docker build -f Dockerfile.war -t exam-portal-api:war .
```

**Run:**
```bash
docker run -d \
  --name exam-portal-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/exam_portal \
  exam-portal-api:war
```

Note: WAR deployment uses port 8080 (Tomcat default) instead of 9090.

## Production Deployment

### 1. Build Production Image

```bash
docker build -t exam-portal-api:1.0.0 .
```

### 2. Use Environment Variables or Secrets

**Option A: Environment File**
```bash
# Create .env file
cat > .env << EOF
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/exam_portal
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your-very-secure-secret-key-min-256-bits
SPRING_MAIL_ENABLED=true
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com
APP_FRONTEND_URL=https://yourdomain.com
EOF

# Run with env file
docker run -d --env-file .env -p 9090:9090 exam-portal-api:1.0.0
```

**Option B: Docker Secrets (Docker Swarm)**
```bash
# Create secrets
echo "your-db-password" | docker secret create db_password -
echo "your-jwt-secret" | docker secret create jwt_secret -

# Use in docker-compose.yml
services:
  exam-portal-api:
    secrets:
      - db_password
      - jwt_secret
```

### 3. Health Checks

The container includes health checks. Monitor with:

```bash
docker ps  # Check health status
docker inspect exam-portal-api | grep Health -A 10
```

### 4. Logs

```bash
# View logs
docker logs exam-portal-api

# Follow logs
docker logs -f exam-portal-api

# Last 100 lines
docker logs --tail 100 exam-portal-api
```

## Troubleshooting

### Container won't start

1. **Check logs:**
   ```bash
   docker logs exam-portal-api
   ```

2. **Verify database connection:**
   - Ensure MySQL is accessible
   - Check connection string format
   - Verify credentials

3. **Check port conflicts:**
   ```bash
   # Check if port 9090 is in use
   netstat -an | grep 9090
   # Or use different port
   docker run -p 9091:9090 ...
   ```

### Database Connection Issues

1. **Wait for database to be ready:**
   ```bash
   # Check MySQL container
   docker-compose ps mysql
   docker-compose logs mysql
   ```

2. **Test connection:**
   ```bash
   docker exec -it exam-portal-mysql mysql -u exam_user -p exam_portal
   ```

### Application Not Responding

1. **Check health:**
   ```bash
   curl http://localhost:9090/exam-portal-api/api/auth/login
   ```

2. **Check if container is running:**
   ```bash
   docker ps | grep exam-portal-api
   ```

3. **Restart container:**
   ```bash
   docker restart exam-portal-api
   ```

## Development

### Rebuild After Code Changes

```bash
# Rebuild image
docker-compose build exam-portal-api

# Restart service
docker-compose up -d exam-portal-api
```

### Access Container Shell

```bash
docker exec -it exam-portal-api /bin/bash
```

### View Application Logs

```bash
docker-compose logs -f exam-portal-api
```

## Docker Compose Services

The `docker-compose.yml` includes:

1. **exam-portal-api**: The Spring Boot application
2. **mysql**: MySQL 8.0 database
3. **phpmyadmin**: Database management UI (optional)

### Start Specific Services

```bash
# Start only API and MySQL (no phpMyAdmin)
docker-compose up -d exam-portal-api mysql

# Start everything
docker-compose up -d
```

## Security Considerations

1. **Change default passwords** in `docker-compose.yml`
2. **Use strong JWT secret** (minimum 256 bits)
3. **Don't commit secrets** to version control
4. **Use Docker secrets** or environment files for production
5. **Limit CORS origins** to your actual frontend domains
6. **Use HTTPS** in production (configure reverse proxy)

## Performance Tuning

### JVM Options

Add JVM options in Dockerfile or docker-compose.yml:

```yaml
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

### Database Connection Pool

Configure in `application-docker.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

## Next Steps

1. Configure your database connection
2. Set up email service (if needed)
3. Update CORS origins for your frontend
4. Change JWT secret
5. Deploy to your Docker host or cloud platform

## Support

For issues or questions:
- Check application logs: `docker logs exam-portal-api`
- Review Docker logs: `docker-compose logs`
- Verify configuration in `application-docker.yml`

