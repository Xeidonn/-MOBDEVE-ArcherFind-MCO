# Phase 3: Full Implementation Checklist

## Authentication
- [ ] Firebase Authentication setup (email/password)
- [ ] Login with validation and error handling
- [ ] Registration with validation (full name, email, student ID, password)
- [ ] Logout functionality (clear session)
- [ ] Persist login state (stay logged in on app restart)

## Database (Firebase Firestore)
- [ ] User collection (name, email, studentId, uid, profilePhoto)
- [ ] Items collection (title, description, location, date, status, photoUrl, postedBy, category)
- [ ] Comments subcollection on each item
- [ ] Chat threads collection (participants, lastMessage, timestamp)
- [ ] Messages subcollection in each chat thread
- [ ] Notifications collection per user

## Photo Storage
- [ ] Decide: Firebase Cloud Storage vs Supabase (check Spark plan limits)
- [ ] Photo upload on Post Lost/Found Item screens
- [ ] CameraX integration for taking photos
- [ ] Gallery picker for selecting existing photos
- [ ] Blurred photo display — apply blur filter (Glide transform) for non-owners
- [ ] Poster sees unblurred photo; everyone else sees blurred version
- [ ] Option for poster to reveal photo in chat during verification

## Item Listings (Home Screen)
- [ ] Fetch items from Firestore in real-time (replace dummy data)
- [ ] Lost/Found tab filtering from database
- [ ] Pull-to-refresh
- [ ] Load item thumbnails from storage (blurred for non-owners)

## Post Lost/Found Item
- [ ] Submit form data to Firestore
- [ ] Upload photo to storage and save URL in Firestore
- [ ] Auto-fill poster info from logged-in user
- [ ] Category selection (Electronics, Personal, Documents, etc.)

## Item Details
- [ ] Fetch full item data from Firestore
- [ ] Display comments section (real-time from subcollection)
- [ ] Post a comment
- [ ] "Claim Item" button → redirects to chat with poster (does NOT mark as claimed)

## Claim Flow
- [ ] "Claim Item" creates or opens a chat thread with the poster
- [ ] Only the original poster can mark item as "Claimed/Resolved"
- [ ] Resolved items visually distinguished or hidden from main feed

## Chat / Messaging
- [ ] Chat list screen (all active conversations)
- [ ] Chat thread screen (real-time messages via Firestore)
- [ ] Send text messages
- [ ] Poster can optionally send unblurred photo in chat for verification
- [ ] Timestamp display on messages

## Search & Filter
- [ ] Real search queries against Firestore
- [ ] Filter by category, status (Lost/Found), date, location
- [ ] Filter chips trigger actual queries (replace static list)

## Manage Listings
- [ ] Fetch only current user's posts from Firestore
- [ ] Edit item (update Firestore document)
- [ ] Delete item (remove from Firestore + delete photo from storage)
- [ ] Mark as Resolved/Claimed (only poster can do this)

## Notifications
- [ ] Firebase Cloud Messaging (FCM) setup
- [ ] Notify poster when someone claims their item
- [ ] Notify poster when someone comments on their item
- [ ] Notify user when they receive a chat message
- [ ] Store notifications in Firestore for in-app notification list

## Profile
- [ ] Display real user data from Firestore
- [ ] Profile photo upload (optional)
- [ ] Show count of user's active listings

## Location / Maps
- [ ] Google Maps SDK integration
- [ ] Location picker on Post Item screens (replace static placeholder)
- [ ] Display item location on map in Item Details
- [ ] Optional: geolocation auto-detect for current location

## UI Polish
- [ ] Replace all placeholder drawables with real images/icons
- [ ] Loading states (progress bars/shimmer) while fetching data
- [ ] Empty states ("No items found", "No notifications")
- [ ] Error handling with user-friendly messages
