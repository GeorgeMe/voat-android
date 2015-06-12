package co.voat.android;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import co.voat.android.api.AuthResponse;
import co.voat.android.api.UserResponse;
import co.voat.android.api.VoatClient;
import co.voat.android.data.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import timber.log.Timber;

/**
 * What are you going to do without signing in... not much
 * Created by Jawn on 6/12/2015.
 */
public class LoginDialog extends AppCompatDialog {

    private static final String PARAM_GRANT_TYPE = "grant_type=password";
    private static final String PARAM_USERNAME = "&username=";
    private static final String PARAM_PASSWORD = "&password=";

    @InjectView(R.id.username)
    EditText usernameEditText;
    @InjectView(R.id.password)
    EditText passwordEditText;

    @OnClick(R.id.login)
    void onLoginClick(View v) {
        final String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean hasError = false;
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("No!");
            hasError = true;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("No!");
            hasError = true;
        }
        if (!hasError) {
            //TODO chain the auth and retrieving the user using RxAndroid
            VoatClient.instance().login(createLoginString(username, password), authResponseCallback);
        }
    }

    private final Callback<AuthResponse> authResponseCallback = new Callback<AuthResponse>() {
        @Override
        public void success(AuthResponse authResponse, Response response) {
            Timber.d("Login success");
            VoatClient.setAuth(authResponse);
            VoatClient.instance().getUserInfo(authResponse.userName, userResponseCallback);
        }

        @Override
        public void failure(RetrofitError error) {
            Timber.e(error.toString());
        }
    };

    private final Callback<UserResponse> userResponseCallback = new Callback<UserResponse>() {
        @Override
        public void success(UserResponse userResponse, Response response) {
            User.setCurrentUser(userResponse.data);
            dismiss();
        }

        @Override
        public void failure(RetrofitError error) {
            Timber.e(error.toString());
        }
    };

    public LoginDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_login);
        ButterKnife.inject(this);
    }

    private TypedString createLoginString(String username, String password) {
        return new TypedString(
                PARAM_GRANT_TYPE + PARAM_USERNAME + username + PARAM_PASSWORD + password
        );
    }
}