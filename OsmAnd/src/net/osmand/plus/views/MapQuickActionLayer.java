package net.osmand.plus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Vibrator;
import android.support.annotation.DimenRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import net.osmand.data.LatLon;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.quickaction.QuickActionFactory;
import net.osmand.plus.quickaction.QuickActionRegistry;
import net.osmand.plus.quickaction.QuickActionsWidget;

import static net.osmand.plus.views.ContextMenuLayer.VIBRATE_SHORT;

/**
 * Created by okorsun on 23.12.16.
 */

public class MapQuickActionLayer extends OsmandMapLayer implements QuickActionRegistry.QuickActionUpdatesListener, QuickAction.QuickActionSelectionListener {

    private final ContextMenuLayer    contextMenuLayer;
    private       ImageView           contextMarker;
    private final MapActivity         mapActivity;
    private final OsmandApplication   app;
    private final OsmandSettings      settings;
    private final QuickActionRegistry quickActionRegistry;

    private ImageButton        quickActionButton;
    private QuickActionsWidget quickActionsWidget;

    private OsmandMapTileView view;
    private boolean           wasCollapseButtonVisible;
    private int               previousMapPosition;


    private boolean inChangeMarkerPositionMode;
    private boolean isLayerOn;

    public MapQuickActionLayer(MapActivity activity, ContextMenuLayer contextMenuLayer) {
        this.mapActivity = activity;
        this.contextMenuLayer = contextMenuLayer;
        app = activity.getMyApplication();
        settings = activity.getMyApplication().getSettings();
        quickActionRegistry = activity.getMapLayers().getQuickActionRegistry();
    }


