package com.firebase.drawing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.HashSet;
import java.util.Set;

/**
 * This static class handles the sync-state of the boards. Any changes to synced boards are automatically
 * downloaded when the application is active, even when the user is not looking at that board.
 *
 * Whether a board is synced on this device is kept in SharedPreferences on the device itself, since
 * each user can have their own preference for what board(s) to keep synced.
 */
public class SyncedBoardManager {
    public static final String PREFS_NAME = "DoodleBoardPrefs";
    public static final String PREF_NAME = "SyncedBoards";
    public static final String TAG = "AndroidDrawing";

    private static Context mContext;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static void restoreSyncedBoards(Firebase boardsRef) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> syncedBoards = preferences.getStringSet(PREF_NAME, new HashSet<String>());
        for (String key: syncedBoards) {
            Log.i(TAG, "Keeping board "+key+" synced");
            boardsRef.child(key).keepSynced(true);
        }
    }

    public static boolean isSynced(String boardId) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> syncedBoards = preferences.getStringSet(PREF_NAME, new HashSet<String>());
        return syncedBoards.contains(boardId);
    }

    public static void toggle(Firebase boardsRef, String boardId) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> syncedBoards = new HashSet<>(preferences.getStringSet(PREF_NAME, new HashSet<String>()));
        if (syncedBoards.contains(boardId)) {
            syncedBoards.remove(boardId);
            boardsRef.child(boardId).keepSynced(false);
        }
        else {
            syncedBoards.add(boardId);
            boardsRef.child(boardId).keepSynced(true);
        }
        preferences.edit().putStringSet(PREF_NAME, syncedBoards).commit();
        Log.i(TAG, "Board " + boardId + " is now " + (syncedBoards.contains(boardId) ? "" : "not ") + "synced");
    }

}
