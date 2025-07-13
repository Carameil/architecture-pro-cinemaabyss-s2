.PHONY: help build up down restart logs test test-local test-k8s clean ps health proxy-logs events-logs kafka-ui

# Default target
help:
	@echo "CinemaAbyss Docker Management"
	@echo "============================="
	@echo "make build       - Build all Docker images"
	@echo "make up          - Start all services"
	@echo "make down        - Stop all services"
	@echo "make restart     - Restart all services"
	@echo "make logs        - Show logs for all services"
	@echo "make test        - Run Postman tests (local environment)"
	@echo "make clean       - Stop services and remove volumes"
	@echo "make ps          - Show running containers"
	@echo "make health      - Check health of all services"
	@echo "make proxy-logs  - Show proxy service logs"
	@echo "make events-logs - Show events service logs"
	@echo "make kafka-ui    - Open Kafka UI URL"

# Build and start all services
start: build up

# Build all services
build:
	docker-compose build

# Start all services
up:
	docker-compose up -d

# Stop all services
down:
	docker-compose down

# Restart all services
restart: down up

# Show logs for all services
logs:
	docker-compose logs -f

# Run tests in Docker
test:
	@echo "Installing test dependencies and running tests..."
	docker run --rm -v $(PWD)/tests/postman:/app -w /app --network cinemaabyss-network \
		node:18-alpine sh -c "npm install && node run-tests.js --environment docker"

# Alternative test command using docker-compose exec
test-local:
	@echo "Running tests against local Docker environment..."
	cd tests/postman && docker run --rm -v $(PWD):/app -w /app --network host \
		node:18-alpine sh -c "npm install && npm run test:local"

# Test Kubernetes environment
test-k8s:
	cd tests/postman && docker run --rm -v $(PWD):/app -w /app --network host \
		node:18-alpine sh -c "npm install && npm run test:kubernetes"

# Clean everything (including volumes)
clean:
	docker-compose down -v

# Show running containers
ps:
	docker-compose ps

# Health check endpoints
health:
	@echo "Checking health endpoints..."
	@echo "\n=== Proxy Service ==="
	@curl -s http://localhost:8000/health || echo "Proxy service is not responding"
	@echo "\n\n=== Monolith ==="
	@curl -s http://localhost:8080/health || echo "Monolith is not responding"
	@echo "\n\n=== Movies Service ==="
	@curl -s http://localhost:8081/api/movies/health || echo "Movies service is not responding"
	@echo "\n\n=== Events Service ==="
	@curl -s http://localhost:8082/api/events/health || echo "Events service is not responding"
	@echo "\n"

# Proxy service specific commands
proxy-logs:
	docker logs -f cinemaabyss-proxy-service

# Events service specific commands
events-logs:
	docker logs -f cinemaabyss-events-service

# Kafka UI
kafka-ui:
	@echo "Kafka UI is available at: http://localhost:8090"
	@echo "Opening in browser..."
	@command -v xdg-open >/dev/null 2>&1 && xdg-open http://localhost:8090 || \
	command -v open >/dev/null 2>&1 && open http://localhost:8090 || \
	echo "Please open http://localhost:8090 in your browser"

# Test individual services
test-proxy:
	@echo "Testing proxy routing..."
	@for i in 1 2 3 4 5; do \
		echo "\nRequest $$i:"; \
		curl -s http://localhost:8000/api/movies | jq -r '.[0].title' 2>/dev/null || echo "Failed"; \
		sleep 1; \
	done

test-events:
	@echo "Testing events creation..."
	@echo "\n=== Creating User Event ==="
	@curl -X POST http://localhost:8000/api/events/user \
		-H "Content-Type: application/json" \
		-d '{"userId": 123, "action": "LOGIN", "details": "User logged in"}' | jq . 2>/dev/null || echo "Failed"
	@echo "\n=== Creating Payment Event ==="
	@curl -X POST http://localhost:8000/api/events/payment \
		-H "Content-Type: application/json" \
		-d '{"paymentId": 456, "userId": 123, "amount": "99.99", "status": "COMPLETED"}' | jq . 2>/dev/null || echo "Failed"
	@echo "\n=== Creating Movie Event ==="
	@curl -X POST http://localhost:8000/api/events/movie \
		-H "Content-Type: application/json" \
		-d '{"movieId": 1, "title": "The Matrix", "action": "VIEWED", "userId": 123}' | jq . 2>/dev/null || echo "Failed"

# Development helpers
dev-rebuild:
	docker-compose up -d --build proxy-service events-service

# Show migration percentage
show-migration:
	@echo "Current migration percentage:"
	@grep MOVIES_MIGRATION_PERCENT docker-compose.yml | grep -o '[0-9]\+'

# Set migration percentage (usage: make set-migration PERCENT=75)
set-migration:
	@if [ -z "$(PERCENT)" ]; then \
		echo "Usage: make set-migration PERCENT=75"; \
	else \
		sed -i 's/MOVIES_MIGRATION_PERCENT: "[0-9]*"/MOVIES_MIGRATION_PERCENT: "$(PERCENT)"/' docker-compose.yml; \
		echo "Migration percentage set to $(PERCENT)%"; \
		echo "Run 'make restart' to apply changes"; \
	fi 