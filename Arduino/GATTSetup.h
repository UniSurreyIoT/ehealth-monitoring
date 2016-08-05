/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             Setup Services and Characteristics for sensor transmission
    
    FILE                     GATTSetup.h

    PURPOSE OF FILE          Declare functions available in GATTSetup
/*=========================================================================*/

void error(const __FlashStringHelper*err);

int setupGatt();

extern Adafruit_BluefruitLE_SPI ble;

extern Adafruit_BluefruitLE_SPI ble2;

void setupSwitchOn();

void sendTemperatureValue(uint32_t value);

boolean setupPulsioximeterGATT();

int setupTemperatureGATT();

int setupECGGATT();

int setupGatt2();

void sendECGValue2(int value);

void sendECGAlpha(String value);

void sendECGValue(int value);

void setupuseSPO2Array(char * array);

void setupuseBPMArray( char * array);

void setupuseECGArray( char * array);

void setupuseTemperatureArray(char * array);

void setupTemperatureSampleArray(char * array);

void setupGetECGOutputTypeArray(char *array);

void sendSPO2Value(int value);

void sendBPMValue(int value);

void setupGetTemperatureDataCompression(char * array);

void setupGetTemperatureMaximum(char * array);

void setupGetTemperatureMinimum(char * array);

void setupGetTemperatureMaxSampleInt(char * array);
