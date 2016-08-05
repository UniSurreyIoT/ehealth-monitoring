
/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             Setup Services and Characteristics for sensor transmission
    
    FILE                     GATTSetup.cpp

    PURPOSE OF FILE          Functions to set up available services and characteristics
/*=========================================================================*/

#include <eHealth.h>

#include <Arduino.h>

#include <SPI.h>

#include <pt.h>

#include "Adafruit_BLE.h"

#include "Adafruit_BluefruitLE_SPI.h"

#include "GATTSetup.h"

#include "E_HealthConfig.h"

//Declare variables

int32_t eHealthServiceId;
int32_t pulsioximeterCharId;
int32_t ecgCharId;
int32_t emgCharId;
int32_t airflowCharId;
int32_t temperatureCharId;
int32_t bloodpressureCharId;
int32_t bodypositionCharId;
int32_t gsrCharId;
int32_t glucometerCharId;

int32_t pulsioximeterServiceId;
int32_t ecgServiceId;
int32_t ecgServiceId2;
int32_t emgServiceId;
int32_t airflowServiceId;
int32_t temperatureServiceId;
int32_t bloodPressureServiceId;
int32_t bodyPositionServiceId;
int32_t gsrServiceId;
int32_t glucometerServiceId;
int32_t additionalBoard1Id;

int32_t useSPO2CharId;
int32_t useBPMCharId;
int32_t useECGCharId;
int32_t useEMGCharId;
int32_t useAirflowCharId;
int32_t useTemperatureCharId;
int32_t useBloodPressureCharId;
int32_t useBodyPositionCharId;
int32_t useGSRCharId;
int32_t useGlucometerCharId;

int32_t spo2ValueId;
int32_t bpmValueId;
int32_t ecgValueId;
int32_t ecgValueId2;
int32_t emgValueId;
int32_t airflowValueId;
int32_t temperatureValueId;
int32_t bloodPressureValueId;
int32_t bodyPositionValueId;
int32_t gsrValueId;
int32_t glucometerValueId;
int32_t testValueId;

int32_t ecgAlphaValueId;

int32_t ecgOutputTypeId;

int32_t bpmSecondsValueId;
int32_t bpmMinutesValueId;

int32_t temperatureSampleTimeId;

int32_t temperatureMaximumId;
int32_t temperatureMinimumId;
int32_t temperatureMaxSampleIntervalId;
int32_t temperatureCompressionId;



// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}


//Define broadcast device name and service, then set avaiable characteristics

