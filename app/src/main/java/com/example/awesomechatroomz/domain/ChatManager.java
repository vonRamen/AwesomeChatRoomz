package com.example.awesomechatroomz.domain;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.awesomechatroomz.models.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private DatabaseReference database;

    @Inject
    public ChatManager(DatabaseReference reference) {
        this.database = reference;
    }

    /**
     * @return the live data of all chat rooms.
     */
    public LiveData<List<ChatRoom>> getChatRooms() {
        final DatabaseReference chatRooms = database.child("chat_rooms");
        final MutableLiveData<List<ChatRoom>> chatRoomsList = new MutableLiveData<>();
        chatRoomsList.setValue(new ArrayList<ChatRoom>());

        //Queries in sorted by last updated.
        chatRooms.orderByChild("lastUpdated").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoom room;
                ArrayList<ChatRoom> chatRooms = new ArrayList<>();

                //The data is added manually to avoid all the redundant data.
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    room = new ChatRoom();

                    room.setName(shot.child("name").getValue(String.class));
                    room.setDescription(shot.child("description").getValue(String.class));

                    Long lastUpdated = shot.child("lastUpdated").getValue(long.class);

                    room.setLastUpdated(lastUpdated);

                    chatRooms.add(0, room);
                }

                chatRoomsList.postValue(chatRooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return chatRoomsList;
    }
}
