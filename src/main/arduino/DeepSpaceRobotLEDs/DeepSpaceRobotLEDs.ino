#include <FastLED.h>

#define NUM_LEDS 7
#define DATA_PIN 12

#define RED_PIN 5
#define GREEN_PIN 6
#define BLUE_PIN 7

#define ONBOARD_LED 13

#define CHIPSET WS2811
#define COLOR_ORDER RGB
#define BRIGHTNESS 150 // 96 to 200

CRGB leds[NUM_LEDS];

void setup() { 
  FastLED.addLeds<CHIPSET, DATA_PIN, COLOR_ORDER>(leds, NUM_LEDS).setCorrection( TypicalLEDStrip );
  FastLED.setBrightness( BRIGHTNESS );
  randomSeed(analogRead(0));
  Serial.begin(9600);
  pinMode(RED_PIN, INPUT);
  pinMode(GREEN_PIN, INPUT);
  pinMode(BLUE_PIN, INPUT);
  pinMode(ONBOARD_LED, OUTPUT);
}

void loop() {
  CRGB color(digitalRead(RED_PIN)*255, digitalRead(GREEN_PIN)*255, digitalRead(BLUE_PIN)*255);
  allColor(color);
  delay(33);

  // just so we know it's running...
  static bool ledState = false;
  ledState ^= true;
  digitalWrite(ONBOARD_LED, ledState);
}

// Changes all LEDS to given color
void allColor(CRGB c){
  for(int i=0; i<NUM_LEDS; i++){
    leds[i] = c;
  }
  FastLED.show();
}


