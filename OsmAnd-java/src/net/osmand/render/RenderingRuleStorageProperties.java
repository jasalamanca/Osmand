package net.osmand.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RenderingRuleStorageProperties {

	public static final String UI_CATEGORY_HIDDEN = "ui_hidden";
	public static final String A_ENGINE_V1 = "engine_v1";
	public static final String A_APP_MODE= "appMode";
	
	public static final String A_DEFAULT_COLOR = "defaultColor";
	public static final String A_SHADOW_RENDERING = "shadowRendering";
	private static final String ATTR_INT_VALUE = "attrIntValue";
	private static final String ATTR_BOOL_VALUE = "attrBoolValue";
	private static final String ATTR_COLOR_VALUE = "attrColorValue";
	private static final String ATTR_STRING_VALUE = "attrStringValue";
	private static final String TEST = "test";
	private static final String DISABLE = "disable";
	
	
	
	private static final String INTERSECTION_MARGIN = "intersectionMargin";
	private static final String INTERSECTION_SIZE_FACTOR = "intersectionSizeFactor";
	private static final String TEXT_ITALIC = "textItalic";
	private static final String TEXT_BOLD= "textBold";
	private static final String TEXT_LENGTH = "textLength";
	private static final String NAME_TAG = "nameTag";
	private static final String NAME_TAG2 = "nameTag2";
	private static final String TEXT_SHIELD = "textShield";
	private static final String SHIELD = "shield";
	private static final String SHADOW_RADIUS = "shadowRadius";
	private static final String SHADOW_COLOR = "shadowColor";
	private static final String ONEWAY_ARROWS_COLOR = "onewayArrowsColor";
	private static final String IGNORE_POLYGON_AS_POINT_AREA = "ignorePolygonAsPointArea";
	private static final String SHADER = "shader";
	private static final String CAP_5 = "cap_5";
	private static final String CAP_4 = "cap_4";
	private static final String CAP_3 = "cap_3";
	private static final String CAP_2 = "cap_2";
	private static final String CAP = "cap";
	private static final String CAP_0 = "cap_0";
	private static final String CAP__1 = "cap__1";
	private static final String CAP__2 = "cap__2";
	private static final String PATH_EFFECT_5 = "pathEffect_5";
	private static final String PATH_EFFECT_4 = "pathEffect_4";
	private static final String PATH_EFFECT_3 = "pathEffect_3";
	private static final String PATH_EFFECT_2 = "pathEffect_2";
	private static final String PATH_EFFECT = "pathEffect";
	private static final String PATH_EFFECT_0 = "pathEffect_0";
	private static final String PATH_EFFECT__1 = "pathEffect__1";
	private static final String PATH_EFFECT__2 = "pathEffect__2";
	private static final String STROKE_WIDTH_5 = "strokeWidth_5";
	private static final String STROKE_WIDTH_4 = "strokeWidth_4";
	private static final String STROKE_WIDTH_3 = "strokeWidth_3";
	private static final String STROKE_WIDTH_2 = "strokeWidth_2";
	private static final String STROKE_WIDTH = "strokeWidth";
	private static final String STROKE_WIDTH_0 = "strokeWidth_0";
	private static final String STROKE_WIDTH__1 = "strokeWidth__1";
	private static final String STROKE_WIDTH__2 = "strokeWidth__2";
	private static final String COLOR_5 = "color_5";
	private static final String COLOR_4 = "color_4";
	private static final String COLOR_3 = "color_3";
	private static final String COLOR = "color";
	private static final String COLOR_2 = "color_2";
	private static final String COLOR_0 = "color_0";
	private static final String COLOR__1 = "color__1";
	private static final String COLOR__2 = "color__2";
	private static final String TEXT_ORDER = "textOrder";
	private static final String ICON_ORDER = "iconOrder";
	private static final String ICON_VISIBLE_SIZE = "iconVisibleSize";
	private static final String TEXT_MIN_DISTANCE = "textMinDistance";
	private static final String TEXT_ON_PATH = "textOnPath";
	private static final String ICON = "icon";
	private static final String LAYER = "layer";
	private static final String ORDER = "order";
	private static final String OBJECT_TYPE = "objectType";
	private static final String POINT = "point";
	private static final String AREA = "area";
	private static final String CYCLE = "cycle";
	public static final String TAG = "tag";
	public static final String VALUE = "value";
	private static final String MINZOOM = "minzoom";
	private static final String MAXZOOM = "maxzoom";
	private static final String ADDITIONAL = "additional";
	private static final String NIGHT_MODE = "nightMode";
	private static final String TEXT_DY = "textDy";
	private static final String TEXT_SIZE = "textSize";
	private static final String TEXT_COLOR = "textColor";
	private static final String TEXT_HALO_RADIUS = "textHaloRadius";
	private static final String TEXT_HALO_COLOR = "textHaloColor";
	private static final String TEXT_WRAP_WIDTH = "textWrapWidth";
	private static final String SHADOW_LEVEL = "shadowLevel";
	private static final String ADD_POINT = "addPoint";

	
	public RenderingRuleProperty R_TEST;
	public RenderingRuleProperty R_DISABLE;
	public RenderingRuleProperty R_ATTR_INT_VALUE;
	public RenderingRuleProperty R_ATTR_BOOL_VALUE;
	public RenderingRuleProperty R_ATTR_COLOR_VALUE;
	private RenderingRuleProperty R_ATTR_STRING_VALUE;
	public RenderingRuleProperty R_TEXT_LENGTH;
	public RenderingRuleProperty R_NAME_TAG;
	public RenderingRuleProperty R_NAME_TAG2;
	public RenderingRuleProperty R_TEXT_SHIELD;
	private RenderingRuleProperty R_SHIELD;
	public RenderingRuleProperty R_SHADOW_RADIUS;
	public RenderingRuleProperty R_SHADOW_COLOR;
	public RenderingRuleProperty R_SHADER;
	private RenderingRuleProperty R_ONEWAY_ARROWS_COLOR;
	private RenderingRuleProperty R_IGNORE_POLYGON_AS_POINT_AREA;
	public RenderingRuleProperty R_CAP_5;
	public RenderingRuleProperty R_CAP_4;
	public RenderingRuleProperty R_CAP_3;
	public RenderingRuleProperty R_CAP_2;
	public RenderingRuleProperty R_CAP;
	public RenderingRuleProperty R_CAP_0;
	public RenderingRuleProperty R_CAP__1;
	public RenderingRuleProperty R_CAP__2;
	public RenderingRuleProperty R_PATH_EFFECT_5;
	public RenderingRuleProperty R_PATH_EFFECT_4;
	public RenderingRuleProperty R_PATH_EFFECT_3;
	public RenderingRuleProperty R_PATH_EFFECT_2;
	public RenderingRuleProperty R_PATH_EFFECT;
	public RenderingRuleProperty R_PATH_EFFECT_0;
	public RenderingRuleProperty R_PATH_EFFECT__1;
	public RenderingRuleProperty R_PATH_EFFECT__2;
	public RenderingRuleProperty R_STROKE_WIDTH_5;
	public RenderingRuleProperty R_STROKE_WIDTH_4;
	public RenderingRuleProperty R_STROKE_WIDTH_3;
	public RenderingRuleProperty R_STROKE_WIDTH_2;
	public RenderingRuleProperty R_STROKE_WIDTH;
	public RenderingRuleProperty R_STROKE_WIDTH_0;
	public RenderingRuleProperty R_STROKE_WIDTH__1;
	public RenderingRuleProperty R_STROKE_WIDTH__2;
	public RenderingRuleProperty R_COLOR_5;
	public RenderingRuleProperty R_COLOR_4;
	public RenderingRuleProperty R_COLOR_3;
	public RenderingRuleProperty R_COLOR;
	public RenderingRuleProperty R_COLOR_2;
	public RenderingRuleProperty R_COLOR_0;
	public RenderingRuleProperty R_COLOR__1;
	public RenderingRuleProperty R_COLOR__2;
	public RenderingRuleProperty R_TEXT_BOLD;
	public RenderingRuleProperty R_TEXT_ITALIC;
	public RenderingRuleProperty R_TEXT_ORDER;
	private RenderingRuleProperty R_ICON_ORDER;
	public RenderingRuleProperty R_TEXT_MIN_DISTANCE;
	public RenderingRuleProperty R_TEXT_ON_PATH;
	private RenderingRuleProperty R_ICON_SHIFT_PX;
	private RenderingRuleProperty R_ICON_SHIFT_PY;
	private RenderingRuleProperty R_ICON__1;
	public RenderingRuleProperty R_ICON;
	private RenderingRuleProperty R_ICON_2;
	private RenderingRuleProperty R_ICON_3;
	private RenderingRuleProperty R_ICON_4;
	private RenderingRuleProperty R_ICON_5;
	private RenderingRuleProperty R_ICON_VISIBLE_SIZE;
	private RenderingRuleProperty R_INTERSECTION_MARGIN;
	private RenderingRuleProperty R_INTERSECTION_SIZE_FACTOR;
	public RenderingRuleProperty R_LAYER;
	private RenderingRuleProperty R_ORDER;
	private RenderingRuleProperty R_POINT;
	private RenderingRuleProperty R_AREA;
	private RenderingRuleProperty R_CYCLE;
	private RenderingRuleProperty R_OBJECT_TYPE;
	public RenderingRuleProperty R_TAG;
	public RenderingRuleProperty R_VALUE;
	public RenderingRuleProperty R_MINZOOM;
	public RenderingRuleProperty R_ADDITIONAL;
	private RenderingRuleProperty R_SHADOW_LEVEL;
	public RenderingRuleProperty R_MAXZOOM;
	public RenderingRuleProperty R_NIGHT_MODE;
	public RenderingRuleProperty R_TEXT_DY;
	public RenderingRuleProperty R_TEXT_SIZE;
	public RenderingRuleProperty R_TEXT_COLOR;
	public RenderingRuleProperty R_TEXT_HALO_RADIUS;
	public RenderingRuleProperty R_TEXT_HALO_COLOR;
	public RenderingRuleProperty R_TEXT_WRAP_WIDTH;
	private RenderingRuleProperty R_ADD_POINT;

	private final Map<String, RenderingRuleProperty> properties;
	// C++
	private final List<RenderingRuleProperty> rules ;
	final List<RenderingRuleProperty> customRules ;
	
	
	public RenderingRuleStorageProperties() {
		properties = new LinkedHashMap<>();
		rules = new ArrayList<>();
		customRules = new ArrayList<>();
		createDefaultRenderingRuleProperties();
	}
	
	public RenderingRuleStorageProperties(RenderingRuleStorageProperties toClone) {
		properties = new LinkedHashMap<>(toClone.properties);
		rules = new ArrayList<>(toClone.rules);
		customRules = new ArrayList<>(toClone.customRules);
		createDefaultRenderingRuleProperties();
	}

	private void createDefaultRenderingRuleProperties() {
		R_TEST = registerRuleInternal(RenderingRuleProperty.createInputBooleanProperty(TEST));
		R_TAG = registerRuleInternal(RenderingRuleProperty.createInputStringProperty(TAG));
		R_VALUE = registerRuleInternal(RenderingRuleProperty.createInputStringProperty(VALUE));
		R_ADDITIONAL = registerRuleInternal(RenderingRuleProperty.createAdditionalStringProperty(ADDITIONAL));
		R_MINZOOM = registerRuleInternal(RenderingRuleProperty.createInputGreaterIntProperty(MINZOOM));
		R_MAXZOOM = registerRuleInternal(RenderingRuleProperty.createInputLessIntProperty(MAXZOOM));
		R_NIGHT_MODE = registerRuleInternal(RenderingRuleProperty.createInputBooleanProperty(NIGHT_MODE));
		R_LAYER = registerRuleInternal(RenderingRuleProperty.createInputIntProperty(LAYER));
		R_POINT = registerRuleInternal(RenderingRuleProperty.createInputBooleanProperty(POINT));
		R_AREA = registerRuleInternal(RenderingRuleProperty.createInputBooleanProperty(AREA));
		R_CYCLE = registerRuleInternal(RenderingRuleProperty.createInputBooleanProperty(CYCLE));
		
		R_INTERSECTION_MARGIN = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(INTERSECTION_MARGIN));
		R_INTERSECTION_SIZE_FACTOR = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(INTERSECTION_SIZE_FACTOR));
		
		R_TEXT_LENGTH = registerRuleInternal(RenderingRuleProperty.createInputIntProperty(TEXT_LENGTH));
		R_NAME_TAG = registerRuleInternal(RenderingRuleProperty.createInputStringProperty(NAME_TAG));
		R_NAME_TAG2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(NAME_TAG2));

		R_DISABLE = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(DISABLE));
		R_ATTR_INT_VALUE = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(ATTR_INT_VALUE));
		R_ATTR_BOOL_VALUE = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(ATTR_BOOL_VALUE));
		R_ATTR_COLOR_VALUE = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(ATTR_COLOR_VALUE));
		R_ATTR_STRING_VALUE = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(ATTR_STRING_VALUE));
		
		// order - no sense to make it float
		R_ORDER = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(ORDER));
		R_OBJECT_TYPE = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(OBJECT_TYPE));
		R_SHADOW_LEVEL = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(SHADOW_LEVEL));

		// text properties
		R_TEXT_WRAP_WIDTH = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(TEXT_WRAP_WIDTH));
		R_TEXT_DY = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(TEXT_DY));
		R_TEXT_HALO_RADIUS = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(TEXT_HALO_RADIUS));
		R_TEXT_HALO_COLOR = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(TEXT_HALO_COLOR));
		R_TEXT_SIZE = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(TEXT_SIZE));
		R_TEXT_ORDER = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(TEXT_ORDER));
		R_ICON_ORDER = registerRuleInternal(RenderingRuleProperty.createOutputIntProperty(ICON_ORDER));
		R_ICON_VISIBLE_SIZE = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(ICON_VISIBLE_SIZE));
		R_TEXT_MIN_DISTANCE = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(TEXT_MIN_DISTANCE));
		R_TEXT_SHIELD = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(TEXT_SHIELD));
		

		R_TEXT_COLOR = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(TEXT_COLOR));

		R_TEXT_BOLD = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(TEXT_BOLD));
		R_TEXT_ITALIC = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(TEXT_ITALIC));
		R_TEXT_ON_PATH = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(TEXT_ON_PATH));

		// point
		R_ICON_SHIFT_PX = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty("icon_shift_px"));
		R_ICON_SHIFT_PY = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty("icon_shift_py"));
		R_ICON__1 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty("icon__1"));
		R_ICON = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(ICON));
		R_ICON_2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty("icon_2"));
		R_ICON_3 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty("icon_3"));
		R_ICON_4 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty("icon_4"));
		R_ICON_5 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty("icon_5"));
		R_SHIELD = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(SHIELD));

		// polygon/way
		R_COLOR = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR));
		R_COLOR_2 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR_2));
		R_COLOR_3 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR_3));
		R_COLOR_4 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR_4));
		R_COLOR_5 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR_5));
		R_COLOR_0 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR_0));
		R_COLOR__1 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR__1));
		R_COLOR__2 = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(COLOR__2));
		
		R_STROKE_WIDTH = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH));
		R_STROKE_WIDTH_2 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH_2));
		R_STROKE_WIDTH_3 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH_3));
		R_STROKE_WIDTH_4 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH_4));
		R_STROKE_WIDTH_5 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH_5));
		R_STROKE_WIDTH_0 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH_0));
		R_STROKE_WIDTH__1 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH__1));
		R_STROKE_WIDTH__2 = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(STROKE_WIDTH__2));

		R_PATH_EFFECT = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT));
		R_PATH_EFFECT_2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT_2));
		R_PATH_EFFECT_3 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT_3));
		R_PATH_EFFECT_4 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT_4));
		R_PATH_EFFECT_5 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT_5));
		R_PATH_EFFECT_0 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT_0));
		R_PATH_EFFECT__1 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT__1));
		R_PATH_EFFECT__2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(PATH_EFFECT__2));
		
		R_CAP = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP));
		R_CAP_2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP_2));
		R_CAP_3 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP_3));
		R_CAP_4 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP_4));
		R_CAP_5 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP_5));
		R_CAP_0 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP_0));
		R_CAP__1 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP__1));
		R_CAP__2 = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(CAP__2));
		R_SHADER = registerRuleInternal(RenderingRuleProperty.createOutputStringProperty(SHADER));

		R_SHADOW_COLOR = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(SHADOW_COLOR));
		R_SHADOW_RADIUS = registerRuleInternal(RenderingRuleProperty.createOutputFloatProperty(SHADOW_RADIUS));

		R_ONEWAY_ARROWS_COLOR = registerRuleInternal(RenderingRuleProperty.createOutputColorProperty(ONEWAY_ARROWS_COLOR));
		R_ADD_POINT = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(ADD_POINT));
		R_IGNORE_POLYGON_AS_POINT_AREA = registerRuleInternal(RenderingRuleProperty.createOutputBooleanProperty(IGNORE_POLYGON_AS_POINT_AREA));
	}

	public RenderingRuleProperty get(String name) {
		return properties.get(name);
	}
	
	public RenderingRuleProperty[] getPoperties() {
		return rules.toArray(new RenderingRuleProperty[rules.size()]);
	}
	
	public List<RenderingRuleProperty> getCustomRules() {
		return customRules;
	}

	public RenderingRuleProperty getCustomRule(String attrName) {
		for (RenderingRuleProperty p : customRules) {
			if (p.getAttrName().equals(attrName)) {
				return p;
			}
		}
		return null;
	}

	private RenderingRuleProperty registerRuleInternal(RenderingRuleProperty p) {
		RenderingRuleProperty existing = get(p.getAttrName());
		properties.put(p.getAttrName(), p);
		if(existing == null) {
			p.setId(rules.size());
			rules.add(p);
		} else {
			p.setId(existing.getId());
			rules.set(existing.getId(), p);
			customRules.remove(existing);
		}
		return p;
	}

	public RenderingRuleProperty registerRule(RenderingRuleProperty p) {
		RenderingRuleProperty ps = registerRuleInternal(p);
		if(!customRules.contains(ps)) {
			customRules.add(p);
		}
		return ps;
	}
}