    @Override
    public void initLayer(OsmandMapTileView view) {
        this.view = view;

        quickActionsWidget = (QuickActionsWidget) mapActivity.findViewById(R.id.quick_action_widget);
        quickActionButton = (ImageButton) mapActivity.findViewById(R.id.map_quick_actions_button);
        setQuickActionButtonMargin();
        isLayerOn = quickActionRegistry.isQuickActionOn();
        quickActionButton.setImageResource(R.drawable.map_quick_action);
        quickActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLayerState(quickActionsWidget.getVisibility() == View.VISIBLE);

            }
        });


        Context context = view.getContext();
        contextMarker = new ImageView(context);
        contextMarker.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        contextMarker.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.map_pin_context_menu));
        contextMarker.setClickable(true);
        int minw = contextMarker.getDrawable().getMinimumWidth();
        int minh = contextMarker.getDrawable().getMinimumHeight();
        contextMarker.layout(0, 0, minw, minh);


        quickActionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) mapActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VIBRATE_SHORT);
                quickActionButton.setOnTouchListener(onQuickActionTouchListener);
                return true;
            }
        });
    }

    public void refreshLayer() {
        setLayerState(true);
        isLayerOn = quickActionRegistry.isQuickActionOn();
        setUpQuickActionBtnVisibility();
    }

    private void setQuickActionButtonMargin() {
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) quickActionButton.getLayoutParams();
        if (AndroidUiHelper.isOrientationPortrait(mapActivity)) {
            Pair<Integer, Integer> fabMargin = settings.getPortraitFabMargin();
            if (fabMargin != null) {
                param.rightMargin = fabMargin.first;
                param.bottomMargin = fabMargin.second;
            } else {
                param.bottomMargin = calculateTotalSizePx(R.dimen.map_button_size, R.dimen.map_button_spacing) * 2;
            }
        } else {
            Pair<Integer, Integer> fabMargin = settings.getLandscapeFabMargin();
            if (fabMargin != null) {
                param.rightMargin = fabMargin.first;
                param.bottomMargin = fabMargin.second;
            } else {
                param.rightMargin = calculateTotalSizePx(R.dimen.map_button_size, R.dimen.map_button_spacing_land) * 2;
            }
        }
        quickActionButton.setLayoutParams(param);
    }

    private int calculateTotalSizePx(@DimenRes int... dimensId) {
        int result = 0;
        for (int id : dimensId) {
            result += mapActivity.getResources().getDimensionPixelSize(id);
        }
        return result;
    }

    /**
     * @param isClosed
     * @return true, if state was changed
     */
    public boolean setLayerState(boolean isClosed) {
        if ((quickActionsWidget.getVisibility() == View.VISIBLE) != isClosed)    // check if state change is needed
            return false;

        quickActionButton.setImageResource(isClosed ? R.drawable.map_quick_action : R.drawable.map_action_cancel);
        quickActionsWidget.setVisibility(isClosed ? View.GONE : View.VISIBLE);

        if (isClosed) {
            quitMovingMarker();
            quickActionRegistry.setUpdatesListener(null);
            quickActionsWidget.setSelectionListener(null);
        } else {
            enterMovingMode(mapActivity.getMapView().getCurrentRotatedTileBox());
            quickActionsWidget.setActions(quickActionRegistry.getQuickActions());
            quickActionRegistry.setUpdatesListener(MapQuickActionLayer.this);
            quickActionsWidget.setSelectionListener(MapQuickActionLayer.this);
        }

        return true;
    }

    private void enterMovingMode(RotatedTileBox tileBox) {
        previousMapPosition = view.getMapPosition();
        view.setMapPosition( OsmandSettings.BOTTOM_CONSTANT);
        MapContextMenu menu = mapActivity.getContextMenu();
        LatLon         ll = menu.isActive() && tileBox.containsLatLon(menu.getLatLon()) ? menu.getLatLon() : tileBox.getCenterLatLon();

        menu.updateMapCenter(null);
        menu.close();

        RotatedTileBox rb = new RotatedTileBox(tileBox);
//        tileBox.setCenterLocation(0.5f, 0.75f);
        rb.setLatLonCenter(ll.getLatitude(), ll.getLongitude());
        double lat = rb.getLatFromPixel(tileBox.getCenterPixelX(), tileBox.getCenterPixelY());
        double lon = rb.getLonFromPixel(tileBox.getCenterPixelX(), tileBox.getCenterPixelY());
        view.setLatLon(lat, lon);

        inChangeMarkerPositionMode = true;
        mark(View.INVISIBLE, R.id.map_ruler_layout,
                R.id.map_left_widgets_panel, R.id.map_right_widgets_panel, R.id.map_center_info);

        View collapseButton = mapActivity.findViewById(R.id.map_collapse_button);
        if (collapseButton != null && collapseButton.getVisibility() == View.VISIBLE) {
            wasCollapseButtonVisible = true;
            collapseButton.setVisibility(View.INVISIBLE);
        } else {
            wasCollapseButtonVisible = false;
        }

        view.refreshMap();
    }

    private void quitMovingMarker() {
        view.setMapPosition(previousMapPosition);
        inChangeMarkerPositionMode = false;
        mark(View.VISIBLE, R.id.map_ruler_layout,
                R.id.map_left_widgets_panel, R.id.map_right_widgets_panel, R.id.map_center_info);

        View collapseButton = mapActivity.findViewById(R.id.map_collapse_button);
        if (collapseButton != null && wasCollapseButtonVisible) {
            collapseButton.setVisibility(View.VISIBLE);
        }
        view.refreshMap();
    }

    private void mark(int status, int... widgets) {
        for (int widget : widgets) {
            View v = mapActivity.findViewById(widget);
            if (v != null) {
                v.setVisibility(status);
            }
        }
    }

    @Override
    public boolean onSingleTap(PointF point, RotatedTileBox tileBox) {
        if (isInChangeMarkerPositionMode() && !pressedQuickActionWidget(point.x, point.y)){
            setLayerState(true);
            return true;
        } else
            return false;
    }

    private boolean pressedQuickActionWidget(float px , float py) {
        return py <=  quickActionsWidget.getHeight();
    }

    @Override
    public void onDraw(Canvas canvas, RotatedTileBox box, DrawSettings settings) {
        if (isInChangeMarkerPositionMode()) {
            canvas.translate(box.getCenterPixelX() - contextMarker.getWidth() / 2, box.getCenterPixelY() - contextMarker.getHeight());
            contextMarker.draw(canvas);
        }
        setUpQuickActionBtnVisibility();
    }

    private void setUpQuickActionBtnVisibility() {
        boolean hideQuickButton = !isLayerOn ||
                contextMenuLayer.isInChangeMarkerPositionMode() ||
                mapActivity.getContextMenu().isVisible() && !mapActivity.getContextMenu().findMenuFragment().get().isRemoving() ||
                mapActivity.getContextMenu().isVisible() && mapActivity.getContextMenu().findMenuFragment().get().isAdded() ||
                mapActivity.getContextMenu().getMultiSelectionMenu().isVisible() && mapActivity.getContextMenu().getMultiSelectionMenu().getFragmentByTag().isAdded() ||
                mapActivity.getContextMenu().getMultiSelectionMenu().isVisible() && !mapActivity.getContextMenu().getMultiSelectionMenu().getFragmentByTag().isRemoving();
        quickActionButton.setVisibility(hideQuickButton ? View.GONE : View.VISIBLE);
    }

    @Override
    public void destroyLayer() {

    }

    @Override
    public boolean drawInScreenPixels() {
        return true;
    }


    @Override
    public void onActionsUpdated() {
        quickActionsWidget.setActions(quickActionRegistry.getQuickActions());
    }

    @Override
    public void onActionSelected(QuickAction action) {
        QuickActionFactory.produceAction(action).execute(mapActivity);
        setLayerState(true);
    }

    public PointF getMovableCenterPoint(RotatedTileBox tb) {
        return new PointF(tb.getPixWidth() / 2, tb.getPixHeight() / 2);
    }

    public boolean isInChangeMarkerPositionMode() {
        return isLayerOn && inChangeMarkerPositionMode;
    }

    public boolean isLayerOn() {
        return isLayerOn;
    }

    public boolean onBackPressed() {
        return setLayerState(true);
    }

    View.OnTouchListener onQuickActionTouchListener = new View.OnTouchListener() {
        private int initialMarginX;
        private int initialMarginY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setUpInitialValues(v, event);
                    return true;
                case MotionEvent.ACTION_UP:
                    quickActionButton.setOnTouchListener(null);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    if (AndroidUiHelper.isOrientationPortrait(mapActivity))
                        settings.setPortraitFabMargin(params.rightMargin, params.bottomMargin);
                    else
                        settings.setLandscapeFabMargin(params.rightMargin, params.bottomMargin);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (initialMarginX == 0 && initialMarginY == 0 && initialTouchX == 0 && initialTouchY == 0)
                        setUpInitialValues(v, event);

                    int deltaX = (int) (initialTouchX - event.getRawX());
                    int deltaY = (int) (initialTouchY - event.getRawY());

                    int newMarginX = initialMarginX + deltaX;
                    int newMarginY = initialMarginY + deltaY;

                    FrameLayout parent = (FrameLayout) v.getParent();
                    FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) v.getLayoutParams();
                    if (v.getHeight() + newMarginY <= parent.getHeight() && newMarginY > 0)
                        param.bottomMargin = newMarginY;

                    if (v.getWidth() + newMarginX <= parent.getWidth() && newMarginX > 0) {
                        param.rightMargin = newMarginX;
                    }

                    v.setLayoutParams(param);

                    return true;
            }
            return false;
        }

        private void setUpInitialValues(View v, MotionEvent event) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();

            initialMarginX = params.rightMargin;
            initialMarginY = params.bottomMargin;

            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
        }
    };
}
