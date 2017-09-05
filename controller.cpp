#include <CurieBLE.h>
#include <BME280.h>
#include <Wire.h>

BLEPeripheral blePeripheral;
BLEService uartService = BLEService("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
BLECharacteristic temperatureInsideString = BLECharacteristic("6e400002-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // температура одежды
BLECharacteristic temperatureOutsideString = BLECharacteristic("6e400003-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // температура окружающей среды
BLECharacteristic batteryString = BLECharacteristic("6e400004-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // от 0 до 100

BLECharacteristic mode = BLECharacteristic("6e400005-b5a3-f393-e0a9-e50e24dcca9e", BLERead | BLEWrite, 20); // 0 - ручн; 1 - авто
BLEUnsignedIntCharacteristic inputTemp = BLEUnsignedIntCharacteristic("6e400006-b5a3-f393-e0a9-e50e24dcca9e", BLERead | BLEWrite); // температура, заданная на телефоне

BME280 bme;
bool isBmeDetected = false;
bool metric = false;
float temp(NAN), hum(NAN), pres(NAN);
uint8_t pressureUnit(3);

long previousMillis = 0;
byte buff[20];
String tempInside;
String tempOutside;
boolean isLedOn = false;

int d9Pin = 9;

void setup () {
  Wire.begin();
  Serial.begin(9600);
  blePeripheral.setLocalName("SMART_COAT");
  blePeripheral.setAdvertisedServiceUuid(uartService.uuid());

  blePeripheral.addAttribute(uartService);
  blePeripheral.addAttribute(temperatureInsideString);
  blePeripheral.addAttribute(temperatureOutsideString);
  blePeripheral.addAttribute(batteryString);

  blePeripheral.addAttribute(mode);
  blePeripheral.addAttribute(inputTemp);

  blePeripheral.begin();

  if (bme.begin()) {
    isBmeDetected = true;
  }

  pinMode(d9Pin, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop () {
  digitalWrite(LED_BUILTIN, HIGH);
  isLedOn = true;
  BLECentral central = blePeripheral.central();
  if ( central ) {
    while ( central.connected() ) {
      if (inputTemp.written()) {
        Wire.beginTransmission(2); // подключаемся к плате №2
        Wire.write(inputTemp.value()); // передаём температуру
        Wire.endTransmission();    // отключаемся
        Serial.println(inputTemp.value());
      }
      long currentMillis = millis();
      if ( currentMillis - previousMillis >= 5000 ) {
        if (isLedOn) {
          digitalWrite(LED_BUILTIN, LOW);
          isLedOn = false;
        } else {
          digitalWrite(LED_BUILTIN, HIGH);
          isLedOn = true;
        }

        updateBatteryLevel();

        updateTemperature();

        previousMillis = currentMillis;
      }
    }
  }
}

void updateTemperature () {

  memset(buff, 0x00, sizeof buff);
  tempInside = getInsideTemp();
  tempInside.getBytes(buff, 20);
  temperatureInsideString.setValue((unsigned char*)buff, 20);

  if (isBmeDetected) {
    bme.ReadData(pres, temp, hum, metric, pressureUnit);

    memset(buff, 0x00, sizeof buff);
    tempOutside = String((temp - 32) * 5 / 9, DEC);
    tempOutside.getBytes(buff, 20);
    temperatureOutsideString.setValue((unsigned char*)buff, 20);
  }
}

void printBuff() {
  for (int i = 0; i < 20; i++) {
    Serial.print(buff[i]);
  }
  Serial.println(";");
}

String getInsideTemp () {
  Wire.requestFrom(2, 4);    // получить 4 байта с устройства #2

  String insideTemp = "";
  while (Wire.available()) {
    char c = Wire.read();
    insideTemp += c;
    Serial.print(c);
  }
  return insideTemp;
}

/**
 * Актуализировать заряд аккумулятора
 */
void updateBatteryLevel() {
  int battery = analogRead(A0);
  int batteryLevel = map(battery, 700, 1023, 0, 100);
  Serial.print("Battery Level % is now: "); // print it
  Serial.println(batteryLevel);
  memset(buff, 0x00, sizeof buff);
  ((String )batteryLevel).getBytes(buff, 20);
  batteryString.setValue((unsigned char*)buff, 20);
}