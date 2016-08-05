/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             
    
    FILE                     Compression.cpp

    PURPOSE OF FILE          SAX compression for ECG sensor
                             Smoothing algorithm for temperature
/*=========================================================================*/

#include <Arduino.h>
#include "Adafruit_BLE.h"

#include "Adafruit_BluefruitLE_SPI.h"
#include "GATTSetup.h"

//Debugging output
#define testoutput 0

//Function for calculating SAX alpha string
//This implementation uses an 8 symbol alphabet
//Segment of 180 samples 

String calculate_mean(int *p){
  //Define variables
  int counter;
  float diff_sqr[180];
  long total = 0;
  int value;

  //Find sum of values 
  //Limit sensor value to 250
  
  for(counter = 0; counter < 180 ; counter++){
    value = *(p + counter);
    if(value > 250){
      value = 250;
    }
    total += value;
    if(testoutput){
      Serial.println(*(p + counter));
    }
  }

  //Find mean
  float mean = (float)(total/180);  
  total = 0;

  //Find sum of difference from mean 
  //Sensor value limited to 250
  
  for(counter = 0; counter < 180; counter++){
    value = *(p + counter);
    if(value > 250){
      value = 250;
    }
    diff_sqr[counter] = sq(value - mean);
    total += diff_sqr[counter];
  }

  //Find variance and standard deviation
  float variance = (float) total/180;
  float std_dev = sqrt(variance);
  
  float normalised[180];
  int counter2 = 0;

  //Find array of normalised values
  for(counter = 0; counter < 180; counter ++){
    value = *(p + counter);
    normalised[counter2] = (float) ((float)(value) - mean )/std_dev;
    counter2++;
    
  }

  //Send strings of 20 converted symbols until array completed
  char alpha[21] = {};
  int x = 0;
  counter = 0;
  String string;
  int normalisedValue;
  for(counter2 = 0; counter2 < 9; counter2++){
    for(counter = 0; counter < 20; counter++){
      normalisedValue = normalised[counter+x] * 100;
      //Serial.println(normalisedValue);
      if(normalisedValue < -115){
        alpha[counter] = 'a';
      }
      else if(normalisedValue < -67){
        alpha[counter] = 'b';
      }
      else if(normalisedValue < -32){
        alpha[counter] = 'c';
      }
      else if(normalisedValue < 0){
        alpha[counter] = 'd';
      }
      else if(normalisedValue < 32){
        alpha[counter] = 'e';
      }
      else if(normalisedValue < 67){
        alpha[counter] = 'f';
      }
      else if(normalisedValue < 115){
        alpha[counter] = 'g';
      }
      else{
        alpha[counter] = 'h';
      }
      Serial.println(alpha[counter]);
        
    }
    
    
  
    alpha[counter] = '\0';
    string = String(alpha);
    sendECGAlpha(string);
    x = x + 20;
  }
  
  return ("abcdefgabcdefgabcd");
}

//Smoothing function for temperature sensing
//Rolling array of temepratue values
//Arguments allow for any array size to be used

boolean smoothingAlgorithm(float * result, float * arrayPointer, int * arraySize, int * sampleSize, float * floatValue){
  if(*arraySize == (*sampleSize)){
    for(int i = 0; i < *arraySize;i++){
      *(arrayPointer + i) = *(arrayPointer+ i + 1);
    }
  }
  *(arrayPointer + *arraySize) = *floatValue;
  if(*arraySize != (*sampleSize)){
    *arraySize =  *arraySize + 1;
  }
  float total = 0;
  for(int i = 0; i < *arraySize; i++){
    total += *(arrayPointer+ i);
  }
  total = total / *arraySize;
  *result = total;
  return true;
}
  
//Temperature PAX
boolean PAXAlgorithm(float * result, float *arrayPointer, int * arraySize, int * sampleSize, float * floatValue){

  if(*arraySize < *sampleSize - 1){
    *(arrayPointer + *arraySize) = *floatValue;
    *arraySize = *arraySize + 1;
    return false;
  }
  else{
    *(arrayPointer + *arraySize) = *floatValue;
    float total;
    for(int i = 0; i < *arraySize; i++){
      total += *(arrayPointer + i);
    }
    total = total/ *arraySize;
    Serial.print("PAA value: ");
    Serial.println(total);
    *arraySize = 0;
    return true;
  }
}

int32_t adaptive = 1;

int32_t adaptiveSampling(int32_t * currentSamplePeriod, int32_t * standardSamplePeriod, int32_t * maxSampleInterval, int32_t * maxValue, int32_t * minValue, int * currentValue){
  if( (*currentValue > *maxValue) || (*currentValue < *minValue)){
    Serial.print("Standard sample period: ");
    Serial.println(*standardSamplePeriod);
    return *standardSamplePeriod; 
  }
  else if(*standardSamplePeriod > *maxSampleInterval){
    return *standardSamplePeriod;
  }
  else{
    if(*currentSamplePeriod < *maxSampleInterval){
      if ((*currentSamplePeriod + adaptive) < *maxSampleInterval){
        adaptive = adaptive*2;
        Serial.print("Current + adaptive: ");
        Serial.println(*currentSamplePeriod + (adaptive/2));
        return (*currentSamplePeriod + (adaptive/2));        
      }
      else{
        Serial.print("MaxSample: ");
        Serial.println(*maxSampleInterval);
        return *maxSampleInterval;
      }
    }
    else{
      Serial.print("MaxSample: ");
      Serial.println(*maxSampleInterval);
      return *maxSampleInterval;
    }
  }
}

