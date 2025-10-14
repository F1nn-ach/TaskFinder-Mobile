package com.lab.taskfinder.manager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.lab.taskfinder.R;
import com.lab.taskfinder.asyn_task.*;
import com.lab.taskfinder.model.LoginModel;
import com.lab.taskfinder.model.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WSManager {
    private static WSManager wsManager;
    private Context context;

    public interface WSManagerListener {
        void onComplete(Object response);

        void onError(String err);
    }

    public WSManager(Context context) {
        this.context = context;
    }

    public static WSManager getWsManager(Context context) {
        if (wsManager == null)
            wsManager = new WSManager(context);
        return wsManager;
    }

    public void doRegister(Object object, final WSManagerListener listener) {
        if (!(object instanceof UserModel)) {
            return;
        }
        UserModel userModel = (UserModel) object;
        userModel.toJSONString();
        WSTask task = new WSTask(this.context,new WSTask.WSTaskListener() {
            @Override
            public void onComplete(String response) {
                UserModel userModel = new UserModel(response);
                listener.onComplete(userModel);
            }

            @Override
            public void onError(String err) {
                listener.onError(err);
            }
        });

        Log.d("data ", userModel.toJSONString());
        task.execute(context.getString(R.string.register_url), userModel.toJSONString());
    }

    public void doLogin(Object object, final WSManagerListener listener) {
        if (!(object instanceof LoginModel)) {
            return;
        }

        LoginModel loginModel = (LoginModel) object;
        String loginData = loginModel.toJSONString();

        WSTask task = new WSTask(this.context, new WSTask.WSTaskListener() {
            @Override
            public void onComplete(String response) {
                listener.onComplete(response);
            }

            @Override
            public void onError(String err) {
                listener.onError(err);
            }
        });

        Log.d("data ", loginData);
        task.execute(context.getString(R.string.login_url), loginData);
    }

    public void listUser(Object object, final WSManagerListener listener) {
        if (!(object instanceof UserModel)) {
            return;
        }
        WSTask task = new WSTask(this.context,new WSTask.WSTaskListener() {
            @Override
            public void onComplete(String response) {
                Log.d("search ", response);
                JSONArray jarr = null;
                try {
                    JSONObject json = new JSONObject(response);
                    jarr = (JSONArray) json.get("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Gson gson = new Gson();
                UserModel.User[] userArray = gson.fromJson(jarr.toString(), UserModel.User[].class);

                listener.onComplete(userArray);
            }

            @Override
            public void onError(String err) {
                listener.onError(err);
            }
        });

        task.execute(context.getString(R.string.list_user), "");
    }

    public void doUpdateUser(Object object, final WSManagerListener listener) {
        if (!(object instanceof UserModel)) {
            return;
        }
        UserModel userModel = (UserModel) object;
        WSTask task = new WSTask(this.context, new WSTask.WSTaskListener() {
            @Override
            public void onComplete(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("status") && jsonResponse.getString("status").equals("success")) {
                        UserModel updatedUserModel = new UserModel(response);
                        listener.onComplete(updatedUserModel);
                    } else {
                        String errorMessage = jsonResponse.has("message") ?
                                jsonResponse.getString("message") : "Update user failed with unknown error";
                        listener.onError(errorMessage);
                    }
                } catch (JSONException e) {
                    listener.onError("Error parsing response: " + e.getMessage());
                }
            }

            @Override
            public void onError(String err) {
                listener.onError(err);
            }
        });

        Log.d("UpdateUser", "Sending update request: " + userModel.toJSONString());
        task.execute(context.getString(R.string.updateprofile), userModel.toJSONString());
    }

}
