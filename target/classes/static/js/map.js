// Republic of Georgia coordinates - centered on Tbilisi
const georgiaCenter = [41.7151, 44.8271];
const georgiaZoom = 7;

// Georgia boundaries (approximate)
const georgiaBounds = [
    [39.5, 39.5],  // Southwest corner
    [43.5, 46.5]   // Northeast corner
];

// Initialize the map
const map = L.map('map', {
    center: georgiaCenter,
    zoom: georgiaZoom,
    maxBounds: georgiaBounds,
    maxBoundsViscosity: 1.0
});

// Add OpenStreetMap base layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// Georgia boundaries polygon
const georgiaBoundary = [
    [41.064, 40.095], [41.023, 40.321], [40.839, 40.204], [40.315, 40.065],
    [39.955, 40.120], [39.688, 40.375], [39.877, 40.734], [40.246, 40.921],
    [40.321, 41.447], [40.706, 41.736], [41.151, 41.736], [41.736, 42.614],
    [42.386, 42.614], [42.614, 42.174], [43.451, 42.174], [43.582, 41.736],
    [43.743, 41.151], [43.451, 40.839], [43.321, 40.580], [42.878, 40.321],
    [42.614, 40.204], [42.174, 40.065], [41.736, 40.120], [41.447, 40.246],
    [41.238, 40.375], [41.064, 40.095]
];

L.polygon(georgiaBoundary, {
    color: '#333',
    weight: 2,
    opacity: 0.8,
    fillColor: 'transparent',
    fillOpacity: 0
}).addTo(map);

let config = {};
let layers = {};
let wmsLayers = {};

// Load configuration and layers
Promise.all([
    fetch('/api/geo/config').then(r => r.json()),
    fetch('/api/geo/layers/available').then(r => r.json())
]).then(([configData, layersData]) => {
    config = configData;
    
    // Create layer controls
    const layerToggles = document.getElementById('layer-toggles');
    
    layersData.forEach(layer => {
        // Create layer control
        const control = document.createElement('div');
        control.className = 'layer-control';
        
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `layer-${layer.name}`;
        checkbox.checked = layer.visible;
        
        const label = document.createElement('label');
        label.htmlFor = checkbox.id;
        
        const colorIndicator = document.createElement('div');
        colorIndicator.className = 'layer-color';
        colorIndicator.style.backgroundColor = layer.color;
        
        label.appendChild(colorIndicator);
        label.appendChild(document.createTextNode(layer.displayName));
        
        control.appendChild(checkbox);
        control.appendChild(label);
        layerToggles.appendChild(control);
        
        // Load layer data
        loadGeoJSONLayer(layer);
        
        // Add event listener
        checkbox.addEventListener('change', function() {
            if (layers[layer.name]) {
                if (this.checked) {
                    map.addLayer(layers[layer.name]);
                } else {
                    map.removeLayer(layers[layer.name]);
                }
            }
        });
    });
    
    // Load factories for the panel
    loadFactories();
}).catch(error => {
    console.error('Error loading configuration:', error);
});

