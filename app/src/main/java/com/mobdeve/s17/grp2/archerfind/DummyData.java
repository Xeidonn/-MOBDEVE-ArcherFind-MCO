package com.mobdeve.s17.grp2.archerfind;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Blue Backpack", "Navy blue Jansport backpack with a laptop and notebooks inside.", "Gokongwei Building, 2F", "June 20, 2026", "Lost", R.drawable.placeholder_image));
        items.add(new Item("iPhone 15 Pro", "Space Black iPhone 15 Pro with a clear case. Lock screen shows a cat.", "Henry Sy Library, 3F", "June 19, 2026", "Lost", R.drawable.placeholder_image));
        items.add(new Item("Student ID Card", "DLSU student ID for Juan Santos, ID 12312345.", "Andrew Building, Lobby", "June 18, 2026", "Found", R.drawable.placeholder_image));
        items.add(new Item("Water Bottle", "Green Hydro Flask 32oz with stickers.", "Velasco Hall, Room 301", "June 18, 2026", "Found", R.drawable.placeholder_image));
        items.add(new Item("Graphing Calculator", "TI-84 Plus CE, has name 'Maria' scratched on back.", "Gokongwei Building, 4F", "June 17, 2026", "Lost", R.drawable.placeholder_image));
        items.add(new Item("Umbrella", "Black foldable umbrella found near entrance.", "La Salle Hall, Entrance", "June 17, 2026", "Found", R.drawable.placeholder_image));
        items.add(new Item("AirPods Pro Case", "White AirPods Pro case, no name on it.", "Br. Andrew Gonzalez Hall", "June 16, 2026", "Found", R.drawable.placeholder_image));
        items.add(new Item("Notebook", "Green spiral notebook with Physics 101 notes.", "Razon Hall, Room 205", "June 15, 2026", "Lost", R.drawable.placeholder_image));
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
        myItems.add(new Item("Blue Backpack", "Navy blue Jansport backpack.", "Gokongwei Building, 2F", "June 20, 2026", "Lost", R.drawable.placeholder_image));
        myItems.add(new Item("Umbrella", "Black foldable umbrella.", "La Salle Hall, Entrance", "June 17, 2026", "Found", R.drawable.placeholder_image));
        return myItems;
    }

    public static List<NotificationItem> getNotifications() {
        List<NotificationItem> notifs = new ArrayList<>();
        notifs.add(new NotificationItem("Item Claimed", "Someone has claimed your 'Blue Backpack' posting.", "2 hours ago"));
        notifs.add(new NotificationItem("New Match", "A found item matching your lost 'iPhone 15 Pro' was posted.", "5 hours ago"));
        notifs.add(new NotificationItem("Listing Approved", "Your posting 'Umbrella' has been approved and is now live.", "1 day ago"));
        notifs.add(new NotificationItem("Reminder", "Your lost item 'Graphing Calculator' has been posted for 5 days.", "2 days ago"));
        notifs.add(new NotificationItem("Welcome!", "Welcome to ArcherFind. Start by posting a lost or found item.", "3 days ago"));
        return notifs;
    }
}
