package firbase.go.beruniy.view_setup;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import firbase.go.beruniy.BuildConfig;
import firbase.go.beruniy.R;
import firbase.go.beruniy.view_setup.choise.CustomArrayAdapter;
import firbase.go.beruniy.view_setup.choise.CustomMultipleSelectionArrayAdapter;
import firbase.go.beruniy.view_setup.choise.CustomSingleSelectionArrayAdapter;
import firbase.go.beruniy.view_setup.choise.ShareAdapter;
import firbase.go.beruniy.view_setup.choise.utils.Utils;


public class MaterialDialog extends AlertDialog {

    private static final String TAG = "MaterialDialog";


    /**
     * Instantiates a new Material dialog.
     *
     * @param builder the builder
     */
    public MaterialDialog(@NonNull final Builder builder) {
        super(builder.context);

        invoke(builder);
    }


    /**
     * Instantiates a new Material dialog.
     *
     * @param builder the builder
     * @param style the style
     */
    public MaterialDialog(@NonNull final Builder builder, @StyleRes int style) {
        super(builder.context, style);

        invoke(builder);
    }


    private void invoke(@NonNull final Builder builder) {
        boolean isListDialog = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (builder.dimPercent != -1) {
                float value = (float) builder.dimPercent / 100f;
                if (value < 0f) {
                    value = 0f;
                }
                if (value > 1f) {
                    value = 1f;
                }
                getWindow().setDimAmount(value);
            }
        }

        if (builder.customView != null) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            if (builder.viewSpacingLeft == -1 && builder.viewSpacingTop == -1
                    && builder.viewSpacingRight == -1 && builder
                    .viewSpacingBottom == -1) {
                builder.viewSpacingLeft = Utils.dp2px(builder.context, 8);
                builder.viewSpacingTop = Utils.dp2px(builder.context, 0);
                builder.viewSpacingRight = Utils.dp2px(builder.context, 12);
                builder.viewSpacingBottom = Utils.dp2px(builder.context, 0);
            }

