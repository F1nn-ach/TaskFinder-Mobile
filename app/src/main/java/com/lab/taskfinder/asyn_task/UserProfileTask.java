package com.lab.taskfinder.asyn_task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.lab.taskfinder.CallBackService;
import com.lab.taskfinder.model.UserModel;


public class UserProfileTask extends AsyncTask {
    private CallBackService callBackService;

    public UserProfileTask(Context context) {
        callBackService = (CallBackService) context;
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();
    //private Context context;

    @Override
    protected String doInBackground(Object[] objects) {
        if (objects.length > 1) {
            RequestBody body = RequestBody.create(JSON, objects[1].toString());
            Request request = new Request.Builder()
                    .url(objects[0].toString())
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Handle the case when there is no body to send
            Request request = new Request.Builder()
                    .url(objects[0].toString())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public ArrayList<UserModel> onParserContentToModel(String dataJSon) {
        Log.e("data json", dataJSon);
        ArrayList<UserModel> userList = new ArrayList<UserModel>();
        try {
            JSONObject jsonObject = new JSONObject(dataJSon);
            JSONObject jsonuser = jsonObject.optJSONObject("result");
            if (jsonuser != null) {
                UserModel userModel = new UserModel();
                userModel.getUser().setUsername(jsonuser.getString("username"));
                userModel.getUser().setFirstname(jsonuser.getString("firstname"));
                userModel.getUser().setLastname(jsonuser.getString("lastname"));
                userModel.getUser().setEmail(jsonuser.getString("email"));
                userModel.getUser().setPassword(jsonuser.getString("password"));
                userModel.getUser().setPhoneNumber(jsonuser.getString("phoneNumber"));
                userModel.getUser().setGender(jsonuser.getString("gender"));
                userModel.getUser().setBirthday(jsonuser.getString("birthday"));
                userList.add(userModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (callBackService != null) {
            ArrayList<UserModel> userList = onParserContentToModel(o.toString());
            callBackService.onRequestCompleteListener(userList);
        }
    }
}
