# Firebase Drawing for Android

This Android application demonstrates the use of the Firebase SDK to create a shared drawing canvas.
Multiple users can run the app and draw on the same canvas. Line segments are synchronized as the user draws them.

![Screenshot](screenshot.png)

## Setup

Update [`DrawingActivity`](/app/src/main/java/com/firebase/androidchat/DrawingActivity.java) and replace
`https://android-drawing.firebaseio-demo.com` with a reference to your Firebase.

## What's here

The drawing and color-picking portions of this application are largely adapted from Google's
ApiDemo application, which was shipped with a previous version of the Android SDK's
[sample code](https://developer.android.com/samples/).

This example is intended to demonstrate how you can adapt single-user applications to be
collaborative with the help of [Firebase](https://www.firebase.com). Create your own Firebase
and incorporate these techniques into your own application!

## More about Firebase on Android

You can do lots more with Firebase on Android. Check out our Android
[Quickstart guide](https://www.firebase.com/docs/java-quickstart.html) to learn more.

