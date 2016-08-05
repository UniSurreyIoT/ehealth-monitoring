/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             Setup Services and Characteristics for sensor transmission
    
    FILE                     e_healthgatt.ino

    PURPOSE OF FILE          Contains main setup and loop of Arduino program
    
    APPLICATION SETTINGS

    FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
   
                              Enabling this will put your Bluefruit LE module
                              in a 'known good' state and clear any config
                              data set in previous sketches or projects, so
                              running this at least once is a good idea.
   
                              When deploying your project, however, you will
                              want to disable factory reset by setting this
                              value to 0.  If you are making changes to your
                              Bluefruit LE device via AT commands, and those
                              changes aren't persisting across resets, this
                              is the reason why.  Factory reset will erase
                              the non-volatile memory where config data is
                              stored, setting it back to factory default
                              values.
       
                              Some sketches that require you to bond to a
                              central device (HID mouse, keyboard, etc.)
                              won't work at all with this feature enabled
                              since the factory reset will clear all of the
                              bonding data stored on the chip, meaning the
                              central device won't be able to reconnect.
    MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
    MODE_LED_BEHAVIOUR        LED activity, valid options are
                              "DISABLE" or "MODE" or "BLEUART" or
                              "HWUART"  or "SPI"  or "MANUAL"
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         1
    #define SENSOR_RESET                0
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

//Include Libraries and headers

#include <PinChangeInt.h>

#include <PinChangeIntConfig.h>

#include <EEPROMex.h>

#include <EEPROMVar.h>

#include <eHealth.h>

#include <Arduino.h>

#include <SPI.h>

#include <pt.h>

#include "Adafruit_BLE.h"

#include "Adafruit_BluefruitLE_SPI.h"

#include "BluefruitConfig.h"

#include "E_healthConfig.h"

#include "Sampling.h"

#include "GATTSetup.h"

#include "compression.h"


// Create the bluefruit objects, two boards in this instance

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

Adafruit_BluefruitLE_SPI ble2(BLUEFRUIT_SPI_CS2, BLUEFRUIT_SPI_IRQ2, BLUEFRUIT_SPI_RST2);


//Declare protothread structures

static struct pt pt1;
static struct pt temperaturePT;
static struct pt checkChangesPT;
static struct pt ecgPT;
static struct pt bpmPT;
static struct pt adaptiveSendPT;
static struct pt ecgPT2;
static struct pt spo2PT;

//Declare variables

int32_t temperatureSampleTime;

int sampleItems;
int temperatureArraySize2 = 0;
int temperatureSmoothingSampleSize2 = 5;
int temperatureArraySize = 0;
int temperatureSmoothingSampleSize = 10;
int ecgSamples[180];
int ecgSampleNo = 0;
int ecgSamplesWorking[180];
int *samplesPointer = ecgSamples;
int currentTemperature;
int previousECG = 0;
int transmitter = 0;
int checkChangesInterval = 2000;
int arrayPointer = 0;
int type = 1;

String spo2GattValue;
String useBPM,useTemperature;
String temperatureSample;

boolean setup_mode = false;
boolean temperatureEnabled;
boolean BPMEnabled;
boolean SPO2Enabled;
boolean ECGEnabled;
boolean testConnection;

char useSPO2Array[50],useBPMArray[50],useTemperatureArray[50];
char temperatureSampleArray[50];
char getTemperatureDataCompression[50];
char getTemperatureMaximum[50];
char getTemperatureMinimum[50];
char getTemperatureMaxSampleInterval[50];
char useECGArray[50];
char ecgOutputTypeArray[50];
char incomingData[30];

float temperatureSmoothingArray[50];
float temperatureResult;
float temperatureResult2;
float *floatpt = temperatureSmoothingArray;
float temperaturePaxArray[50];
float *floatpt2 = temperaturePaxArray;
float value;

int32_t temperatureSamplePeriod;
int32_t temperatureCompressionType;
int32_t temperatureMaximum;
int32_t temperatureMinimum;
int32_t temperatureMaxSampleInterval;
int32_t currentTemperatureSamplePeriod;
int32_t ecgOutputType = 0;
int32_t testAdaptive;
int32_t testStandard = 2;
int32_t testMaxSample = 40;
int32_t testMaxValue = 3000;
int32_t testMinValue = 2500;
int32_t adaptiveSendTime;
int32_t gattvalue;

