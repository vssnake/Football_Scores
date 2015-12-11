package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by vssnake on 7/12/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayWidgetRemoteViewService extends RemoteViewsService{


    public final String LOG_TAG = TodayWidgetRemoteViewService.class.getSimpleName();

    private static final String[] FORECAST_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY,

    };


    public static final int COL_HOME = 4;
    public static final int COL_AWAY = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 3;

    // these indices must match the projection
    static final int INDEX_MATH_ID = 0;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(LOG_TAG,"onGetViewFactory");
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                Log.d(LOG_TAG,"onCreate");
            }

            @Override
            public void onDataSetChanged() {

                Log.d(LOG_TAG,"onDataSetChanged");
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Date date = new Date();
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                Uri uriDate = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(uriDate,
                        FORECAST_COLUMNS,
                        null,
                        new String[]{mformat.format(date)},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(LOG_TAG,"onGetViewAt");
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }


                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_today_list_item);
                String nameHome = data.getString(COL_HOME);
                String nameAway = data.getString(COL_AWAY);

                String scoreVisitor = data.getString(COL_AWAY_GOALS);
                String scoreHome = data.getString(COL_HOME_GOALS);
                String description = (data.getLong(COL_ID))+"";


                String mathTime = data.getString(COL_MATCHTIME);

                views.setTextViewText(R.id.home_name,nameHome);
                views.setTextViewText(R.id.away_name,nameAway);

                if (scoreHome.contains("-1") && scoreVisitor.contains("-1")){
                    views.setTextViewText(R.id.score_textview," | ");
                }else{
                    views.setTextViewText(R.id.score_textview,scoreHome + " | " + scoreVisitor);

                }


                views.setTextViewText(R.id.data_textview,mathTime);

                views.setImageViewResource(R.id.home_crest,Utilies.getTeamCrestByTeamName(
                        data.getString(scoresAdapter.COL_HOME)));
                views.setImageViewResource(R.id.away_crest,Utilies.getTeamCrestByTeamName(
                        data.getString(scoresAdapter.COL_AWAY)));


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }




                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                Log.d(LOG_TAG,"onSetRemoteContentDescription");
                //views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.d(LOG_TAG,"onGetLoadingView");
                return new RemoteViews(getPackageName(), R.layout.widget_today_list_item);
            }

            @Override
            public int getViewTypeCount() {
                Log.d(LOG_TAG,"onGetViewTypeCount");
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(LOG_TAG,"onGetItemId");
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_MATH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
