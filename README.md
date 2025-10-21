# My Android Application

This is a simple Android application that allows you to hide a text message within an image file using steganography.

## Features

*   Select an image from the gallery.
*   Enter a text message to hide in the image.
*   Save the new image with the hidden message to your device's `Pictures/Steganography` folder.

## How to Use

1.  Clone this repository.
2.  Open the project in Android Studio.
3.  Build and run the application on an emulator or a physical device.
4.  Grant the necessary storage permissions when prompted.
5.  Select an image, enter your text, and press "Save Data".

## How to Download the APK

A debug APK is automatically built for every push to the `main` or `master` branch. You can download it directly from GitHub:

1.  Go to the **"Actions"** tab in the GitHub repository.
2.  Click on the latest workflow run in the list (it should have a green checkmark if successful).
3.  Under the **"Artifacts"** section on the summary page, you will find `app-debug.apk`.
4.  Click on it to download the file.

## Project Structure

*   `app`: The main application module.
*   `library`: A local module containing the steganography logic.
*   `build.gradle`: Build scripts for the application.
