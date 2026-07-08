#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <ArduinoJson.h>
#include <time.h>


const char *WIFI_SSID = "";
const char *WIFI_PASS = "";

const char *NTP_SERVER = "pool.ntp.org";
const long GMT_OFFSET_SEC = 2 * 3600;
const int DAYLIGHT_OFFSET_SEC = 3600;


const char *MQTT_HOST = "192.168.0.43";
const uint16_t MQTT_PORT = 1883;

const char *MQTT_USER = nullptr;
const char *MQTT_PASS = nullptr;

const char *DEVICE_ID = "esp32-living";
const char *TOPIC_TELEMETRY = "smarthome/sensors/esp32-living/dht22";

const char *TOPIC_FLOWER_POWER = "smarthome/actuators/esp32-living/flower/power";
const char *TOPIC_FLOWER_BRIGHTNESS = "smarthome/actuators/esp32-living/flower/brightness";

// DHT22
#define DHTPIN 4
#define DHTTYPE DHT22

#define LED_PIN 2
const unsigned long LED_ON_MS = 2000;
bool ledIsOn = false;
unsigned long ledOffAtMs = 0;


const int FLOWER_LED_PIN = 5;
const int PWM_CH = 0;
const int PWM_FREQ = 5000;
const int PWM_RES_BITS = 8;
int flowerBrightness = 0;
bool flowerPower = false;


const unsigned long PUBLISH_INTERVAL_MS = 5000;


const float TEMP_EPS = 0.1f;
const float HUM_EPS = 0.5f;


WiFiClient espClient;
PubSubClient mqtt(espClient);
DHT dht(DHTPIN, DHTTYPE);

unsigned long lastPublishMs = 0;
bool hasLast = false;
float lastT = NAN, lastH = NAN;

static void applyFlowerOutput()
{

    int duty = flowerPower ? flowerBrightness : 0;
    duty = constrain(duty, 0, 255);
    ledcWrite(PWM_CH, duty);

    Serial.print("[FLOWER] power=");
    Serial.print(flowerPower ? "on" : "off");
    Serial.print(" brightness=");
    Serial.print(flowerBrightness);
    Serial.print(" duty=");
    Serial.println(duty);
}

void mqttCallback(char *topic, byte *payload, unsigned int length)
{
    String msg;
    msg.reserve(length + 1);
    for (unsigned int i = 0; i < length; ++i)
        msg += (char)payload[i];
    msg.trim();

    Serial.print("[MQTT] topic=");
    Serial.print(topic);
    Serial.print(" payload=");
    Serial.println(msg);

    if (strcmp(topic, TOPIC_FLOWER_POWER) == 0)
    {
        if (msg.equalsIgnoreCase("on"))
        {
            flowerPower = true;

            applyFlowerOutput();
        }
        else if (msg.equalsIgnoreCase("off"))
        {
            flowerPower = false;
            applyFlowerOutput();
        }
    }
    else if (strcmp(topic, TOPIC_FLOWER_BRIGHTNESS) == 0)
    {
        int val = msg.toInt(); // 0..255
        val = constrain(val, 0, 255);
        flowerBrightness = val;

        if (flowerBrightness > 0)
            flowerPower = true;

        applyFlowerOutput();
    }
}

void connectWiFi()
{
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);

    Serial.print("WiFi connecting");
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);
        Serial.print(".");
    }
    Serial.println();
    Serial.print("WiFi connected, IP=");
    Serial.println(WiFi.localIP());
}

void subscribeCommandTopics()
{

    bool ok1 = mqtt.subscribe(TOPIC_FLOWER_POWER);
    bool ok2 = mqtt.subscribe(TOPIC_FLOWER_BRIGHTNESS);

    Serial.print("Subscribe ");
    Serial.print(TOPIC_FLOWER_POWER);
    Serial.print(" => ");
    Serial.println(ok1 ? "OK" : "FAIL");

    Serial.print("Subscribe ");
    Serial.print(TOPIC_FLOWER_BRIGHTNESS);
    Serial.print(" => ");
    Serial.println(ok2 ? "OK" : "FAIL");
}

