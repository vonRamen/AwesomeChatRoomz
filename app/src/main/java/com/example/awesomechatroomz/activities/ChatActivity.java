package com.example.awesomechatroomz.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.awesomechatroomz.R;
import com.example.awesomechatroomz.activities.fragments.UserChatInputFragment;
import com.example.awesomechatroomz.adapters.ChatMessagesAdapter;
import com.example.awesomechatroomz.components.LoginComponent;
import com.example.awesomechatroomz.repository.ActiveChatInstance;
import com.example.awesomechatroomz.domain.ActiveChatManager;
import com.example.awesomechatroomz.repository.ImageManager;
import com.example.awesomechatroomz.repository.LoginManager;
import com.example.awesomechatroomz.models.ChatRoom;
import com.example.awesomechatroomz.models.User;
import com.example.awesomechatroomz.services.ChatRoomSubscriptionService;

import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.DaggerFragment;

public class ChatActivity extends AppCompatActivity implements HasAndroidInjector, UserChatInputFragment.OnFragmentInteractionListener {
    private LoginComponent comp;
    @Inject
    DispatchingAndroidInjector<Object> activityDispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<DaggerFragment> fragmentDispatchingAndroidInjector;

    @Inject
    ActiveChatManager chatManager;

    private boolean requestedSubscribe;

    ActiveChatInstance chatInstance;

    @Inject
    LoginManager loginManager;

    private ChatRoom room;

    @Inject
    ImageManager imgManager;

    @Inject
    ChatMessagesAdapter adapter;

    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupRecyclerView();


        Uri data = getIntent().getData();
        getSupportActionBar().setTitle(getChatRoomName());
        loginManager.AttemptAutoLogin(new LoginManager.LoginCallback() {
            @Override
            public void OnFinished(User user) {
                room = new ChatRoom(user);
                room.setName(getChatRoomName());
                chatInstance = chatManager.create(room);
                adapter.setActiveInstance(chatInstance);

                //Adapter must subscribe from main thread.
                new Handler(getApplicationContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.prepare();
                    }
                });
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.getOlderMessages(new ChatMessagesAdapter.ChatMessagesAdapterListener() {
                    @Override
                    public void onGetOlderDone() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.chat_activity_recyclerView);
        swipeRefreshLayout = findViewById(R.id.chat_activity_swipeLayout);
        layoutManager = new LinearLayoutManager(this);

        final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_END;
            }
        };
        recyclerView.setLayoutManager(layoutManager);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                smoothScroller.setTargetPosition(adapter.getItemCount());
                layoutManager.startSmoothScroll(smoothScroller);
            }
        });


        recyclerView.setAdapter(adapter);
    }

    private String getChatRoomName() {
        if (getIntent().getData() != null) {
            return getIntent().getData().toString().split("=")[1];
        } else {
            return getIntent().getStringExtra("chat_room");
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTextSend(String text) {
        chatInstance.sendMessage(text);
        subscribe();
    }

    private void subscribe() {
        if (!requestedSubscribe) {
            if (chatManager.alreadySubscribedTo(this.chatInstance)) {
                requestedSubscribe = true;
                return;
            }
            new AlertDialog.Builder(this).setTitle("Subscribe?").setMessage("Do you want to receive notifications, when new messages arrive?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestedSubscribe = true;
                            chatManager.getSubscribedTo().add(chatInstance);
                            Intent i = new Intent(new Intent(ChatActivity.this, ChatRoomSubscriptionService.class));
                            startService(i);
                        }
                    })
                    .setNegativeButton("No thanks!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestedSubscribe = true;
                        }
                    }).show();

        }
    }

    @Override
    public void onImageUploadRequest(final Uri imageUri) {
        try {
            chatInstance.sendImage(imageUri);
        } catch (IOException e) {
            new AlertDialog.Builder(this).setTitle("Image Message Failed!").setMessage("Something unexpected happened while uploading your picture..\nPlease try again.").show();
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraUploadRequest(Bitmap imageBitmap) {
        try {
            chatInstance.sendImage(imageBitmap);
        } catch (IOException e) {
            new AlertDialog.Builder(this).setTitle("Image Message Failed!").setMessage("Something unexpected happened while uploading your picture..\nPlease try again.").show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public AndroidInjector androidInjector() {
        return activityDispatchingAndroidInjector;
    }
}