int setupGatt(){
  boolean success;
  if (! ble.sendCommandCheckOK(F("AT+GAPDEVNAME=Paediatric Monitor")) ) {
    error(F("Could not set device name"));
    return 0;
  }

  /* Add the Heart Rate Service definition */
  /* Service ID should be 1 */
  Serial.println(F("Adding the eHealth Service definition (UUID = 0x2000): "));
  success = ble.sendCommandWithIntReply(F( "AT+GATTADDSERVICE=UUID=0x2000" ), &eHealthServiceId);
  if (! success) {
    error(F("Could not add eHealth service"));
    return 2;
  }
  
  
  /* Add the pulsiximeter characteristic */
  //if available
  
  if(hasSPO2){
    Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2B00): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B00, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &pulsioximeterCharId);
    if (! success) {
      error(F("Could not add Pulsioximeter characteristic"));
      return PULSIOXIMETER;
    }
  }
  
  //not available
  
  else{
    Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2B00): "));
  
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B00, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &pulsioximeterCharId);
    if (! success) {
      error(F("Could not add Pulsioximeter characteristic"));
      return PULSIOXIMETER;
    }
  }

  
  /* Add the ECG characteristic */
  
  //if available
  
  if(hasECG){
    Serial.println(F("Adding the ECG characteristic (UUID = 0x2B01): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B01, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &ecgCharId);
    if (! success) {
      error(F("Could not add ECG characteristic"));
      return ECG;
    }
  }
  
  //not available
  
  else{
    Serial.println(F("Adding the ECG characteristic (UUID = 0x2B01): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B01, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &ecgCharId);
    if (! success) {
      error(F("Could not add ECG characteristic"));
      return ECG;
    }
  }
  
  
  /* Add the EMG characteristic */
  //if available
  if(hasEMG){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B02): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B02, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &emgCharId);
    if (! success) {
      error(F("Could not add EMG characteristic"));
      return EMG;
    }
  }

  //not available
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B02): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B02, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &emgCharId);
    if (! success) {
      error(F("Could not add EMG characteristic"));
      return EMG;
    }
  }

  /*Add the Airflow characteristic */
  //if available
  
  if(hasAirflow){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B03): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B03, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &airflowCharId);
    if (! success) {
      error(F("Could not add Airflow characteristic"));
      return AIRFLOW;
    }
  }

  //not available
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B03): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B03, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &airflowCharId);
    if (! success) {
      error(F("Could not add Airflow characteristic"));
      return AIRFLOW;
    }
  }  

  /*Add the temperature characteristic */
  //if available
  
  if(hasTemperature){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B04): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B04, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &temperatureCharId);
    if (! success) {
      error(F("Could not add Temperature characteristic"));
      return TEMPERATURE;
    }
  }

  //not available
  
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B04): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B04, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &temperatureCharId);
    if (! success) {
      error(F("Could not add Temperature characteristic"));
      return TEMPERATURE;
    }
  }
  
  
  /*Add the Bloodpressure characteristic */
  //if available
  
  if(hasBloodpressure){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B05): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B05, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &bloodpressureCharId);
    if (! success) {
      error(F("Could not add Bloodpressure characteristic"));
      return BLOODPRESSURE;
    }
  }
  
  //not available
  
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B05): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B05, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &bloodpressureCharId);
    if (! success) {
      error(F("Could not add Bloodpressure characteristic"));
      return BLOODPRESSURE;
    }
  }
  
  /*Add the bodyposition characteristic */
  //if available
  
  if(hasBodyposition){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B06): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B06, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &bodypositionCharId);
    if (! success) {
      error(F("Could not add Bodyposition characteristic"));
      return BODYPOSITION;
    }
  }
  
  //not available
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B06): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B06, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &bodypositionCharId);
    if (! success) {
      error(F("Could not add Bodyposition characteristic"));
      return BODYPOSITION;
    }
  }  

  /*Add the GSR characteristic */
  //if available
  
  if(hasGSR){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B07): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B07, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &gsrCharId);
    if (! success) {
      error(F("Could not add GSR characteristic"));
      return GSR;
    }
  }
  
  //not available
  
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B07): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B07, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &gsrCharId);
    if (! success) {
      error(F("Could not add GSR characteristic"));
      return GSR;
    }
  }

  /*Add the Glucometer characteristic */
  //if available
  
  if(hasGlucometer){
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B08): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B08, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &glucometerCharId);
    if (! success) {
      error(F("Could not add Glucometer characteristic"));
      return GLUCOMETER;
    }
  }
  
  //not available
  
  else{
    Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B08): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B08, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &glucometerCharId);
    if (! success) {
      error(F("Could not add Glucometer characteristic"));
      return GLUCOMETER;
    }
  }

  /*Add details of additional boards */
  
  Serial.println(F("Adding the eHealth characteristic (UUID = 0x2B09): "));
    success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2B09, PROPERTIES=0x02, MIN_LEN=1, MAX_LEN=20, VALUE=C6:9A:E4:10:23:93"), &additionalBoard1Id);
    if (! success) {
      error(F("Could not add additional board characteristic"));
      return 10;
    }
  
  return 1;
}


// Set up second board service and characteristic
int setupGatt2(){
  boolean success;
  if (! ble2.sendCommandCheckOK(F("AT+GAPDEVNAME=Paediatric Monitor2")) ) {
    error(F("Could not set device name2"));
    return 0;
  }

  Serial.println(F("Adding the ECG Service definition (UUID = 0x2002): "));
  success = ble2.sendCommandWithIntReply(F( "AT+GATTADDSERVICE=UUID=0x2002" ), &ecgServiceId2);
  if (! success) {
    error(F("Could not add ECG service"));
    return 2;
  }

  Serial.println(F("Adding the ECG characteristic (UUID = 0x2C11): "));
  success = ble2.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C11, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=2, VALUE=1"), &ecgValueId2);
  if (! success) {
    error(F("Could not add ECG characteristic"));
    return 3;
  }
}
  
// Setup Pulsioximeter Service

