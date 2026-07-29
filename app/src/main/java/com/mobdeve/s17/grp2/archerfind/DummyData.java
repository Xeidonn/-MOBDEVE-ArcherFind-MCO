package com.mobdeve.s17.grp2.archerfind;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Temporary in-memory data source, kept only as a fallback while fragments are
// migrated one by one to Firestore-backed repositories (see ItemRepository).
public class DummyData {

    private static Item makeItem(String id, String title, String description, String location,
                                  String category, String status, String ownerId, String ownerName,
                                  int daysAgo) {
        Item item = new Item(title, description, location, category, status, ownerId, ownerName);
        item.setId(id);
        item.setCreatedAt(new Date(System.currentTimeMillis() - daysAgo * 24L * 60 * 60 * 1000));
        return item;
    }

    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        items.add(makeItem("1", "Blue Backpack", "Navy blue Jansport backpack with a laptop and notebooks inside.", "Gokongwei Building, 2F", "Personal", "Lost", "dummy_user_1", "Juan Dela Cruz", 1));
        items.add(makeItem("2", "iPhone 15 Pro", "Space Black iPhone 15 Pro with a clear case. Lock screen shows a cat.", "Henry Sy Library, 3F", "Electronics", "Lost", "dummy_user_2", "Maria Reyes", 2));
        items.add(makeItem("3", "Student ID Card", "DLSU student ID for Juan Santos, ID 12312345.", "Andrew Building, Lobby", "Documents", "Found", "dummy_user_3", "Ana Cruz", 3));
        items.add(makeItem("4", "Water Bottle", "Green Hydro Flask 32oz with stickers.", "Velasco Hall, Room 301", "Personal", "Found", "dummy_user_4", "Carlo Tan", 3));
        items.add(makeItem("5", "Graphing Calculator", "TI-84 Plus CE, has name 'Maria' scratched on back.", "Gokongwei Building, 4F", "Electronics", "Lost", "dummy_user_5", "Paolo Lim", 4));
        items.add(makeItem("6", "Umbrella", "Black foldable umbrella found near entrance.", "La Salle Hall, Entrance", "Personal", "Found", "dummy_user_1", "Juan Dela Cruz", 4));
        items.add(makeItem("7", "AirPods Pro Case", "White AirPods Pro case, no name on it.", "Br. Andrew Gonzalez Hall", "Electronics", "Found", "dummy_user_6", "Liza Santos", 5));
        items.add(makeItem("8", "Notebook", "Green spiral notebook with Physics 101 notes.", "Razon Hall, Room 205", "Documents", "Lost", "dummy_user_7", "Miguel Ortiz", 6));
        return items;
    }

    public static List<Item> getLostItems() {
        List<Item> lost = new ArrayList<>();
        for (Item item : getAllItems()) {
            if (item.getStatus().equals("Lost")) lost.add(item);
        }
        return lost;
    }

    public static List<Item> getFoundItems() {
        List<Item> found = new ArrayList<>();
        for (Item item : getAllItems()) {
            if (item.getStatus().equals("Found")) found.add(item);
        }
        return found;
    }

    public static List<Item> getUserListings() {
        List<Item> myItems = new ArrayList<>();
        for (Item item : getAllItems()) {
            if (item.getOwnerId().equals("dummy_user_1")) myItems.add(item);
        }
        return myItems;
    }

    private static NotificationItem makeNotif(String type, String title, String message, int hoursAgo) {
        NotificationItem n = new NotificationItem("dummy_user_1", type, title, message);
        n.setCreatedAt(new Date(System.currentTimeMillis() - hoursAgo * 60L * 60 * 1000));
        return n;
    }

    public static List<NotificationItem> getNotifications() {
        List<NotificationItem> notifs = new ArrayList<>();
        notifs.add(makeNotif(NotificationItem.TYPE_CLAIM, "Item Claimed", "Someone has claimed your 'Blue Backpack' posting.", 2));
        notifs.add(makeNotif(NotificationItem.TYPE_SYSTEM, "New Match", "A found item matching your lost 'iPhone 15 Pro' was posted.", 5));
        notifs.add(makeNotif(NotificationItem.TYPE_SYSTEM, "Listing Approved", "Your posting 'Umbrella' has been approved and is now live.", 24));
        notifs.add(makeNotif(NotificationItem.TYPE_SYSTEM, "Reminder", "Your lost item 'Graphing Calculator' has been posted for 5 days.", 48));
        notifs.add(makeNotif(NotificationItem.TYPE_SYSTEM, "Welcome!", "Welcome to ArcherFind. Start by posting a lost or found item.", 72));
        return notifs;
    }
}
