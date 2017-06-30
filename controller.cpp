#include <CurieBLE.h>

BLEPeripheral blePeripheral;
BLEService uartService = BLEService("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
BLECharacteristic temperatureInsideString = BLECharacteristic("6e400002-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // температура одежды
BLECharacteristic temperatureOutsideString = BLECharacteristic("6e400003-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // температура окружающей среды
BLECharacteristic batteryString = BLECharacteristic("6e400004-b5a3-f393-e0a9-e50e24dcca9e", BLENotify, 20); // от 0 до 100

BLECharacteristic mode = BLECharacteristic("6e400005-b5a3-f393-e0a9-e50e24dcca9e", BLERead | BLEWrite, 20); // 0 - ручн; 1 - авто
BLEUnsignedIntCharacteristic inputTemp = BLEUnsignedIntCharacteristic("6e400006-b5a3-f393-e0a9-e50e24dcca9e", BLERead | BLEWrite); // температура, заданная на телефоне


long previousMillis = 0;
byte buff[20];
String tempInside;
String tempOutside;
String battery;

int d9Pin = 9;

void setup () {

  Serial.begin(9600);

  Serial.println("Starting...");

  blePeripheral.setLocalName("SMART_COAT");
  blePeripheral.setAdvertisedServiceUuid(uartService.uuid());

  blePeripheral.addAttribute(uartService);
  blePeripheral.addAttribute(temperatureInsideString);
  blePeripheral.addAttribute(temperatureOutsideString);
  blePeripheral.addAttribute(batteryString);

  blePeripheral.addAttribute(mode);
  blePeripheral.addAttribute(inputTemp);

  blePeripheral.begin();

  pinMode(d9Pin, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);

  Serial.println("Setup complete");
}

void loop () {
  digitalWrite(LED_BUILTIN, HIGH);

  BLECentral central = blePeripheral.central();

  if ( central ) {
    while ( central.connected() ) {
      if (inputTemp.written()) {
        analogWrite(d9Pin, inputTemp.value());
//        Serial.println(inputTemp.value());
      }
      long currentMillis = millis();
      if ( currentMillis - previousMillis >= 3000 ) {
        previousMillis = currentMillis;
        updateTemperature();
      }
    }
  }
}

void updateTemperature () {

  memset(buff, 0x00, sizeof buff);
  tempInside = getInsideTemp();
  tempInside.getBytes(buff, 20);
  temperatureInsideString.setValue((unsigned char*)buff, 20);
  // Serial.print("Setting temperatureInsideString: ");
  // printBuff();

  memset(buff, 0x00, sizeof buff);
  tempOutside = getOutsideTemp();
  tempOutside.getBytes(buff, 20);
  temperatureOutsideString.setValue((unsigned char*)buff, 20);
  // Serial.print("Setting temperatureOutsideString: ");
  // printBuff();

  memset(buff, 0x00, sizeof buff);
  battery = getBattery();
  battery.getBytes(buff, 20);
  batteryString.setValue((unsigned char*)buff, 20);
  // Serial.print("Setting batteryString: ");
  // printBuff();
}

void printBuff() {
  for (int i = 0; i < 20; i++) {
    Serial.print(buff[i]);
  }
  Serial.println(";");
}

String getInsideTemp () {
  return (String)36.7;
}

String getOutsideTemp () {
  return (String)-15.0;
}

String getBattery () {
  return (String)75;
}