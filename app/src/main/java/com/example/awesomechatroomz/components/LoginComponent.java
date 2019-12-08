package com.example.awesomechatroomz.components;

import android.app.Application;

import com.example.awesomechatroomz.activities.ChatActivity;
import com.example.awesomechatroomz.activities.ChatMenuActivity;
import com.example.awesomechatroomz.activities.MainActivity;
import com.example.awesomechatroomz.activities.SplashScreenActivity;
import com.example.awesomechatroomz.activities.fragments.UserChatInputFragment;
import com.example.awesomechatroomz.implementations.FacebookLoginMethod;
import com.example.awesomechatroomz.models.LoggedInUser;
import com.example.awesomechatroomz.models.User;
import com.example.awesomechatroomz.modules.AppModule;
import com.example.awesomechatroomz.modules.HelloWorldModule;
import com.example.awesomechatroomz.modules.LoggedUserModule;
import com.example.awesomechatroomz.modules.PersistenceModule;
import com.example.awesomechatroomz.modules.RoomModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules={
        RoomModule.class,
        LoggedUserModule.class,
        PersistenceModule.class,
        AppModule.class})
public interface LoginComponent {
    public User getLoggedInUser();
    public void inject(MainActivity activity);
    public void inject(ChatMenuActivity activity);
    public void inject(SplashScreenActivity activity);
    public void inject(ChatActivity activity);
    public FacebookLoginMethod getFacebookLoginMethod();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        LoginComponent build();
        Builder roomModule(RoomModule roomModule);
        Builder loggedUserModule(LoggedUserModule roomModule);
        Builder persistenceModule(PersistenceModule roomModule);
        Builder appModule(AppModule roomModule);
    }
}
