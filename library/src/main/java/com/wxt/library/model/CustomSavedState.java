package com.wxt.library.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;

import com.wxt.library.retention.NotProguard;

public class CustomSavedState extends View.BaseSavedState {

    @NotProguard
    public SparseArray childrenStates;

    public CustomSavedState(Parcelable superState) {
        super(superState);
    }

    public CustomSavedState(Parcel in, ClassLoader classLoader) {
        super(in);
        childrenStates = in.readSparseArray(classLoader);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSparseArray(childrenStates);
    }

    public static final ClassLoaderCreator<CustomSavedState> CREATOR = new ClassLoaderCreator<CustomSavedState>() {
        @Override
        public CustomSavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new CustomSavedState(source, loader);
        }

        @Override
        public CustomSavedState createFromParcel(Parcel source) {
            return createFromParcel(null);
        }

        public CustomSavedState[] newArray(int size) {
            return new CustomSavedState[size];
        }
    };
}