function loadGeoJSONLayer(layerConfig) {
    fetch(`/api/geo/layers/${layerConfig.name}/geojson`)
        .then(response => response.json())
        .then(geojsonData => {
            const layer = L.geoJSON(geojsonData, {
                style: function(feature) {
                    return {
                        color: layerConfig.color,
                        weight: layerConfig.weight || 2,
                        opacity: layerConfig.opacity || 0.8,
                        fillColor: layerConfig.fillColor || layerConfig.color,
                        fillOpacity: layerConfig.fillOpacity || 0.3
                    };
                },
                onEachFeature: function(feature, layer) {
                    // Add popup with feature information
                    let html = '<h4>' + layerConfig.displayName + ' ინფორმაცია</h4>';
                    
                    if (layerConfig.name === 'factories') {
                        html += '<strong>სახელი:</strong> ' + (feature.properties.name || 'N/A') + '<br>';
                        html += '<strong>ინდუსტრია:</strong> ' + (feature.properties.industryType || 'N/A') + '<br>';
                        html += '<strong>შესაძლებლობა:</strong> ' + (feature.properties.capacity || 'N/A') + '<br>';
                        html += '<strong>დაარსების წელი:</strong> ' + (feature.properties.establishedYear || 'N/A') + '<br>';
                        html += '<strong>სტატუსი:</strong> ' + (feature.properties.status || 'N/A') + '<br>';
                    } else if (layerConfig.name === 'roads') {
                        html += '<strong>სახელი:</strong> ' + (feature.properties.name || 'N/A') + '<br>';
                        html += '<strong>გზის ტიპი:</strong> ' + (feature.properties.roadType || 'N/A') + '<br>';
                        html += '<strong>სიგრძე (კმ):</strong> ' + (feature.properties.lengthKm || 'N/A') + '<br>';
                        html += '<strong>ზედაპირი:</strong> ' + (feature.properties.surfaceType || 'N/A') + '<br>';
                        html += '<strong>მაქს. სიჩქარე:</strong> ' + (feature.properties.maxSpeed || 'N/A') + ' კმ/სთ<br>';
                    } else if (layerConfig.name === 'forests') {
                        html += '<strong>სახელი:</strong> ' + (feature.properties.name || 'N/A') + '<br>';
                        html += '<strong>ტყის ტიპი:</strong> ' + (feature.properties.forestType || 'N/A') + '<br>';
                        html += '<strong>ფართობი (ჰა):</strong> ' + (feature.properties.areaHectares || 'N/A') + '<br>';
                        html += '<strong>დაცვის სტატუსი:</strong> ' + (feature.properties.protectionStatus || 'N/A') + '<br>';
                    } else {
                        for (let key in feature.properties) {
                            if (key !== 'id' && feature.properties[key] !== null) {
                                html += '<strong>' + key + ':</strong> ' + feature.properties[key] + '<br>';
                            }
                        }
                    }
                    
                    layer.bindPopup(html);
                }
            });
            
            // Store layer and add to map
            layers[layerConfig.name] = layer;
            layer.addTo(map);
            
            console.log(`✅ Loaded GeoJSON layer: ${layerConfig.name}`);
        })
        .catch(err => console.log('Error loading layer:', layerConfig.name, err));
}

// Factory Management Functions
let editingFactoryId = null;
let coordinatePickingMode = false;

// Routing Functions
let routingMode = null; // 'start' or 'end'
let startLocation = null;
let endLocation = null;
let currentRoute = null;
let startMarker = null;
let endMarker = null;

function loadFactories() {
    fetch('/api/factories')
        .then(response => response.json())
        .then(factories => {
            const factoryList = document.getElementById('factory-list');
            factoryList.innerHTML = '';
            
            factories.forEach(factory => {
                const item = document.createElement('div');
                item.className = 'factory-item';
                item.dataset.factoryId = factory.id;
                item.innerHTML = `
                    <div><strong>${factory.name}</strong></div>
                    <div><small>${factory.industryType} | ${factory.status}</small></div>
                `;
                
                item.addEventListener('click', () => selectFactory(factory.id, item));
                factoryList.appendChild(item);
            });
        })
        .catch(error => console.error('Error loading factories:', error));
}

function selectFactory(factoryId, element) {
    // Remove previous selection
    document.querySelectorAll('.factory-item').forEach(item => {
        item.classList.remove('selected');
    });
    
    // Add selection to current item
    element.classList.add('selected');
    
    // Load factory details
    fetch(`/api/factories/${factoryId}`)
        .then(response => response.json())
        .then(factory => {
            document.getElementById('factory-name').value = factory.name;
            document.getElementById('factory-industry').value = factory.industryType;
            document.getElementById('factory-capacity').value = factory.capacity;
            document.getElementById('factory-year').value = factory.establishedYear;
            document.getElementById('factory-status').value = factory.status;
            
            editingFactoryId = factoryId;
            
            // Show factory location on map
            if (factory.location && factory.location.coordinates) {
                const [lng, lat] = factory.location.coordinates;
                map.setView([lat, lng], 15);
            }
        })
        .catch(error => console.error('Error loading factory details:', error));
}

