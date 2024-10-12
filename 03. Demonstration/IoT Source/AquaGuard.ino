#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

#define TURBIDITY A0
#define ULTRASONIC_TRIG D6
#define ULTRASONIC_ECHO D7
#define RELAY D1

const char* SSID_ = "Astesia";
const char* PASSWORD = "abcd@1234";

const char* SERVER_URL = "http://192.168.137.1:2004/set-sensor-data";

void setup() {
  Serial.begin(9200);
  pinMode(TURBIDITY, INPUT);
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
  pinMode(RELAY, OUTPUT);

  digitalWrite(LED_BUILTIN, LOW);
  digitalWrite(ULTRASONIC_TRIG, LOW);

  turnRelay(0);
}

void loop() {
  delay(250);
  digitalWrite(LED_BUILTIN, HIGH);

  int relayStatus;
  int waterLevel = getWaterLevel();
  int waterQuality = getWaterQuality();

  if (waterLevel >= 12) {
    turnRelay(1);
    relayStatus = 1;
  } else if (waterLevel <= 1) {
    turnRelay(0);
    relayStatus = 0;
  }

  if (WiFi.status() == WL_CONNECTED) {
    WiFiClient wifi;
    HTTPClient http;
    http.begin(wifi, SERVER_URL);
    http.addHeader("Content-Type", "application/json");

    String payload = "{\"level\":" + String(waterLevel) +
                     ",\"quality\":" + String(waterQuality) +
                     ",\"relay\":" + String(relayStatus) + "}";
    Serial.print("Client request : ");
    Serial.println(payload);
    int httpResponseCode = http.POST(payload);

    if (httpResponseCode != 200) {
      for (int i = 0; i < 4; i++) {
        digitalWrite(LED_BUILTIN, LOW);
        delay(100);
        digitalWrite(LED_BUILTIN, HIGH);
        delay(100);
      }
    }

    http.end();
  } else {
    connectToWiFi();
  }

  delay(250);
  digitalWrite(LED_BUILTIN, LOW);
}

void connectToWiFi() {
  WiFi.begin(SSID_, PASSWORD);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("\nConnected to WiFi");
}

int getWaterQuality() {
  return analogRead(TURBIDITY);
}

int getWaterLevel() {
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);
  long t = pulseIn(ULTRASONIC_ECHO, HIGH);
  return (t / 2) / 29.1;
}

void turnRelay(int relayControl) {
  switch (relayControl) {
    case 0:
      Serial.println("Relay Status : OFF");
      digitalWrite(RELAY, HIGH);
      break;
    case 1:
      Serial.println("Relay Status : ON");
      digitalWrite(RELAY, LOW);
      break;
  }
}
