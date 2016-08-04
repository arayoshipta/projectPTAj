#ATTENTION
I have uploaded new version of PTA (PTA2).
Although PTA2 is still SNAPSHOT version, it would be more useful than PTA because,

1. Point detection can be performed by 'find maxima' of ImageJ
2. Four different methods for localization can be used. "Find maxima", "Centroid", Center of Mass", and "2D Gaussian fitting"
3. No need to use .dll (.jnilib) file. 
4. Simple edit tool for trackes are equiped (delete, split, and concatenate)
5. You can call PTA2 from macro

PTA2 can be found here.
https://github.com/arayoshipta/PTA2

2016.08.04

=======
I have deleted some files that contains error in MSD plots.
Please use PTA_.jar in jar folder.
Sorry for your inconvinience.
2016.07.05
-----
I have corrected the inadequate indication in MSD plot.
Please use the latest version of PTA_.jar and manual.

Sorry for your inconvenience.
2015.03.24
-----

#What can do this plugin?
The main purpose of this plugin is to track the single molecule fluorescence particles. 
But the objects which can be discrimanted by threshold can be detected and tracked (for example, moving cells). 

##PTA is a plugin which runs on ImageJ
 - Detecting and Tracking the multiple particles simultaneously.
 - The detection method is centroid and 2 dimensional Gaussian distribution fitting by Levenberg-Marquardt method.
 - Running on MacOSX (Intel CPU, 64 bit) and Windows (32bit and 64bit).
 - ATTENTION!! Now MacOSX version only runs on MacOSX10.7 or higher. I'm sorry for your inconvenience.

##How to use this plugin?
I strongly recommend you to read Manuals.
This is a brief way to install this plugin.

1. Check your platform. For MacOSX10.7 (or higher) and Windows (32bit, 64bit) platforms are available.
2. Put jar files in /jar folder (PTA_.jar, jcommon.jar, jfreechart.jar) to your ImageJ/plugins folder
3. Put fit2DGauss.dll (for Win) or libfit2DGauss.jnilib (for MacOSX) in lib folder to your ImageJ folder (not plugins folder).

Good luck!

Yoshiyuki Arai
