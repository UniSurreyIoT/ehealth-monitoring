/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             
    
    FILE                     Sampling.h

    PURPOSE OF FILE          Header file for retrieving samples from sensors
/*=========================================================================*/
void samplePulseOxiometer(int *result);

float sampleECG();

int sampleEMG();

int sampleAirFlow();

float sampleTemperature();

int sampleBloodPressure();
int sampleBodyPosition();

int sampleGSR();

int sampleGlucometer();

extern long temperatureSamplingTime;

int sampleBPM();

int samplePulse();



