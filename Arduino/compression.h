String calculate_mean(int *p);

boolean smoothingAlgorithm(float * result, float * arrayPointer, int * arraySize, int * sampleSize, float * floatValue);
  
boolean PAXAlgorithm(float * result, float *arrayPointer, int * arraySize, int * sampleSize, float * floatValue);

int32_t adaptiveSampling(int32_t * currentSamplePeriod, int32_t * standardSamplePeriod, int32_t * maxSampleInterval, int32_t * maxValue, int32_t * minValue, int * currentValue);
