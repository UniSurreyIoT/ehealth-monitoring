/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             
    
    FILE                     Sampling.cpp

    PURPOSE OF FILE          Functions for retrieving samples from sensors
/*=========================================================================*/

#include <eHealth.h>

void samplePulseOxiometer(int *result){
  
  result[0] = eHealth.getOxygenSaturation();
  result[1] = eHealth.getBPM();
  
}

int sampleBPM(){
   return eHealth.getBPM();
}

int samplePulse(){
  return eHealth.getOxygenSaturation();
  
}
float sampleECG(){
  return eHealth.getECG();
}

int sampleEMG(){
return 66;  
}

int sampleAirFlow(){
return 55;  
}

float sampleTemperature(){
 
  return eHealth.getTemperature();
    
}

int sampleBloodPressure(){
return 44;  
}

int sampleBodyPosition(){
  return 33;
}

int sampleGSR(){
return 22;  
}

int sampleGlucometer(){
  return 11;
}