static boolean ecgFlag = false;

//Create byte checksum from two bytes of value
uint32_t addChecksum(int value){
  int a = value & 0xff;
  int b = (value >> 8) & 0xff;
  int checksum = a ^ b;
  uint32_t result;
  result = (uint32_t) value;
  result = (result << 8) | checksum;
  return result;
}


//Move working values to array to allow data collection to continue
void store(int f){
    if(ecgSampleNo < 180){
      ecgSamples[ecgSampleNo] = f;
      ecgSampleNo++;
      ecgFlag = false;
    }
    else{
      memcpy(ecgSamplesWorking,ecgSamples,160);
      ecgSampleNo = 0;
      ecgFlag = true;
    }
}

//Function to send SAX data from ECG
void setECGFlag(){
  ecgFlag = false;
  String i = calculate_mean(samplesPointer);
  //sendECGAlpha(i);
  //Serial.print("String: ");
  //Serial.println(i);
}

//Setup each protothread routine

//Temperature sampling thread
int temperatureThread(struct pt *pt, long interval) {
  static unsigned long timestamp = 0;
  static unsigned long starttime = 0;
  static unsigned long endtime = 0;
  uint32_t i = 0;
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    starttime = micros();

    //Sample temperature
    float valueFloat = sampleTemperature();

    //Convert to integer
    int valueInt = (int) (valueFloat*100);
  
    currentTemperature = valueInt;

    //No compression
    if(temperatureCompressionType == 1){
      i = addChecksum(valueInt);
      endtime=micros();
      sendTemperatureValue(i);
      temperatureSampleTime = temperatureSamplePeriod;
    }

    //Averaging
    else if (temperatureCompressionType == 2){
      Serial.println("Smoothing");
      smoothingAlgorithm(&temperatureResult,floatpt,&temperatureArraySize,&temperatureSmoothingSampleSize, &valueFloat);
      i = addChecksum((int)(temperatureResult*100));
      //sendTemperatureValue(valueInt);
      sendTemperatureValue(i);
      temperatureSampleTime = temperatureSamplePeriod;
    }

    //PAX
    else if (temperatureCompressionType == 3){
      Serial.println("PAX");
      boolean sendValue = PAXAlgorithm(&temperatureResult2,floatpt2,&temperatureArraySize2, &temperatureSmoothingSampleSize2, &valueFloat);
      if(sendValue){
        i = addChecksum((int)(temperatureResult2*100));
        sendTemperatureValue(i);
        //sendTemperatureValue((int) (temperatureResult2*100));  
      }
      temperatureSampleTime = temperatureSamplePeriod;
    }

    //SAX
    else if (temperatureCompressionType == 4){
      testAdaptive = adaptiveSampling(&testAdaptive, &temperatureSamplePeriod, &temperatureMaxSampleInterval, &temperatureMaximum, &temperatureMinimum, &valueInt); 
      adaptiveSendTime = testAdaptive;
    }
    Serial.print("Start time: ");
    Serial.println(starttime);
    Serial.print("End time: ");
    Serial.println(endtime);
  }
  PT_END(pt);
}

//Change checking thread
int checkChangesThread(struct pt *pt, int interval){
  static unsigned long timestamp = 0;
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    checkSettings();
  }
  PT_END(pt);
}


//ECG sampling thread
int ecgThread(struct pt *pt, int interval){
  static unsigned long timestamp = 0;
  
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    int f = (int) (sampleECG() *100);
    
    store(f);

    //Swap between transmitters
    if(ecgOutputType == 0){
      if(transmitter == 0){
        sendECGValue(f);
        transmitter = 1;
      }
      else{
        sendECGValue2(f);
        transmitter = 0;
      }
    }
  }
  PT_END(pt);
}

//BPM sampling thread
int bpmThread(struct pt *pt, int interval){
  static unsigned long timestamp = 0;
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    int f = sampleBPM();
    Serial.print("BPM: ");
    Serial.println(f);
    sendBPMValue(f*100);
  }
  PT_END(pt);
}


//SPO2 sampling thread
int spo2Thread(struct pt *pt, int interval){
  static unsigned long timestamp = 0;
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    int f = samplePulse();
    Serial.print("SPO2: ");
    Serial.println(f);
    sendSPO2Value(f*100);
  }
  PT_END(pt);
}


