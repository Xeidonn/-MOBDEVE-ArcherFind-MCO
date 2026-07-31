# ArcherFind — Campus Lost & Found App

A mobile application that helps DLSU students report, search, and claim lost or found items on campus.

**Course:** MOBDEVE — Mobile Development
**Phase:** 3 — Final Application

## Screens

| # | Screen | Description |
|---|--------|-------------|
| 1 | Login | Firebase Auth email/password sign-in, session persists across restarts |
| 2 | Register | Full name, email, student ID, password — creates a Firebase Auth account + Firestore profile |
| 3 | Home | Live Firestore items via Lost/Found tabs; FAB opens a Lost/Found post picker |
| 4 | Item Details | Photo (blurred for non-owners), description, date, location, comments, View on Map, Claim Item |
| 5 | Post Lost Item | Title/description/category/location form, current-location capture, camera or gallery photo |
| 6 | Post Found Item | Same form as Post Lost Item |
| 7 | Search & Filter | Real Firestore search with text query + Lost/Found/category/Today filter chips |
| 8 | Chats | List of active claim conversations by item |
| 9 | Chat Thread | Real-time messages with the other party; poster can reveal the unblurred photo |
| 10 | Notifications | Real-time Firestore notifications (claim + comment events) |
| 11 | Profile | Signed-in user's real name/email/student ID and active listing count |
| 12 | Manage Listings | User's own posts — Resolve and Delete are live; Edit has no screen yet |

## Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Architecture:** Single Activity + Fragments, thin repository layer over Firebase/Supabase (no ViewModel/LiveData layer)
- **Navigation:** Jetpack Navigation Component (`nav_graph.xml`), including a global action and a Nav-result pattern for the in-app camera
- **Backend:** Firebase Authentication, Cloud Firestore (real-time listeners throughout), Firebase Cloud Messaging
- **Photo storage:** Supabase Storage (public bucket, uploaded via its REST API over OkHttp) — chosen over Firebase Cloud Storage to avoid requiring the Blaze billing plan
- **Photo capture:** CameraX (in-app preview + shutter — not the system Camera app) plus the system Photo Picker for gallery selection
- **Location:** FusedLocationProviderClient + Geocoder for reverse geocoding, `geo:` intent for "View on Map" (no Maps SDK/API key)
- **Image loading:** Glide, including a custom `BlurTransformation` for photo verification
- **UI Components:** Material Design 3 — MaterialToolbar, TextInputLayout, MaterialCardView, ChipGroup, FloatingActionButton, BottomNavigationView, TabLayout
- **Min SDK:** 26 (Android 8.0) · **Target/Compile SDK:** 34 (Android 14)

## Project Structure

```
app/src/main/
├── java/com/mobdeve/s17/grp2/archerfind/
│   ├── MainActivity.java, LoginFragment, RegisterFragment
│   ├── HomeFragment, SearchFilterFragment, ItemDetailFragment
│   ├── PostItemFragmentBase (+ PostLostItemFragment, PostFoundItemFragment)
│   ├── CameraCaptureFragment              # In-app CameraX capture screen
│   ├── ChatListFragment, ChatThreadFragment
│   ├── NotificationsFragment, ProfileFragment, ManageListingsFragment
│   ├── AuthRepository, ItemRepository, CommentRepository,
│   │   ChatRepository, NotificationRepository, SupabaseStorageRepository
│   ├── FirestoreCallback / FirestoreListCallback / FirestoreVoidCallback
│   ├── FCMService                          # Push token registration + display
│   ├── BlurTransformation                  # Glide transform for photo verification
│   ├── Item, UserProfile, Comment, ChatThread, Message, NotificationItem
│   └── ItemAdapter, ManageItemAdapter, CommentAdapter, ChatThreadAdapter,
│       MessageAdapter, NotificationAdapter
├── res/
│   ├── layout/                    # All fragment and card layouts
│   ├── navigation/nav_graph.xml   # Navigation graph with all destinations
│   ├── menu/bottom_nav_menu.xml   # Home, Search, Chats, Notifications, Profile
│   └── values/                    # Colors, strings, themes
```

## Navigation Flow

```
Login ──→ Register
  │
  └──→ Home (bottom nav) ──→ Item Details ──→ Chat Thread (Claim)
         │                 ──→ Post Lost/Found Item ──→ Camera Capture
         │
       Search (bottom nav) ──→ Item Details
         │
       Chats (bottom nav) ──→ Chat Thread
         │
       Notifications (bottom nav)
         │
       Profile (bottom nav) ──→ Manage Listings
                             ──→ Logout → Login
```

## How to Run

1. Open **Android Studio** → **File → Open** → select the project folder → wait for **Gradle sync**.
2. Make sure `app/google-services.json` is present (Firebase Auth + Firestore for project `archerfind-f69d5`).
3. Add to `local.properties` (not committed): `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_BUCKET` (defaults to `item-photos`), and optionally `MAPS_API_KEY` (currently unused — Maps is intentionally implemented via `geo:` intents instead of the Maps SDK).
4. Click **Run** and select an emulator or device.

## Known Gaps / Scope Notes

- **Edit listing** has no dedicated screen yet — Manage Listings shows an explicit "not available yet" message rather than a silent no-op.
- **FCM push notifications**: the client-side plumbing (token registration, `FCMService`, notification channel) is real, but nothing server-side currently triggers a send for claim/comment/message events — that would need a Cloud Function, which requires the Blaze billing plan (the same reason Supabase was chosen over Firebase Cloud Storage). The in-app **Notifications** tab is real-time and fully working today.
- **Firestore security rules** are in test mode (open read/write, expiring ~30 days after the Firestore database was created) rather than locked-down production rules.
- Search is a one-shot fetch-and-filter (Firestore has no native substring query), not a live-updating query.

## Group 2

MOBDEVE S15