void connectMQTT()
{
    mqtt.setServer(MQTT_HOST, MQTT_PORT);
    mqtt.setCallback(mqttCallback);

    while (!mqtt.connected())
    {
        String clientId = String("esp32-") + DEVICE_ID + "-" + String((uint32_t)ESP.getEfuseMac(), HEX);
        Serial.print("MQTT connecting as ");
        Serial.print(clientId);
        Serial.print(" ... ");

        bool ok;
        if (MQTT_USER && MQTT_PASS)
        {
            ok = mqtt.connect(clientId.c_str(), MQTT_USER, MQTT_PASS);
        }
        else
        {
            ok = mqtt.connect(clientId.c_str());
        }

        if (ok)
        {
            Serial.println("connected.");
            subscribeCommandTopics();
        }
        else
        {
            Serial.print("failed, rc=");
            Serial.print(mqtt.state());
            Serial.println(" retry in 2s");
            delay(2000);
        }
    }
}

bool shouldPublish(float t, float h)
{
    if (!hasLast)
        return true;
    if (isnan(t) || isnan(h))
        return false;

    if (fabs(t - lastT) >= TEMP_EPS)
        return true;
    if (fabs(h - lastH) >= HUM_EPS)
        return true;

    return true;
}

void publishTelemetry(float t, float h)
{
    time_t now;
    time(&now);

    uint64_t epochMillis = (uint64_t)now * 1000ULL;

    StaticJsonDocument<256> doc;
    doc["deviceId"] = DEVICE_ID;
    doc["sensor"] = "dht22";
    doc["temperatureC"] = t;
    doc["humidityPct"] = h;
    doc["ts"] = epochMillis;

    char payload[256];
    size_t n = serializeJson(doc, payload, sizeof(payload));

    bool ok = mqtt.publish(TOPIC_TELEMETRY, payload, n);
    Serial.print("Publish ");
    Serial.print(TOPIC_TELEMETRY);
    Serial.print(" => ");
    Serial.println(ok ? "OK" : "FAIL");

    Serial.println(payload);

    lastT = t;
    lastH = h;
    hasLast = true;
}

void initTime()
{
    configTime(GMT_OFFSET_SEC, DAYLIGHT_OFFSET_SEC, NTP_SERVER);

    Serial.print("Syncing time");
    time_t now;
    int retry = 0;
    const int retry_count = 15;

    while (retry < retry_count)
    {
        time(&now);
        if (now > 1700000000)
        {
            Serial.println("\nTime synced!");
            return;
        }
        Serial.print(".");
        delay(500);
        retry++;
    }

    Serial.println("\nFailed to sync time (NTP)");
}

void setup()
{
    Serial.begin(115200);

    pinMode(LED_PIN, OUTPUT);
    digitalWrite(LED_PIN, LOW);
    delay(500);

    pinMode(FLOWER_LED_PIN, OUTPUT);
    ledcSetup(PWM_CH, PWM_FREQ, PWM_RES_BITS);
    ledcAttachPin(FLOWER_LED_PIN, PWM_CH);
    flowerPower = true;
    flowerBrightness = 100;
    applyFlowerOutput();

    dht.begin();
    connectWiFi();
    initTime();
    connectMQTT();
}

void loop()
{
    if (WiFi.status() != WL_CONNECTED)
    {
        connectWiFi();
    }
    if (!mqtt.connected())
    {
        connectMQTT();
    }
    mqtt.loop();

    if (ledIsOn && (long)(millis() - ledOffAtMs) >= 0)
    {
        digitalWrite(LED_PIN, LOW);
        ledIsOn = false;
    }

    unsigned long now = millis();
    if (now - lastPublishMs >= PUBLISH_INTERVAL_MS)
    {
        lastPublishMs = now;

        float h = dht.readHumidity();
        float t = dht.readTemperature();

        if (isnan(t) || isnan(h))
        {
            Serial.println("DHT read failed (NaN).");
            return;
        }

        if (shouldPublish(t, h))
        {
            digitalWrite(LED_PIN, HIGH);
            ledIsOn = true;
            ledOffAtMs = millis() + LED_ON_MS;
            publishTelemetry(t, trunc(h));
        }
    }
}
