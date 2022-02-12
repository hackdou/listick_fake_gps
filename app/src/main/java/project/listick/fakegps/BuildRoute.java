package project.listick.fakegps;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;

import org.osmdroid.util.GeoPoint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import project.listick.fakegps.API.LFGSimpleApi;
import project.listick.fakegps.Enumerations.ERouteTransport;

/*
 * Created by LittleAngry on 01.01.19 (macOS 10.12)
 * */
public class BuildRoute extends AsyncTask<Void, Void, Void> {

    private final double sourceLat;
    private final double sourceLong;
    private final double destLat;
    private final double destLong;
    public double distance;

    private ArrayList<GeoPoint> geoPoints;
    private final ERouteTransport transport;
    private final WeakReference<Context> mContext;
    private WeakReference<View> mProgressDialog;
    private final BuildRouteListener mListener;

    public interface BuildRouteListener {
        void onRouteBuilt(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void onRouteError(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void onCancel();
    }

    private void inflateProgressLayout() {
        Activity activity = (Activity) mContext.get();

        LinearLayout progressLayoutContainer = activity.findViewById(R.id.route_building_status_container);
        progressLayoutContainer.setVisibility(View.VISIBLE);
        mProgressDialog = new WeakReference<>(activity.getLayoutInflater().inflate(R.layout.route_building_fullscreen_dialog, null));
        mProgressDialog.get().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        progressLayoutContainer.addView(mProgressDialog.get());

        mProgressDialog.get().findViewById(R.id.loading).setOnClickListener(v -> { });
    }

    private void removeProgressLayout() {
        Activity activity = (Activity) mContext.get();
        LinearLayout progressLayoutContainer = activity.findViewById(R.id.route_building_status_container);
        progressLayoutContainer.removeView(mProgressDialog.get());
        progressLayoutContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        inflateProgressLayout();

        View cancel = mProgressDialog.get().findViewById(R.id.cancel);

        cancel.setOnClickListener(v -> {
            mListener.onCancel();
            removeProgressLayout();
            BuildRoute.this.cancel(false);
        } );
    }

    public BuildRoute(double sourceLat, double sourceLong, double destLat, double destLong, Context context, ERouteTransport transport, BuildRouteListener listener) {
        this.sourceLat = sourceLat;
        this.sourceLong = sourceLong;
        this.destLat = destLat;
        this.destLong = destLong;
        this.mContext = new WeakReference<>(context);
        this.transport = transport;
        this.mListener = listener;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        removeProgressLayout();

        if (geoPoints == null || geoPoints.isEmpty()) {
            geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(sourceLat, sourceLong));
            geoPoints.add(new GeoPoint(destLat, destLong));

            mListener.onRouteError(geoPoints, sourceLat, sourceLong, destLat, destLong, distance, transport);
        } else {
            mListener.onRouteBuilt(geoPoints, sourceLat, sourceLong, destLat, destLong, distance, transport);
        }

        MapUtil.pointsBackup = geoPoints;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        if (NetworkUtils.isNetworkAvailable())
            buildRoute();
        return null;
    }

    private void buildRoute(){
        LFGSimpleApi.Directions openRoute = new LFGSimpleApi.Directions(sourceLat, sourceLong, destLat, destLong, transport);
        String content = openRoute.getContent();
        geoPoints = openRoute.downloadRoute(content, true);
    }

}
