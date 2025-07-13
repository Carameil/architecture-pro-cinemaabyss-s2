package main

import (
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"strconv"
	"strings"
	"time"
)

type ProxyConfig struct {
	Port                   string
	MonolithURL           string
	MoviesServiceURL      string
	EventsServiceURL      string
	GradualMigration      bool
	MoviesMigrationPercent int
}

func loadConfig() *ProxyConfig {
	config := &ProxyConfig{
		Port:                   getEnv("PORT", "8000"),
		MonolithURL:           getEnv("MONOLITH_URL", "http://localhost:8080"),
		MoviesServiceURL:      getEnv("MOVIES_SERVICE_URL", "http://localhost:8081"),
		EventsServiceURL:      getEnv("EVENTS_SERVICE_URL", "http://localhost:8082"),
		GradualMigration:      getEnvBool("GRADUAL_MIGRATION", true),
		MoviesMigrationPercent: getEnvInt("MOVIES_MIGRATION_PERCENT", 50),
	}
	return config
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvBool(key string, defaultValue bool) bool {
	if value := os.Getenv(key); value != "" {
		return strings.ToLower(value) == "true"
	}
	return defaultValue
}

func getEnvInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func createReverseProxy(targetURL string) (*httputil.ReverseProxy, error) {
	target, err := url.Parse(targetURL)
	if err != nil {
		return nil, err
	}
	
	proxy := httputil.NewSingleHostReverseProxy(target)
	
	// Customize the director to preserve the original path
	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
		req.Host = target.Host
		req.URL.Host = target.Host
		req.URL.Scheme = target.Scheme
	}
	
	return proxy, nil
}

func (config *ProxyConfig) shouldRouteToNewService() bool {
	if !config.GradualMigration {
		return false
	}
	return rand.Intn(100) < config.MoviesMigrationPercent
}

func (config *ProxyConfig) handleRequest(w http.ResponseWriter, r *http.Request) {
	path := r.URL.Path
	
	// Health check endpoint
	if path == "/health" {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"healthy","service":"proxy"}`))
		return
	}
	
	// Route to appropriate service based on path
	var targetURL string
	
	switch {
	case strings.HasPrefix(path, "/api/events"):
		// Events service handles all event-related requests
		targetURL = config.EventsServiceURL
		
	case strings.HasPrefix(path, "/api/movies"):
		// Movies: use Strangler Fig pattern for gradual migration
		if config.shouldRouteToNewService() {
			targetURL = config.MoviesServiceURL
			log.Printf("Routing /api/movies to new Movies Service")
		} else {
			targetURL = config.MonolithURL
			log.Printf("Routing /api/movies to Monolith")
		}
		
	default:
		// All other requests go to monolith (users, payments, subscriptions)
		targetURL = config.MonolithURL
	}
	
	// Create reverse proxy and forward the request
	proxy, err := createReverseProxy(targetURL)
	if err != nil {
		http.Error(w, "Proxy configuration error", http.StatusInternalServerError)
		log.Printf("Error creating proxy for %s: %v", targetURL, err)
		return
	}
	
	// Log the routing decision
	log.Printf("Proxying request: %s %s -> %s", r.Method, r.URL.Path, targetURL)
	
	// Forward the request
	proxy.ServeHTTP(w, r)
}

func main() {
	// Initialize random seed
	rand.Seed(time.Now().UnixNano())
	
	// Load configuration
	config := loadConfig()
	
	// Log configuration
	log.Printf("Starting Proxy Service on port %s", config.Port)
	log.Printf("Monolith URL: %s", config.MonolithURL)
	log.Printf("Movies Service URL: %s", config.MoviesServiceURL)
	log.Printf("Events Service URL: %s", config.EventsServiceURL)
	log.Printf("Gradual Migration: %v", config.GradualMigration)
	log.Printf("Movies Migration Percent: %d%%", config.MoviesMigrationPercent)
	
	// Setup HTTP server
	http.HandleFunc("/", config.handleRequest)
	
	// Start server
	addr := fmt.Sprintf(":%s", config.Port)
	log.Printf("Proxy Service listening on %s", addr)
	if err := http.ListenAndServe(addr, nil); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
} 