function enableCoordinatePicking() {
    coordinatePickingMode = true;
    map.getContainer().style.cursor = 'crosshair';
    
    // Show instruction
    alert('კლიკი ნახეთ რუქაზე კოორდინატების ასარჩევად');
}

function saveFactory() {
    const name = document.getElementById('factory-name').value;
    const industryType = document.getElementById('factory-industry').value;
    const capacity = parseInt(document.getElementById('factory-capacity').value);
    const establishedYear = parseInt(document.getElementById('factory-year').value);
    const status = document.getElementById('factory-status').value;
    
    if (!name || !industryType) {
        alert('გთხოვთ შეავსოთ სავალდებულო ველები');
        return;
    }
    
    const factoryData = {
        name,
        industryType,
        capacity,
        establishedYear,
        status
    };
    
    const url = editingFactoryId 
        ? `/api/factories/${editingFactoryId}`
        : '/api/factories';
    
    const method = editingFactoryId ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(factoryData)
    })
    .then(response => response.json())
    .then(result => {
        alert(editingFactoryId ? 'ქარხანა განახლდა!' : 'ქარხანა შეიქმნა!');
        clearFactoryForm();
        loadFactories();
        
        // Reload the factories layer
        if (layers.factories) {
            map.removeLayer(layers.factories);
            const factoriesConfig = { name: 'factories', displayName: 'ქარხნები', color: '#ff6b35' };
            loadGeoJSONLayer(factoriesConfig);
        }
    })
    .catch(error => {
        console.error('Error saving factory:', error);
        alert('შეცდომა ქარხნის შენახვისას');
    });
}

