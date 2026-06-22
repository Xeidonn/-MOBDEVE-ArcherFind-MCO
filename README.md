# ArcherFind — Campus Lost & Found App

A mobile application that helps DLSU students report, search, and claim lost or found items on campus.

**Course:** MOBDEVE — Mobile Development  
**Phase:** 2 — Interactive Prototype (UI-Only)

## Screenshots

> _Screenshots to be added after running on emulator/device._

## Screens

| # | Screen | Description |
|---|--------|-------------|
| 1 | Login | Email/password fields, login button, link to Register |
| 2 | Register | Full name, email, student ID, password fields |
| 3 | Home | RecyclerView of lost/found items with Lost/Found tabs and FAB to post |
| 4 | Item Details | Photo placeholder, description, date, location, Claim button |
| 5 | Post Lost Item | Form with photo picker (UI only), description, location, map placeholder |
| 6 | Post Found Item | Same layout as Post Lost Item |
| 7 | Search & Filter | Search bar with filter chips (Lost, Found, Electronics, Personal, Today) |
| 8 | Notifications | List of dummy notification items |
| 9 | Profile | Dummy user info, My Listings button, Logout button |
| 10 | Manage Listings | User's own posts with Edit, Resolve, and Delete actions (Snackbar feedback) |

## Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Architecture:** Single Activity + Fragments
- **Navigation:** Jetpack Navigation Component (`nav_graph.xml`)
- **UI Components:** Material Design 3 — MaterialToolbar, TextInputLayout, MaterialCardView, ChipGroup, FloatingActionButton, BottomNavigationView, TabLayout
- **Lists:** RecyclerView with custom adapters
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

## Project Structure

```
app/src/main/
├── java/com/mobdeve/s17/grp2/archerfind/
│   ├── MainActivity.java          # Single activity hosting NavHostFragment
│   ├── LoginFragment.java         # Login screen
│   ├── RegisterFragment.java      # Registration screen
│   ├── HomeFragment.java          # Home / View Listings with tabs
│   ├── ItemDetailFragment.java    # Item detail view
│   ├── PostLostItemFragment.java  # Report lost item form
│   ├── PostFoundItemFragment.java # Report found item form
│   ├── SearchFilterFragment.java  # Search with filter chips
│   ├── NotificationsFragment.java # Notifications list
│   ├── ProfileFragment.java       # User profile
│   ├── ManageListingsFragment.java# Manage own listings
│   ├── Item.java                  # Item POJO model
│   ├── NotificationItem.java      # Notification POJO model
│   ├── DummyData.java             # Hardcoded dummy data
│   ├── ItemAdapter.java           # RecyclerView adapter for items
│   ├── NotificationAdapter.java   # RecyclerView adapter for notifications
│   └── ManageItemAdapter.java     # RecyclerView adapter for manage listings
├── res/
│   ├── layout/                    # All fragment and card layouts
│   ├── navigation/nav_graph.xml   # Navigation graph with all destinations
│   ├── menu/bottom_nav_menu.xml   # Bottom navigation menu
│   ├── drawable/                  # Placeholder drawables
│   └── values/                    # Colors, strings, themes
```

## Navigation Flow

```
Login ──→ Register
  │
  └──→ Home (bottom nav) ──→ Item Details
         │                 ──→ Post Lost Item
         │                 ──→ Post Found Item
         │
       Search (bottom nav) ──→ Item Details
         │
       Notifications (bottom nav)
         │
       Profile (bottom nav) ──→ Manage Listings
                             ──→ Logout → Login
```

## How to Run

1. Open **Android Studio**
2. **File → Open** → select the project folder
3. Wait for **Gradle sync** to complete
4. Click **Run** or press `Shift + F10`
5. Select an emulator or connected device

## Phase 2 Notes

- All data is hardcoded — no backend, database, or API calls
- Photo picker and map are UI placeholders only (no camera or geolocation)
- Login has no validation — any input (or none) navigates to Home
- Edit/Delete/Resolve buttons show Snackbar feedback only

## Phase 3 Roadmap

- Firebase Authentication for real login/registration
- Cloud Firestore for item storage and retrieval
- Firebase Cloud Messaging for push notifications
- CameraX integration for photo capture
- Google Maps SDK for location tagging
- Real-time search and filtering

## Group 2

MOBDEVE S17