//Adaptive thread for temperature sampling
int adaptiveSendThread(struct pt *pt, int interval){
  static unsigned long timestamp = 0;
  PT_BEGIN(pt);
  while(1){
    PT_WAIT_UNTIL(pt,millis() - timestamp > interval );
    timestamp = millis();
    uint32_t i = addChecksum(currentTemperature);
    sendTemperatureValue(i);
    //sendTemperatureValue(currentTemperature);
  }
  PT_END(pt);
}

/**************************************************************************/
/*!
    Sets up the HW an the BLE module and define stored settings
*/
/**************************************************************************/

void setup(void)
{
  
  Serial.begin(115200);
  Serial.println(F("eHealth Setup"));
  Serial.println(F("------------------------------------------------"));
  
  
  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }

  Serial.println( F("OK!") );

  /* Initialise second modle */
  
  if ( !ble2.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit2, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK2!") );
  
  /* Reset the Bluetooth Boards to clear Services and characteristics */
  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset2: "));
    if ( ! ble2.factoryReset() ){
      error(F("Couldn't factory reset2"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  ble2.echo(false);

  Serial.println("Requesting Bluefruit info:");

  /* Print Bluefruit information */
  ble.info();

  Serial.println("Requesting Bluefruit board 2 info:");

  ble2.info();

  ble.verbose(false);  // debug info is a little annoying after this point!
  ble2.verbose(false);
  
  Serial.println(F("******************************"));

  // LED Activity command is only supported from 0.6.6
  
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  if ( ble2.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
     //Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble2.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  Serial.println(F("******************************"));

  // Initialise the protothreads
  
  PT_INIT(&temperaturePT);
  PT_INIT(&checkChangesPT);
  PT_INIT(&ecgPT);
  PT_INIT(&bpmPT);
  PT_INIT(&adaptiveSendPT);
  PT_INIT(&ecgPT2);
  PT_INIT(&spo2PT);
  
  
  // Setup the eHealth GATT services and characteristics
  setupGatt();

  delay(300);

  //Setup the sensor GATT services and charactersitics if required
  if(hasBPM | hasSPO2){
    setupPulsioximeterGATT();
  }
  
  if(hasTemperature){
    setupTemperatureGATT();
  }
  
  if(hasECG){
    setupECGGATT();
  }

  
  delay(300);

  // Services only work after board reset
  ble.reset();
  
  delay(300);

  // Setup second BLE board services and characteristics
  setupGatt2();
  
  ble2.reset();



  // If we want to clear the settings
  if(SENSOR_RESET)
    EEPROM.write(FIRSTUSE,1);
  
  // If it is the first use of the board
  if(EEPROM.read(FIRSTUSE) == 1){
    Serial.println("In first use");
    
    //Write default values to the settings
    EEPROM.writeLong(TEMPERATUREINTERVAL,1);
    EEPROM.writeInt(SAMPLETEMPERATURE,1);
    EEPROM.writeInt(TEMPERATURECOMPRESSIONTYPE,1);
    EEPROM.writeInt(TEMPERATUREMAXIMUM,1);
    EEPROM.writeInt(TEMPERATUREMINIMUM,1);
    EEPROM.writeLong(TEMPERATUREMAXSAMPLEINTERVAL,1);
    EEPROM.write(FIRSTUSE,0);
    
    temperatureEnabled = true;
    temperatureCompressionType = 1;
    temperatureSamplePeriod = 1;
    temperatureMaximum = 1;
    temperatureMinimum = 1;
    temperatureMaxSampleInterval = 1;
  }

  // If it's not the first use
  else{
    Serial.println("Not in first use");
    
    //Check if we want to use the BPM sensor
    int useType = EEPROM.readInt(SAMPLEBPM);
    
    if(useType == 0){
      BPMEnabled = false;
    }
    
    else {
      BPMEnabled = true;
    }
    
    //Check if we want to use the temperature sensor
    useType = EEPROM.readInt(SAMPLETEMPERATURE);
    if(useType == 0){
      temperatureEnabled = false;
    }
    else{
      temperatureEnabled = true; 
    }

    //Check if we want to use the ECG sensor
    useType = EEPROM.readInt(SAMPLEECG);
    if(useType == 0){
      ECGEnabled = false;
    }
    else{
      ECGEnabled = true;
    }


    // Read the sampling variables and setup local variables with the values
    temperatureSamplePeriod = EEPROM.readLong(TEMPERATUREINTERVAL);
    Serial.println(temperatureSamplePeriod);
    
    temperatureSampleTime = temperatureSamplePeriod;
    
    adaptiveSendTime = temperatureSamplePeriod;
    
    temperatureCompressionType = (long) EEPROM.readInt(TEMPERATURECOMPRESSIONTYPE);
    Serial.println(temperatureCompressionType);
    
    temperatureMaximum = (long) EEPROM.readInt(TEMPERATUREMAXIMUM);
    Serial.println(temperatureMaximum);
    
    temperatureMinimum = (long) EEPROM.readInt(TEMPERATUREMINIMUM);
    Serial.println(temperatureMinimum);
    
    temperatureMaxSampleInterval = EEPROM.readLong(TEMPERATUREMAXSAMPLEINTERVAL);
    Serial.println(temperatureMaxSampleInterval);

    ecgOutputType = EEPROM.readInt(ECGOUTPUTTYPE);
    Serial.println(ecgOutputType);

    // Set the characteristics to these variable values so that the Android device can read them
    
    setupSwitchOn();
  
  }

  // Wait until there is a connection
  
  while (! ble.isConnected()) {
      delay(500);
  }

  // Setup the strings for reading the characteristics
  
  char *SPO2PT = useSPO2Array;
  setupuseSPO2Array(SPO2PT);

  char *BPMPT = useBPMArray;
  setupuseBPMArray(BPMPT);
  
  char * temperaturePT = useTemperatureArray;
  setupuseTemperatureArray(temperaturePT);

  char * temperatureSamplePT = temperatureSampleArray;
  setupTemperatureSampleArray(temperatureSamplePT);

  char * temperatureCompressionPT = getTemperatureDataCompression;
  setupGetTemperatureDataCompression(temperatureCompressionPT);

  char * temperatureMaxPT = getTemperatureMaximum;
  setupGetTemperatureMaximum(temperatureMaxPT);

  char * temperatureMinPT = getTemperatureMinimum;
  setupGetTemperatureMinimum(temperatureMinPT);

  char * temperatureMaxSampleIntPT = getTemperatureMaxSampleInterval;
  setupGetTemperatureMaxSampleInt(temperatureMaxSampleIntPT);

  char * ECGPT = useECGArray;
  setupuseECGArray(ECGPT);

  char * ecgOutputTypePT = ecgOutputTypeArray;
  setupGetECGOutputTypeArray(ecgOutputTypePT);
  
  
  // Use the supplies library to setup the Pulsioximeter
  eHealth.initPulsioximeter();

  //Attach the inttruptions for using the pulsioximeter.
  attachInterrupt(digitalPinToInterrupt(18), readPulsioximeter, RISING);

  //Wait 
  delay(10000);

}


/**************************************************************************/
/*!
    @brief  Constantly initialise the protothreads for sampling
*/
/**************************************************************************/

void loop(void){
  
    //Check for any settings changes
    checkChangesThread(&checkChangesPT,checkChangesInterval);

    //Check if sending SAX data
    if(ecgFlag){
      ecgFlag = false;
      if(ecgOutputType == 1){
        setECGFlag();
      }
    }

    //Check if ECG sampling enabled if it exists
    if(hasECG){
      if(ECGEnabled){
        if(ecgOutputType == 1){
          ecgThread(&ecgPT,4);
        }
        else{
          ecgThread(&ecgPT,7);
        }
          
      }
    }

    //Check if BPM sampling enabled if it exists
    if(hasBPM){
      if(BPMEnabled){ 
        bpmThread(&bpmPT,2000);
      }
    }

    //Check if temperature sampling enable if it exists
    if(hasTemperature){
      if(temperatureEnabled){
        temperatureThread(&temperaturePT,temperatureSampleTime*1000); 
        if(temperatureCompressionType == 4){
          adaptiveSendThread(&adaptiveSendPT,adaptiveSendTime*1000);
        }
      }
    }

    //Check if SPO2 sampling enabled if it exists
    if(hasSPO2){
      if(SPO2Enabled){
        spo2Thread(&spo2PT,2000);
      }
    }

    delay(1);
 
}


int cont = 0;

//Read Pulsioximeter
void readPulsioximeter(){

  cont ++;
  //Serial.println("Interrupt works");
  if (cont == 50) { //Get only of one 50 measures to reduce the latency
    eHealth.readPulsioximeter();
    cont = 0;
  }
}




                
