#!/bin/bash

# GIS Map Application - Installation Script
# Author: Archil Odishelidze - DevSpace
# Description: Installs dependencies and sets up the GIS mapping application

set -e

echo "🗺️  GIS Map Application - Installation Script"
echo "=============================================="
echo ""

# Check if running on macOS or Linux
OS_TYPE="$(uname -s)"
echo "🔍 Detected OS: $OS_TYPE"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to install Homebrew on macOS
install_homebrew() {
    if ! command_exists brew; then
        echo "📦 Installing Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    else
        echo "✅ Homebrew already installed"
    fi
}

# Function to install Docker
install_docker() {
    if ! command_exists docker; then
        echo "🐳 Installing Docker..."
        if [[ "$OS_TYPE" == "Darwin" ]]; then
            echo "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
            echo "After installation, please restart this script."
            exit 1
        elif [[ "$OS_TYPE" == "Linux" ]]; then
            # Install Docker on Linux
            curl -fsSL https://get.docker.com -o get-docker.sh
            sh get-docker.sh
            sudo usermod -aG docker $USER
            echo "⚠️  Please log out and log back in for Docker permissions to take effect"
        fi
    else
        echo "✅ Docker already installed"
    fi
}

# Function to install Docker Compose
install_docker_compose() {
    if ! command_exists docker-compose; then
        echo "🐙 Installing Docker Compose..."
        if [[ "$OS_TYPE" == "Darwin" ]]; then
            brew install docker-compose
        elif [[ "$OS_TYPE" == "Linux" ]]; then
            sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
        fi
    else
        echo "✅ Docker Compose already installed"
    fi
}

# Function to install Java 17
install_java() {
    if ! command_exists java || ! java -version 2>&1 | grep -q "17\|21"; then
        echo "☕ Installing Java 17..."
        if [[ "$OS_TYPE" == "Darwin" ]]; then
            brew install openjdk@17
            echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
        elif [[ "$OS_TYPE" == "Linux" ]]; then
            sudo apt update
            sudo apt install -y openjdk-17-jdk
        fi
    else
        echo "✅ Java already installed"
    fi
}

# Function to install Maven
install_maven() {
    if ! command_exists mvn; then
        echo "📦 Installing Maven..."
        if [[ "$OS_TYPE" == "Darwin" ]]; then
            brew install maven
        elif [[ "$OS_TYPE" == "Linux" ]]; then
            sudo apt install -y maven
        fi
    else
        echo "✅ Maven already installed"
    fi
}

# Function to verify installation
verify_installation() {
    echo ""
    echo "🔍 Verifying installation..."
    echo "----------------------------"
    
    echo -n "Docker: "
    if command_exists docker; then
        echo "✅ $(docker --version)"
    else
        echo "❌ Not found"
    fi
    
    echo -n "Docker Compose: "
    if command_exists docker-compose; then
        echo "✅ $(docker-compose --version)"
    else
        echo "❌ Not found"
    fi
    
    echo -n "Java: "
    if command_exists java; then
        echo "✅ $(java -version 2>&1 | head -n 1)"
    else
        echo "❌ Not found"
    fi
    
    echo -n "Maven: "
    if command_exists mvn; then
        echo "✅ $(mvn -version | head -n 1)"
    else
        echo "❌ Not found"
    fi
}

# Main installation process
main() {
    echo "🚀 Starting installation process..."
    echo ""
    
    # Install dependencies based on OS
    if [[ "$OS_TYPE" == "Darwin" ]]; then
        install_homebrew
        install_docker
        install_docker_compose
        install_java
        install_maven
    elif [[ "$OS_TYPE" == "Linux" ]]; then
        sudo apt update
        install_docker
        install_docker_compose
        install_java
        install_maven
    else
        echo "❌ Unsupported operating system: $OS_TYPE"
        exit 1
    fi
    
    # Create data directories
    echo "📁 Creating data directories..."
    mkdir -p data/postgres data/geoserver
    
    # Set permissions
    echo "🔐 Setting permissions..."
    chmod 755 data/postgres data/geoserver
    
    # Verify installation
    verify_installation
    
    echo ""
    echo "🎉 Installation completed successfully!"
    echo ""
    echo "📖 Next steps:"
    echo "   1. Run './start.sh' to start the application"
    echo "   2. Wait for all services to start (may take 2-3 minutes)"
    echo "   3. Open http://localhost:8081/map in your browser"
    echo ""
    echo "📋 Useful commands:"
    echo "   - Start:  ./start.sh"
    echo "   - Stop:   ./stop.sh"
    echo "   - Logs:   docker-compose logs -f"
    echo ""
}

# Run main function
main "$@"