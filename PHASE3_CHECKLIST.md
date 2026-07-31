# Phase 3: Full Implementation Checklist

## Authentication
- [x] Firebase Authentication setup (email/password)
- [x] Login with validation and error handling
- [x] Registration with validation (full name, email, student ID, password)
- [x] Logout functionality (clear session)
- [x] Persist login state (stay logged in on app restart)

## Database (Firebase Firestore)
- [x] User collection (name, email, studentId, uid, profilePhoto)
- [x] Items collection (title, description, location, date, status, photoUrl, postedBy, category)
- [x] Comments subcollection on each item
- [x] Chat threads collection (participants, lastMessage, timestamp)
- [x] Messages subcollection in each chat thread
- [x] Notifications collection per user

## Photo Storage
- [x] Decide: Firebase Cloud Storage vs Supabase (check Spark plan limits) — chose Supabase to avoid the Blaze plan
- [x] Photo upload on Post Lost/Found Item screens
- [x] CameraX integration for taking photos
- [x] Gallery picker for selecting existing photos
- [x] Blurred photo display — apply blur filter (Glide transform) for non-owners
- [x] Poster sees unblurred photo; everyone else sees blurred version
- [x] Option for poster to reveal photo in chat during verification

## Item Listings (Home Screen)
- [x] Fetch items from Firestore in real-time (replace dummy data)
- [x] Lost/Found tab filtering from database
- [ ] Pull-to-refresh — dependency added, not wired up (real-time listeners auto-update instead)
- [x] Load item thumbnails from storage (blurred for non-owners)

## Post Lost/Found Item
- [x] Submit form data to Firestore
- [x] Upload photo to storage and save URL in Firestore
- [x] Auto-fill poster info from logged-in user
- [x] Category selection (Electronics, Personal, Documents, etc.)

## Item Details
- [x] Fetch full item data from Firestore
- [x] Display comments section (real-time from subcollection)
- [x] Post a comment
- [x] "Claim Item" button → redirects to chat with poster (does NOT mark as claimed)

## Claim Flow
- [x] "Claim Item" creates or opens a chat thread with the poster
- [x] Only the original poster can mark item as "Claimed/Resolved"
- [x] Resolved items get a badge on Detail and are filtered out of Home/Search (still visible on Manage Listings for the owner)

## Chat / Messaging
- [x] Chat list screen (all active conversations)
- [x] Chat thread screen (real-time messages via Firestore)
- [x] Send text messages
- [x] Poster can optionally send unblurred photo in chat for verification
- [x] Timestamp display on messages

## Search & Filter
- [x] Real search queries against Firestore
- [x] Filter by category, status (Lost/Found), date (Today), and location (dedicated text filter)
- [x] Filter chips trigger actual queries (replace static list)

## Manage Listings
- [x] Fetch only current user's posts from Firestore
- [x] Edit item — pre-fills the Post form (including photo/location) and updates the Firestore doc in place
- [x] Delete item (removes the Firestore doc; does not also delete the Supabase photo object)
- [x] Mark as Resolved/Claimed (only poster can do this)

## Notifications
- [x] Firebase Cloud Messaging (FCM) client setup — token registration + FCMService
- [x] Notify poster when someone claims their item (in-app, Firestore-backed)
- [x] Notify poster when someone comments on their item (in-app, Firestore-backed)
- [x] Notify users of a possible lost/found match when a new item is posted (same category + opposite status + unresolved)
- [ ] Notify user when they receive a chat message
- [x] Store notifications in Firestore for in-app notification list
- **Note:** nothing server-side triggers an actual FCM *push* yet (needs a Cloud Function → Blaze plan); the in-app Notifications tab is the real, working delivery mechanism today.

## Profile
- [x] Display real user data from Firestore
- [ ] Profile photo upload (optional) — not implemented
- [x] Show count of user's active listings

## Location / Maps
- [ ] Google Maps SDK integration — deliberately skipped (see below)
- [x] Location capture on Post Item screens ("Use Current Location" button, not a map picker)
- [x] Display item location — "View on Map" button opens a `geo:` intent in an installed map app (not an embedded map view)
- [x] Geolocation auto-detect for current location
- **Note:** chose this lightweight approach over embedding the Maps SDK to skip a Google Cloud Console API-key setup step under deadline pressure; it still genuinely exercises the Geolocation service.

## UI Polish
- [x] Real photos via Glide/Supabase replace the old placeholder drawables (placeholder remains only as an intentional fallback for missing photos)
- [ ] Loading states (progress bars/shimmer) while fetching data — not added
- [x] Empty states ("No items found", "No notifications", etc.) on every list screen
- [x] Error handling with user-friendly messages (Snackbars throughout)