function deleteFactory() {
    if (!editingFactoryId) {
        alert('გთხოვთ აირჩიოთ ქარხანა წასაშლელად');
        return;
    }
    
    if (!confirm('დარწმუნებული ხართ, რომ გსურთ ამ ქარხნის წაშლა?')) {
        return;
    }
    
    fetch(`/api/factories/${editingFactoryId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            alert('ქარხანა წაიშალა!');
            clearFactoryForm();
            loadFactories();
            
            // Reload the factories layer
            if (layers.factories) {
                map.removeLayer(layers.factories);
                const factoriesConfig = { name: 'factories', displayName: 'ქარხნები', color: '#ff6b35' };
                loadGeoJSONLayer(factoriesConfig);
            }
        } else {
            throw new Error('Failed to delete factory');
        }
    })
    .catch(error => {
        console.error('Error deleting factory:', error);
        alert('შეცდომა ქარხნის წაშლისას');
    });
}

function clearFactoryForm() {
    document.getElementById('factory-name').value = '';
    document.getElementById('factory-industry').value = '';
    document.getElementById('factory-capacity').value = '';
    document.getElementById('factory-year').value = '';
    document.getElementById('factory-status').value = 'Active';
    editingFactoryId = null;
    
    // Remove selection
    document.querySelectorAll('.factory-item').forEach(item => {
        item.classList.remove('selected');
    });
}

// Routing functions
function setStartLocation() {
    routingMode = 'start';
    map.getContainer().style.cursor = 'crosshair';
    alert('კლიკი ნახეთ საწყისი წერტილის ასარჩევად');
}

function setEndLocation() {
    routingMode = 'end';
    map.getContainer().style.cursor = 'crosshair';
    alert('კლიკი ნახეთ დანიშნულების წერტილის ასარჩევად');
}

function calculateRoute() {
    if (!startLocation || !endLocation) {
        alert('გთხოვთ აირჩიოთ საწყისი და საბოლოო წერტილები');
        return;
    }
    
    // Clear previous route
    if (currentRoute) {
        map.removeLayer(currentRoute);
    }
    
    // Get selected transport mode
    const routeType = document.querySelector('input[name="route-type"]:checked').value;
    
    // Try real routing services in order
    tryOSRMWithType(routeType)
        .catch(() => tryOpenRouteServiceWithType(routeType))
        .catch(() => tryGraphHopperWithType(routeType))
        .catch(() => {
            console.log('All routing services failed, falling back to simple route');
            drawSimpleRoute();
        });
}

function swapLocations() {
    if (!startLocation || !endLocation) {
        alert('გთხოვთ პირველ რიგში აირჩიოთ ორივე წერტილი');
        return;
    }
    
    // Swap the locations
    const temp = startLocation;
    startLocation = endLocation;
    endLocation = temp;
    
    // Update input fields
    document.getElementById('start-location').value = `${startLocation.lat.toFixed(6)}, ${startLocation.lng.toFixed(6)}`;
    document.getElementById('end-location').value = `${endLocation.lat.toFixed(6)}, ${endLocation.lng.toFixed(6)}`;
    
    // Update markers
    if (startMarker) {
        startMarker.setLatLng(startLocation);
    }
    if (endMarker) {
        endMarker.setLatLng(endLocation);
    }
    
    // Update marker icons to reflect the swap
    if (startMarker) {
        startMarker.setIcon(L.divIcon({
            className: 'start-marker',
            html: '<div style="background: #28a745; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">A</div>'
        }));
    }
    
    if (endMarker) {
        endMarker.setIcon(L.divIcon({
            className: 'end-marker',
            html: '<div style="background: #dc3545; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">B</div>'
        }));
    }
    
    // Recalculate route if exists
    if (currentRoute) {
        calculateRoute();
    }
}

function drawSimpleRoute() {
    const latlngs = [startLocation, endLocation];
    
    currentRoute = L.polyline(latlngs, {
        color: '#ff6b35',
        weight: 6,
        opacity: 0.8,
        dashArray: '10, 10'
    }).addTo(map);
    
    // Calculate approximate distance
    const distance = (startLocation.distanceTo(endLocation) / 1000).toFixed(2);
    
    // Show route info
    const routeInfo = document.getElementById('route-info');
    routeInfo.innerHTML = `
        <strong>მარშრუტის ინფორმაცია:</strong><br>
        <strong>მანძილი:</strong> ~${distance} კმ (პირდაპირი ხაზი)<br>
        <strong>ტიპი:</strong> გამარტივებული მარშრუტი<br>
        <small>ნაჩვენებია პირდაპირი ხაზი ორ წერტილს შორის</small>
    `;
    routeInfo.classList.remove('hidden');
    
    // Fit map to show the route
    const group = new L.featureGroup([startMarker, endMarker, currentRoute]);
    map.fitBounds(group.getBounds().pad(0.1));
}

// Note: Legacy routing code removed - now using real OpenStreetMap routing services

function haversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth's radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

// Real routing service functions with type support
function tryOSRMWithType(routeType) {
    const start = `${startLocation.lng},${startLocation.lat}`;
    const end = `${endLocation.lng},${endLocation.lat}`;
    
    // Map route types to OSRM profiles
    const profileMap = {
        'driving': 'driving',
        'walking': 'foot',
        'cycling': 'bike'
    };
    
    const profile = profileMap[routeType] || 'driving';
    
    // Using public OSRM demo server
    const url = `https://router.project-osrm.org/route/v1/${profile}/${start};${end}?geometries=geojson&overview=full&steps=true`;
    
    return fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('OSRM failed');
            return response.json();
        })
        .then(data => {
            console.log('OSRM success:', data);
            drawOSRMRoute(data, routeType);
        });
}

