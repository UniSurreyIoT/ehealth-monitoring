
/*=========================================================================

    APPLICATION              eHealth Application for MSc dissertation
                             2016
                             Phil Winderbank-Scott

    SETTINGS                 Interact with Cooking Hacks eHealth board
                             Adafruit BLE transmission

                             Setup Services and Characteristics for sensor transmission
    
    FILE                     E_healthConfig.h

    PURPOSE OF FILE          Header file defining external variables and functions
                             Also defines memory locations in EEPROM
/*=========================================================================*/

#define AVAILABLEMONITOR  0
#define CURRENTMONITOR  2
#define FIRSTUSE 4
#define TEMPERATUREINTERVAL 30          //Four bytes
#define SAMPLETEMPERATURE 34            //Two bytes
#define TEMPERATURECOMPRESSIONTYPE 36   //Two Bytes
#define TEMPERATUREMAXIMUM 38           //Two Bytes
#define TEMPERATUREMINIMUM 40           //Two Bytes
#define TEMPERATUREMAXSAMPLEINTERVAL 42 //Four Bytes
#define SAMPLEBPM 50                    //Two Bytes
#define SAMPLESPO2 60                   //Two Bytes
#define SAMPLEECG 70
#define ECGOUTPUTTYPE 74

#define PULSIOXIMETER  10
#define ECG  11
#define EMG  12
#define AIRFLOW  13
#define TEMPERATURE  14
#define BLOODPRESSURE  15
#define BODYPOSITION  16
#define GSR  17
#define GLUCOMETER  18
#define BPM_ID 19
#define SPO2_ID 20

#define hasSPO2 1
#define hasBPM 1
#define hasEMG 0
#define hasECG 1
#define hasAirflow 0
#define hasTemperature 1
#define hasBloodpressure 0
#define hasBodyposition 0
#define hasGSR 0
#define hasGlucometer 0

extern Adafruit_BluefruitLE_SPI ble;

extern char useTemperatureArray[];

extern char temperatureSampleArray[];

extern char useBPMArray[];

extern char useSPO2Array[];

extern char useECGArray[];

extern char getTemperatureDataCompression[];

extern char getTemperatureMaximum[];

extern char getTemperatureMinimum[];

extern char getTemperatureMaxSampleInterval[];

extern char ecgOutputTypeArray[];

extern int32_t gattvalue;

extern int32_t temperatureSamplePeriod;

extern int32_t temperatureCompressionType;

extern int32_t temperatureMaximum;

extern int32_t temperatureMinimum;

extern int32_t temperatureMaxSampleInterval;

extern int32_t ecgOutputType;

extern boolean temperatureEnabled;

extern boolean BPMEnabled;

extern boolean SPO2Enabled;

extern boolean ECGEnabled;

void checkSettings();
