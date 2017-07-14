/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml),
 *            Marcelina Knitter (@marcelinkaaa)
 * Copyright (c) 2011-2017, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.android.gui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.search.KeywordFilter;
import com.frostwire.util.Logger;

/**
 * @author aldenml
 * @author gubatron
 * @author marcelinkaaa
 */
public class KeywordTagView extends LinearLayout {

    private static final Logger LOG = Logger.getLogger(KeywordTagView.class);

    private boolean dismissible;
    private KeywordFilter keywordFilter;
    private int count;
    private KeywordTagViewListener listener;

    public interface KeywordTagViewListener {
        void onKeywordTagViewDismissed(KeywordTagView view);

        void onKeywordTagViewTouched(KeywordTagView view);
    }

    private KeywordTagView(Context context, AttributeSet attrs, KeywordFilter keywordFilter) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.KeywordTagView, 0, 0);
        count = attributes.getInteger(R.styleable.KeywordTagView_keyword_tag_count, 0);
        dismissible = attributes.getBoolean(R.styleable.KeywordTagView_keyword_tag_dismissable, true);

        if (keywordFilter == null) { // try to build one from attribute values
            boolean inclusive = attributes.getBoolean(R.styleable.KeywordTagView_keyword_tag_inclusive, true);
            String keyword = attributes.getString(R.styleable.KeywordTagView_keyword_tag_keyword);
            if (keyword == null) {
                keyword = ""; // dummy value
            }
            this.keywordFilter = new KeywordFilter(inclusive, keyword, null);
        }

        attributes.recycle();
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setVisibility(View.VISIBLE);
    }

    public KeywordTagView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public KeywordTagView(Context context, KeywordFilter keywordFilter, int count, boolean dismissible, KeywordTagViewListener listener) {
        this(context, null, keywordFilter);
        this.count = count;
        this.dismissible = dismissible;
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.view_keyword_tag, this);
        updateComponents();
    }

    private void updateComponents() {
        ImageButton inclusiveIndicatorImageView = (ImageButton) findViewById(R.id.view_keyword_tag_inclusive_indicator);
        TextView keywordTextView = (TextView) findViewById(R.id.view_keyword_tag_keyword);
        TextView countTextView = (TextView) findViewById(R.id.view_keyword_tag_count);
        LinearLayout tagContainer = (LinearLayout) findViewById(R.id.view_keyword_tag_container);
        if (count == -1) {
            countTextView.setVisibility(View.GONE);
        }
        ImageView dismissTextView = (ImageView) findViewById(R.id.view_keyword_tag_dismiss);
        inclusiveIndicatorImageView.setImageResource(keywordFilter.isInclusive() ? R.drawable.filter_add : R.drawable.filter_minus);
        keywordTextView.setText(keywordFilter.getKeyword());
        countTextView.setText("(" + String.valueOf(count) + ")");

        if (isInEditMode()) {
            return;
        }

        if (dismissible) {
            tagContainer.setBackgroundResource(R.drawable.keyword_tag_background_active);
            keywordTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_text_white));
            inclusiveIndicatorImageView.setBackgroundResource(R.drawable.keyword_tag_inclusive_background);
            inclusiveIndicatorImageView.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#2e9ec7"), PorterDuff.Mode.SRC_IN));
            inclusiveIndicatorImageView.setVisibility(View.VISIBLE);
            dismissTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDismissed();
                }
            });
        } else {
            tagContainer.setBackgroundResource(R.drawable.keyword_tag_background);
            keywordTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_text_primary));
            inclusiveIndicatorImageView.setBackgroundResource(0);
            inclusiveIndicatorImageView.setVisibility(View.GONE);
            dismissTextView.setVisibility(View.GONE);
        }

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeywordTagViewTouched();
            }
        };
        inclusiveIndicatorImageView.setOnClickListener(onClickListener);
        keywordTextView.setOnClickListener(onClickListener);
        countTextView.setOnClickListener(onClickListener);
    }

    private void onKeywordTagViewTouched() {
        if (this.listener != null) {
            this.listener.onKeywordTagViewTouched(this);
        }
    }

    public KeywordFilter getKeywordFilter() {
        return keywordFilter;
    }

    public boolean isDismissible() {
        return dismissible;
    }

    /**
     * Replaces instance of internal KeywordFilter with one that toggles the previous one's inclusive mode
     */
    public KeywordFilter toogleFilterInclusionMode() {
        KeywordFilter oldKeywordFilter = getKeywordFilter();
        KeywordFilter newKeywordFilter = new KeywordFilter(!oldKeywordFilter.isInclusive(), oldKeywordFilter.getKeyword(), oldKeywordFilter.getFeature());
        this.keywordFilter = newKeywordFilter;
        updateComponents();
        return newKeywordFilter;
    }

    public void setListener(KeywordTagViewListener listener) {
        this.listener = listener;
    }

    private void onDismissed() {
        if (listener != null) {
            try {
                listener.onKeywordTagViewDismissed(this);
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }
}
