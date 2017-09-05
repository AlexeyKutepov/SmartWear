#include <Wire.h>

int d9Pin = 9; // управляющее напряжение для регулировки температуры

void setup() {
  Wire.begin(2);                // подключаемся к шине с адресом 2
  Wire.onReceive(receiveEvent); // регистрируем событие для получения данных
  Wire.onRequest(requestEvent); // регистрируем событие для отправки данных
  Serial.begin(9600);

  pinMode(LED_BUILTIN, OUTPUT);  // светодиод, подающий признаки жизни
}

void loop() {
  digitalWrite(LED_BUILTIN, HIGH);
  delay(1000);
  digitalWrite(LED_BUILTIN, LOW);
  delay(1000);
}

void receiveEvent(int howMany) {
  // Регулировка температуры
  int inputTemp = Wire.read();
  analogWrite(d9Pin, inputTemp);
  Serial.println(inputTemp);
}

void requestEvent() {
  // отправляем внутреннюю температуру
  Wire.write("36.6");
}