#!/bin/bash

# GIS Map Application - Stop Script
# Author: Archil Odishelidze - DevSpace
# Description: Stops all services for the GIS mapping application

set -e

echo "🗺️  GIS Map Application - Stop Script"
echo "====================================="
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to stop Spring Boot application
stop_spring_boot() {
    echo "🛑 Stopping Spring Boot application..."
    
    # Check if PID file exists and process is running
    if [ -f app.pid ]; then
        local pid=$(cat app.pid)
        if kill -0 "$pid" 2>/dev/null; then
            echo "   Stopping process with PID: $pid"
            kill "$pid"
            
            # Wait for graceful shutdown
            local count=0
            while kill -0 "$pid" 2>/dev/null && [ $count -lt 10 ]; do
                echo "   Waiting for graceful shutdown..."
                sleep 1
                ((count++))
            done
            
            # Force kill if still running
            if kill -0 "$pid" 2>/dev/null; then
                echo "   Force stopping process..."
                kill -9 "$pid" || true
            fi
        fi
        rm -f app.pid
    fi
    
    # Kill any remaining GisMapApplication processes
    if command_exists pkill; then
        pkill -f "GisMapApplication" || true
        pkill -f "spring-boot:run" || true
    fi
    
    echo "✅ Spring Boot application stopped"
}

# Function to stop Docker services
stop_docker_services() {
    echo "🐳 Stopping Docker services..."
    
    if command_exists docker-compose; then
        # Stop and remove containers
        docker-compose down
        echo "✅ Docker services stopped"
    else
        echo "⚠️  docker-compose not found, skipping Docker services"
    fi
}

# Function to clean up log files
cleanup_logs() {
    echo "🧹 Cleaning up log files..."
    
    # Remove application logs
    rm -f application.log
    rm -f app.log
    rm -f nohup.out
    
    echo "✅ Log files cleaned up"
}

# Function to show final status
show_final_status() {
    echo ""
    echo "📊 Final Status"
    echo "==============="
    
    # Check for any remaining processes
    if command_exists pgrep; then
        if pgrep -f "GisMapApplication" >/dev/null; then
            echo "⚠️  Some GisMapApplication processes may still be running"
            echo "   PIDs: $(pgrep -f "GisMapApplication" | tr '\n' ' ')"
        else
            echo "✅ No GisMapApplication processes running"
        fi
    fi
    
    # Check Docker containers
    if command_exists docker-compose; then
        local running_containers=$(docker-compose ps -q)
        if [ -n "$running_containers" ]; then
            echo "⚠️  Some Docker containers may still be running:"
            docker-compose ps --format "table {{.Name}}\t{{.Status}}"
        else
            echo "✅ No Docker containers running"
        fi
    fi
    
    echo ""
    echo "🔌 Ports should now be available:"
    echo "   - 8081 (Spring Boot)"
    echo "   - 8080 (GeoServer)"
    echo "   - 5432 (PostgreSQL)"
}

# Function to force stop everything
force_stop() {
    echo "💥 Force stopping all services..."
    
    # Force kill Java processes
    if command_exists pkill; then
        pkill -9 -f "java.*GisMapApplication" || true
        pkill -9 -f "mvn.*spring-boot:run" || true
    fi
    
    # Force stop Docker containers
    if command_exists docker; then
        docker kill $(docker ps -q --filter "name=postgis") 2>/dev/null || true
        docker kill $(docker ps -q --filter "name=geoserver") 2>/dev/null || true
        docker rm $(docker ps -aq --filter "name=postgis") 2>/dev/null || true
        docker rm $(docker ps -aq --filter "name=geoserver") 2>/dev/null || true
    fi
    
    echo "✅ Force stop completed"
}

# Main stop process
main() {
    echo "🛑 Stopping GIS Map Application..."
    echo ""
    
    # Check for force flag
    if [ "$1" = "--force" ] || [ "$1" = "-f" ]; then
        force_stop
        cleanup_logs
        show_final_status
        echo ""
        echo "💥 All services force stopped!"
        return
    fi
    
    # Graceful shutdown
    stop_spring_boot
    stop_docker_services
    cleanup_logs
    show_final_status
    
    echo ""
    echo "✅ All services stopped successfully!"
    echo ""
    echo "📝 To start again, run: ./start.sh"
    echo "💥 For force stop, run: ./stop.sh --force"
}

# Handle script interruption
cleanup() {
    echo ""
    echo "🛑 Stop script interrupted"
    exit 1
}

trap cleanup INT TERM

# Run main function
main "$@"