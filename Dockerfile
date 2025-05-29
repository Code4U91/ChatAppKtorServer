# --------- Build Stage ---------
FROM gradle:8.4.0-jdk17 AS builder

# Copy everything into the container
COPY . /app
WORKDIR /app

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the Ktor server (adjust for your project)
RUN ./gradlew installDist

# --------- Runtime Stage ---------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built files from the builder stage
COPY --from=builder /app/build/install/ChatAppKtorServer /app

# Set environment variable for Render's dynamic port
ENV PORT=8080
EXPOSE 8080

# Start your Ktor server
CMD ["./bin/ChatAppKtorServer"]
