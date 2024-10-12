const endpoint = 'http://192.168.137.1:2004/get-sensor-data';

function updateTank(level, quality, relay) {
    const waterLevel = document.getElementById('water-level');
    const levelDisplay = document.getElementById('level-display');
    const qualityDisplay = document.getElementById('quality-display');
    const relayDisplay = document.getElementById('relay-display');

    waterLevel.style.height = `${level}%`;

    let waterColor;
    if (quality > 90) {
        waterColor = 'blue';
    } else if (quality > 80) {
        waterColor = 'orange';
    } else {
        waterColor = 'red';
    }
    waterLevel.style.backgroundColor = waterColor;

    levelDisplay.textContent = level;
    qualityDisplay.textContent = quality;
    if (relay === 1) {
        relayDisplay.textContent = 'ON';
    } else {
        relayDisplay.textContent = 'OFF';
    }
}

function getSensorData() {
    fetch(endpoint)
        .then(response => response.json())
        .then((data) => {
            console.log(data);
            const {level, quality, relay} = data;
            updateTank(level, quality, relay);
        })
        .catch(error => console.error('Error fetching sensor data:', error));
}

setInterval(getSensorData, 500);