boolean setupPulsioximeterGATT(){
  boolean success;

  /* Add the Pulsioximeter Service definition */
  Serial.println(F("Adding the Pulsioximeter Service definition (UUID = 0x2001): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2001"), &pulsioximeterServiceId);
  if (! success) {
    error(F("Could not add Pulsioximeter service"));
    return 0;
  }
  
  
  /* Add the SPO2 characteristic */
  Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2C00): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C00, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=2, VALUE=1"), &spo2ValueId); // change max length to 20 for string
  if (! success) {
    error(F("Could not add Pulsioximeter characteristic"));
    return 2;
  }
  

  Serial.println(F("Adding the BPM characteristic (UUID = 0x2C01): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C01, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=2, VALUE=1"), &bpmValueId);
  if (! success) {
    error(F("Could not add BPM characteristic"));
    return 3;
  }

  Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2C02): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C02, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useSPO2CharId);
  if (! success) {
    error(F("Could not add Pulsioximeter characteristic"));
    return 3;
  }

  Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2C03): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C03, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useBPMCharId);
  if (! success) {
    error(F("Could not add Pulsioximeter characteristic"));
    return 4;
  }

  Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2C04): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C04, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &bpmSecondsValueId);
  if (! success) {
    error(F("Could not add Pulsioximeter characteristic"));
    return 4;
  }

  Serial.println(F("Adding the Pulsioximeter characteristic (UUID = 0x2C05): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C05, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &bpmMinutesValueId);
  if (! success) {
    error(F("Could not add Pulsioximeter characteristic"));
    return 4;
  }
  return 1;
}


//Setup ECG service if required
int setupECGGATT(){
  
  boolean success;
  Serial.println(F("Adding the ECG Service definition (UUID = 0x2002): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2002"), &ecgServiceId);
  if (! success) {
    error(F("Could not add ECG service"));
    return 0;
  }
  
  Serial.println(F("Adding the ECG characteristic (UUID = 0x2C10): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C10, PROPERTIES=0xA, MIN_LEN=1, MAX_LEN=1, VALUE=0"), &useECGCharId);
  if (! success) {
    error(F("Could not add ECG characteristic"));
    return 2;
  }

  Serial.println(F("Adding the ECG characteristic (UUID = 0x2C11): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C11, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=2, VALUE=1"), &ecgValueId);
  if (! success) {
    error(F("Could not add ECG characteristic"));
    return 3;
  }

  Serial.println(F("Adding the ECG characteristic (UUID = 0x2C12): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C12, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=20, VALUE=1"), &ecgAlphaValueId);
  if (! success) {
    error(F("Could not add ECG characteristic"));
    return 4;
  }

  Serial.println(F("Adding the ECG characteristic (UUID = 0x2C13): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C13, PROPERTIES=0xA, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &ecgOutputTypeId);
  if (! success) {
    error(F("Could not add ECG characteristic"));
    return 5;
  }
  
  return 1;
}


// Setup EMG service if required
int setupEMGGATT(){
  boolean success;
  Serial.println(F("Adding the EMG Service definition (UUID = 0x2002): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2C20"), &emgServiceId);
  if (! success) {
    error(F("Could not add EMG service"));
    return 0;
  }
  
  Serial.println(F("Adding the EMG characteristic (UUID = 0x2C20): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C20, PROPERTIES=0xA, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useEMGCharId);
  if (! success) {
    error(F("Could not add EMG characteristic"));
    return 2;
  }
  
  return 1;
}


//Set up Airflow service if requried
int setupAirflowGATT(){
  boolean success;
  Serial.println(F("Adding the Airflow Service definition (UUID = 0x2003): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2003"), &airflowServiceId);
  if (! success) {
    error(F("Could not add Airflow service"));
    return 0;
  }
  
  Serial.println(F("Adding the Airflow characteristic (UUID = 0x2C30): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C30, PROPERTIES=0xA, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useAirflowCharId);
  if (! success) {
    error(F("Could not add Airflow characteristic"));
    return 2;
  }
  
  Serial.println(F("Adding the Airflow characteristic (UUID = 0x2C31): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C31, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=2, VALUE=1"), &useAirflowCharId);
  if (! success) {
    error(F("Could not add Airflow characteristic"));
    return 3;
  }

  return 1;
}

//Set up temperature service if required
int setupTemperatureGATT(){
  boolean success;
  Serial.println(F("Adding the Temperature Service definition (UUID = 0x2004): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2004"), &temperatureServiceId);
  if (! success) {
    error(F("Could not add Temperature service"));
    return 0;
  }
  
  Serial.println(F("Adding the Temperature characteristic (UUID = 0x2C40): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C40, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useTemperatureCharId);
  if (! success) {
    error(F("Could not add Temperature characteristic"));
    return 2;
  }

  Serial.println(F("Adding the Temperature characteristic (UUID = 0x2C41): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C41, PROPERTIES=0x10, MIN_LEN=3, MAX_LEN=3, VALUE=1"), &temperatureValueId); //change min and max to 2
  if (! success) {
    error(F("Could not add Temperature characteristic"));
    return 3;
  }

  Serial1.println(F("Adding the Temperature characteristic (UUID = 0x2C42): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C42, PROPERTIES=0x0A, MIN_LEN=2, MAX_LEN=20, VALUE=1"), &temperatureSampleTimeId);
  if (! success) {
    error(F("Could not add Temperature Sample Time characteristic"));
    return 4;
  }

  Serial1.println(F("Adding the Temperature characteristic (UUID = 0x2C43): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C43, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=2, VALUE=1"), &temperatureMinimumId);
  if (! success) {
    error(F("Could not add Temperature Sample Time characteristic"));
    return 5;
  }

  Serial1.println(F("Adding the Temperature characteristic (UUID = 0x2C44): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C44, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=2, VALUE=1"), &temperatureMaximumId);
  if (! success) {
    error(F("Could not add Temperature Sample Time characteristic"));
    return 6;
  }
    
  Serial1.println(F("Adding the Temperature characteristic (UUID = 0x2C45): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C45, PROPERTIES=0x0A, MIN_LEN=2, MAX_LEN=20, VALUE=1"), &temperatureMaxSampleIntervalId);
  if (! success) {
    error(F("Could not add Temperature Sample Time characteristic"));
    return 7;
  }

  Serial1.println(F("Adding the Temperature characteristic (UUID = 0x2C46): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C46, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &temperatureCompressionId);
  if (! success) {
    error(F("Could not add Temperature Sample Time characteristic"));
    return 7;
  }
  return 1;
}

// Set up Blood pressure service if required
int setupBloodPressureGATT(){
  boolean success;
  Serial.println(F("Adding the BP Service definition (UUID = 0x2005): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2005"), &bloodPressureServiceId);
  if (! success) {
    error(F("Could not add BP service"));
    return 0;
  }
  
  Serial.println(F("Adding the BP characteristic (UUID = 0x2C50): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C50, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useBloodPressureCharId);
  if (! success) {
    error(F("Could not add BP characteristic"));
    return 2;
  }

  Serial.println(F("Adding the BP characteristic (UUID = 0x2C51): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C51, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=1"), &bloodPressureValueId);
  if (! success) {
    error(F("Could not add BP characteristic"));
    return 3;
  }

  return 1;
}

//Set up body Position service if required
int setupBodyPositionGATT(){
  boolean success;
  
  Serial.println(F("Adding the Position Service definition (UUID = 0x2C60): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2C60"), &bodyPositionServiceId);
  if (! success) {
    error(F("Could not add Position service"));
    return 0;
  }
  
  Serial.println(F("Adding the Position characteristic (UUID = 0x2C61): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C61, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useBodyPositionCharId);
  if (! success) {
    error(F("Could not add Position characteristic"));
    return 2;
  }

  Serial.println(F("Adding the Position characteristic (UUID = 0x2C62): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C62, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=1"), &bodyPositionValueId);
  if (! success) {
    error(F("Could not add Position characteristic"));
    return 3;
  }


  return 1;
}

//Set up GSR serivce if required
int setupGSRGATT(){
  boolean success;
  Serial.println(F("Adding the GSR Service definition (UUID = 0x2C70): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2C70"), &gsrServiceId);
  if (! success) {
    error(F("Could not add GSR service"));
    return 0;
  }
  
  Serial.println(F("Adding the GSR characteristic (UUID = 0x2C71): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C71, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useGSRCharId);
  if (! success) {
    error(F("Could not add GSR characteristic"));
    return 2;
  }

  Serial.println(F("Adding the GSR characteristic (UUID = 0x2C72): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C72, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=1"), &gsrValueId);
  if (! success) {
    error(F("Could not add GSR characteristic"));
    return 3;
  }


  return 1;
}


//Set up Glucometer service if required
int setupGlucometerGATT(){
  boolean success;
  Serial.println(("Adding the Glucometer Service definition (UUID = 0x2008): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDSERVICE=UUID=0x2008"), &glucometerServiceId);
  if (! success) {
    error(F("Could not add Glucometer service"));
    return 0;
  }
  
  
  Serial.println(("Adding the Glucometer characteristic (UUID = 0x2C80): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C80, PROPERTIES=0x0A, MIN_LEN=1, MAX_LEN=1, VALUE=1"), &useGlucometerCharId);
  if (! success) {
    error(F("Could not add Glucometer characteristic"));
    return 2;
  }

  Serial.println(("Adding the Glucometer characteristic (UUID = 0x2C81): "));
  success = ble.sendCommandWithIntReply( F("AT+GATTADDCHAR=UUID=0x2C81, PROPERTIES=0x10, MIN_LEN=2, MAX_LEN=3, VALUE=1"), &glucometerValueId);
  if (! success) {
    error(F("Could not add Glucometer characteristic"));
    return 3;
  }


  return 1;
}


// Functions for sending data to characteristics

void sendTemperatureValue(uint32_t value){
  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureValueId );
  ble.print( F(",") );
  ble.println(value);

  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

void sendECGValue(int value){
  ble.print( F("AT+GATTCHAR=") );
  ble.print( ecgValueId );
  ble.print( F(",") );
  ble.println(value);

  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

void sendECGAlpha(String value){
  ble.print( F("AT+GATTCHAR=") );
  ble.print( ecgAlphaValueId );
  ble.print( F(",") );
  ble.println(value);

  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

void sendSPO2Value(int value){
  ble.print( F("AT+GATTCHAR=") );
  ble.print( spo2ValueId );
  ble.print( F(",") );
  ble.println(value);
  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

void sendBPMValue(int value){
  
  ble.print( F("AT+GATTCHAR=") );
  ble.print( bpmValueId );
  ble.print( F(",") );
  ble.println(value);
  if ( !ble.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

void sendECGValue2(int value){
  
  ble2.print( F("AT+GATTCHAR=") );
  ble2.print( ecgValueId2 );
  ble2.print( F(",") );
  ble2.println(value);
  if ( !ble2.waitForOK() )
  {
    Serial.println(F("Failed to get response!"));
  }
}

//Setup queries for testing characteristic values

void setupuseSPO2Array(char* array){
  String useSPO2 = "AT+GATTCHAR=";
  useSPO2 += useSPO2CharId;
  Serial.print("GattValue");
  Serial.println(useSPO2);
  useSPO2.toCharArray(array,50);
}

void setupuseBPMArray(char* array){
  String useBPM = "AT+GATTCHAR=";
  useBPM += useBPMCharId;
  Serial.print("GattValue");
  Serial.println(useBPM);
  useBPM.toCharArray(array,50);

}

void setupuseECGArray(char* array){
  String useECG = "AT+GATTCHAR=";
  useECG += useECGCharId;
  useECG.toCharArray(array,50);
}


void setupuseTemperatureArray(char *array){
  String useTemperature = "AT+GATTCHAR=";
  useTemperature += useTemperatureCharId;
  Serial.print("GattValue");
  Serial.println(useTemperature);
  useTemperature.toCharArray(array,50);
}

void setupTemperatureSampleArray(char * array){
  String temperatureSample = "AT+GATTCHAR=";
  temperatureSample += temperatureSampleTimeId;
  temperatureSample.toCharArray(array,50);
}


void setupGetTemperatureDataCompression(char * array){
  String temperatureCompression = "AT+GATTCHAR=";
  temperatureCompression += temperatureCompressionId;
  temperatureCompression.toCharArray(array,50);
}

void setupGetTemperatureMaximum(char * array){
  String temperatureMaximum = "AT+GATTCHAR=";
  temperatureMaximum += temperatureMaximumId;
  temperatureMaximum.toCharArray(array,50);
}

void setupGetTemperatureMinimum(char * array){
  String temperatureMinimum = "AT+GATTCHAR=";
  temperatureMinimum += temperatureMinimumId;
  temperatureMinimum.toCharArray(array,50);
}

void setupGetTemperatureMaxSampleInt(char * array){
  String temperatureMaxSampleInt = "AT+GATTCHAR=";
  temperatureMaxSampleInt += temperatureMaxSampleIntervalId;
  temperatureMaxSampleInt.toCharArray(array,50);
}
  
void setupGetECGOutputTypeArray(char *array){
  String ecgOutputType = "AT+GATTCHAR=";
  ecgOutputType += ecgOutputTypeId;
  ecgOutputType.toCharArray(array,50);
}


//Queries for sampling details of power up
void setupSwitchOn(){
  
  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureSampleTimeId );
  ble.print( F(",") );
  ble.println(temperatureSamplePeriod);

  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureCompressionId );
  ble.print( F(",") );
  ble.println(temperatureCompressionType);

  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureMaximumId );
  ble.print( F(",") );
  ble.println(temperatureMaximum);

  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureMinimumId );
  ble.print( F(",") );
  ble.println(temperatureMinimum);

  ble.print( F("AT+GATTCHAR=") );
  ble.print( temperatureMaxSampleIntervalId );
  ble.print( F(",") );
  ble.println(temperatureMaxSampleInterval);
}