function tryOpenRouteServiceWithType(routeType) {
    const start = `${startLocation.lng},${startLocation.lat}`;
    const end = `${endLocation.lng},${endLocation.lat}`;
    
    // Map route types to OpenRouteService profiles
    const profileMap = {
        'driving': 'driving-car',
        'walking': 'foot-walking',
        'cycling': 'cycling-regular'
    };
    
    const profile = profileMap[routeType] || 'driving-car';
    
    // Using public demo API (limited requests)
    const url = `https://api.openrouteservice.org/v2/directions/${profile}/geojson`;
    
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({
            coordinates: [[startLocation.lng, startLocation.lat], [endLocation.lng, endLocation.lat]],
            format: 'geojson'
        })
    })
    .then(response => {
        if (!response.ok) throw new Error('OpenRouteService failed');
        return response.json();
    })
    .then(data => {
        console.log('OpenRouteService success:', data);
        drawRealRoute(data, 'OpenRouteService', routeType);
    });
}

function tryGraphHopperWithType(routeType) {
    const start = `${startLocation.lng},${startLocation.lat}`;
    const end = `${endLocation.lng},${endLocation.lat}`;
    
    // Map route types to GraphHopper vehicles
    const vehicleMap = {
        'driving': 'car',
        'walking': 'foot',
        'cycling': 'bike'
    };
    
    const vehicle = vehicleMap[routeType] || 'car';
    
    // Using public GraphHopper API (limited requests)
    const url = `https://graphhopper.com/api/1/route?point=${startLocation.lat},${startLocation.lng}&point=${endLocation.lat},${endLocation.lng}&vehicle=${vehicle}&locale=ka&instructions=true&calc_points=true&debug=true&elevation=false&points_encoded=false`;
    
    return fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('GraphHopper failed');
            return response.json();
        })
        .then(data => {
            console.log('GraphHopper success:', data);
            drawGraphHopperRoute(data, routeType);
        });
}

function drawOSRMRoute(data, routeType) {
    if (!data.routes || data.routes.length === 0) {
        throw new Error('No routes found');
    }
    
    const route = data.routes[0];
    const coordinates = route.geometry.coordinates.map(coord => [coord[1], coord[0]]);
    
    // Color by transport mode
    const colorMap = {
        'driving': '#007bff',
        'walking': '#28a745', 
        'cycling': '#ffc107'
    };
    
    currentRoute = L.polyline(coordinates, {
        color: colorMap[routeType] || '#007bff',
        weight: 6,
        opacity: 0.8
    }).addTo(map);
    
    // Show route info
    const distance = (route.distance / 1000).toFixed(2);
    const duration = Math.round(route.duration / 60);
    
    const routeInfo = document.getElementById('route-info');
    routeInfo.innerHTML = `
        <strong>მარშრუტის ინფორმაცია (OSRM):</strong><br>
        <strong>მანძილი:</strong> ${distance} კმ<br>
        <strong>დრო:</strong> ${duration} წუთი<br>
        <strong>ტრანსპორტი:</strong> ${getTransportName(routeType)}<br>
        <small>რეალური მარშრუტი გზების მიხედვით</small>
    `;
    routeInfo.classList.remove('hidden');
    
    // Fit map to show the route
    const group = new L.featureGroup([startMarker, endMarker, currentRoute]);
    map.fitBounds(group.getBounds().pad(0.1));
}

