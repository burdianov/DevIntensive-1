package com.softdesign.devintensive.data.storage.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.utils.Const;

public class UserLoginDataOperation extends BaseChronosOperation<String> {

    private String mLogin;

    public UserLoginDataOperation() {
        this.mAction = Action.LOAD;
    }

    public UserLoginDataOperation(String s) {
        this.mLogin = s;
        this.mAction = Action.SAVE;
    }

    public UserLoginDataOperation(Action action) {
        if (action == Action.SAVE) return; //only CLEAR and LOAD allowed this way
        this.mAction = action;
    }

    @Nullable
    @Override
    public String run() {
        switch (this.mAction) {
            case CLEAR:
                SharedPreferences.Editor edit = SHARED_PREFERENCES.edit();
                edit.remove(Const.BUILTIN_ACCESS_USER_ID);
                edit.remove(Const.BUILTIN_ACCESS_TOKEN);
                return null;
            case SAVE:
                SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
                if (mLogin == null) {
                    editor.remove(Const.SAVE_LOGIN);
                    editor.remove(Const.SAVED_LOGIN_NAME);
                } else {
                    editor.putBoolean(Const.SAVE_LOGIN, true);
                    editor.putString(Const.SAVED_LOGIN_NAME, mLogin);
                }
                editor.apply();
                return null;
            case LOAD:
                if (SHARED_PREFERENCES.getBoolean(Const.SAVE_LOGIN, false))
                    return SHARED_PREFERENCES.getString(Const.SAVED_LOGIN_NAME, "");
                else return null;
        }
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<String>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<String> {
    }
}

