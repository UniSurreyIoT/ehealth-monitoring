/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             Setup Services and Characteristics for sensor transmission
    
    FILE                     E_healthConfig.cpp

    PURPOSE OF FILE          Function to check settings changed by Android app
/*=========================================================================*/
#include <EEPROMex.h>

#include <EEPROMVar.h>

#include <eHealth.h>

#include <Arduino.h>

#include <SPI.h>

#include <pt.h>

#include "Adafruit_BLE.h"

#include "Adafruit_BluefruitLE_SPI.h"

#include "E_healthConfig.h"

#define debug 0

int sampleTemperature = 1;
int sample;

int32_t gattInt;
int32_t gatt32;


//Check through all of the settings that can be changed by the Android app
//Remember to use modified library and swap bytes

void checkSettings(){

  int32_t currentSamplePeriod;
  
  boolean success; 

  //Check if we still want to read temperature values
  if(hasTemperature){
    success = ble.sendCommandWithIntReply(useTemperatureArray,&gattvalue); 
    if(!success){
      Serial.println(F("Failed to get response!"));
    }
    if(debug)
    {
      Serial.print("Use Temperature:");
      Serial.println(gattvalue);
    }
    if(temperatureEnabled){
      sampleTemperature = 1;
    }
    else{ 
      sampleTemperature = 0;
    }

    //store new value if different
    
    if(gattvalue != sampleTemperature){
      if(gattvalue == 1){
        temperatureEnabled = true;
      }
      else{
        temperatureEnabled = false;
      }
      EEPROM.writeInt(SAMPLETEMPERATURE, gattvalue);
    }
    
    // If we are reading temperature, check the rest of the settings

    //Check sampling interval
    if(sampleTemperature == 1){
      success = ble.sendCommandWithIntReplyBigEndian(temperatureSampleArray ,&currentSamplePeriod); 
      if(!success){
        Serial.println(F("Failed to get response!"));
      }
      if(debug){
        Serial.print("TemperatureSampleTime: ");
        Serial.println(currentSamplePeriod);
      }
      if(currentSamplePeriod != temperatureSamplePeriod){
        temperatureSamplePeriod = currentSamplePeriod;
        if(debug){
          Serial.print("TemperatureSamplePeriod: ");
          Serial.println(temperatureSamplePeriod);
        }
        EEPROM.writeLong(TEMPERATUREINTERVAL,temperatureSamplePeriod);
      }
    }

    //Check temperature compression
    success = ble.sendCommandWithIntReply(getTemperatureDataCompression,&gattInt);
    if(debug){
      Serial.print("Temperature compression");
      Serial.println(gattInt);
    }
    if(gattInt != temperatureCompressionType){
      EEPROM.writeInt(TEMPERATURECOMPRESSIONTYPE,gattInt);
      if(debug){
        Serial.print("TemperatureCompressionType: ");
        Serial.println(temperatureCompressionType);
      }
      temperatureCompressionType = gattInt;
    }

    //Check maximum temperature for adaptive compression
    success = ble.sendCommandWithIntReplyBigEndian(getTemperatureMaximum,&gattInt);
    if(debug){
      Serial.print("Temperature Max");
      Serial.println(gattInt);
    }
    if(gattInt != temperatureMaximum){
      EEPROM.writeInt(TEMPERATUREMAXIMUM,gattInt);
      if(debug){
        Serial.print("TemperatureMaximum: ");
        Serial.println(temperatureMaximum);
      }
      temperatureMaximum = gattInt;
    }

    //Check minimum temperature for adaptive compression
    success = ble.sendCommandWithIntReplyBigEndian(getTemperatureMinimum,&gattInt);
    if(debug){
      Serial.print("Temperature Min");
      Serial.println(gattInt);
    }
    if(gattInt != temperatureMinimum){
      if(debug){
        Serial.print("TemperatureMinimum: ");
        Serial.println(temperatureMinimum);
      }
      EEPROM.writeInt(TEMPERATUREMINIMUM,gattInt);
      temperatureMinimum = gattInt;
    }

    //Check maximum interval for adaptive compression
    success = ble.sendCommandWithIntReplyBigEndian(getTemperatureMaxSampleInterval, &gatt32);
    if(debug){
      Serial.print("Temperature max interval");
      Serial.println(gatt32);
    }
    
    if(gatt32 != temperatureMaxSampleInterval){
      if(debug){
        Serial.print("TemperatureMaxSample: ");
        Serial.println(temperatureMaxSampleInterval);
      }
      EEPROM.writeLong(TEMPERATUREMAXSAMPLEINTERVAL,gatt32);
      temperatureMaxSampleInterval = gatt32;
    } 
  }

  // If we have a BPM sensor
  if(hasBPM){
    
    success = ble.sendCommandWithIntReply(useBPMArray,&gattvalue); 
  
    if(success){
      
      // Check if we are using it
      if(BPMEnabled == true) {
        sample = 1;
      }
      else {
        sample = 0;
      }

      //If values different - save
      if(gattvalue != sample) {
        if(gattvalue == 1){
          BPMEnabled = true;
        }
        else{
          BPMEnabled = false;
        }
        EEPROM.writeInt(SAMPLEBPM,gattvalue);
      }
      if(debug){    
        Serial.print("Sample BPM:");
        Serial.println(gattvalue);
      }
    }
    else {
      Serial.println(F("Failed to get response!"));
    }
    
  
  }

  //If there is a SPO2 sensor, check if it should be sampled
  if(hasSPO2){
    
  
    success = ble.sendCommandWithIntReply(useSPO2Array,&gattvalue); 
    if(success){
      

      if(SPO2Enabled == true) {
        sample = 1;
      }
      else {
        sample = 0;
      }
    
      if(gattvalue != sample) {
        if(gattvalue == 1){
          SPO2Enabled = true;
        }
        else{
          SPO2Enabled = false;
        }
        EEPROM.writeInt(SAMPLEBPM,gattvalue);
      }
      if(debug){
        Serial.print("Sample BPM:");
        Serial.println(gattvalue);
      }
    }
    else {
      Serial.println(F("Failed to get response!"));
    }
  }

  //If there is an ECG sensor, check if it should be sampled
  if(hasECG){
    success = ble.sendCommandWithIntReply(useECGArray,&gattvalue); 
    
    if(success){
      

      if(ECGEnabled == true) {
        sample = 1;
      }
      else {
        sample = 0;
      }
    
      if(gattvalue != sample) {
        if(gattvalue == 1){
          ECGEnabled = true;
        }
        else{
          ECGEnabled = false;
        }
        EEPROM.writeInt(SAMPLEECG,gattvalue);
      }
      if(debug){
        Serial.print("ECG Gatt VALUE:");
        Serial.println(gattvalue);
      }
    }
    else {
      Serial.println(F("Failed to get response!"));
    }

    //Check sampling type for ECG sensor
    success = ble.sendCommandWithIntReply(ecgOutputTypeArray,&gattvalue);
    if(gattvalue != ecgOutputType){
      ecgOutputType = gattvalue;
      EEPROM.writeInt(ECGOUTPUTTYPE,gattvalue);
    }
    
  }
}
