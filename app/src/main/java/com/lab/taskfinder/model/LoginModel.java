package com.lab.taskfinder.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoginModel {
    Login login = new Login();

    public LoginModel(){
        login = new LoginModel.Login();
    }

    public LoginModel(String jsonResponse) {
        Gson gson = new GsonBuilder().create();
        login = gson.fromJson(jsonResponse, LoginModel.Login.class);
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this.login);
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public class Login {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
