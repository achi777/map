#!/bin/bash

# GIS Map Application - Start Script
# Author: Archil Odishelidze - DevSpace
# Description: Starts all services for the GIS mapping application

set -e

echo "🗺️  GIS Map Application - Start Script"
echo "======================================"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if port is in use
port_in_use() {
    local port=$1
    if command_exists lsof; then
        lsof -i :$port >/dev/null 2>&1
    elif command_exists netstat; then
        netstat -an | grep -q ":$port "
    else
        return 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - waiting..."
        sleep 2
        ((attempt++))
    done
    
    echo "❌ $service_name failed to start within expected time"
    return 1
}

# Function to check prerequisites
check_prerequisites() {
    echo "🔍 Checking prerequisites..."
    
    local missing_deps=()
    
    if ! command_exists docker; then
        missing_deps+=("docker")
    fi
    
    if ! command_exists docker-compose; then
        missing_deps+=("docker-compose")
    fi
    
    if ! command_exists java; then
        missing_deps+=("java")
    fi
    
    if ! command_exists mvn; then
        missing_deps+=("maven")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        echo "❌ Missing dependencies: ${missing_deps[*]}"
        echo "   Please run './install.sh' first"
        exit 1
    fi
    
    echo "✅ All prerequisites satisfied"
}

# Function to start Docker services
start_docker_services() {
    echo "🐳 Starting Docker services..."
    
    # Check if Docker is running
    if ! docker info >/dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    # Start PostgreSQL and GeoServer
    docker-compose up -d
    
    echo "✅ Docker services started"
}

# Function to build Spring Boot application
build_application() {
    echo "🔨 Building Spring Boot application..."
    
    # Clean and compile
    mvn clean compile -q
    
    echo "✅ Application built successfully"
}

# Function to start Spring Boot application
start_spring_boot() {
    echo "🚀 Starting Spring Boot application..."
    
    # Check if port 8081 is already in use
    if port_in_use 8081; then
        echo "⚠️  Port 8081 is already in use. Stopping existing process..."
        pkill -f "GisMapApplication" || true
        sleep 2
    fi
    
    # Start Spring Boot in background
    nohup mvn spring-boot:run > application.log 2>&1 &
    echo $! > app.pid
    
    echo "✅ Spring Boot application starting..."
}

# Function to display status
show_status() {
    echo ""
    echo "📊 Service Status"
    echo "=================="
    
    # Check Docker services
    echo "Docker Services:"
    docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    echo "Application Status:"
    if [ -f app.pid ] && kill -0 "$(cat app.pid)" 2>/dev/null; then
        echo "✅ Spring Boot Application: Running (PID: $(cat app.pid))"
    else
        echo "❌ Spring Boot Application: Not running"
    fi
    
    echo ""
    echo "🌐 Access URLs:"
    echo "   Map Interface:    http://localhost:8081/map"
    echo "   GeoServer Admin:  http://localhost:8080/geoserver (admin/admin)"
    echo "   API Endpoints:    http://localhost:8081/api/geo/"
    
    echo ""
    echo "📋 Useful Commands:"
    echo "   View logs:        tail -f application.log"
    echo "   Stop services:    ./stop.sh"
    echo "   Docker logs:      docker-compose logs -f"
}

# Main startup process
main() {
    echo "🚀 Starting GIS Map Application..."
    echo ""
    
    # Check prerequisites
    check_prerequisites
    
    # Start Docker services
    start_docker_services
    
    # Wait for PostgreSQL to be ready
    wait_for_service "PostgreSQL" "http://localhost:5432" || true
    
    # Wait for GeoServer to be ready
    wait_for_service "GeoServer" "http://localhost:8080/geoserver" || true
    
    # Build application
    build_application
    
    # Start Spring Boot application
    start_spring_boot
    
    # Wait for Spring Boot to be ready
    wait_for_service "Spring Boot Application" "http://localhost:8081/api/geo/config"
    
    # Show final status
    show_status
    
    echo ""
    echo "🎉 All services started successfully!"
    echo "🌍 Open http://localhost:8081/map in your browser to view the map"
    echo ""
}

# Handle script interruption
cleanup() {
    echo ""
    echo "🛑 Script interrupted. Services may still be running."
    echo "   Use './stop.sh' to stop all services"
    exit 1
}

trap cleanup INT TERM

# Run main function
main "$@"