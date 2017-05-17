Now all Build.MODEL <-> Marketing Name are extracted from Supported devices of Google Play:

https://support.google.com/googleplay/android-developer/answer/6154891?hl=en

1.Download the excel and put it under: 

[android-device-model-to-marketing-name/device_model_to_marketing_name/src/main/assets](https://github.com/zfdang/android-device-model-to-marketing-name/blob/master/device_model_to_marketing_name/src/main/assets/)

2.Run extractDeviceInfos.py to extract the following two files:

$ python extractDeviceInfos.py -f supported_devices.csv

[device_model_to_marketing_name/src/main/assets/branding.properties](https://github.com/zfdang/android-device-model-to-marketing-name/blob/master/device_model_to_marketing_name/src/main/assets/branding.properties)
[device_model_to_marketing_name/src/main/assets/marketingName.properties](https://github.com/zfdang/android-device-model-to-marketing-name/blob/master/device_model_to_marketing_name/src/main/assets/marketingName.properties)
  
You can add entries to these two files to enrich the database.

NOTE: please make sure all strings are escaped properly: 
    
    http://stackoverflow.com/questions/2406975/how-to-escape-the-equals-sign-in-properties-files

NOTE: please remove supported_devices.csv to reduce package size

3.Update version, build zip file, and upload it to bintray.com

