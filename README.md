# EnvironmentalDiffTest
This application detects nearby audio and compares it to project audio files using Musicg to determine the most likely sound of the nearby audio.
## Environment
This application is built for Android Devices. Thus, in order to use this application, you must have access to an Android Device. The Audio Processing portion of the application 
is implemented using [Musicg](https://github.com/loisaidasam/musicg).
## Installation/Running
In order to install this application on your Android Device, you must install Android Studio. Once you have installed Android Studio, clone into this repository and open it in Android Studio. The application can then 
be run on a device. In order for the application to run as intended, make sure that the device has granted the application the following permissions:
* READ_EXTERNAL_STORAGE
* WRITE_EXTERNAL_STORAGE
* RECORD_AUDIO
* WAKE_LOCK
## Adjustable Parameters
For this application, there are no adjustable parameters in the source code. However, if you would like to add additional audio files for Musicg to compare the nearby audio to, you may do so by adding the 
WAV file to the ```raw``` folder (located in the res folder). Then update the compareAudio() function to read in this file and compare it to the nearby audio file.
## Important Functions/Methods
* compareAudio: This function reads in all WAV files in the ```raw``` folder and compares them against the recorded audio file.
* Wave.getFingerprintSimilarity: This is a Musicg function that returns the audio fingerprint similarity between two audio files. One of these files is the recorded audio and the other is the audio WAV file to compare it to.
* FingerprintSimilarity.getSimilarity: This function returns the similarity of the audio files in numerical form.
## Bottlenecks/Bugs
This application has no known bugs or bottlenecks. However, the accuracy of the application can vary greatly depending on the differences in audio structure of similar sounds.
## Future Work
The main goal for the future of this project should be studying how we can increase the accuracy of the environmental detection. Due to the variability of the audio structure of similar sounds, 
accurately classifying the nearby audio can be difficult given the current tools. If there are better tools for this effort, they should be implemented into this project.
