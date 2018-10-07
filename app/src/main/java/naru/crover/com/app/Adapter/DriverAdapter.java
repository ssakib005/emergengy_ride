package naru.crover.com.app.Adapter;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import naru.crover.com.app.R;

public class DriverAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;

    @Override
    public int getCount() {
        return 0;
    }

    public DriverAdapter(Context context){
        this.context = context;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_driver_map, null);
        FrameLayout layout = (FrameLayout) view.findViewById(R.id.layoutFF);

        ViewPager vp = (ViewPager) container;
        vp.addView(view);

        return view;
    }
}
