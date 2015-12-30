package org.slavick.dailydozen.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.slavick.dailydozen.Args;
import org.slavick.dailydozen.R;
import org.slavick.dailydozen.activity.FoodInfoActivity;
import org.slavick.dailydozen.model.Food;
import org.slavick.dailydozen.model.Servings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FoodServings extends RecyclerView.ViewHolder {
    private final static String TAG = FoodServings.class.getSimpleName();

    private Date date;
    private Food food;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    @Bind(R.id.food_checkboxes)
    protected ViewGroup vgCheckboxes;

    @Bind(R.id.food_name)
    protected TextView tvName;

    private ClickListener listener;

    public FoodServings(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setDateAndFood(final Date date, final Food food) {
        this.date = date;
        this.food = food;

        initFoodName();
        initCheckboxes();
    }

    private void initFoodName() {
        tvName.setText(food.getName());

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = itemView.getContext();

                final Intent intent = new Intent(context, FoodInfoActivity.class);
                intent.putExtra(Args.FOOD, food);

                context.startActivity(intent);
            }
        });
    }

    private void initCheckboxes() {
        int numExistingServings = getNumExistingServings();

        final List<CheckBox> checkBoxes = new ArrayList<>();

        for (int i = 0; i < food.getRecommendedServings(); i++) {
            final boolean isChecked = numExistingServings > 0;

            checkBoxes.add(createCheckBox(isChecked));

            numExistingServings--;
        }

        vgCheckboxes.removeAllViews();

        // Add the checkboxes in reverse order because they were checked from left to right. Reversing the order
        // makes it so the checks appear right to left.
        Collections.reverse(checkBoxes);
        for (CheckBox checkBox : checkBoxes) {
            vgCheckboxes.addView(checkBox);
        }

        // The maximum number of servings for any food is 5. Here we set all FoodServings to have the same width of
        // checkboxes so they line up nicely.
        setWidthOfCheckBoxesToFiveWide();
    }

    private void setWidthOfCheckBoxesToFiveWide() {
        final CheckBox checkBox = new CheckBox(itemView.getContext());
        checkBox.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final ViewGroup.LayoutParams params = vgCheckboxes.getLayoutParams();
        params.width = checkBox.getMeasuredWidth() * 5;
        vgCheckboxes.setLayoutParams(params);
    }

    private int getNumExistingServings() {
        final Servings servings = Servings.getByDateAndFood(date, food);
        return servings != null ? servings.getServings() : 0;
    }

    private CheckBox createCheckBox(final boolean isChecked) {
        final CheckBox checkBox = new CheckBox(itemView.getContext());

        // It is necessary to set the checked status before we set the onCheckedChangeListener
        checkBox.setChecked(isChecked);

        checkBox.setOnCheckedChangeListener(getOnCheckedChangeListener());

        return checkBox;
    }

    private CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        if (onCheckedChangeListener == null) {
            onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        handleServingChecked();
                    } else {
                        handleServingUnchecked();
                    }
                }
            };
        }

        return onCheckedChangeListener;
    }

    private void handleServingChecked() {
        final Servings servings = Servings.createServingsIfDoesNotExist(date, food);
        if (servings != null) {
            servings.increaseServings();
            servings.save();

            if (listener != null) {
                listener.onServingChecked();
            }

            Log.d(TAG, String.format("Increased Servings for %s", servings));
        }
    }

    private void handleServingUnchecked() {
        final Servings servings = Servings.getByDateAndFood(date, food);
        if (servings != null) {
            servings.decreaseServings();

            if (listener != null) {
                listener.onServingUnchecked();
            }

            if (servings.getServings() > 0) {
                servings.save();

                Log.d(TAG, String.format("Decreased Servings for %s", servings));
            } else {
                Log.d(TAG, String.format("Deleting %s", servings));
                servings.delete();
            }
        }
    }

    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

    public interface ClickListener {
        void onServingChecked();

        void onServingUnchecked();
    }
}