function drawRealRoute(data, serviceName, routeType) {
    let coordinates;
    let distance, duration;
    
    if (serviceName === 'OpenRouteService') {
        coordinates = data.features[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
        const props = data.features[0].properties.segments[0];
        distance = (props.distance / 1000).toFixed(2);
        duration = Math.round(props.duration / 60);
    }
    
    // Color by transport mode
    const colorMap = {
        'driving': '#007bff',
        'walking': '#28a745', 
        'cycling': '#ffc107'
    };
    
    currentRoute = L.polyline(coordinates, {
        color: colorMap[routeType] || '#007bff',
        weight: 6,
        opacity: 0.8
    }).addTo(map);
    
    // Show route info
    const routeInfo = document.getElementById('route-info');
    routeInfo.innerHTML = `
        <strong>მარშრუტის ინფორმაცია (${serviceName}):</strong><br>
        <strong>მანძილი:</strong> ${distance} კმ<br>
        <strong>დრო:</strong> ${duration} წუთი<br>
        <strong>ტრანსპორტი:</strong> ${getTransportName(routeType)}<br>
        <small>რეალური მარშრუტი გზების მიხედვით</small>
    `;
    routeInfo.classList.remove('hidden');
    
    // Fit map to show the route
    const group = new L.featureGroup([startMarker, endMarker, currentRoute]);
    map.fitBounds(group.getBounds().pad(0.1));
}

function drawGraphHopperRoute(data, routeType) {
    if (!data.paths || data.paths.length === 0) {
        throw new Error('No paths found');
    }
    
    const path = data.paths[0];
    const coordinates = path.points.coordinates.map(coord => [coord[1], coord[0]]);
    
    // Color by transport mode
    const colorMap = {
        'driving': '#007bff',
        'walking': '#28a745', 
        'cycling': '#ffc107'
    };
    
    currentRoute = L.polyline(coordinates, {
        color: colorMap[routeType] || '#007bff',
        weight: 6,
        opacity: 0.8
    }).addTo(map);
    
    // Show route info
    const distance = (path.distance / 1000).toFixed(2);
    const duration = Math.round(path.time / 60000);
    
    const routeInfo = document.getElementById('route-info');
    routeInfo.innerHTML = `
        <strong>მარშრუტის ინფორმაცია (GraphHopper):</strong><br>
        <strong>მანძილი:</strong> ${distance} კმ<br>
        <strong>დრო:</strong> ${duration} წუთი<br>
        <strong>ტრანსპორტი:</strong> ${getTransportName(routeType)}<br>
        <small>რეალური მარშრუტი გზების მიხედვით</small>
    `;
    routeInfo.classList.remove('hidden');
    
    // Fit map to show the route
    const group = new L.featureGroup([startMarker, endMarker, currentRoute]);
    map.fitBounds(group.getBounds().pad(0.1));
}

function getTransportName(routeType) {
    const names = {
        'driving': 'მანქანა',
        'walking': 'ფეხით',
        'cycling': 'ველოსიპედით'
    };
    return names[routeType] || 'მანქანა';
}

function clearRoute() {
    if (currentRoute) {
        map.removeLayer(currentRoute);
        currentRoute = null;
    }
    
    if (startMarker) {
        map.removeLayer(startMarker);
        startMarker = null;
        startLocation = null;
    }
    
    if (endMarker) {
        map.removeLayer(endMarker);
        endMarker = null;
        endLocation = null;
    }
    
    routingMode = null;
    document.getElementById('start-location').value = '';
    document.getElementById('end-location').value = '';
    document.getElementById('route-info').classList.add('hidden');
    
    map.getContainer().style.cursor = '';
}

// Map click handler
map.on('click', function(e) {
    if (coordinatePickingMode) {
        // Handle factory coordinate picking
        coordinatePickingMode = false;
        map.getContainer().style.cursor = '';
        alert(`კოორდინატები: ${e.latlng.lat.toFixed(6)}, ${e.latlng.lng.toFixed(6)}`);
    } else if (routingMode) {
        // Handle routing point selection
        const latlng = e.latlng;
        
        if (routingMode === 'start') {
            startLocation = latlng;
            
            if (startMarker) {
                map.removeLayer(startMarker);
            }
            
            startMarker = L.marker(latlng, {
                icon: L.divIcon({
                    className: 'start-marker',
                    html: '<div style="background: #28a745; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">A</div>'
                })
            }).addTo(map);
            
            document.getElementById('start-location').value = `${latlng.lat.toFixed(6)}, ${latlng.lng.toFixed(6)}`;
            
        } else if (routingMode === 'end') {
            endLocation = latlng;
            
            if (endMarker) {
                map.removeLayer(endMarker);
            }
            
            endMarker = L.marker(latlng, {
                icon: L.divIcon({
                    className: 'end-marker',
                    html: '<div style="background: #dc3545; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">B</div>'
                })
            }).addTo(map);
            
            document.getElementById('end-location').value = `${latlng.lat.toFixed(6)}, ${latlng.lng.toFixed(6)}`;
        }
        
        routingMode = null;
        map.getContainer().style.cursor = '';
    }
});