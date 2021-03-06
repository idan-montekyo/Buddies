package com.example.buddies.ViewModel;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.buddies.Model.Model;
import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Comment;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.enums.ePostType;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationRequestEventHandler;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationResponseEventHandler;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardRequestEventHandler;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardResponseEventHandler;
import com.example.buddies.interfaces.LoadPostCommentsEvent.ILoadPostCommentsRequestEventHandler;
import com.example.buddies.interfaces.LoadPostCommentsEvent.ILoadPostCommentsResponsesEventHandler;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsRequestEventHandler;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsResponseEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
import com.example.buddies.interfaces.LocationSelectionEvent.ILocationSelect_EventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginRequestEventHandler;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutRequestEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationRequestEventHandler;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationResponseEventHandler;
import com.example.buddies.interfaces.ResolveUIDToUserProfileEvent.IResolveUIDToUserProfileRequestEventHandler;
import com.example.buddies.interfaces.ResolveUIDToUserProfileEvent.IResolveUIDToUserProfileResponsesEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupRequestEventHandler;
import com.example.buddies.interfaces.SignupEvent.ISignupResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.MVVM.IViewModel;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListRequestEventHandler;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListResponsesEventHandler;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileRequestEventHandler;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileResponsesEventHandler;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ViewModel implements IViewModel,
                                  ILocationSelect_EventHandler,
                                  ILoginRequestEventHandler,
                                  ILoginResponsesEventHandler,
                                  ILogoutRequestEventHandler,
                                  ILogoutResponsesEventHandler,
                                  ISignupRequestEventHandler,
                                  ISignupResponsesEventHandler,
                                  IPostCreationRequestEventHandler,
                                  IPostCreationResponseEventHandler,
                                  IUpdateCitiesAutocompleteListRequestEventHandler,
                                  IUpdateCitiesAutocompleteListResponsesEventHandler,
                                  IUpdateProfileRequestEventHandler,
                                  IUpdateProfileResponsesEventHandler,
                                  ILoadUserProfileRequestEventHandler,
                                  ILoadUserProfileResponseEventHandler,
                                  ILoadPostsRequestEventHandler,
                                  ILoadPostsResponseEventHandler,
                                  ILoadPostCommentsRequestEventHandler,
                                  ILoadPostCommentsResponsesEventHandler,
                                  ILoadPostCardRequestEventHandler,
                                  ILoadPostCardResponseEventHandler,
                                  IResolveUIDToUserProfileRequestEventHandler,
                                  IResolveUIDToUserProfileResponsesEventHandler,
                                  ICommentCreationRequestEventHandler,
                                  ICommentCreationResponseEventHandler {
    
    private static ViewModel _instance = null;
    private Model m_Model = null;

    List<IView> views = new ArrayList<IView>();

    private ViewModel() {
        this.m_Model = Model.getInstance();
        this.m_Model.registerForEvents(((IViewModel) this));
    }

    public static ViewModel getInstance() {
        if (_instance == null) {
            _instance = new ViewModel();
        }

        return _instance;
    }

    @Override
    public void registerForEvents(IView i_NewView) {
        this.views.add(i_NewView);
    }

    @Override
    public void unregisterForEvents(IView i_ViewToUnregister) {
        this.views.remove(i_ViewToUnregister);
    }

    @Override
    public void onLocationSelected(LatLng i_SelectedLocation) {
        for (IView view : views) {
            if (view instanceof ILocationSelect_EventHandler) {
                ((ILocationSelect_EventHandler) view).onLocationSelected(i_SelectedLocation);
            }
        }
    }

    /*
    ****************************************************************************************************
                                            TASK: Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogin(Context i_Context, String i_UserName, String i_Password) {
        this.m_Model.onRequestToLogin(i_Context, i_UserName, i_Password);
    }

    @Override
    public void onSuccessToLogin() {
        for (IView view : views) {
            if (view instanceof ILoginResponsesEventHandler) {
                ((ILoginResponsesEventHandler) view).onSuccessToLogin();
            }
        }
    }

    @Override
    public void onFailureToLogin(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoginResponsesEventHandler) {
                ((ILoginResponsesEventHandler) view).onFailureToLogin(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                        TASK: Anonymous Login
    ****************************************************************************************************
    */

    @Override
    public void onRequestToAnonymousLogin(Context i_Context) {
        this.m_Model.onRequestToAnonymousLogin(i_Context);
    }

    @Override
    public void onSuccessToAnonymousLogin() {
        for (IView view : views) {
            if (view instanceof ILoginResponsesEventHandler) {
                ((ILoginResponsesEventHandler) view).onSuccessToAnonymousLogin();
            }
        }
    }

    @Override
    public void onFailureToAnonymousLogin(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoginResponsesEventHandler) {
                ((ILoginResponsesEventHandler) view).onFailureToAnonymousLogin(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                            TASK: Logout
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLogout(Context i_Context) {
        this.m_Model.onRequestToLogout(i_Context);
    }

    @Override
    public void onSuccessToLogout() {
        for (IView view : views) {
            if (view instanceof ILogoutResponsesEventHandler) {
                ((ILogoutResponsesEventHandler) view).onSuccessToLogout();
            }
        }
    }

    @Override
    public void onFailureToLogout(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILogoutResponsesEventHandler) {
                ((ILogoutResponsesEventHandler) view).onFailureToLogout(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                            TASK: Signup
    ****************************************************************************************************
    */

    @Override
    public void onRequestToSignup(Context i_Context, String i_UserName, String i_Password, String i_FullName, String i_Age, eDogGender i_DogGender) {
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToSignup", "calling onRequestToSignup()");
        this.m_Model.onRequestToSignup(i_Context, i_UserName, i_Password, i_FullName, i_Age, i_DogGender);
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToSignup", "returned from onRequestToSignup()");
    }

    @Override
    public void onSuccessToSignup() {
        for (IView view : views) {
            if (view instanceof ISignupResponsesEventHandler) {
                ((ISignupResponsesEventHandler) view).onSuccessToSignup();
            }
        }
    }

    @Override
    public void onFailureToSignup(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ISignupResponsesEventHandler) {
                ((ISignupResponsesEventHandler) view).onFailureToSignup(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                         TASK: Create Post
    ****************************************************************************************************
    */

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToCreatePost(Context i_Context, String i_UserID, String i_CityOfMeeting, String i_StreetOfMeeting, String i_DateOfMeeting, String i_TimeOfMeeting, LatLng i_LocationOfMeeting, String i_ContentOfPost) {
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToCreatePost", "calling onRequestToCreatePost()");
        this.m_Model.onRequestToCreatePost(i_Context, i_UserID, i_CityOfMeeting, i_StreetOfMeeting, i_DateOfMeeting, i_TimeOfMeeting, i_LocationOfMeeting, i_ContentOfPost);
        AppUtils.printDebugToLogcat("ViewModel", "onRequestToCreatePost", "returned from onRequestToCreatePost()");

    }

    @Override
    public void onSuccessToCreatePost(Post i_Post) {
        for (IView view : views) {
            if (view instanceof IPostCreationResponseEventHandler) {
                ((IPostCreationResponseEventHandler) view).onSuccessToCreatePost(i_Post);
            }
        }
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof IPostCreationResponseEventHandler) {
                ((IPostCreationResponseEventHandler) view).onFailureToCreatePost(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                      TASK: Update Cities List
    ****************************************************************************************************
    */

    @Override
    public void onRequestToUpdateListOfCities() {
        ((IUpdateCitiesAutocompleteListRequestEventHandler) this.m_Model).onRequestToUpdateListOfCities();
    }

    @Override
    public void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities) {
        for (IView view : views) {
            if (view instanceof IUpdateCitiesAutocompleteListResponsesEventHandler) {
                ((IUpdateCitiesAutocompleteListResponsesEventHandler) view).onSuccessToUpdateListOfCities(i_UpdatedListOfCities);
            }
        }
    }

    @Override
    public void onFailureToUpdateListOfCities(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof IUpdateCitiesAutocompleteListResponsesEventHandler) {
                ((IUpdateCitiesAutocompleteListResponsesEventHandler) view).onFailureToUpdateListOfCities(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Profile
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLoadProfile() {
        m_Model.onRequestToLoadProfile();
    }

    @Override
    public void onSuccessToLoadProfile(UserProfile i_UserProfile) {
        for (IView view : views) {
            if (view instanceof ILoadUserProfileResponseEventHandler) {
                ((ILoadUserProfileResponseEventHandler) view).onSuccessToLoadProfile(i_UserProfile);
            }
        }
    }

    @Override
    public void onFailureToLoadProfile(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoadUserProfileResponseEventHandler) {
                ((ILoadUserProfileResponseEventHandler) view).onFailureToLoadProfile(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                       TASK: Update Profile Info
    ****************************************************************************************************
    */

    @Override
    public void onRequestToUpdateProfile(Context i_Context, String i_FullName, String i_Age, eDogGender i_DogGender, String i_ProfileImage) {
        this.m_Model.onRequestToUpdateProfile(i_Context, i_FullName, i_Age, i_DogGender, i_ProfileImage);
    }

    @Override
    public void onSuccessToUpdateProfile() {
        for (IView view : views) {
            if (view instanceof IUpdateProfileResponsesEventHandler) {
                ((IUpdateProfileResponsesEventHandler) view).onSuccessToUpdateProfile();
            }
        }
    }

    @Override
    public void onFailureToUpdateProfile(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof IUpdateProfileResponsesEventHandler) {
                ((IUpdateProfileResponsesEventHandler) view).onFailureToUpdateProfile(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Posts
    ****************************************************************************************************
    */

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToLoadPosts(ePostType type) {
        m_Model.onRequestToLoadPosts(type);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToLoadPostsByCity(String i_SearchedCity) {
        m_Model.onRequestToLoadPostsByCity(i_SearchedCity);
    }

    @Override
    public void onSuccessToLoadPosts(List<Post> i_PostsList) {
        for (IView view : views) {
            if (view instanceof ILoadPostsResponseEventHandler) {
                ((ILoadPostsResponseEventHandler) view).onSuccessToLoadPosts(i_PostsList);
            }
        }
    }

    @Override
    public void onFailureToLoadPosts(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoadPostsResponseEventHandler) {
                ((ILoadPostsResponseEventHandler) view).onFailureToLoadPosts(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                         TASK: Load Post Cards
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate) {
        m_Model.onRequestToLoadPostCard(i_CreatorUserUID, i_PostAdapterToUpdate);
    }

    @Override
    public void onSuccessToLoadPostCard(UserProfile i_UserProfile, PostAdapter i_PostAdapterToUpdate) {
        ((ILoadPostCardResponseEventHandler) i_PostAdapterToUpdate).onSuccessToLoadPostCard(i_UserProfile, i_PostAdapterToUpdate);
    }

    @Override
    public void onFailureToLoadPostCard(Exception i_Reason, PostAdapter i_PostAdapterToUpdate) {
        ((ILoadPostCardResponseEventHandler) i_PostAdapterToUpdate).onFailureToLoadPostCard(i_Reason, i_PostAdapterToUpdate);
    }

    /*
    ****************************************************************************************************
                                      TASK: Resolve UID To User Profile
    ****************************************************************************************************
    */

    @Override
    public void onRequestToResolveUIDToUserProfile(String i_UserIDToResolve, IView i_Caller) {
        this.m_Model.onRequestToResolveUIDToUserProfile(i_UserIDToResolve, i_Caller);
    }

    @Override
    public void onSuccessToResolveUIDToUserProfile(UserProfile i_ResolvedUserProfile, IView i_Caller) {
        ((IResolveUIDToUserProfileResponsesEventHandler) i_Caller).onSuccessToResolveUIDToUserProfile(i_ResolvedUserProfile, i_Caller);
    }

    @Override
    public void onFailureToResolveUIDToUserProfile(Exception i_Reason, IView i_Caller) {
        ((IResolveUIDToUserProfileResponsesEventHandler) i_Caller).onFailureToResolveUIDToUserProfile(i_Reason, i_Caller);
    }

    /*
    ****************************************************************************************************
                                         TASK: Create Comment
    ****************************************************************************************************
    */

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToCreateComment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent, String i_PostID) {
        m_Model.onRequestToCreateComment(i_CreatorUserUID, i_UserProfileImageUri, i_CommentContent, i_PostID);
    }

    @Override
    public void onSuccessToCreateComment(Comment i_Comment) {
        for (IView view : views) {
            if (view instanceof ICommentCreationResponseEventHandler) {
                ((ICommentCreationResponseEventHandler) view).onSuccessToCreateComment(i_Comment);
            }
        }
    }

    @Override
    public void onFailureToCreateComment(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ICommentCreationResponseEventHandler) {
                ((ICommentCreationResponseEventHandler) view).onFailureToCreateComment(i_Reason);
            }
        }
    }

    /*
    ****************************************************************************************************
                                  TASK: Load Comments For A Specific Post
    ****************************************************************************************************
    */

    @Override
    public void onRequestToLoadPostComments(String i_PostID) {
        this.m_Model.onRequestToLoadPostComments(i_PostID);
    }

    @Override
    public void onSuccessToLoadPostComments(List<Comment> i_Comments) {
        for (IView view : views) {
            if (view instanceof ILoadPostCommentsResponsesEventHandler) {
                ((ILoadPostCommentsResponsesEventHandler) view).onSuccessToLoadPostComments(i_Comments);
            }
        }
    }

    @Override
    public void onFailureToLoadPostComments(Exception i_Reason) {
        for (IView view : views) {
            if (view instanceof ILoadPostCommentsResponsesEventHandler) {
                ((ILoadPostCommentsResponsesEventHandler) view).onFailureToLoadPostComments(i_Reason);
            }
        }
    }
}