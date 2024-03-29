package com.example.awesomechatroomz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomechatroomz.R;
import com.example.awesomechatroomz.domain.ChatManager;
import com.example.awesomechatroomz.models.ChatRoom;

import java.util.List;

import javax.inject.Inject;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ChatViewHolder> {
    private ChatRoomEvent itemClickListener;
    private ChatManager manager;
    private List<ChatRoom> chatRooms;

    public interface ChatRoomEvent {
        void onChatRoomClicked(ChatRoom room);
    }
    public interface ChatRoomRefreshEvent {
        void refresh();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView chatroomTitleTextView;
        public TextView chatroomDescriptionTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.chatroomTitleTextView = itemView.findViewById(R.id.chat_room_title);
            this.chatroomDescriptionTextView = itemView.findViewById(R.id.chat_room_description);
        }
    }

    public void setOnClickListener(ChatRoomEvent listener) {
        itemClickListener = listener;
    }

    @Inject
    public ChatRoomsAdapter(ChatManager manager) {//To be continued.. Pass the data from repository. (Inject)
        this.manager = manager;
        refresh();
    }

    public void refresh(final ChatRoomRefreshEvent event) {
        LiveData<List<ChatRoom>> rooms = manager.getChatRooms();


        rooms.observeForever(new Observer<List<ChatRoom>>() {
            @Override
            public void onChanged(List<ChatRoom> chatRooms) {
                System.out.println(chatRooms.size());
                ChatRoomsAdapter.this.chatRooms = chatRooms;
                ChatRoomsAdapter.this.notifyDataSetChanged();

                if(event!=null) {
                    event.refresh();
                }
            }
        });
    }

    public void refresh() {
        refresh(null);
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_menu_chat, parent, false);


        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        final ChatRoom current = this.chatRooms.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onChatRoomClicked(current);
            }
        });

        holder.chatroomDescriptionTextView.setText(current.getDescription());
        holder.chatroomTitleTextView.setText(current.getName());
    }


    @Override
    public int getItemCount() {
        List<ChatRoom> chatRooms = this.chatRooms;
        return chatRooms != null ? chatRooms.size() : 0;
    }
}
