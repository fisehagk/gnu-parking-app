🚗 ParkGMU — Smart Campus Parking App
📌 Overview
ParkGMU is a real-time, crowd-powered parking application designed for students at George Mason University. The app enables users to discover, claim, and navigate to available parking spots using Google Maps integration and Firebase-backed real-time data synchronization.
By leveraging user participation, ParkGMU reduces time spent searching for parking and improves overall campus efficiency.
📸 App Preview
🏠 Welcome Screen






6
🔐 Authentication (Sign In)






6
🗺️ Live Parking Map (Available Spots)






7
📍 Check-In / Parking Claimed






6
📍 Current Location Tracking






7
👤 User Profile + Navigation






6
✨ Key Features
🔐 Secure Authentication
Firebase Authentication (Email/Password)
Email verification required before access
Persistent login sessions
🗺️ Interactive Map-Based Parking
Google Maps integration with real-time markers
Visual differentiation:
🔵 Default campus marker
🟢 Available parking spots
🟡 User’s parked location
🚘 Real-Time Parking System
Users can:
Check-In to claim a parking spot
Check-Out to release it
Data synced via Firebase Firestore
📍 Smart Navigation
One-tap navigation to parked vehicle
Opens Google Maps with directions
👤 User Profile Dashboard
Displays user info (name, email)
Shows current parking location
Quick navigation to vehicle
🔄 Automatic Session Handling
Auto-redirects logged-in users to map view
Seamless user experience
🏗️ Tech Stack
Layer	Technology
Frontend	Android (Java), XML
Maps	Google Maps SDK
Backend	Firebase Firestore
Auth	Firebase Authentication
Location	Android Location Services
🧠 System Architecture
User → Firebase Auth → App Access
↓
Firestore Database
↓
Real-Time Map Updates
↓
User Actions (Check-In / Check-Out)
⚙️ Setup Instructions
1. Clone the Repository
   git clone https://github.com/your-username/parkgmu.git
2. Open in Android Studio
3. Configure Firebase
   Add your own google-services.json
   Enable:
   Authentication (Email/Password)
   Firestore Database
4. Add Google Maps API Key
   In local.properties:
   MAPS_API_KEY=your_api_key_here
5. Run the App
   Use emulator or physical device
   Enable location services
   🔐 Permissions
   Internet Access
   Fine & Coarse Location
   📂 Project Structure
   ParkGMU/
   ├── activities/
   ├── models/
   ├── res/
   │   ├── layout/
   │   ├── drawable/
   ├── Firebase Config
   ├── AndroidManifest.xml
   🚀 Future Improvements
   Real-time listeners (live updates without reload)
   Parking expiration timers
   Push notifications for available spots
   UI/UX enhancements
   ML-based parking prediction
   🎥 Demo
   📌 (Upload your demo video next — I’ll embed it cleanly here with a preview GIF)
   💡 Why This Project Stands Out
   Solves a real-world campus problem
   Full-stack mobile + cloud integration
   Real-time data synchronization
   Clean architecture & modular design
   Strong use of Google Maps + Firebase
   🧑‍💻 Author
   Fiseha K.

