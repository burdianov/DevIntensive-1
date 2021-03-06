package com.softdesign.devintensive.utils;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.binding.fields.ObservableString;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.api.res.SoftDesignApiErrorRes;
import com.softdesign.devintensive.data.network.restmodels.AddRepo;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.data.storage.viewmodels.RepoViewModel;
import com.softdesign.devintensive.databinding.ItemAlertDialogBinding;
import com.softdesign.devintensive.databinding.ItemInputDialogBinding;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Helper class to work with UI
 */
@SuppressWarnings({"unused", "deprecation"})
public class AppUtils {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final BounceInterpolator BOUNCE_INTERPOLATOR = new BounceInterpolator();
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    //region :::::::::::::::::::::::::::::::::::::::::: Network

    /**
     * Checks if network is available at this time
     */
    public static Boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Animation

    public static ObjectAnimator getFadeXAnimator(View view, int duration) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.1f);
        anim.setDuration(duration);
        anim.setInterpolator(DECELERATE_INTERPOLATOR);
        return anim;
    }

    public static ObjectAnimator getFadeYAnimator(View view, int duration) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.1f);
        anim.setDuration(duration);
        anim.setInterpolator(DECELERATE_INTERPOLATOR);
        return anim;
    }

    @NonNull
    public static ObjectAnimator getGrowXAnimator(ImageView view, int duration) {
        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(view, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(duration);
        bounceAnimX.setInterpolator(DECELERATE_INTERPOLATOR);
        return bounceAnimX;
    }

    @NonNull
    public static ObjectAnimator getGrowYAnimator(ImageView view, int duration) {
        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(view, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(duration);
        bounceAnimY.setInterpolator(DECELERATE_INTERPOLATOR);
        return bounceAnimY;
    }

    @NonNull
    public static ObjectAnimator getBounceXAnimator(ImageView view, int duration) {
        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1f);
        bounceAnimX.setDuration(duration);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);
        return bounceAnimX;
    }

    @NonNull
    public static ObjectAnimator getBounceYAnimator(ImageView view, int duration) {
        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1f);
        bounceAnimY.setDuration(duration);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        return bounceAnimY;
    }

    @NonNull
    public static ObjectAnimator getRotationAnimator(ImageView view, int duration, float degrees) {
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, degrees);
        rotationAnim.setDuration(duration);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        return rotationAnim;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Animation

    //region :::::::::::::::::::::::::::::::::::::::::: UI

    //region Calculations
    public static Drawable getMaskDrawable(Context context, int maskId) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(maskId);
        } else {
            drawable = context.getResources().getDrawable(maskId);
        }

        if (drawable == null) {
            throw new IllegalArgumentException("maskId is invalid");
        }

        return drawable;
    }

    public static int getScreenOrientation() {
        Display getOrient = DevIntensiveApplication.getCurrentActivity().getWindowManager().getDefaultDisplay();
        if (getOrient.getWidth() < getOrient.getHeight()) {
            return Configuration.ORIENTATION_PORTRAIT;
        } else {
            return Configuration.ORIENTATION_LANDSCAPE;
        }
    }

    public static void openWebPage(@NonNull Context context, @Nullable String url) {
        if (url == null || url.isEmpty()) return;
        if (!url.startsWith("http")) url = "http://" + url;
        startActivity(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * @return StatusBarHeight in current context
     */
    public static int getStatusBarHeight() {
        @DimenRes
        int resourceId = CONTEXT.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return CONTEXT.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * @return Action bar height in current context
     */
    public static float getAppBarSize() {
        float appBarSize;
        final TypedArray styledAttributes = CONTEXT.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        appBarSize = styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return appBarSize;
    }

    /**
     * @param v examined view
     * @return minimum view height which this view needs to wrap its content
     */
    public static int getViewMinHeight(View v) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredHeight();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * sets ListView Height Based On its Children
     *
     * @param listView to resize
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        View listItem = listAdapter.getView(0, null, listView);
        listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
        totalHeight = listItem.getMeasuredHeight() + (listItem.getMeasuredHeight() * (listAdapter.getCount() - 1));

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    //endregion

    //region Announce
    public static void showSnackbar(Context context, @StringRes int stringRes, boolean isLong) {
        showSnackbar(context, context.getString(stringRes), isLong);
    }

    public static void showSnackbar(Context context, String text, boolean isLong) {
        if (context instanceof Activity) {
            View rootView = ((ViewGroup) ((Activity) context).findViewById(android.R.id.content)).getChildAt(0);
            if (rootView instanceof CoordinatorLayout)
                showSnackbar((CoordinatorLayout) rootView, text, isLong);
        }
    }

    public static void showSnackbar(CoordinatorLayout layout, @StringRes int stringRes, boolean isLong) {
        showSnackbar(layout, layout.getResources().getString(stringRes), isLong);
    }

    public static void showSnackbar(CoordinatorLayout layout, String text, boolean isLong) {
        Snackbar snack = Snackbar.make(layout, text,
                isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT);
        snack.setAction(android.R.string.ok, v -> {
        });
        snack.show();
    }

    public static void showInputDialog(Context context, String hint, ObservableString text) {
        String oldText = text.get();
        ItemInputDialogBinding dialogBinding = DataBindingUtil
                .inflate(LayoutInflater.from(context), R.layout.item_input_dialog, null, false);
        dialogBinding.setText(text);
        dialogBinding.setHint(hint);
        int margin = (int) context.getResources().getDimension(R.dimen.size_small_16);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> text.set(oldText));
        builder.setOnCancelListener(dialog -> text.set(oldText));
        builder.setView(dialogBinding.getRoot(), margin, margin, margin, margin);
        builder.show();
    }

    public static void showAlertDialog(Context context, String hint, String text) {
        ItemAlertDialogBinding dialogBinding = DataBindingUtil
                .inflate(LayoutInflater.from(context), R.layout.item_alert_dialog, null, false);
        dialogBinding.setText(text);
        dialogBinding.setHint(hint);
        int margin = (int) context.getResources().getDimension(R.dimen.size_small_16);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setView(dialogBinding.getRoot(), margin, margin, margin, margin);
        builder.show();
    }

    public static void showSnackbar(View layout, @StringRes int stringRes, boolean isLong,
                                    @StringRes int actionButtonText, View.OnClickListener listener) {
        Snackbar.make(layout, R.string.error_access_permissions_needed,
                isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)
                .setAction(actionButtonText, listener)
                .show();
    }
    //endregion

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: IO system methods

    /**
     * creates empty png file at SDCARD in folder Pictures with name IMG_yyyyMMdd_HHmmss_1238162378618.png
     *
     * @return file
     */
    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".png", storageDir);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        CONTEXT.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * creates image file with given name -> name.png  at internal storage
     *
     * @param fileName name
     * @return file
     */
    public static File createImageFromName(String fileName) {
        File image = new File(CONTEXT.getFilesDir(), fileName + ".png");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        CONTEXT.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * converts internal path like content:// into correct file path
     *
     * @param uri uri
     * @return correct filepath
     */
    public static String filePathFromUri(@NonNull Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = CONTEXT.getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            }
        } else {
            filePath = uri.getPath();
        }
        return filePath;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Packages and Activities methods

    private static void startActivity(@NonNull Context context, @NonNull Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * checks if there some apps that can handle this implicit intent
     *
     * @param intent to check
     * @return true if there any
     */
    public static Boolean queryIntentActivities(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.SENDTO")) {
            ComponentName emailApp = intent.resolveActivity(context.getPackageManager());
            ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
            return emailApp != null && !emailApp.equals(unsupportedAction);
        } else {
            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return activities.size() > 0;
        }
    }

    /**
     * @param context     cur context
     * @param packageName package to check
     * @return true if packageName is installed
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Converters
    public static String getJsonFromObject(Object object, Class<?> typeClass) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(object, typeClass);
    }

    public static Object getObjectFromJson(String json, Class<?> typeClass) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, typeClass);
    }

    public static String repoListIntoJson(List<Repo> list) {

        String[] array = new String[0];
        for (Repo r : list) {
            if (!isEmptyOrNull(r.getGit())) {
                array = java.util.Arrays.copyOf(array, array.length + 1);
                if (isEmptyOrNull(r.getId())) {
                    array[array.length - 1] = getJsonFromObject(new AddRepo(r.getGit()), AddRepo.class);
                } else {
                    array[array.length - 1] = getJsonFromObject(r, Repo.class);
                }
            }
        }

        return Arrays.toString(array);
    }

    public static ArrayList<String> repoModelIntoString(final List<RepoViewModel> list) {
        return new ArrayList<String>() {{
            for (RepoViewModel r : list) {
                add(r.getRepoUri());
            }
        }};
    }

    public static ArrayList<String> repoIntoString(final List<Repo> list) {
        return new ArrayList<String>() {{
            for (Repo r : list) {
                add(r.getGit());
            }
        }};
    }

    public static ArrayList<String> profileModelIntoString(final List<ProfileViewModel> list) {
        return new ArrayList<String>() {{
            for (ProfileViewModel r : list) {
                add(r.getRemoteId());
            }
        }};
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Comparison
    public static boolean compareRepoModelLists(List<RepoViewModel> list1, List<RepoViewModel> list2) {
        List<String> repoList1 = repoModelIntoString(list1);
        List<String> repoList2 = repoModelIntoString(list2);
        return compareLists(repoList1, repoList2);
    }

    public static boolean compareRepoLists(List<Repo> list1, List<Repo> list2) {
        ArrayList<String> tempList1 = repoIntoString(list1);
        for (Repo r : list2) {
            if (!tempList1.remove(r.getGit())) {
                return false;
            }
        }
        return tempList1.isEmpty();
    }

    public static <K, V> boolean compareMaps(final Map<K, V> map1, final Map<K, V> map2) {
        return compareLists(map1.keySet(), map2.keySet()) && compareLists(map1.values(), map2.values());
    }

    public static <T> boolean compareLists(final Collection<T> list1, final Collection<T> list2) {
        ArrayList<T> tempList1 = new ArrayList<>(list1);
        for (T o : list2) {
            if (!tempList1.remove(o)) {
                return false;
            }
        }
        return tempList1.isEmpty();
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static <T> T nN(T a, T b) {
        return !isEmptyOrNull(a) ? a : b;
    }

    /**
     * checks all args if they equals null or empty
     *
     * @param args array of args
     * @return true if null or empty
     */
    public static boolean isEmptyOrNull(Object... args) {
        for (Object s : args) {
            if (s == null || s.toString().trim().isEmpty())
                return true;
        }
        return false;
    }

    public static boolean isEmptyOrNull(List<?> list) {
        return list == null || list.isEmpty();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Errors

    /**
     * Converts received from server http error into human-readable error
     */
    @SuppressWarnings("unused")
    public static class BackendHttpError {

        private int statusCode;
        private String mErrorMessage;

        public BackendHttpError() {
        }

        public BackendHttpError(String message, int statusCode) {
            this.statusCode = statusCode;
            this.mErrorMessage = message;
        }

        public String getErrorMessage() {
            return this.mErrorMessage;
        }

        public int getStatusCode() {
            return this.statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public void setErrorMessage(String errorMessage) {
            mErrorMessage = errorMessage;
        }
    }

    public static BackendHttpError parseHttpError(Response<?> response) {
        int errorCode = 0;
        if (!AppUtils.isEmptyOrNull(response)) errorCode = response.code();

        Converter<ResponseBody, SoftDesignApiErrorRes> converter =
                ServiceGenerator.getRetrofit()
                        .responseBodyConverter(SoftDesignApiErrorRes.class, new Annotation[0]);

        SoftDesignApiErrorRes httpError = null;
        try {
            httpError = converter.convert(response.errorBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (httpError == null || isEmptyOrNull(httpError.getError())) {
            return new BackendHttpError("Error " + errorCode, errorCode);
        } else return new BackendHttpError(httpError.getError(), errorCode);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}