            this.setView(
                    builder.customView,
                    builder.viewSpacingLeft, builder.viewSpacingTop,
                    builder.viewSpacingRight, builder.viewSpacingBottom);
        }

        // list
        if ((builder.items != null && builder.items.length > 0) || builder.shareAppDialog
                || builder.adapter != null) {
            isListDialog = true;

            if (builder.customView != null) {
                Log.w(TAG, "ListView will override the custom view.");
            }

            LayoutInflater layoutInflater = (LayoutInflater) builder.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout llListDialog = (LinearLayout) layoutInflater
                    .inflate(R.layout.dialog_list, null);
            this.setView(
                    llListDialog,
                    builder.viewSpacingLeft, builder.viewSpacingTop,
                    builder.viewSpacingRight, builder.viewSpacingBottom);

            final ListView lv = (ListView) llListDialog.findViewById(R.id.list_dialog_listview);
            final TextView tv = (TextView) llListDialog.findViewById(R.id.list_dialog_tv_message);
            if (builder.message != null) {
                // ensure to set message
                tv.setText(getSpannable(builder, builder.message));
            } else {
                // remove message and set space on top
                llListDialog.removeView(tv);
                lv.setPadding(0, Utils.dp2px(builder.context, 16), 0, 0);
            }
            if (builder.title == null) {
                lv.setPadding(0, Utils.dp2px(builder.context, 8), 0,
                        Utils.dp2px(builder.context, 8));
            }

            if (!builder.multiChoice && !builder.blankListing && !builder.shareAppDialog
                    && builder.adapter == null) {
                // single selection (RadioButton)
                int preselectedIndex = 0;
                if (builder.preSelectedIndices.length > 0) {
                    preselectedIndex = builder.preSelectedIndices[0];
                    if (preselectedIndex >= builder.items.length) {
                        Log.w(TAG,
                                "Selected item greater than item count. Will select first item.");
                        preselectedIndex = 0;
                    }
                }
                if (builder.preSelectedIndices.length != 1) {
                    Log.w(TAG,
                            "Can't select multiple items in single selection. Will only select \"" +
                                    builder.items[builder.preSelectedIndices[
                                            builder.preSelectedIndices.length - 1]] + "\".");
                }

                final CustomSingleSelectionArrayAdapter cssaa = new CustomSingleSelectionArrayAdapter(
                        builder.context,
                        builder.items,
                        preselectedIndex,
                        builder.itemClickListener,
                        builder.itemLongClickListener,
                        builder.typeface);

                lv.setDivider(null);
                lv.setAdapter(cssaa);
            } // end single selection (RadioButton)

            else if (!builder.multiChoice && builder.adapter == null && !builder.shareAppDialog) {
                // single selection (blank)
                final CustomArrayAdapter caa = new CustomArrayAdapter(
                        this,
                        builder.context,
                        builder.items,
                        builder.itemClickListener,
                        builder.itemLongClickListener,
                        builder.dismissOnSelection,
                        builder.typeface);

                lv.setDivider(null);
                lv.setAdapter(caa);

            } // end single selection (blank)

            else if (builder.adapter == null && !builder.shareAppDialog) {
                // multi-choice (CheckBox)
                final CustomMultipleSelectionArrayAdapter cmsaa = new CustomMultipleSelectionArrayAdapter(
                        builder.context,
                        builder.items,
                        builder.preSelectedIndices,
                        builder.itemClickListener,
                        builder.itemLongClickListener,
                        builder.typeface);

                lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                lv.setDivider(null);
                lv.setAdapter(cmsaa);
            } // end multi-choice (CheckBox)

            if (builder.adapter != null) {
                // set base adapter
                lv.setDivider(null);
                lv.setAdapter(builder.adapter);
                if (builder.adapterItemClickListener != null) {
                    lv.setOnItemClickListener(builder.adapterItemClickListener);
                }
            } // end base adapter

            if (builder.shareAppDialog) {
                // share app dialog
                ShareAdapter sa = new ShareAdapter(builder.context, builder.typeface);
                lv.setDivider(null);
                lv.setAdapter(sa);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        ResolveInfo info = (ResolveInfo) lv.getAdapter().getItem(position);
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        intent.putExtra(Intent.EXTRA_TEXT,
                                (builder.shareAppMessage != null ? builder.shareAppMessage : "") + "\n"
                                        +
                                        "https://play.google.com/store/apps/details?id=" + builder.context
                                        .getPackageName());
                        builder.context.startActivity(intent);
                    }
                });
            } // end share app dialog

        } // end list

        if (builder.title != null) {
            this.setTitle(getSpannable(builder, builder.title));
        }

        if (builder.message != null) {
            if (!isListDialog) {
                this.setMessage(getSpannable(builder, builder.message));
            }
        }

        if (builder.icon != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.setIcon(builder.context.getDrawable(builder.icon));
            } else {
                this.setIcon(builder.context.getResources().getDrawable(builder.icon));
            }
        }
        if (builder.drawable != null) {
            this.setIcon(builder.drawable);
        }

        if (builder.positiveText != null) {
            this.setButton(BUTTON_POSITIVE, builder.positiveText, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (builder.buttonCallback != null) {
                        builder.buttonCallback.onAny(MaterialDialog.this);
                        builder.buttonCallback.onPositive(MaterialDialog.this);
                    }
                }
            });
        }

        if (builder.neutralText != null) {
            this.setButton(BUTTON_NEUTRAL, builder.neutralText, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (builder.buttonCallback != null) {
                        builder.buttonCallback.onAny(MaterialDialog.this);
                        builder.buttonCallback.onNeutral(MaterialDialog.this);
                    }
                }
            });
        }

        if (builder.negativeText != null) {
            this.setButton(BUTTON_NEGATIVE, builder.negativeText, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (builder.buttonCallback != null) {
                        builder.buttonCallback.onAny(MaterialDialog.this);
                        builder.buttonCallback.onNegative(MaterialDialog.this);
                    }
                }
            });
        }

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                AlertDialog dialog = (AlertDialog) d;

                if (builder.fullscreen) {
                    getWindow().setLayout(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT);
                }

                if (builder.showListener != null) {
                    builder.showListener.onShow(dialog);
                }

                if (builder.typeface != null) {
                    dialog.getButton(BUTTON_POSITIVE).setTypeface(builder.typeface);
                }
                if (builder.typeface != null) {
                    dialog.getButton(BUTTON_NEUTRAL).setTypeface(builder.typeface);
                }
                if (builder.typeface != null) {
                    dialog.getButton(BUTTON_NEGATIVE).setTypeface(builder.typeface);
                }

                // button color
                if (builder.positiveText != null && builder.positiveColor != -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        dialog.getButton(BUTTON_POSITIVE)
                                .setTextColor(builder.context.getColor(builder.positiveColor));
                    } else {
                        dialog.getButton(BUTTON_POSITIVE)
                                .setTextColor(builder.context.getResources().getColor(builder
                                        .positiveColor));
                    }
                }
                if (builder.neutralText != null && builder.neutralColor != -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        dialog.getButton(BUTTON_NEUTRAL)
                                .setTextColor(builder.context.getColor(builder.neutralColor));
                    } else {
                        dialog.getButton(BUTTON_NEUTRAL)
                                .setTextColor(builder.context.getResources().getColor(builder
                                        .neutralColor));
                    }
                }
                if (builder.negativeText != null && builder.negativeColor != -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        dialog.getButton(BUTTON_NEGATIVE)
                                .setTextColor(builder.context.getColor(builder.negativeColor));
                    } else {
                        dialog.getButton(BUTTON_NEGATIVE)
                                .setTextColor(builder.context.getResources().getColor(builder
                                        .negativeColor));
                    }
                }

                // dialog height
                Log.i(TAG, "MaterialDialog " + getWindow().getDecorView().getHeight());

                if ((builder.scaleX != -1) || (builder.scaleY != -1)) {
                    float fScaleX = (builder.scaleX != -1 ? (builder.scaleX / 100f) : 1);
                    float fScaleY = (builder.scaleY != -1 ? (builder.scaleY / 100f) : 1);

                    Rect displayRectangle = new Rect();
                    Window window = getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                    dialog.getWindow().setLayout(
                            (int) (displayRectangle.width() * fScaleX),
                            (int) (displayRectangle.height() * fScaleY));
                }
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (builder.dismissListener != null) {
                    builder.dismissListener.onDismiss();
                }
            }
        });

    }


    private SpannableString getSpannable(@NonNull Builder builder, CharSequence charSequence) {
        if (builder.typeface != null) {
            SpannableString s = new SpannableString(charSequence);
            s.setSpan(new TypefaceSpan(builder.typeface), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return s;
        }
        return SpannableString.valueOf(charSequence);
    }


    /**
     * The type Thread demo.
     */
    class ThreadDemo extends Thread {

        private Thread t;
        private ListView lv;
        private String threadName;


        /**
         * Instantiates a new Thread demo.
         *
         * @param lv the lv
         * @param threadName the thread name
         */
        ThreadDemo(ListView lv, String threadName) {
            this.lv = lv;
            this.threadName = threadName;
        }


        public void run() {
            synchronized (lv) {
                lv.notify();
            }
            System.out.println("Thread " + threadName + " exiting.");
        }


        public void start() {
            System.out.println("Starting " + threadName);
            if (t == null) {
                t = new Thread(this, threadName);
                t.start();
            }
        }

    }


    /**
     * The type Builder.
     */
    public static class Builder {

        /**
         * Base
         */
        private final Context context;
        private int style = -1;
        private CharSequence title;
        private CharSequence message;
        private Typeface typeface;
        private int dimPercent = -1;
        private CharSequence positiveText;
        private CharSequence neutralText;
        private CharSequence negativeText;
        private int positiveColor = -1;
        private int neutralColor = -1;
        private int negativeColor = -1;
        private ButtonCallback buttonCallback;
        private ShowListener showListener;
        private DismissListener dismissListener;
        private int icon = -1;
        private Drawable drawable;
        private boolean canceledOnTouchOutside = true;
        private boolean cancelable = true;
        private View customView;

        private int viewSpacingLeft = -1;
        private int viewSpacingTop = -1;
        private int viewSpacingRight = -1;
        private int viewSpacingBottom = -1;

        private int scaleX = -1;
        private int scaleY = -1;

        private boolean fullscreen = false;

        /**
         * List
         */
        // false will show RadioButton / CheckBox
        private boolean blankListing;
        private boolean multiChoice;
        private boolean dismissOnSelection = false;
        private String[] items;
        private BaseAdapter adapter;
        private Integer[] preSelectedIndices;
        private ItemClickListener itemClickListener;
        private ItemLongClickListener itemLongClickListener;
        private ItemSelectedListener itemSelectedListener;
        private AdapterView.OnItemClickListener adapterItemClickListener;
        private boolean itemLongClickable = false;

        // delayed positive
        private boolean positiveDelayed;
        private long millisInFuture;
        private long countDownInterval;
        private String finishedText;

        /**
         * Share App
         */
        private boolean shareAppDialog = false;
        private String shareAppMessage = "";

        /**
         * License
         */
        private boolean licenseDialog = false;
        private String[] libNames;
        private String[] libDevelopers;
        private String[] libLicenses;

        /**
         * Changelog
         */
        private boolean changelogDialog = false;
        private String[] clVersionNames;
        private String[] clDates;
        private String clBullet;

        /**
         * File-Chooser
         */
        private Activity activity;
        private int requestCode;
        private List<File> files;
        private boolean fileChooser = false;
        private boolean allowDirectorySelection = false;
        private File startPath;
        private FileChooserListener fileChooserListener;


        /**
         * Instantiates a new Builder.
         *
         * @param context the context
         */
        public Builder(@NonNull Context context) {
            this.context = context;
        }


        /**
         * Instantiates a new Builder.
         *
         * @param context the context
         * @param style the style
         */
        public Builder(@NonNull Context context, @StyleRes int style) {
            this.context = context;
            this.style = style;
        }


        /**
         * Title builder.
         *
         * @param title the title
         * @return the builder
         */
        public Builder title(@Nullable CharSequence title) {
            this.title = title;
            return this;
        }


        /**
         * Title builder.
         *
         * @param title the title
         * @return the builder
         */
        public Builder title(@StringRes int title) {
            this.title = context.getString(title);
            return this;
        }


        /**
         * Message builder.
         *
         * @param message the message
         * @return the builder
         */
        public Builder message(@Nullable CharSequence message) {
            this.message = message;
            return this;
        }


        /**
         * Message builder.
         *
         * @param message the message
         * @return the builder
         */
        public Builder message(@StringRes int message) {
            this.message = context.getString(message);
            return this;
        }


        /**
         * Font builder.
         *
         * @param typeface the typeface
         * @return the builder
         */
        public Builder font(Typeface typeface) {
            this.typeface = typeface;
            return this;
        }


        /**
         * Dim builder.
         *
         * @param percent the percent
         * @return the builder
         */
        public Builder dim(int percent) {
            this.dimPercent = percent;
            return this;
        }


        /**
         * Scale builder.
         *
         * @param percentX the percent x
         * @param percentY the percent y
         * @return the builder
         */
        public Builder scale(int percentX, int percentY) {
            this.scaleX = percentX;
            this.scaleY = percentY;
            return this;
        }


        /**
         * Fullscreen builder.
         *
         * @param fullscreen the fullscreen
         * @return the builder
         */
        public Builder fullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            return this;
        }


        /**
         * Positive text builder.
         *
         * @param positiveText the positive text
         * @return the builder
         */
        public Builder positiveText(@NonNull CharSequence positiveText) {
            this.positiveText = positiveText;
            return this;
        }


        /**
         * Positive text builder.
         *
         * @param positiveText the positive text
         * @return the builder
         */
        public Builder positiveText(@StringRes int positiveText) {
            this.positiveText = context.getString(positiveText);
            return this;
        }


        /**
         * Neutral text builder.
         *
         * @param neutralText the neutral text
         * @return the builder
         */
        public Builder neutralText(@NonNull CharSequence neutralText) {
            this.neutralText = neutralText;
            return this;
        }


        /**
         * Neutral text builder.
         *
         * @param neutralText the neutral text
         * @return the builder
         */
        public Builder neutralText(@StringRes int neutralText) {
            this.neutralText = context.getString(neutralText);
            return this;
        }


        /**
         * Negative text builder.
         *
         * @param negativeText the negative text
         * @return the builder
         */
        public Builder negativeText(@NonNull CharSequence negativeText) {
            this.negativeText = negativeText;
            return this;
        }


        /**
         * Negative text builder.
         *
         * @param negativeText the negative text
         * @return the builder
         */
        public Builder negativeText(@StringRes int negativeText) {
            this.negativeText = context.getString(negativeText);
            return this;
        }


        /**
         * Positive color builder.
         *
         * @param colorId the color id
         * @return the builder
         */
        public Builder positiveColor(@ColorRes int colorId) {
            this.positiveColor = colorId;
            return this;
        }


        /**
         * Neutral color builder.
         *
         * @param colorId the color id
         * @return the builder
         */
        public Builder neutralColor(@ColorRes int colorId) {
            this.neutralColor = colorId;
            return this;
        }


        /**
         * Negative color builder.
         *
         * @param colorId the color id
         * @return the builder
         */
        public Builder negativeColor(@ColorRes int colorId) {
            this.negativeColor = colorId;
            return this;
        }


        /**
         * Icon builder.
         *
         * @param drawableId the drawable id
         * @return the builder
         */
        public Builder icon(@DrawableRes int drawableId) {
            this.icon = drawableId;
            return this;
        }


        /**
         * Icon builder.
         *
         * @param drawable the drawable id
         * @return the builder
         */
        public Builder icon(@NonNull Drawable drawable) {
            this.drawable = drawable;
            return this;
        }


        /**
         * Button callback builder.
         *
         * @param buttonCallback the button callback
         * @return the builder
         */
        public Builder buttonCallback(@NonNull ButtonCallback buttonCallback) {
            this.buttonCallback = buttonCallback;
            return this;
        }


        /**
         * Show listener builder.
         *
         * @param showListener the show listener
         * @return the builder
         */
        public Builder showListener(@NonNull ShowListener showListener) {
            this.showListener = showListener;
            return this;
        }


        /**
         * Dismiss listener builder.
         *
         * @param dismissListener the dismiss listener
         * @return the builder
         */
        public Builder dismissListener(@NonNull DismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }


        /**
         * Canceled on touch outside builder.
         *
         * @param canceledOnTouchOutside the canceled on touch outside
         * @return the builder
         */
        public Builder canceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }


        /**
         * Cancelable builder.
         *
         * @param cancelable the cancelable
         * @return the builder
         */
        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }


        /**
         * Custom view builder.
         *
         * @param customView the custom view
         * @return the builder
         */
        public Builder customView(@NonNull View customView) {
            this.customView = customView;
            return this;
        }


        /**
         * Custom view builder.
         *
         * @param layoutId the layout id
         * @return the builder
         */
        public Builder customView(@LayoutRes int layoutId) {
            LayoutInflater inflater = LayoutInflater.from(context);
            customView = inflater.inflate(layoutId, null);
            return this;
        }


        /**
         * View spacing dp builder.
         *
         * @param viewSpacingLeftDp the view spacing left dp
         * @param viewSpacingTopDp the view spacing top dp
         * @param viewSpacingRightDp the view spacing right dp
         * @param viewSpacingBottomDp the view spacing bottom dp
         * @return the builder
         */
        public Builder viewSpacingDp(int viewSpacingLeftDp, int viewSpacingTopDp,
                                     int viewSpacingRightDp, int
                                             viewSpacingBottomDp) {
            this.viewSpacingLeft = Utils.dp2px(context, viewSpacingLeftDp);
            this.viewSpacingTop = Utils.dp2px(context, viewSpacingTopDp);
            this.viewSpacingRight = Utils.dp2px(context, viewSpacingRightDp);
            this.viewSpacingBottom = Utils.dp2px(context, viewSpacingBottomDp);
            return this;
        }


        /**
         * List items builder.
         *
         * @param dismissOnSelection the dismiss on selection
         * @param adapter the adapter
         * @param adapterItemClickListener the adapter item click listener
         * @return the builder
         */
        // list
        public Builder adapter(boolean dismissOnSelection, @NonNull BaseAdapter adapter,
                               @Nullable AdapterView.OnItemClickListener
                                       adapterItemClickListener) {
            this.blankListing = true;
            this.dismissOnSelection = dismissOnSelection;
            this.adapter = adapter;
            this.adapterItemClickListener = adapterItemClickListener;
            return this;
        }


        /**
         * List items builder.
         *
         * @param dismissOnSelection the dismiss on selection
         * @param items the items
         * @return the builder
         */
        // list
        public Builder listItems(boolean dismissOnSelection, @NonNull String... items) {
            this.blankListing = true;
            this.dismissOnSelection = dismissOnSelection;
            this.multiChoice = false;
            this.items = items;
            return this;
        }


        /**
         * List items single selection builder.
         *
         * @param dismissOnSelection the dismiss on selection
         * @param items the items
         * @return the builder
         */
        public Builder listItemsSingleSelection(boolean dismissOnSelection,
                                                @NonNull String... items) {
            this.blankListing = false;
            this.dismissOnSelection = dismissOnSelection;
            this.multiChoice = false;
            this.items = items;
            return this;
        }


        /**
         * List items multi choice builder.
         *
         * @param items the items
         * @return the builder
         */
        public Builder listItemsMultiChoice(@NonNull String... items) {
            this.blankListing = false;
            this.multiChoice = true;
            this.items = items;
            return this;
        }


        /**
         * Selection builder.
         *
         * @param preSelected the pre selected
         * @return the builder
         */
        public Builder selection(Integer... preSelected) {
            this.preSelectedIndices = preSelected;
            return this;
        }


        /**
         * Item long clickable builder.
         *
         * @param itemLongClickable the item long clickable
         * @return the builder
         */
        public Builder itemLongClickable(boolean itemLongClickable) {
            this.itemLongClickable = itemLongClickable;
            return this;
        }


        /**
         * Item click listener builder.
         *
         * @param itemClickListener the item click listener
         * @return the builder
         */
        public Builder itemClickListener(@NonNull ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }


        /**
         * Item long click listener builder.
         *
         * @param itemLongClickListener the item long click listener
         * @return the builder
         */
        public Builder itemLongClickListener(@NonNull ItemLongClickListener itemLongClickListener) {
            this.itemLongClickable = true;
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }


        /**
         * Item selected listener builder.
         *
         * @param itemSelectedListener the item selected listener
         * @return the builder
         */
        public Builder itemSelectedListener(@NonNull ItemSelectedListener itemSelectedListener) {
            this.itemSelectedListener = itemSelectedListener;
            return this;
        }


        /**
         * Positive delayed builder.
         *
         * @param millisInFuture the millis in future
         * @param countDownInterval the count down interval
         * @param finishedText the finished text
         * @return the builder
         */
        public Builder positiveDelayed(long millisInFuture, long countDownInterval,
                                       @NonNull String finishedText) {
            this.positiveDelayed = true;
            this.millisInFuture = millisInFuture;
            this.countDownInterval = countDownInterval;
            this.finishedText = finishedText;
            return this;
        }

        /**
         * Share app dialog builder.
         *
         * @param shareAppDialog the share app dialog
         * @param shareAppMessage the share app message
         * @return the builder
         */
        public Builder shareAppDialog(boolean shareAppDialog, @Nullable String shareAppMessage) {
            this.shareAppDialog = shareAppDialog;
            this.shareAppMessage = shareAppMessage;
            this.message = null;

            return this;
        }


        /**
         * Build material dialog.
         *
         * @return the material dialog
         */
        public MaterialDialog build() {
            if (style == -1) {
                return new MaterialDialog(this);
            } else {
                return new MaterialDialog(this, style);
            }
        }


        /**
         * Show.
         */
        public void show() {
            this.build().show();
        }


        private void setViewSpacing(int spacing) {
            this.viewSpacingLeft = spacing;
            this.viewSpacingTop = spacing;
            this.viewSpacingRight = spacing;
            this.viewSpacingBottom = spacing;
        }
    }


    /**
     * The type Button callback.
     */
    public abstract static class ButtonCallback {

        /**
         * On any.
         *
         * @param dialog the dialog
         */
        public void onAny(MaterialDialog dialog) {
            Log.d(TAG, "onAny " + "");
        }


        /**
         * On positive.
         *
         * @param dialog the dialog
         */
        public void onPositive(MaterialDialog dialog) {
            Log.d(TAG, "onPositive " + "");
        }


        /**
         * On neutral.
         *
         * @param dialog the dialog
         */
        public void onNeutral(MaterialDialog dialog) {
            Log.d(TAG, "onNeutral " + "");
        }


        /**
         * On negative.
         *
         * @param dialog the dialog
         */
        public void onNegative(MaterialDialog dialog) {
            Log.d(TAG, "onNegative " + "");
        }


        /**
         * Instantiates a new Button callback.
         */
        public ButtonCallback() {
        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Dismiss listener.
     */
    public abstract static class DismissListener {

        /**
         * Instantiates a new Dismiss listener.
         */
        public DismissListener() {
        }


        /**
         * On dismiss.
         */
        public void onDismiss() {
            Log.d(TAG, "onDismiss " + "");
        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Show listener.
     */
    public abstract static class ShowListener {

        /**
         * Instantiates a new Show listener.
         */
        public ShowListener() {
        }


        /**
         * On show.
         *
         * @param dialog the dialog
         */
        public void onShow(AlertDialog dialog) {
            Log.d(TAG, "onShow " + "");
        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Item click listener.
     */
    public abstract static class ItemClickListener {

        /**
         * Instantiates a new Item click listener.
         */
        public ItemClickListener() {
        }


        /**
         * On click.
         *
         * @param v the v
         * @param position the position
         * @param id the id
         */
        public void onClick(View v, int position, long id) {
            Log.d(TAG, "onClick " + position);
        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Item long click listener.
     */
    public abstract static class ItemLongClickListener {

        /**
         * Instantiates a new Item long click listener.
         */
        public ItemLongClickListener() {
        }


        /**
         * On long click.
         *
         * @param view the view
         * @param position the position
         * @param id the id
         */
        public void onLongClick(View view, int position, long id) {
            Log.d(TAG, "onLongClick " + position);
        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Item selected listener.
     */
    public abstract static class ItemSelectedListener {

        /**
         * Instantiates a new Item selected listener.
         */
        public ItemSelectedListener() {
        }


        /**
         * On selected.
         *
         * @param view the view
         * @param position the position
         * @param id the id
         */
        public void onSelected(View view, int position, long id) {
            Log.d(TAG, "onSelected " + position);
        }


        /**
         * On nothing selected.
         *
         * @param parent the parent
         */
        public void onNothingSelected(AdapterView<?> parent) {

        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Item selected listener.
     */
    public abstract static class FileChooserListener {

        /**
         * Instantiates a new Item selected listener.
         */
        public FileChooserListener() {
        }


        /**
         * On file selected.
         *
         * @param dialog the dialog
         * @param view the view
         * @param position the position
         * @param id the id
         * @param file the file
         */
        public void onFileSelected(MaterialDialog dialog, View view, int position, long id,
                                   File file) {
            Log.d(TAG, "onSelected " + position);
        }


        /**
         * On file set.
         *
         * @param dialog the dialog
         * @param file the file
         */
        public void onFileSet(MaterialDialog dialog, File file) {
            Log.d(TAG, "onFileSet: " + file.getAbsolutePath());
        }


        /**
         * On nothing selected.
         *
         * @param parent the parent
         */
        public void onNothingSelected(AdapterView<?> parent) {

        }


        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        public final boolean equals(Object o) {
            return super.equals(o);
        }


        protected final void finalize() throws Throwable {
            super.finalize();
        }


        public final int hashCode() {
            return super.hashCode();
        }


        public final String toString() {
            return super.toString();
        }
    }


    /**
     * The type Version.
     */
    public static class Version {


        /**
         * Gets version name.
         *
         * @return the version name
         */
        public static String getVersionName() {
            return BuildConfig.VERSION_NAME;
        }


        /**
         * Gets version info.
         *
         * @return the version info
         */
        public static String getVersionInfo() {
            return "materialdialog-" + BuildConfig.VERSION_NAME;
        }


        /**
         * Gets license.
         *
         * @return The license text.
         */
        public static String getLicense() {
            return "Copyright (c) 2016 Martin Pfeffer\n" +
                    " \n" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "you may not use this file except in compliance with the License.\n" +
                    "You may obtain a copy of the License at\n" +
                    " \n" +
                    "     http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " \n" +
                    "Unless required by applicable law or agreed to in writing, software\n" +
                    "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "See the License for the specific language governing permissions and\n" +
                    "limitations under the License.";
        }

    }

}