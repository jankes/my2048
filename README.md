# (Another) clone of 2048 for Android

![screen shot](https://raw.github.com/jankes/my2048/master/2048.png)

## About

This is my implementation of [2048](http://gabrielecirulli.github.io/2048/) for Android. It's a fun side project to learn some Android APIs.

## How to play

- Swipe up, down, left, or right to shift the blocks around.

- If two blocks have the same value, they merge into one block whose value is the sum of the two merged blocks.

- Try to get the 2048 block.

Long press the screen to start a new game

## Running the code

This project was developed with Android Studio running on Ubuntu 13.10.  
Compiling depends on API level 19 (Android SDK Platform 4.4.2) and Android SDK Build-tools 19.1 (see build.gradle).

I've tested running on an AVD created from the Galaxy Nexus generic device definition (4.7 inch screen, 720x1280 normal xhdpi)
and my Galaxy S3 Phone

Note I haven't tried building/running the project in an environment outside my local machine.  
However, assuming you have Android Studio and the proper Android SDK stuff installed, you could probably just
clone this repository, build, then run in the simulator or on your phone.
