> Namma-Raste Reporter

Namma-Raste Reporter is an Android application designed to help citizens report public infrastructure issues such as potholes, damaged roads, broken streetlights, drainage problems, and garbage accumulation. The app simplifies issue reporting through image capture, automatic location detection, complaint tracking, and real-time status updates, helping improve communication between citizens and local authorities.

> Overview

Urban infrastructure issues often go unreported or are delayed due to unclear reporting channels and lack of transparency. Namma-Raste Reporter addresses this problem by providing a simple mobile platform where users can quickly register complaints, attach photos, track progress, and receive updates.

The project was built using modern Android development practices with Kotlin, Jetpack Compose, Firebase, Room Database, and AI-assisted categorization features.

> Features

> User Authentication
- Secure user registration and login with Firebase Authentication
- Session handling and logout support

> Issue Reporting
- Capture issue images using CameraX
- Detect the user’s current location automatically
- Select issue categories before submission
- Submit complaints with a unique ticket ID

> Complaint Management
- Track complaint progress in real time
- View complaint history
- Receive complaint status notifications
- Store complaint data in Firebase Firestore

> Offline Support
- Save report data locally using Room Database
- Improve usability in unstable network conditions

> AI-Assisted Support
- Smart issue categorization
- Suggestive input assistance for faster complaint filing
- Improved interaction flow for users

> Tech Stack

> Android
- Kotlin
- Jetpack Compose
- Material 3
- MVVM Architecture
- Navigation Compose

 > Backend and Storage
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Room Database

> APIs and Libraries
- CameraX API
- Google Maps API
- Firebase Cloud Messaging (FCM)
- Kotlin Coroutines
- Hilt Dependency Injection

> Application Screenshots

| Dashboard | Report Issue | Map Tracking |
|----------|--------------|--------------|
| ![Dashboard](assets/screenshots/dashboard.png) | ![Report Issue](assets/screenshots/report_issue.png) | ![Map Tracking](assets/screenshots/map_tracking.png) |

> Add your actual screenshots inside `assets/screenshots/` and update the filenames if needed.

> Architecture

The application follows the MVVM architecture pattern to separate UI logic, business logic, and data handling. Jetpack Compose is used to build a modern reactive UI, while Firebase services manage authentication, cloud storage, and notifications. Room Database provides local persistence for offline-friendly usage.

> Project Structure

```bash
com.nammaraste.reporter
│
├── data
├── di
├── firebase
├── navigation
├── repository
├── ui
├── utils
└── viewmodel
```

> Installation

> Prerequisites
- Android Studio
- Kotlin support enabled
- Firebase project setup
- Google Maps API key

> Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Namma-Raste-Reporter.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle and allow dependencies to download.

4. Configure Firebase:
   - Create a Firebase project
   - Add your Android app to Firebase
   - Download `google-services.json`
   - Place it inside the `app/` directory

5. Enable the following Firebase services:
   - Authentication
   - Firestore Database
   - Firebase Storage
   - Cloud Messaging

6. Add your Google Maps API key if required in the project configuration.

7. Build and run the app on an emulator or Android device.

> Permissions Used

```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

> Use Cases

- Report potholes and damaged roads
- Register broken streetlight complaints
- Report drainage overflow or waterlogging
- Raise garbage and sanitation-related civic issues
- Track submitted complaints without visiting an office

> Future Improvements

- AI-based road damage detection from captured images
- Authority-side dashboard for issue monitoring
- Heatmaps and analytics for complaint patterns
- Better offline synchronization
- Multilingual support
- Emergency alert and priority escalation features
- AI chatbot for complaint assistance
- Smart city integration

> Objective

The goal of Namma-Raste Reporter is to create a practical, user-friendly, and transparent civic reporting platform that enables faster issue reporting and better complaint tracking for public infrastructure problems.

> Author

Appu Rathod  
Android Developer | Kotlin | Jetpack Compose | Firebase | AI Integration

> License

This project was developed for educational, internship, and learning purposes as part of Android application development using generative AI technologies.
