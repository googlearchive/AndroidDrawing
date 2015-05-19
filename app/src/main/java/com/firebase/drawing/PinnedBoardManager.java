package com.firebase.drawing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by puf on 5/17/15.
 */
public class PinnedBoardManager {
    public static final String PREFS_NAME = "DoodleBoardPrefs";
    public static final String PREF_NAME = "PinnedBoards";

    private static Context mContext;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static void restorePinnedBoards(Firebase boardsRef) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> pinnedBoards = preferences.getStringSet(PREF_NAME, new HashSet<String>());
        for (String key: pinnedBoards) {
            Log.i("AndroidDrawing", "Pinning board "+key);
            boardsRef.child(key).pin();
        }
    }

    public static boolean isPinned(String boardId) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> pinnedBoards = preferences.getStringSet(PREF_NAME, new HashSet<String>());
        return pinnedBoards.contains(boardId);
    }

    public static void toggle(Firebase boardsRef, String boardId) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        Set<String> pinnedBoards = new HashSet<>(preferences.getStringSet(PREF_NAME, new HashSet<String>()));
        if (pinnedBoards.contains(boardId)) {
            pinnedBoards.remove(boardId);
            boardsRef.child(boardId).unpin();
        }
        else {
            pinnedBoards.add(boardId);
            boardsRef.child(boardId).pin();
        }
        preferences.edit().putStringSet(PREF_NAME, pinnedBoards).commit();
        Log.i("AndroidDrawing", "Board " + boardId + " is now " + (pinnedBoards.contains(boardId) ? "" : "not ") + "pinned");
    }

}
